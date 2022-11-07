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

Creare il secret `pn-opensearch-master` con chiavi `username` e `password` da utilizzare come credenziali dell'utente master di OpenSearch. Il secret va creato nella regione `eu-south-1`.


Eseguire il seguente comando dalla cartella `./scripts/aws`

`./infra.sh -p \${PROFILE} -e ${ENVIRONMENT}`

# Opensearch
Dopo aver creato il bastion host per accedere al cluster OpenSearch, eseguire i comandi come indicato nel manuale operativo "\[PN\] Manuale Operativo".

Vanno eseguiti preliminarmente gli script di configurazione ruoli ed utenti:
- PN-LOGS-READER-ROLE: crea il ruolo pn-log-extractor-reader
- LAMBDA-WRITER-ROLE: crea il ruolo pn-lambda-writer
- PN-LOGS-READER-USER: crea l'utente associato al ruolo pn-log-extractor-reader (bisogna fornire una password in input)
- LAMBDA-WRITER-USER: crea l'utente associato al ruolo pn-lambda-writer (bisogna fornire una password in input)

Creare il secret `pn-opensearch-logextractor` con chiavi `username` e `password` da utilizzare come credenziali dell'utente pn-log-extractor-reader di OpenSearch, creato al passwo precedente. Il secret va creato nella regione `eu-south-1`.

Gli script di configurazione dell'indice possono essere eseguiti in questo ordine:
- BOOTSTRAP INGEST PIPELINE
- BOOTSTRAP INDEX 10Y
- BOOTSTRAP INDEX 5Y
- BOOTSTRAP INDEX 120D
- BOOTSTRAP INDEX TEMPLATE 10Y
- BOOTSTRAP INDEX TEMPLATE 5Y
- BOOTSTRAP INDEX TEMPLATE 120D
- BOOTSTRAP LIFECYCLE POLICY 10Y
- BOOTSTRAP LIFECYCLE POLICY 5Y
- BOOTSTRAP LIFECYCLE POLICY 120D
- BOOTSTRAP ROUTING INDEX
- BOOTSTRAP ROUTING INGEST PIPELINE: questo script va eseguito all'interno di DevTools di OpenSearch

Le chiamate vanno autenticate con Basic Auth utilizzando le credenziali del master user di OpenSearch.

# Backend
Eseguire il seguente comando dalla cartella `./scripts/aws`

`./backend.sh -p \${PROFILE} -e ${ENVIRONMENT} -t ${BUILD_TAG}`

# Open Issues

1) in services.yaml non è definito il dedicated master node (https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-opensearchservice-domain-clusterconfig.html#cfn-opensearchservice-domain-clusterconfig-dedicatedmastercount)
2) se ricarico la pagina una volta aperto il frontend, ottengo un 403; bisogna aggiungere una regola che rimanda i 404 verso index.html così che il routing di react possa attivarsi (https://gist.github.com/bradwestfall/b5b0e450015dbc9b4e56e5f398df48ff#spa)
3) fornire una dimensione iniziale dello spazio del cluster che non vada in conflitto con gli allarmi (l'allarme scatta quando si scende sotto 20GB ma il cluster parte da 10GB); probabilmente va rivisto anche il limite dell'allarme che è troppo alto.