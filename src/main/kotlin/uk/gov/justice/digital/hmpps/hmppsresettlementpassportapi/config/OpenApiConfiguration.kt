package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementSarContent
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent

@Configuration
class OpenApiConfiguration(
  buildProperties: BuildProperties,
  @Value("\${api.base.url.oauth}") val oauthUrl: String,
) {
  private val version: String = buildProperties.version!!

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("/").description("Current url"),
      ),
    )
    .info(
      Info().title("HMPPS Resettlement Passport  API")
        .version(version)
        .description("API for viewing and managing Resettlement Passport for prison leavers")
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
    .components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization"),
      )
        .addSecuritySchemes(
          "hmpps-auth",
          SecurityScheme()
            .flows(getFlows())
            .type(SecurityScheme.Type.OAUTH2)
            .openIdConnectUrl("$oauthUrl/.well-known/openid-configuration"),
        )
        .addSecuritySchemes("sar-role", SecurityScheme().addBearerJwtRequirement("ROLE_SAR_DATA_ACCESS")),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
    .addSecurityItem(SecurityRequirement().addList("hmpps-auth"))

  fun getFlows(): OAuthFlows {
    val flows = OAuthFlows()
    val clientCredflow = OAuthFlow()
    clientCredflow.tokenUrl = "$oauthUrl/oauth/token"
    val scopes = Scopes()
      .addString("read", "Allows read of data")
      .addString("write", "Allows write of data")
    clientCredflow.scopes = scopes
    val authflow = OAuthFlow()
    authflow.authorizationUrl = "$oauthUrl/oauth/authorize"
    authflow.tokenUrl = "$oauthUrl/oauth/token"
    authflow.scopes = scopes
    return flows.clientCredentials(clientCredflow).authorizationCode(authflow)
  }

  @Bean
  fun openAPICustomiser(): OpenApiCustomizer = OpenApiCustomizer {
    it.components.schemas.forEach { (_, schema: Schema<*>) ->
      val properties = schema.properties ?: mutableMapOf()
      for (propertyName in properties.keys) {
        val propertySchema = properties[propertyName]!!
        if (propertySchema is DateTimeSchema) {
          properties.replace(
            propertyName,
            StringSchema()
              .example("2021-07-05T10:35:17")
              .pattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$")
              .description(propertySchema.description)
              .required(propertySchema.required),
          )
        }
      }
    }
    typedContentForSar(it)
  }

  private fun typedContentForSar(openApi: OpenAPI) {
    // register the SAR Content DTO
    val resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(ResettlementSarContent::class.java).also {
      openApi.components.addSchemas(it.schema.name, it.schema)
      it.referencedSchemas.forEach { (key, schema) -> openApi.components.addSchemas(key, schema) }
    }
    // Touch up the SAR schema
    openApi.components.schemas[HmppsSubjectAccessRequestContent::class.simpleName]?.let { sarSchema ->
      sarSchema.properties["content"] = resolvedSchema.schema
      sarSchema.properties["attachments"]?.let {
        it.description = "(Not in use) ${it.description}"
        it.example = null
      }
    }
    // Add security requirements to SAR endpoints
    listOf(
      "/subject-access-request",
      "/subject-access-request/template",
    ).forEach { openApi.paths[it]!!.get!!.security = listOf(SecurityRequirement().addList("sar-role", listOf("read"))) }
  }

  private fun SecurityScheme.addBearerJwtRequirement(role: String): SecurityScheme = type(SecurityScheme.Type.HTTP)
    .scheme("bearer")
    .bearerFormat("JWT")
    .`in`(SecurityScheme.In.HEADER)
    .name("Authorization")
    .description("A HMPPS Auth access token with the `$role` role.")
}
