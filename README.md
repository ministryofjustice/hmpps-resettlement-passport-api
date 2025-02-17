# hmpps-resettlement-passport-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-resettlement-passport-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-resettlement-passport-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-resettlement-passport-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-resettlement-passport-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://resettlement-passport-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

# Resettlement Passport API

This is a Backend-For-Frontend style API that support both the
[Prepare someone for release](https://github.com/ministryofjustice/hmpps-resettlement-passport-ui)
and [Plan your future](https://github.com/ministryofjustice/hmpps-resettlement-passport-person-on-probation-ui)
frontends

## Running locally

### Starting the API
* Start the database, stubs and dependencies with `docker compose -f docker-compose-local.yml up -d`
* Make sure to set environment variables for `RESETTLEMENT_PASSPORT_API_CLIENT_ID` and `RESETTLEMENT_PASSPORT_API_CLIENT_SECRET`
* Start with `gradle bootrun --args='--spring.profiles.active=dev'` or use a run profile in Intellij (using the dev profile)

## Yaml autocomplete/validation
### Setting up the schema association
Questions for a Prepare someone for release report are defined in yaml, see for example [accommodation-assessment-v1.yml](src/main/resources/assessment-config/accommodation/accommodation-assessment-v1.yml)

To get autocomplete for these files in intelliJ you can use the [assessment-schema.json](assessment-schema.json)

Add it as a JSON schema mapping in Languages and Frameworks -> Schemas and DTDs -> JSON Schema Mappings

![intellij config menu](./doc/intellij-schema-config.png)

### Regenerating the schema
If the yaml structure changes, rerun the `GenerateAssessmentSchema` file and commit the changes.

## Testing wiremock stubs locally
- build the docker image: `docker build -f stubs.Dockerfile -t stubs .`
- run the stubs locally on port 8080: `docker run -p 8080:8080 stubs`
- test it, for example: `http://localhost:8080/resettlement-passport-and-delius-api/appointments/U338861`
