# INFRA
## Procedura

Compilare il file `scripts/environments/.env.infra.${ENVIRONMENT}` con i parametri:
- *VpcId*: ID della private VPC
- *PrivateSubnetIds*: ID delle subnets della private VPC, separati da virgola
- *OpenSearchInitialStorageSize*: Dimensione in Gig dello storage iniziale del cluster OpenSearch

Eseguire il seguente comando per assicurarsi che l'utente possa creare cluster OpenSearch sostituendo opportunamente la variabile profile. 

```
aws --profile=${PROFILE} \
        iam create-service-linked-role \
            --aws-service-name opensearchservice.amazonaws.com
```

Creare il secret `pn-opensearch-master` con chiavi `username` e `password` da utilizzare come credenziali dell'utente master di OpenSearch. Il secret va creato nella regione `eu-south-1`.


Eseguire il seguente comando dalla cartella `./scripts/aws`

`./deployInfra.sh -p ${PROFILE} -e ${ENVIRONMENT}`

Eseguire il seguente comando dalla cartella `./scripts/aws`

`./deployStorage.sh -p ${PROFILE} -e ${ENVIRONMENT}`

Eseguire il seguente comando dalla cartella `./scripts/aws`

`./deployFrontend.sh -p ${PROFILE} -e ${ENVIRONMENT}`

# Opensearch
Dopo aver creato il bastion host per accedere al cluster OpenSearch, eseguire i comandi attraverso Postman, avendo opportunamente configurato una connessione tramite port forwarding al cluster OpenSearch.

Il workspace Postman è disponibile nella cartella `scripts/opensearch`.

Nel workspace Postman va creato un environment con le seguenti variabili:
- `OPENSEARCH-DOMAIN-URL`: es. http://localhost
- `PORT`: es. 5601
- `READER_PASSWORD`: password dell'utente `pn-log-extractor-reader` 
- `WRITER_PASSWORD`: password dell'utente `pn-lambda-writer` 
- `MASTER`: password dell'utente `master`

Vanno eseguiti preliminarmente gli script di configurazione ruoli ed utenti:
- PN-LOGS-READER-ROLE: crea il ruolo `pn-log-extractor-reader`
- LAMBDA-WRITER-ROLE: crea il ruolo `pn-lambda-writer`
- PN-LOGS-READER-USER: crea l'utente associato al ruolo `pn-log-extractor-reader` (bisogna fornire una password in input)
- LAMBDA-WRITER-USER: crea l'utente associato al ruolo `pn-lambda-writer` (bisogna fornire una password in input)
- PN-LOGS-READER-USER MAPPING: associazione ruolo utente per `pn-log-extractor-reader`
- LAMBDA-WRITER-USER MAPPING: associazione ruolo utente per `pn-lambda-writer`

Creare il secret `pn-opensearch-logextractor` con chiavi `username` e `password` da utilizzare come credenziali dell'utente `pn-log-extractor-reader` di OpenSearch, creato al password precedente. Il secret va creato nella regione `eu-south-1`.

Gli script di configurazione dell'indice possono essere eseguiti in questo ordine:
- BOOTSTRAP ROUTING INGEST PIPELINE
- BOOTSTRAP INGEST PIPELINE
- BOOTSTRAP INDEX TEMPLATE 10Y
- BOOTSTRAP INDEX TEMPLATE 5Y
- BOOTSTRAP INDEX TEMPLATE 120D
- BOOTSTRAP LIFECYCLE POLICY 10Y
- BOOTSTRAP LIFECYCLE POLICY 5Y
- BOOTSTRAP LIFECYCLE POLICY 120D
- BOOTSTRAP INDEX 10Y
- BOOTSTRAP INDEX 5Y
- BOOTSTRAP INDEX 120D
- BOOTSTRAP ROUTING INDEX

Le chiamate vanno autenticate con Basic Auth utilizzando le credenziali del master user di OpenSearch.

# Backend

Compilare il file `scripts/environments/.env.backend.${ENVIRONMENT}` con i parametri:
- *PnCoreRootPath*: Base URL dell'API interne di PN Core
- *PnDataVaultRootPath*: Base URL dell'API interne di PN Data Vault
- *SafeStorageEndpoint*: hostname dell'endpoint di SafeStorage
- *SafeStorageStage*: stage dell'endpoint di SafeStorage
- *SafeStorageCxId*: SafeStorage CX ID
- *CognitoGetUserEndpoint*: Default a https://cognito-idp.eu-central-1.amazonaws.com
- *AllowedOrigin*: Dominio da impostare nel CORS delle API
- *OpenSearchSecretArn*: Arn del secret `pn-opensearch-logextractor` 

Eseguire i seguenti comando dalla cartella `./scripts/aws`

`./deployBackend.sh -p ${PROFILE} -e ${ENVIRONMENT} -i ${CONTAINER_IMAGE_URL}`

# Frontend
Come pre-requisito è necessario avere installato node.js 16.x e yarn.

Scaricare il progetto https://github.com/pagopa/pn-helpdesk-fe
Posizionarsi nella root del progetto ed eseguire il comando `yarn install` 

Eseguire il seguente comando dalla root del progetto:

`./scripts/aws/uploadApplication.sh -p ${PROFILE} -e ${ENVIRONMENT}`

# Test
Creare un utente nel pool di Cognito (disponibile nella region eu-central-1) e definire anche l'attributo custom `custom:log_identifier` come un valido codice fiscale.

Accedere all'url impostata su Cloudfront ed eseguire il login con le credenziali dell'utente creato al passo precedente.

Eseguire una ricerca per IUN o codice fiscale.