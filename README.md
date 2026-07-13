# hmpps-resettlement-passport-api
[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-resettlement-passport-api/badge)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-resettlement-passport-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-resettlement-passport-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://resettlement-passport-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)
[![Pipeline [test -> build -> deploy]](https://github.com/ministryofjustice/hmpps-resettlement-passport-api/actions/workflows/pipeline.yml/badge.svg?branch=main)](https://github.com/ministryofjustice/hmpps-resettlement-passport-api/actions/workflows/pipeline.yml)

# Resettlement Passport API

This is a Backend-For-Frontend style API that support both the
[Prepare someone for release](https://github.com/ministryofjustice/hmpps-resettlement-passport-ui)
and [Plan your future](https://github.com/ministryofjustice/hmpps-resettlement-passport-person-on-probation-ui)
frontends

## Running the application locally
This backend application depends on several services to run.

| Dependency                   | Description                                              | Default | Override Env Var                                                                  |
|------------------------------|----------------------------------------------------------|---------|-----------------------------------------------------------------------------------|
| hmpps-auth                   | OAuth2 API server for authenticating requests            |         | `API_BASE_URL_OAUTH`                                                              |
| Prisoner Search API          |                                                          |         | `API_BASE_URL_PRISONER_SEARCH`                                                    |
| CVL API                      |                                                          |         | `API_BASE_URL_CVL`                                                                |
| ARN API                      |                                                          |         | `API_BASE_URL_ARN`                                                                |
| Prison API                   |                                                          |         | `API_BASE_URL_PRISON`                                                             |
| RP delius                    |                                                          |         | `API_BASE_URL_RESETTLEMENT-PASSPORT-DELIUS`                                       |
| Education Employment API     | "Get someone ready to work" backend                      |         | `API_BASE_URL_EDUCATION-EMPLOYMENT`                                               |
| Interventions API            |                                                          |         | `API_BASE_URL_INTERVENTIONS`                                                      |
| Person on Probation user API | parts of Plan your future                                |         | `API_BASE_URL_pop-user-service`                                                   |
| Offender Case notes API      |                                                          |         | `API_BASE_URL_CASE-NOTES`                                                         |
| Key worker API               |                                                          |         | `API_BASE_URL_KEY-WORKER`                                                         |
| Allocation manager API       |                                                          |         | `API_BASE_URL_ALLOCATION`                                                         |
| Manage Users API             |                                                          |         | `API_BASE_URL_MANAGE-USERS`                                                       |
| Curious APIs                 |                                                          |         | `API_BASE_URL_CURIOUS_SERVICE`                                                    |
| SQS queue of Domain events   | Receive Offender domain events                           |         | `OFFENDER_EVENT_QUEUE_NAME`, `OFFENDER_EVENT_DLQ_NAME`                            |
| SQS queue of HMPPS Audit     | Send audit events to the queue                           |         | `HMPPS_SQS_QUEUES_AUDIT_QUEUE_NAME`                                               |
| S3 bucket for document       |                                                          |         | `S3_DOCUMENT_BUCKET_NAME`                                                         |
| Database                     | Database server (`postgres` on local, `RDS` on live env) |         | `DATABASE_NAME`, `DATABASE_ENDPOINT`, `DATABASE_USERNAME` and `DATABASE_PASSWORD` |

### Preparation
Obtain API client credentials
- populate those value from kubernetes secrets `hmpps-resettlement-passport-api`.
  ```shell
  kubectl -n hmpps-resettlement-passport-dev get secret hmpps-resettlement-passport-api -o json | jq '.data | map_values(@base64d)' 
  ```
- fill in the API client credentials in these files: `*_CLIENT_ID`, `*_CLIENT_SECRET`
    - `.env` for running outside docker
    - `.env.docker` for running in docker
    - client credentials
      - `SYSTEM_CLIENT_ID` to `RESETTLEMENT_PASSPORT_API_CLIENT_ID`
      - `SYSTEM_CLIENT_SECRET` to `RESETTLEMENT_PASSPORT_API_CLIENT_SECRET`

---
### Running with docker compose
The easiest way to run the app is to use docker compose to create the service and all dependencies.
1. Prepare `.env.docker` (from `.env.docker.sample`)
    ```shell
    cp .env.docker.sample .env.docker
    ```
    - fill in the API client credentials in `.env.docker`
      see above to obtain these
2. Then run
   ```shell
   docker compose --profile api up
   ```
   will run the application (from latest image) and PostgreSQL within a local docker instance.
3. Check if application is up and running
    * See `http://localhost:8080/health` to check the app is running.
    * See `http://localhost:8080/swagger-ui/index.html` to explore the OpenAPI spec document.
    * See `http://localhost:8080/info` to check the app info

It connects HMPPS Auth and other upstream APIs in `dev` environment. Thus, a set of valid dev API clients are required to run the application.

---
### Running the application in IntelliJ
1. Prepare `.env` (from `.env.local.sample`)
    ```shell
    cp .env.local.sample .env
    ```
    - fill in the API client credentials in `.env`:
      see above to obtain these
2. Run this
    ```shell
   docker compose up -d 
    ```
    * will start dependencies only without the API application
    * `-d` for detached run
3. Run `bootRun` with  `.env` file prepared above
    * either IntelliJ
        - run `bootRun` with `EnvFile` plugin
        - add `.env`
        - enable integrations
    * or Gradle wrapper
      ```shell
      export $(grep -v '^#' .env | xargs)
      ./gradlew bootRun
      ```

## Yaml autocomplete/validation
### Setting up the schema association
Questions for a Prepare someone for release report are defined in yaml, see for example [accommodation-assessment-v1.yml](src/main/resources/assessment-config/accommodation/accommodation-assessment-v1.yml)

To get autocomplete for these files in intelliJ you can use the [assessment-schema.json](assessment-schema.json)

Add it as a JSON schema mapping in Languages and Frameworks -> Schemas and DTDs -> JSON Schema Mappings

![intellij config menu](./doc/intellij-schema-config.png)

### Regenerating the schema
If the yaml structure changes, rerun the `GenerateAssessmentSchema` file and commit the changes.

## Testing wiremock stubs locally
- build the docker image: 
```shell
docker build -f stubs.Dockerfile -t stubs .
```
- run the stubs locally on port 8080: 
```shell
docker run -p 8080:8080 stubs
```
- test it, for example: `http://localhost:8080/resettlement-passport-and-delius-api/appointments/U338861`

## Run docker image on local

### Build a local docker image
1. Build the app jar
2. Copy jar to project root
3. Build docker image

```shell
BUILD_NUMBER=1_0_0 ./gradlew clean assemble && cp ./build/libs/*.jar .
```
```shell
BUILD_NUMBER=1_0_0 docker build --build-arg BUILD_NUMBER=$BUILD_NUMBER . -t "hmpps-resettlement-passport-api:local"
```

### Run a local docker image
* Prepare `.env.docker.local` from sample
  - copy
    ```shell
    cp .env.docker.local.sample .env.docker.local
    ```
  - fill in the API client credentials in `.env.docker.local`
    see above to obtain these
  - `host.docker.internal` is used to connect back to docker host from container

then run this
```shell
docker run --name hmpps-resettlement-passport-api-app --env-file .env.docker.local -p 8080:8080 -d "hmpps-resettlement-passport-api:local"
```
