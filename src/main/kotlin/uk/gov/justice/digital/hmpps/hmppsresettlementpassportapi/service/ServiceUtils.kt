package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.nimbusds.jwt.JWTParser
import org.apache.commons.text.WordUtils
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusAuthor
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AppointmentsService.Companion.SECTION_DELIMITER
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass
import kotlin.streams.asSequence

private val log = LoggerFactory.getLogger(object {}::class.java.`package`.name)

val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
const val STRING_LENGTH = 6
const val BCST_CASE_NOTE_PREFIX = "Case note summary from"
const val BCST_CASE_NOTE_POSTFIX = "report"
val BCST_CASE_NOTE_REGEX = Regex("$BCST_CASE_NOTE_PREFIX (.*) (${ResettlementAssessmentType.entries.flatMap { listOfNotNull(it.displayName, it.alternativeDisplayName) }.joinToString("|")}) $BCST_CASE_NOTE_POSTFIX")

fun <T : Enum<*>> convertStringToEnum(enumClass: KClass<T>, stringValue: String?): T? {
  val enum = enumClass.java.enumConstants.firstOrNull { it.name.fuzzyMatch(stringValue) }
  if (enum == null) {
    log.warn("Conversion error reading string [{}] into enum [{}]", stringValue, enumClass)
  }
  return enum
}

@Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
fun <I, T, Q> convertEnumStringToEnum(enumClass: KClass<T>, secondaryEnumClass: KClass<Q>? = null, stringValue: String?): I where T : Enum<*>, T : I, Q : Enum<*>, Q : I {
  var enum: I? = enumClass.java.enumConstants.firstOrNull { it.name == stringValue }
  if (secondaryEnumClass != null && enum == null) {
    enum = secondaryEnumClass.java.enumConstants.firstOrNull { it.name == stringValue }
  }
  return enum ?: throw IllegalArgumentException("$stringValue does not exist in enum ${enumClass.simpleName} (or ${secondaryEnumClass?.simpleName})")
}

fun String.fuzzyMatch(string2: String?): Boolean = this == string2?.trim()?.replace(Regex("[^A-Za-z0-9_ ]"), "")?.replace(Regex("\\s+"), "_")?.uppercase()

fun String.convertNameToTitleCase(): String = WordUtils.capitalizeFully(this).trim()

fun constructAddress(addressElements: Array<String?>): String {
  var address = ""
  addressElements.filterNotNull().forEach {
    if (it.isNotBlank()) {
      address += "${it.trim()}, "
    }
  }
  return address.removeSuffix(", ")
}

interface EnumWithLabel {
  fun customLabel(): String? = null
}

fun <T> getLabelFromEnum(enum: T?): String? where T : Enum<T>, T : EnumWithLabel = if (enum != null) {
  if (enum.customLabel() != null) {
    enum.customLabel()
  } else {
    enum.name.convertEnumToContent()
  }
} else {
  null
}

private fun String.convertEnumToContent(): String = this.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

fun String.convertEnumStringToLowercaseContent(): String = this.replace("_", " ").lowercase()

fun <T> convertEnumSetToStringSet(enumSet: Set<T>?, other: String?): Set<String>? where T : Enum<T>, T : EnumWithLabel {
  var stringSet: Set<String>? = null
  if (enumSet != null) {
    stringSet = mutableSetOf()
    enumSet.forEach { enum ->
      if (enum.name != "OTHER") {
        getLabelFromEnum(enum)?.let { stringSet.add(it) }
      }
    }
    if (other?.isNotBlank() == true) {
      stringSet.add(other)
    }
  }
  return stringSet
}

inline fun <reified E : Enum<E>> enumIncludes(name: String): Boolean = enumValues<E>().any { it.name == name }

fun getClaimFromJWTToken(token: String, claimName: String): String? {
  val jwtClaimsSet = JWTParser.parse(token.replaceFirst("Bearer ", "")).jwtClaimsSet
  return jwtClaimsSet.getStringClaim(claimName)
}

fun getCustomFieldsFromNotes(notes: String, id: Long?): List<String> {
  try {
    return notes.split(Regex("(^|\\n)$SECTION_DELIMITER\\n"))[1].split(Regex("\\n"))
  } catch (e: Exception) {
    throw IllegalArgumentException("Cannot extract custom fields from notes in database for delius_contact with id [$id]", e)
  }
}

fun extractSectionFromNotes(customFields: List<String>, section: String, id: Long?): String {
  val title = customFields.find { it.startsWith("$section: ") }?.removePrefix("$section: ")
  return title?.trim() ?: throw IllegalArgumentException("Cannot get $section from notes in database for delius_contact with ID [$id]")
}

fun extractSectionFromNotesTrimToNull(customFields: List<String>, section: String, id: Long?) = extractSectionFromNotes(customFields, section, id).ifBlank { null }

fun randomAlphaNumericString(): String = ThreadLocalRandom.current()
  .ints(STRING_LENGTH.toLong(), 0, charPool.size)
  .asSequence()
  .map(charPool::get)
  .joinToString("")

fun extractCaseNoteTypeFromBcstCaseNote(text: String) = CaseNoteType.getByDisplayName(BCST_CASE_NOTE_REGEX.find(text)?.groups?.get(1)?.value)

fun getFirstLineOfBcstCaseNote(pathway: Pathway, type: ResettlementAssessmentType) = "$BCST_CASE_NOTE_PREFIX ${pathway.displayName} ${type.displayName} $BCST_CASE_NOTE_POSTFIX"

fun convertFromNameToDeliusAuthor(prisonCode: String, name: String): DeliusAuthor {
  val splitName = name.trim().split(Regex("\\s+(?=\\S*+\$)"))
  return DeliusAuthor(
    prisonCode = prisonCode,
    forename = splitName.first(),
    surname = if (splitName.size != 1) splitName.last() else "",
  )
}

fun convertToDeliusCaseNoteType(assessmentType: ResettlementAssessmentType) = when (assessmentType) {
  ResettlementAssessmentType.BCST2 -> DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT
  ResettlementAssessmentType.RESETTLEMENT_PLAN -> DeliusCaseNoteType.PRE_RELEASE_REPORT
}

tailrec fun getFibonacciNumber(n: Int, a: Int = 0, b: Int = 1): Long = if (n == 0) a.toLong() else getFibonacciNumber(n - 1, b, a + b)

fun searchTermMatchesPrisoner(searchTerm: String, prisoner: PrisonersSearch): Boolean {
  val trimmedSearchTerm = searchTerm.trim()
  return prisoner.prisonerNumber.lowercase() == trimmedSearchTerm.lowercase() || prisoner.firstName.lowercase().startsWith(trimmedSearchTerm.lowercase()) || prisoner.lastName.lowercase().startsWith(trimmedSearchTerm.lowercase())
}
