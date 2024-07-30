package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.opentelemetry.instrumentation.annotations.WithSpan
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.web.bind.annotation.RestController

class TraceTest {
  @TestFactory
  fun `check all endpoints declare trace`(): List<DynamicTest> {
    val methods = ClassPathScanningCandidateComponentProvider(false)
      .also { it.addIncludeFilter(AnnotationTypeFilter(RestController::class.java)) }
      .findCandidateComponents("uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi")
      .flatMap { beanDefinition ->
        Class.forName(beanDefinition.beanClassName).methods.toList()
      }
      .filter { method ->
        method.annotations.any {
          it.annotationClass.qualifiedName!!.startsWith("org.springframework.web.bind.annotation")
        }
      }

    return methods.map { method ->
      DynamicTest.dynamicTest("${method.declaringClass.simpleName}:${method.name}") {
        assertThat(method.getDeclaredAnnotation(WithSpan::class.java)).isNotNull()
      }
    }
  }
}
