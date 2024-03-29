#!/usr/bin/env bash
    
set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

usage() {
      cat <<EOF
    Usage: $(basename "${BASH_SOURCE[0]}") [-h] [-v] [-p <aws-profile>] -e <env-type>

    [-h]                      : this help message
    [-v]                      : verbose mode
    [-p <aws-profile>]        : aws cli profile (optional)
    -e <env-type>             : one of dev / uat / svil / coll / cert / prod
    
EOF
  exit 1
}

parse_params() {
  # default values of variables set from params
  aws_profile=""
  env_type=""

  while :; do
    case "${1-}" in
    -h | --help) usage ;;
    -v | --verbose) set -x ;;
    -p | --profile) 
      aws_profile="${2-}"
      shift
      ;;
    -e | --env-name) 
      env_type="${2-}"
      shift
      ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  args=("$@")

  # check required params and arguments
  [[ -z "${env_type-}" ]] && usage 
  return 0
}

dump_params(){
  echo ""
  echo "######      PARAMETERS      ######"
  echo "##################################"
  echo "Env Name:          ${env_type}"
  echo "AWS profile:       ${aws_profile}"
}


# START SCRIPT

parse_params "$@"
dump_params

environment="${env_type}"
profile=${aws_profile}
profile_option="--profile ${profile}"
dest_dir='dist'
project_name="pn-logextractor-${environment}"
bucket_name="${project_name}-infra"
cognito_bucket_name="${project_name}-cognito"
bucket_url="s3://${bucket_name}"
HelpdeskAccount=$(aws sts get-caller-identity --profile $profile | jq -r .Account)

echo "\r\n\r\n"
echo "source ./environments/.env.infra.${environment}"
source ./environments/.env.infra.${environment}


mkdir -p $dest_dir

# global
echo "aws cloudformation package ${profile_option} --template-file \"global.yaml\" --s3-bucket ${bucket_name} --s3-prefix \"regional\" --output-template-file \"dist/template.global.${environment}.packaged.yaml\" --force-upload"
aws cloudformation package ${profile_option} --template-file "global.yaml" --s3-bucket ${bucket_name} --s3-prefix "regional" --output-template-file "${dest_dir}/template.global.${environment}.packaged.yaml" --force-upload

echo "\r\n\r\n"
echo "aws cloudformation deploy ${profile_option} --region \"eu-south-1\" --template-file \"global.yaml\" --stack-name \"pn-logextractor-global-${environment}\" --parameter-overrides \"ProjectName=pn-logextractor-${environment}\""
aws cloudformation deploy ${profile_option} --region "eu-south-1" --template-file "global.yaml" --stack-name "pn-logextractor-global-${environment}" --parameter-overrides "ProjectName=${project_name}"

s3_region="eu-south-1"
if ([ $env_type = 'hotfix' ]) then ## the s3 bucket has been wrongly created in the us-east-1 region (see task PN-3889)
  s3_region="us-east-1"
fi

## deploy bucket to store cognito lambdas
aws cloudformation deploy ${profile_option} --region "eu-central-1" --template-file "cognito-bucket.yaml" --stack-name "pn-cognito-bucket-${environment}" --parameter-overrides "ProjectName=${project_name}"

## zip and upload lambda
(cd lambdas/cognito-trigger && npm ci && zip -r function.zip .)
LambdaPath=lambdas/cognito-trigger.zip
aws s3 cp ${profile_option} --region "eu-central-1" lambdas/cognito-trigger/function.zip s3://${cognito_bucket_name}/${LambdaPath}

echo "\r\n\r\n"
echo "aws s3 sync ${profile_option} --region \"${s3_region}\" --exclude \".git/*\" --exclude \"bin/*\" . \"${bucket_url}\""
aws s3 sync ${profile_option} --region "${s3_region}" --exclude ".git/*" --exclude "bin/*" . "${bucket_url}"

echo "\r\n\r\n"
echo "aws cloudformation deploy ${profile_option} --region \"eu-central-1\" --template-file \"cognito-lambda.yaml\" --stack-name \"pn-cognito-lambda-${environment}\" --parameter-overrides \"ProjectName=${project_name}\" \"LambdaS3Bucket=${cognito_bucket_name}\" \"LambdaS3Path=${LambdaPath}\" \"LogRetentionPeriod=${LogRetentionPeriod}\""
aws cloudformation deploy ${profile_option} --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND --region "eu-central-1" --template-file "cognito-lambda.yaml" --stack-name "pn-cognito-lambda-${environment}" --parameter-overrides "ProjectName=${project_name}" "LambdaS3Bucket=${cognito_bucket_name}" "LambdaS3Path=${LambdaPath}" "LogRetentionPeriod=${LogRetentionPeriod}"

echo "\r\n\r\n"
echo "aws cloudformation deploy ${profile_option} --region \"eu-central-1\" --template-file \"support.yaml\" --stack-name \"pn-logextractor-support-${environment}\" --parameter-overrides \"ProjectName=${project_name}\""
aws cloudformation deploy ${profile_option} --region "eu-central-1" --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND --template-file "support.yaml" --stack-name "pn-logextractor-support-${environment}" --parameter-overrides "ProjectName=${project_name}" "CognitoTriggerFunctionName=${project_name}-post-auth-cognito-trigger" "CognitoLogsS3=${project_name}-cognito-logs-bucket"

echo "\r\n\r\n"
echo "aws cloudformation ${profile_option} --region \"eu-south-1\" package --template-file \"main.yaml\" --s3-bucket ${bucket_name} --s3-prefix \"regional\" --output-template-file \"dist/template.${environment}.packaged.yaml\" --force-upload"
aws cloudformation ${profile_option} --region "eu-south-1" package --template-file "main.yaml" --s3-bucket ${bucket_name} --s3-prefix "regional" --output-template-file "dist/template.${environment}.packaged.yaml" --force-upload

AlternateApiDomain=""

CloudFrontLogBucketDomainName=$( aws ${profile_option} --region="eu-central-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-support-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"CloudFrontLogBucketDomainName\") | .OutputValue" \
    )

CognitoUserPoolArn=$( aws ${profile_option} --region="eu-central-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-support-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"CognitoUserPoolArn\") | .OutputValue" \
    )

OptionalParameters=""
if ( [ ! -z "$AlternateApiDomain" ] ) then
  OptionalParameters="${OptionalParameters} AlternateApiDomain=${AlternateApiDomain}"
fi

aws cloudformation deploy ${profile_option} --region "eu-south-1" --template-file "dist/template.${environment}.packaged.yaml" \
  --stack-name "pn-logextractor-${environment}" \
  --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
  --parameter-overrides "TemplateBucketBaseUrl=http://${bucket_name}.s3.amazonaws.com" \
  "ProjectName=${project_name}" \
  "CloudFrontLogBucketDomainName=${CloudFrontLogBucketDomainName}" \
  "ApiCognitoUserPoolArn=${CognitoUserPoolArn}" \
  "VpcId=${VpcId}" \
  "PrivateSubnetIds=${PrivateSubnetIds}" \
  "ApiDomain=${ApiDomain}" \
  "ApiCertificateArn=${ApiCertificateArn}" \
  "HostedZoneId=${HostedZoneId}" \
  $OptionalParameters

rm -rf dist