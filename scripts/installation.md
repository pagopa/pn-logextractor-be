# INFRA
## Procedura

Compilare il file `scripts/environments/.env.${ENVIRONMENT}` con i parametri:
- *VpcId*: ID della private VPC
- *PrivateSubnetIds*: ID delle subnets della private VPC, separati da virgola

Eseguire il seguente comando per assicurarsi che l'utente possa creare cluster OpenSearch sostituendo opportunamente la variabile profile. 

```
aws --profile=${PROFILE} \
        iam create-service-linked-role \
            --aws-service-name opensearchservice.amazonaws.com
```

Eseguire il seguente comando dalla cartella `./scripts/aws`

`./infra.sh -p \${PROFILE} -e ${ENVIRONMENT}`

# Backend
Eseguire il seguente comando dalla cartella `./scripts/aws`

`./backend.sh -p \${PROFILE} -e ${ENVIRONMENT} -t ${BUILD_TAG}`

# Open Issues

1) perché eseguire il comando package se poi il deploy viene fatto con i file locali e non con gli output
2) i sotto stack referenziati dentro main.yaml vanno spostati nella cartella "fragments" (modifica proposta nel branch)
3) il bucket referenziato nei vari script va creato nel global.yaml (modifica proposta nel branch)
4) in services.yaml non è definito il dedicated master node (https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-opensearchservice-domain-clusterconfig.html#cfn-opensearchservice-domain-clusterconfig-dedicatedmastercount)
5) che valore inserire nella variabile AdditionalMicroserviceSecurityGroup