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
bucket_url="s3://${bucket_name}"
HelpdeskAccount=$(aws sts get-caller-identity --profile $profile | jq -r .Account)

mkdir -p $dest_dir

aws cloudformation ${profile_option} --region "eu-south-1" package --template-file "frontend.yaml" --s3-bucket ${bucket_name} --s3-prefix "regional" --output-template-file "dist/template.${environment}.packaged.yaml" --force-upload

echo "\r\n\r\n"
echo "aws s3 sync ${profile_option} --region \"us-east-1\" --exclude \".git/*\" --exclude \"bin/*\" . \"${bucket_url}\""
aws s3 sync ${profile_option} --region "us-east-1" --exclude ".git/*" --exclude "bin/*" . "${bucket_url}"

echo "\r\n\r\n"
echo "source ./environments/.env.infra.${environment}"
AlternateWebDomain=""

source ./environments/.env.infra.${environment}

CloudFrontLogBucketDomainName=$( aws ${profile_option} --region="eu-central-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-support-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"CloudFrontLogBucketDomainName\") | .OutputValue" \
    )

ApiId=$( aws ${profile_option} --region="eu-south-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"ApiId\") | .OutputValue" \
    )
    
OptionalParameters=""
if ( [ ! -z "$AlternateWebDomain" ] ) then
  OptionalParameters="${OptionalParameters} AlternateWebDomain=${AlternateWebDomain}"
  OptionalParameters="${OptionalParameters} WebDomainReferenceToSite=false"
  OptionalParameters="${OptionalParameters} AlternateWebDomainReferenceToSite=true"
fi

aws cloudformation deploy ${profile_option} --region "eu-south-1" --template-file "dist/template.${environment}.packaged.yaml" \
  --stack-name "pn-logextractor-frontend-${environment}" \
  --capabilities "CAPABILITY_IAM" \
  --parameter-overrides "TemplateBucketBaseUrl=http://${bucket_name}.s3.amazonaws.com" \
  "ProjectName=${project_name}" \
  "CloudFrontLogBucketDomainName=${CloudFrontLogBucketDomainName}" \
  "VpcId=${VpcId}" \
  "ApiId=${ApiId}" \
  "PrivateSubnetIds=${PrivateSubnetIds}" \
  "WebDomain=${WebDomain}" \
  "WebCertificateArn=${WebCertificateArn}" \
  "HostedZoneId=${HostedZoneId}" \
  $OptionalParameters

rm -rf dist