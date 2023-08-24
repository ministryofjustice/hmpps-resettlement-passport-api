package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceUtilsTest {

  @ParameterizedTest
  @MethodSource("test enum conversion data")
  fun `test enum conversion`(inputString: String, expectedEnum: TestEnum?) {
    Assertions.assertEquals(expectedEnum, convertStringToEnum(TestEnum::class, inputString))
  }

  private fun `test enum conversion data`(): Stream<Arguments> = Stream.of(
    Arguments.of("", null),
    Arguments.of(" ", null),
    Arguments.of("    ", null),
    Arguments.of("some random string", null),
    Arguments.of("yes", TestEnum.YES),
    Arguments.of("Yes", TestEnum.YES),
    Arguments.of(" yes ", TestEnum.YES),
    Arguments.of("  yes  ", TestEnum.YES),
    Arguments.of("YES", TestEnum.YES),
    Arguments.of("yEs", TestEnum.YES),
    Arguments.of("no", TestEnum.NO),
    Arguments.of("No", TestEnum.NO),
    Arguments.of(" no ", TestEnum.NO),
    Arguments.of("  no  ", TestEnum.NO),
    Arguments.of("!\"£$%^&*()-+={}[]@~'#<>?,.no`¬±", TestEnum.NO),
    Arguments.of("NO", TestEnum.NO),
    Arguments.of("nO", TestEnum.NO),
    Arguments.of("DONT_KNOW", TestEnum.DONT_KNOW),
    Arguments.of("dont_know", TestEnum.DONT_KNOW),
    Arguments.of("DON'T KNOW", TestEnum.DONT_KNOW),
    Arguments.of("don't know", TestEnum.DONT_KNOW),
    Arguments.of("dont know", TestEnum.DONT_KNOW),
    Arguments.of("Dont Know", TestEnum.DONT_KNOW),
    Arguments.of("NA", TestEnum.NA),
    Arguments.of("N/A", TestEnum.NA),
    Arguments.of("na", TestEnum.NA),
    Arguments.of("n/a", TestEnum.NA),
  )
}

enum class TestEnum {
  YES, NO, DONT_KNOW, NA
}
