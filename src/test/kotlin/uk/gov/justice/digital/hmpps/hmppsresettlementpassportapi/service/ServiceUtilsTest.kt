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

  @ParameterizedTest
  @MethodSource("test convert name to title case data")
  fun `test convert name to title case`(inputString: String, expectedString: String) {
    Assertions.assertEquals(expectedString, inputString.convertNameToTitleCase())
  }

  private fun `test convert name to title case data`(): Stream<Arguments> = Stream.of(
    Arguments.of("", ""),
    Arguments.of(" ", ""),
    Arguments.of("    ", ""),
    Arguments.of("some random name", "Some Random Name"),
    Arguments.of("SOME RANDOM NAME", "Some Random Name"),
    Arguments.of("Some Random Name", "Some Random Name"),
    Arguments.of("sOMe RaNDoM nAme", "Some Random Name"),
    Arguments.of("  some random name   ", "Some Random Name"),
  )

  @ParameterizedTest
  @MethodSource("test construct address data")
  fun `test construct address`(inputStrings: Array<String?>, expectedString: String) {
    Assertions.assertEquals(expectedString, constructAddress(inputStrings))
  }

  private fun `test construct address data`(): Stream<Arguments> = Stream.of(
    Arguments.of(arrayOf(""), ""),
    Arguments.of(arrayOf<String?>(null), ""),
    Arguments.of(arrayOf("A bit of address"), "A bit of address"),
    Arguments.of(arrayOf("A bit of address", "another bit", "some more"), "A bit of address, another bit, some more"),
    Arguments.of(
      arrayOf("   A bit of address   ", " another bit  ", "   some more "),
      "A bit of address, another bit, some more",
    ),
  )

  @ParameterizedTest
  @MethodSource("test get label from enum data")
  fun `test get label from enum`(enum: TestEnumWithCustomLabels?, expectation: String?) {
    Assertions.assertEquals(expectation, getLabelFromEnum(enum))
  }

  private fun `test get label from enum data`() = Stream.of(
    Arguments.of(null, null),
    Arguments.of(TestEnumWithCustomLabels.NO, "No"),
    Arguments.of(TestEnumWithCustomLabels.YES, "This is a custom label"),
    Arguments.of(TestEnumWithCustomLabels.OTHER_SENTENCE_OF_WORDS, "Other sentence of words"),
  )

  @ParameterizedTest
  @MethodSource("test convert enum set to string set data")
  fun `test convert enum set to string set`(enums: Set<TestEnumWithCustomLabels>?, other: String?, expectation: Set<String>?) {
    Assertions.assertEquals(expectation, convertEnumSetToStringSet(enums, other))
  }

  private fun `test convert enum set to string set data`() = Stream.of(
    Arguments.of(null, null, null),
    Arguments.of(setOf(TestEnumWithCustomLabels.YES, TestEnumWithCustomLabels.NO, TestEnumWithCustomLabels.OTHER_SENTENCE_OF_WORDS, TestEnumWithCustomLabels.OTHER), "Another thing", setOf("This is a custom label", "No", "Other sentence of words", "Another thing")),
    Arguments.of(setOf(TestEnumWithCustomLabels.YES, TestEnumWithCustomLabels.NO, TestEnumWithCustomLabels.OTHER_SENTENCE_OF_WORDS), null, setOf("This is a custom label", "No", "Other sentence of words")),
  )
}

enum class TestEnum {
  YES, NO, DONT_KNOW, NA
}

enum class TestEnumWithCustomLabels : EnumWithLabel {
  YES {
    override fun customLabel() = "This is a custom label"
  },
  NO,
  OTHER_SENTENCE_OF_WORDS,
  OTHER,
}
