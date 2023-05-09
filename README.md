## online-shop project - Single-table design from Quarkus

### Requisites:

* JDK17
* podman

## Running the application in dev mode

Start a dynamodb with:

```shell
podman run -p 8000:8000 amazon/dynamodb-local
```

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

Import the postman collection from:

```shell
/api/OnlineShop.postman_collection.json
```

## Highlights of the app:

* Built in clean code/hexagonal/ports&adapters approach
* API First using Openapi-generator
* Using MapStruct for DTO <> Domain <> DAO conversions
* Using DynamoDB Enhanced Client V2
* Single-table design optimized for query