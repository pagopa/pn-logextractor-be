#!/usr/bin/env bash
    
set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

usage() {
      cat <<EOF
    Usage: $(basename "${BASH_SOURCE[0]}") [-h] [-v] [-p <aws-profile>] -e <env-type> -t <tag>

    [-h]                      : this help message
    [-v]                      : verbose mode
    [-p <aws-profile>]        : aws cli profile (optional)
    -e <env-type>             : one of dev / uat / svil / coll / cert / prod
    -t <tag>                  : docker build tag
    
EOF
  exit 1
}

parse_params() {
  # default values of variables set from params
  aws_profile=""
  env_type=""
  build_tag=""

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
    -t |  --tag)
      build_tag="${2-}"
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
  [[ -z "${build_tag-}" ]] && usage 
  return 0
}

dump_params(){
  echo ""
  echo "######      PARAMETERS      ######"
  echo "##################################"
  echo "Env Name:          ${env_type}"
  echo "AWS profile:       ${aws_profile}"
  echo "Build tag:         ${build_tag}"
}


# START SCRIPT

parse_params "$@"
dump_params


environment="${env_type}"
profile=${aws_profile}
profile_option="--profile ${profile}"
HelpdeskAccount=$(aws sts get-caller-identity --profile $profile | jq -r .Account)

source ./environments/.env.infra.${environment}
source ./environments/.env.backend.${environment}

CognitoUserPoolArn=$( aws ${profile_option} --region="eu-central-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-support-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"CognitoUserPoolArn\") | .OutputValue" \
    )

OpenSearchEndpoint=$( aws ${profile_option} --region="eu-south-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"OpenSearchEndpoint\") | .OutputValue" \
    )

ElasticacheEndpoint=$( aws ${profile_option} --region="eu-south-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"ElasticacheEndpoint\") | .OutputValue" \
    )

ElasticacheSecurityGroup=$( aws ${profile_option} --region="eu-south-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"ElasticacheSecurityGroup\") | .OutputValue" \
    )


AlbListenerArn=$( aws ${profile_option} --region="eu-south-1" cloudformation describe-stacks \
      --stack-name "pn-logextractor-${environment}" | jq -r \
      ".Stacks[0].Outputs | .[] | select(.OutputKey==\"AlbListenerArn\") | .OutputValue" \
    )

echo "CognitoUserPoolArn="${CognitoUserPoolArn}
echo "OpenSearchEndpoint="${OpenSearchEndpoint}
echo "ElasticacheEndpoint="${ElasticacheEndpoint}
echo "ElasticacheSecurityGroup="${ElasticacheSecurityGroup}
echo "AlbListenerArn="${AlbListenerArn}

cd ../../
aws cloudformation deploy ${profile_option} --region "eu-south-1" --template-file "ecs-service.yaml" \
    --stack-name "pn-logextractor-service-${environment}" \
    --parameter-overrides "AdditionalMicroserviceSecurityGroup=${ElasticacheSecurityGroup}" "MicroServiceUniqueName=pn-logextractor-be-${environment}" \
        "ECSClusterName=pn-logextractor-${environment}-ecs-cluster" "MappedPaths=/*" \
        "ContainerImageURI=${HelpdeskAccount}.dkr.ecr.eu-south-1.amazonaws.com/pn-logextractor-${environment}:${build_tag}" \
        "CpuValue=1024" "MemoryAmount=4GB" "VpcId=${VpcId}" \
        "Subnets=${PrivateSubnetIds}" \
        "LoadBalancerListenerArn=${AlbListenerArn}" \
        "LoadbalancerRulePriority=11" \
        "ContainerEnvEntry1=ENSURE_RECIPIENT_BY_EXTERNAL_ID_URL=${PnDataVaultRootPath}/datavault-private/v1/recipients/external/%s" \
        "ContainerEnvEntry2=GET_RECIPIENT_DENOMINATION_BY_INTERNAL_ID_URL=${PnDataVaultRootPath}/datavault-private/v1/recipients/internal" \
        "ContainerEnvEntry3=GET_SENT_NOTIFICATION_URL=${PnCoreRootPath}/delivery-private/search" \
        "ContainerEnvEntry4=GET_SENT_NOTIFICATION_DETAILS_URL=${PnCoreRootPath}/delivery-private/notifications/%s" \
        "ContainerEnvEntry5=GET_SENT_NOTIFICATION_HISTORY_URL=${PnCoreRootPath}/delivery-push-private/%s/history" \
        "ContainerEnvEntry6=GET_ENCODED_IPA_CODE_URL=${PnCoreRootPath}/ext-registry/pa/v1/activated-on-pn" \
        "ContainerEnvEntry7=GET_PUBLIC_AUTHORITY_NAME_URL=${PnCoreRootPath}/ext-registry-private/pa/v1/activated-on-pn/%s" \
        "ContainerEnvEntry8=DOWNLOAD_FILE_URL=https://${SafeStorageEndpoint}/safe-storage/v1/files/%s" \
        "ContainerEnvEntry9=SAFESTORAGE_ENDPOINT=${SafeStorageEndpoint}" \
        "ContainerEnvEntry10=SAFESTORAGE_STAGE=dev" \
        "ContainerEnvEntry11=SAFESTORAGE_CXID=${SafeStorageCxId}" \
        "ContainerEnvEntry12=SEARCH_URL=${OpenSearchEndpoint}/pn-logs/_search" \
        "ContainerEnvEntry13=SEARCH_FOLLOWUP_URL=${OpenSearchEndpoint}/_search/scroll" \
        "ContainerEnvEntry14=ELASTICACHE_HOSTNAME=${ElasticacheEndpoint}" \
        "ContainerEnvEntry15=ELASTICACHE_PORT=6379" \
        "ContainerEnvEntry16=COGNITO_GET_USER_ENDPOINT=${CognitoGetUserEndpoint}" \
        "ContainerEnvEntry17=ALLOWED_ORIGIN=${AllowedOrigin}" \
        "ContainerSecret1=BASIC_AUTH_USERNAME=${OpenSearchSecretArn}:username:AWSCURRENT:" \
        "ContainerSecret2=BASIC_AUTH_PASSWORD=${OpenSearchSecretArn}:password:AWSCURRENT:" \
    --capabilities "CAPABILITY_NAMED_IAM"