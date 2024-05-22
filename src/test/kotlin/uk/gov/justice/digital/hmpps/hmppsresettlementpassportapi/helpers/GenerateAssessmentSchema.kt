package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import com.fasterxml.jackson.databind.JsonNode
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.AssessmentQuestionSet
import java.io.File

fun main() {
  val configBuilder: SchemaGeneratorConfigBuilder =
    SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
  val config: SchemaGeneratorConfig = configBuilder.build()
  val generator: SchemaGenerator = SchemaGenerator(config)
  val jsonSchema: JsonNode = generator.generateSchema(AssessmentQuestionSet::class.java)

  File("assessment-schema.json").writeText(jsonSchema.toPrettyString())
}
