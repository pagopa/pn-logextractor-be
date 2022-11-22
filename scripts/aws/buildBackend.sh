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


aws ecr get-login-password ${profile_option} --region eu-south-1 | docker login --username AWS --password-stdin ${HelpdeskAccount}.dkr.ecr.eu-south-1.amazonaws.com

cd ../../

docker build -t pn-logextractor-${environment} .

docker tag pn-logextractor-${environment}:latest ${HelpdeskAccount}.dkr.ecr.eu-south-1.amazonaws.com/pn-logextractor-${environment}:${build_tag}

docker push ${HelpdeskAccount}.dkr.ecr.eu-south-1.amazonaws.com/pn-logextractor-${environment}:${build_tag}

echo "Build container image: "${HelpdeskAccount}.dkr.ecr.eu-south-1.amazonaws.com/pn-logextractor-${environment}:${build_tag}