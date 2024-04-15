package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceUtilsTest {

  @ParameterizedTest
  @MethodSource("test convert string to enum data")
  fun `test convert string to enum`(inputString: String, expectedEnum: TestEnum?) {
    Assertions.assertEquals(expectedEnum, convertStringToEnum(TestEnum::class, inputString))
  }

  private fun `test convert string to enum data`(): Stream<Arguments> = Stream.of(
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
  @MethodSource("test convert enum string to enum data")
  fun `test convert enum string to enum`(inputString: String, expectedEnum: Enum<*>?) {
    if (expectedEnum != null) {
      Assertions.assertEquals(expectedEnum, convertEnumStringToEnum(enumClass = TestEnum::class, stringValue = inputString))
    } else {
      assertThrows<IllegalArgumentException> { convertEnumStringToEnum(enumClass = TestEnum::class, stringValue = inputString) }
    }
  }

  @ParameterizedTest
  @MethodSource("test convert enum string to enum with secondary enum data")
  fun `test convert enum string to enum with secondary enum`(inputString: String, expectedEnum: Enum<*>?) {
    if (expectedEnum != null) {
      Assertions.assertEquals(expectedEnum, convertEnumStringToEnum(enumClass = TestEnum::class, secondaryEnumClass = TestEnum2::class, stringValue = inputString))
    } else {
      assertThrows<IllegalArgumentException> { convertEnumStringToEnum(enumClass = TestEnum::class, secondaryEnumClass = TestEnum2::class, stringValue = inputString) }
    }
  }

  private fun `test convert enum string to enum data`(): Stream<Arguments> = Stream.of(
    Arguments.of("", null),
    Arguments.of(" ", null),
    Arguments.of("some random string", null),
    Arguments.of("yes", null),
    Arguments.of("Yes", null),
    Arguments.of("YES", TestEnum.YES),
    Arguments.of("No", null),
    Arguments.of("  no  ", null),
    Arguments.of("!\"£$%^&*()-+={}[]@~'#<>?,.no`¬±", null),
    Arguments.of("NO", TestEnum.NO),
    Arguments.of("DONT_KNOW", TestEnum.DONT_KNOW),
    Arguments.of("dont_know", null),
    Arguments.of("dont know", null),
    Arguments.of("Dont Know", null),
    Arguments.of("NA", TestEnum.NA),
    Arguments.of("N/A", null),
    Arguments.of("n/a", null),
  )

  private fun `test convert enum string to enum with secondary enum data`(): Stream<Arguments> = Stream.of(
    Arguments.of("", null),
    Arguments.of(" ", null),
    Arguments.of("some random string", null),
    Arguments.of("yes", null),
    Arguments.of("Yes", null),
    Arguments.of("YES", TestEnum.YES),
    Arguments.of("No", null),
    Arguments.of("  no  ", null),
    Arguments.of("!\"£$%^&*()-+={}[]@~'#<>?,.no`¬±", null),
    Arguments.of("NO", TestEnum.NO),
    Arguments.of("DONT_KNOW", TestEnum.DONT_KNOW),
    Arguments.of("dont_know", null),
    Arguments.of("dont know", null),
    Arguments.of("Dont Know", null),
    Arguments.of("NA", TestEnum.NA),
    Arguments.of("N/A", null),
    Arguments.of("n/a", null),
    Arguments.of("OPTION_1", TestEnum2.OPTION_1),
    Arguments.of("OPTION_2", TestEnum2.OPTION_2),
    Arguments.of("OPTION_3", TestEnum2.OPTION_3),
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

  @ParameterizedTest
  @MethodSource("test get custom fields from notes data")
  fun `test get custom fields from notes`(notes: String, expectedCustomFields: List<String>?, exception: Boolean) {
    if (!exception) {
      Assertions.assertEquals(expectedCustomFields, getCustomFieldsFromNotes(notes, 0))
    } else {
      assertThrows<IllegalArgumentException> { getCustomFieldsFromNotes(notes, 0) }
    }
  }

  private fun `test get custom fields from notes data`() = Stream.of(
    Arguments.of("", null, true),
    Arguments.of(
      """
        ###
        some text
        ###
        notes
        ###
      """.trimIndent(),
      listOf("some text"),
      false,
    ),
    Arguments.of(
      """
        ###
        Appointment Title: My appointment title
        Contact: John
        Organisation: Resettlement agency
        Location: 
          Building Name: The office
          Building Number: 123
          Street Name: Main Street
          District:
          Town: Leeds
          County: West Yorkshire
          Postcode: LS1 1AA
        ###
        custom notes
        free text etc
        ###
      """.trimIndent(),
      listOf("Appointment Title: My appointment title", "Contact: John", "Organisation: Resettlement agency", "Location: ", "  Building Name: The office", "  Building Number: 123", "  Street Name: Main Street", "  District:", "  Town: Leeds", "  County: West Yorkshire", "  Postcode: LS1 1AA"),
      false,
    ),
    Arguments.of(
      """
        ###
        Appointment Title: ### My appointment title ###
        Contact: John
        Organisation: Resettlement agency
        Location: 
          Building Name: ###
          Building Number: 123
          Street Name: Main ### Street
          District: !"£$%^&*()-_=+{}[]@'#~?/><,.`¬±|\
          Town: Leeds
          County:
          Postcode: LS1 #
        ###
        custom notes ###
        ###
        free ### text etc
        ###
      """.trimIndent(),
      listOf("Appointment Title: ### My appointment title ###", "Contact: John", "Organisation: Resettlement agency", "Location: ", "  Building Name: ###", "  Building Number: 123", "  Street Name: Main ### Street", "  District: !\"£$%^&*()-_=+{}[]@'#~?/><,.`¬±|\\", "  Town: Leeds", "  County:", "  Postcode: LS1 #"),
      false,
    ),
  )

  @ParameterizedTest
  @MethodSource("test extract section from notes data")
  fun `test extract section from notes`(customFields: List<String>, section: String, expectedString: String?, exception: Boolean) {
    if (!exception) {
      Assertions.assertEquals(expectedString, extractSectionFromNotes(customFields, section, 0))
    } else {
      assertThrows<IllegalArgumentException> { extractSectionFromNotes(customFields, section, 0) }
    }
  }

  private fun `test extract section from notes data`() = Stream.of(
    Arguments.of(listOf<String>(), "section", null, true),
    Arguments.of(listOf("section: "), "section", "", false),
    Arguments.of(listOf("section: Test string", "another section: 1234", "section 1: 1234"), "section", "Test string", false),
    Arguments.of(getTestCustomFields(), "Appointment Title", "### My appointment title ###", false),
    Arguments.of(getTestCustomFields(), "Contact", "John", false),
    Arguments.of(getTestCustomFields(), "Organisation", "Resettlement agency", false),
    Arguments.of(getTestCustomFields(), "  Building Name", "###", false),
    Arguments.of(getTestCustomFields(), "  Building Number", "123", false),
    Arguments.of(getTestCustomFields(), "  Street Name", "Main ### Street", false),
    Arguments.of(getTestCustomFields(), "  District", "!\"£\$%^&*()-_=+{}[]@'#~?/><,.`¬±|\\", false),
    Arguments.of(getTestCustomFields(), "  Town", "Leeds", false),
    Arguments.of(getTestCustomFields(), "  County", "", false),
    Arguments.of(getTestCustomFields(), "  Postcode", "LS1 #", false),
  )

  @ParameterizedTest
  @MethodSource("test extract section from notes data trim to null")
  fun `test extract section from notes trim to null`(customFields: List<String>, section: String, expectedString: String?, exception: Boolean) {
    if (!exception) {
      Assertions.assertEquals(expectedString, extractSectionFromNotesTrimToNull(customFields, section, 0))
    } else {
      assertThrows<IllegalArgumentException> { extractSectionFromNotesTrimToNull(customFields, section, 0) }
    }
  }

  private fun `test extract section from notes data trim to null`() = Stream.of(
    Arguments.of(listOf<String>(), "section", null, true),
    Arguments.of(listOf("section: "), "section", null, false),
    Arguments.of(listOf("section: Test string", "another section: 1234", "section 1: 1234"), "section", "Test string", false),
    Arguments.of(getTestCustomFields(), "Appointment Title", "### My appointment title ###", false),
    Arguments.of(getTestCustomFields(), "Contact", "John", false),
    Arguments.of(getTestCustomFields(), "Organisation", "Resettlement agency", false),
    Arguments.of(getTestCustomFields(), "  Building Name", "###", false),
    Arguments.of(getTestCustomFields(), "  Building Number", "123", false),
    Arguments.of(getTestCustomFields(), "  Street Name", "Main ### Street", false),
    Arguments.of(getTestCustomFields(), "  District", "!\"£\$%^&*()-_=+{}[]@'#~?/><,.`¬±|\\", false),
    Arguments.of(getTestCustomFields(), "  Town", "Leeds", false),
    Arguments.of(getTestCustomFields(), "  County", null, false),
    Arguments.of(getTestCustomFields(), "  Postcode", "LS1 #", false),
  )

  private fun getTestCustomFields() = listOf(
    "Appointment Title: ### My appointment title ###",
    "Contact: John",
    "Organisation: Resettlement agency",
    "Location: ",
    "  Building Name: ###",
    "  Building Number: 123",
    "  Street Name: Main ### Street",
    "  District: !\"£$%^&*()-_=+{}[]@'#~?/><,.`¬±|\\",
    "  Town: Leeds",
    "  County: ",
    "  Postcode: LS1 #",
    "section 1: 1234",
  )

  @ParameterizedTest
  @MethodSource("test extractCaseNoteTypeFromBcstCaseNote data")
  fun `test extractCaseNoteTypeFromBcstCaseNote`(text: String, expectedCaseNoteType: CaseNoteType?) {
    Assertions.assertEquals(expectedCaseNoteType, extractCaseNoteTypeFromBcstCaseNote(text))
  }

  private fun `test extractCaseNoteTypeFromBcstCaseNote data`() = Stream.of(
    Arguments.of("", null),
    Arguments.of("Some random text", null),
    Arguments.of("Case note summary from Accommodation BCST2 report", CaseNoteType.ACCOMMODATION),
    Arguments.of("Case note summary from Accommodation BCST2 report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.ACCOMMODATION),
    Arguments.of("Case note summary from Attitudes, thinking and behaviour BCST2 report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR),
    Arguments.of("Case note summary from Children, families and communities BCST2 report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY),
    Arguments.of("Case note summary from Drugs and alcohol BCST2 report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.DRUGS_AND_ALCOHOL),
    Arguments.of("Case note summary from Education, skills and work BCST2 report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.EDUCATION_SKILLS_AND_WORK),
    Arguments.of("Case note summary from Finance and ID BCST2 report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.FINANCE_AND_ID),
    Arguments.of("Case note summary from Health BCST2 report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.HEALTH),
    Arguments.of("Case note summary from Accommodation Pre-release report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.ACCOMMODATION),
    Arguments.of("Case note summary from Attitudes, thinking and behaviour Pre-release report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR),
    Arguments.of("Case note summary from Children, families and communities Pre-release report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY),
    Arguments.of("Case note summary from Drugs and alcohol Pre-release report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.DRUGS_AND_ALCOHOL),
    Arguments.of("Case note summary from Education, skills and work Pre-release report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.EDUCATION_SKILLS_AND_WORK),
    Arguments.of("Case note summary from Finance and ID Pre-release report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.FINANCE_AND_ID),
    Arguments.of("Case note summary from Health Pre-release report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.HEALTH),
  )

  @ParameterizedTest
  @MethodSource("test getFirstLineOfBcstCaseNote data")
  fun `test getFirstLineOfBcstCaseNote`(pathway: Pathway, resettlementAssessmentType: ResettlementAssessmentType, expectedText: String) {
    Assertions.assertEquals(expectedText, getFirstLineOfBcstCaseNote(pathway, resettlementAssessmentType))
  }

  private fun `test getFirstLineOfBcstCaseNote data`() = Stream.of(
    Arguments.of(Pathway.ACCOMMODATION, ResettlementAssessmentType.BCST2, "Case note summary from Accommodation BCST2 report"),
    Arguments.of(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentType.BCST2, "Case note summary from Attitudes, thinking and behaviour BCST2 report"),
    Arguments.of(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentType.BCST2, "Case note summary from Children, families and communities BCST2 report"),
    Arguments.of(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentType.BCST2, "Case note summary from Drugs and alcohol BCST2 report"),
    Arguments.of(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentType.BCST2, "Case note summary from Education, skills and work BCST2 report"),
    Arguments.of(Pathway.FINANCE_AND_ID, ResettlementAssessmentType.BCST2, "Case note summary from Finance and ID BCST2 report"),
    Arguments.of(Pathway.HEALTH, ResettlementAssessmentType.BCST2, "Case note summary from Health BCST2 report"),
  )
}

enum class TestEnum {
  YES,
  NO,
  DONT_KNOW,
  NA,
}

enum class TestEnum2 {
  OPTION_1,
  OPTION_2,
  OPTION_3,
}

enum class TestEnumWithCustomLabels : EnumWithLabel {
  YES {
    override fun customLabel() = "This is a custom label"
  },
  NO,
  OTHER_SENTENCE_OF_WORDS,
  OTHER,
}
