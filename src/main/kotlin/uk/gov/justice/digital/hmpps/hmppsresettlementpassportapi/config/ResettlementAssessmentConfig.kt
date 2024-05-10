package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.ResourcePatternResolver
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.AssessmentQuestionSet
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.AssessmentQuestionSets

@Configuration
class ResettlementAssessmentConfig {

  @Bean
  fun assessmentQuestionSets(resourcePatternResolver: ResourcePatternResolver): AssessmentQuestionSets {
    val yamlMapper = YAMLMapper().registerKotlinModule()
    val configFiles = resourcePatternResolver.getResources("classpath:/assessment-config/**/*.yml").map { it.file }
    return AssessmentQuestionSets(configFiles.map { yamlMapper.readValue(it, AssessmentQuestionSet::class.java) })
  }
}
