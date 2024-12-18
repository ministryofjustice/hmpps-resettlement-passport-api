FROM wiremock/wiremock

COPY helm_deploy/hmpps-resettlement-passport-api/files/stubs/__files/* /home/wiremock/__files/
COPY helm_deploy/hmpps-resettlement-passport-api/files/stubs/mappings/* /home/wiremock/mappings/

EXPOSE 8080

CMD ["/docker-entrypoint.sh", "--no-request-journal", "--global-response-templating"]
