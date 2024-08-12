FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jre-jammy AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

# Copy just gradle download step so that it's cached unless gradle version is changed
WORKDIR /app
COPY gradlew build.gradle.kts gradle.properties ./
COPY gradle/ ./gradle
RUN ./gradlew downloadDependencies

# Copy the rest
COPY . .
RUN ./gradlew --no-daemon assemble

FROM eclipse-temurin:21-jre-jammy
LABEL maintainer="HMPPS Digital Studio <info@digital.justice.gov.uk>"

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

RUN apt-get update
RUN apt-get -y upgrade
RUN rm -rf /var/lib/apt/lists/*

RUN mkdir /home/appuser/.postgresql
ADD --chown=appuser:appgroup https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem /home/appuser/.postgresql/root.crt

WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /app/build/libs/hmpps-resettlement-passport-api*.jar /app/app.jar
# Temp fix to use older version of appplication insights agent jar
#COPY --from=builder --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
ADD --chown=appuser:appgroup https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.5.1/applicationinsights-agent-3.5.1.jar /app/agent.jar
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.json /app

USER 2000

ENTRYPOINT ["java", "-XX:+AlwaysActAsServerClassMachine", "-javaagent:/app/agent.jar", "-jar", "/app/app.jar"]
