package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusAuthor
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Validation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
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
    Arguments.of("Case note summary from Accommodation Immediate needs report", CaseNoteType.ACCOMMODATION),
    Arguments.of("Case note summary from Accommodation Immediate needs report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.ACCOMMODATION),
    Arguments.of("Case note summary from Attitudes, thinking and behaviour Immediate needs report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR),
    Arguments.of("Case note summary from Children, families and communities Immediate needs report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY),
    Arguments.of("Case note summary from Drugs and alcohol Immediate needs report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.DRUGS_AND_ALCOHOL),
    Arguments.of("Case note summary from Education, skills and work Immediate needs report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.EDUCATION_SKILLS_AND_WORK),
    Arguments.of("Case note summary from Finance and ID Immediate needs report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.FINANCE_AND_ID),
    Arguments.of("Case note summary from Health Immediate needs report\n\nSome text after\n\nDetails of case note etc", CaseNoteType.HEALTH),
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
    Arguments.of(Pathway.ACCOMMODATION, ResettlementAssessmentType.BCST2, "Case note summary from Accommodation Immediate needs report"),
    Arguments.of(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentType.BCST2, "Case note summary from Attitudes, thinking and behaviour Immediate needs report"),
    Arguments.of(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentType.BCST2, "Case note summary from Children, families and communities Immediate needs report"),
    Arguments.of(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentType.BCST2, "Case note summary from Drugs and alcohol Immediate needs report"),
    Arguments.of(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentType.BCST2, "Case note summary from Education, skills and work Immediate needs report"),
    Arguments.of(Pathway.FINANCE_AND_ID, ResettlementAssessmentType.BCST2, "Case note summary from Finance and ID Immediate needs report"),
    Arguments.of(Pathway.HEALTH, ResettlementAssessmentType.BCST2, "Case note summary from Health Immediate needs report"),
  )

  @ParameterizedTest
  @MethodSource("test convertFromNameToDeliusAuthor data")
  fun `test convertFromNameToDeliusAuthor`(name: String, expectedDeliusAuthor: DeliusAuthor) {
    Assertions.assertEquals(expectedDeliusAuthor, convertFromNameToDeliusAuthor("MDI", name))
  }

  @Test
  fun `test convertToDeliusCaseNoteType`() {
    Assertions.assertEquals(DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT, convertToDeliusCaseNoteType(ResettlementAssessmentType.BCST2))
    Assertions.assertEquals(DeliusCaseNoteType.PRE_RELEASE_REPORT, convertToDeliusCaseNoteType(ResettlementAssessmentType.RESETTLEMENT_PLAN))
  }

  @ParameterizedTest
  @MethodSource("test getFibonacciNumber data")
  fun `test getFibonacciNumber`(input: Int, output: Long) {
    Assertions.assertEquals(output, getFibonacciNumber(input))
  }

  private fun `test getFibonacciNumber data`() = Stream.of(
    Arguments.of(0, 0),
    Arguments.of(1, 1),
    Arguments.of(2, 1),
    Arguments.of(3, 2),
    Arguments.of(4, 3),
    Arguments.of(5, 5),
    Arguments.of(6, 8),
    Arguments.of(7, 13),
    Arguments.of(8, 21),
    Arguments.of(9, 34),
    Arguments.of(10, 55),
  )

  private fun `test convertFromNameToDeliusAuthor data`() = Stream.of(
    Arguments.of("", DeliusAuthor("MDI", "", "")),
    Arguments.of("John Smith", DeliusAuthor("MDI", "John", "Smith")),
    Arguments.of("Mary Williams-Smith", DeliusAuthor("MDI", "Mary", "Williams-Smith")),
    Arguments.of("Mary Jane Miller", DeliusAuthor("MDI", "Mary Jane", "Miller")),
    Arguments.of("Chris", DeliusAuthor("MDI", "Chris", "")),
  )

  @ParameterizedTest
  @MethodSource("test searchTermMatchesPrisoner data")
  fun `test searchTermMatchesPrisoner`(searchTerm: String, prisoner: PrisonersSearch, matches: Boolean) {
    Assertions.assertEquals(matches, searchTermMatchesPrisoner(searchTerm, prisoner))
  }

  private fun `test searchTermMatchesPrisoner data`() = Stream.of(
    Arguments.of("", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of(" ", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("ABC1234", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("abc1234", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("Joe", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("joe", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("joE", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("Bloggs", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("bloggs", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("blogGs", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("Jo", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("jo", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("blog", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), true),
    Arguments.of("ggs", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), false),
    Arguments.of("oe", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), false),
    Arguments.of("random string", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), false),
    Arguments.of("!\"£$%^&*()_-+=[]{};:@'#~<>,.?/`¬|\\ç", getPrisonersSearch("ABC1234", "Joe", "Bloggs"), false),
    Arguments.of("John", getPrisonersSearch("ABC1234", "John James", "Smith"), true),
    Arguments.of("  john ", getPrisonersSearch("ABC1234", "John James", "Smith"), true),
  )

  private fun getPrisonersSearch(prisonerNumber: String, firstName: String, lastName: String) = PrisonersSearch(
    prisonerNumber = prisonerNumber,
    firstName = firstName,
    lastName = lastName,
    cellLocation = null,
    prisonId = "ABC",
    prisonName = "HMP ABC",
    youthOffender = null,
  )

  @Test
  fun `test generateLinkOnlyDeliusCaseNoteText - immediate needs report`() {
    val expectedCaseNote = """
      Immediate needs report completed.
  
      View accommodation report information in PSfR: https://example.com/accommodation/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View attitudes, thinking and behaviour report information in PSfR: https://example.com/attitudes-thinking-and-behaviour/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View children, families and communities report information in PSfR: https://example.com/children-families-and-communities/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View drugs and alcohol report information in PSfR: https://example.com/drugs-and-alcohol/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View education, skills and work report information in PSfR: https://example.com/education-skills-and-work/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View finance and ID report information in PSfR: https://example.com/finance-and-id/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View health report information in PSfR: https://example.com/health-status/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
    """.trimIndent()
    Assertions.assertEquals(expectedCaseNote, generateLinkOnlyDeliusCaseNoteText("ABC1234", ResettlementAssessmentType.BCST2, "https://example.com"))
  }

  @Test
  fun `test generateLinkOnlyDeliusCaseNoteText - pre-release report`() {
    val expectedCaseNote = """
      Pre-release report completed.
  
      View accommodation report information in PSfR: https://example.com/accommodation/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View attitudes, thinking and behaviour report information in PSfR: https://example.com/attitudes-thinking-and-behaviour/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View children, families and communities report information in PSfR: https://example.com/children-families-and-communities/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View drugs and alcohol report information in PSfR: https://example.com/drugs-and-alcohol/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View education, skills and work report information in PSfR: https://example.com/education-skills-and-work/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View finance and ID report information in PSfR: https://example.com/finance-and-id/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
      View health report information in PSfR: https://example.com/health-status/?prisonerNumber=ABC1234&fromDelius=true#assessment-information
    """.trimIndent()
    Assertions.assertEquals(expectedCaseNote, generateLinkOnlyDeliusCaseNoteText("ABC1234", ResettlementAssessmentType.RESETTLEMENT_PLAN, "https://example.com"))
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("test validateAnswer data")
  fun `validate answer`(title: String, questionAndAnswer: ResettlementAssessmentQuestionAndAnswer, expectedException: ServerWebInputException?) {
    if (expectedException == null) {
      validateAnswer(questionAndAnswer)
    } else {
      Assertions.assertEquals(expectedException.message, assertThrows<ServerWebInputException> { validateAnswer(questionAndAnswer) }.message)
    }
  }

  private fun `test validateAnswer data`() = Stream.of(
    Arguments.of(
      "Optional question not answered",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.OPTIONAL,
          validation = Validation(ValidationType.OPTIONAL),
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer(null),
      ),
      null,
    ),
    Arguments.of(
      "Optional question answered",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.OPTIONAL,
          validation = Validation(ValidationType.OPTIONAL),
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer("Here is some text"),
      ),
      null,
    ),
    Arguments.of(
      "Mandatory question answered",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.MANDATORY,
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer("Here is some text"),
      ),
      null,
    ),
    Arguments.of(
      "Mandatory question not answered",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.MANDATORY,
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer(null),
      ),
      ServerWebInputException("No answer provided for mandatory question [MY_QUESTION]"),
    ),
    Arguments.of(
      "Mandatory question with missing (null) answer",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.MANDATORY,
        ),
        originalPageId = "MY_PAGE",
        answer = null,
      ),
      ServerWebInputException("Answer cannot be null for [MY_QUESTION]"),
    ),
    Arguments.of(
      "Optional question with regex not answered",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.OPTIONAL,
          customValidation = Validation(regex = "^\\d+$", message = "Failed validation"),
          validation = Validation(ValidationType.OPTIONAL, "^\\d+$", "Failed validation"),
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer(null),
      ),
      null,
    ),
    Arguments.of(
      "Question with answer matching regex",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.OPTIONAL,
          customValidation = Validation(regex = "^\\d+$", message = "Failed validation"),
          validation = Validation(ValidationType.OPTIONAL, "^\\d+$", "Failed validation"),
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer("1234"),
      ),
      null,
    ),
    Arguments.of(
      "Question with answer not matching regex",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.MANDATORY,
          customValidation = Validation(regex = "^\\d+$", message = "Failed validation"),
          validation = Validation(regex = "^\\d+$", message = "Failed validation"),
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer("abcd"),
      ),
      ServerWebInputException("Invalid answer to question [MY_QUESTION] as failed to match regex [^\\d+$]"),
    ),
    Arguments.of(
      "Question with answer only partially matching regex (should fail)",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.MANDATORY,
          customValidation = Validation(regex = "^\\d+$", message = "Failed validation"),
          validation = Validation(regex = "^\\d+$", message = "Failed validation"),
        ),
        originalPageId = "MY_PAGE",
        answer = StringAnswer("123abc"),
      ),
      ServerWebInputException("Invalid answer to question [MY_QUESTION] as failed to match regex [^\\d+$]"),
    ),
    Arguments.of(
      "Question with regex validation but wrong type of answer",
      ResettlementAssessmentQuestionAndAnswer(
        question = ResettlementAssessmentQuestion(
          id = "MY_QUESTION",
          title = "My question",
          type = TypeOfQuestion.LONG_TEXT,
          validationType = ValidationType.MANDATORY,
          customValidation = Validation(regex = "^\\d+$", message = "Failed validation"),
          validation = Validation(regex = "^\\d+$", message = "Failed validation"),
        ),
        originalPageId = "MY_PAGE",
        answer = ListAnswer(listOf("123", "456")),
      ),
      ServerWebInputException("Invalid answer format to question [MY_QUESTION]. Must be a StringAnswer as regex validation is enabled."),
    ),
  )

  @Test
  fun `test generateContentOnlyDpsCaseNoteText - immediate needs report`() {
    val expectedCaseNote = """
      Immediate needs report completed.
  
      Go to prepare someone for release (PSfR) service to see the report information.
    """.trimIndent()
    Assertions.assertEquals(expectedCaseNote, generateContentOnlyDpsCaseNoteText(ResettlementAssessmentType.BCST2))
  }

  @Test
  fun `test generateContentOnlyDpsCaseNoteText - pre-release report`() {
    val expectedCaseNote = """
      Pre-release report completed.
  
      Go to prepare someone for release (PSfR) service to see the report information.
    """.trimIndent()
    Assertions.assertEquals(expectedCaseNote, generateContentOnlyDpsCaseNoteText(ResettlementAssessmentType.RESETTLEMENT_PLAN))
  }

  @ParameterizedTest
  @MethodSource("test removeOtherPrefix data")
  fun `test removeOtherPrefix`(input: String, expected: String) {
    Assertions.assertEquals(expected, removeOtherPrefix(input))
  }

  private fun `test removeOtherPrefix data`() = Stream.of(
    Arguments.of("", ""),
    Arguments.of("Random text", "Random text"),
    Arguments.of("  Random text   ", "Random text"),
    Arguments.of("OTHER_SUPPORT_NEEDS: Another support need", "Another support need"),
    Arguments.of("OTHER_SUPPORT_NEEDS:Another support need", "Another support need"),
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
