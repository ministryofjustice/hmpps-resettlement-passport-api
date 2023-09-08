package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.apache.commons.text.WordUtils
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

private val log = LoggerFactory.getLogger(object {}::class.java.`package`.name)

fun <T : Enum<*>> convertStringToEnum(enumClass: KClass<T>, stringValue: String?): T? {
  val enum = enumClass.java.enumConstants.firstOrNull { it.name.fuzzyMatch(stringValue) }
  if (enum == null) {
    log.warn("Conversion error reading string [{}] into enum [{}]", stringValue, enumClass)
  }
  return enum
}

fun String.fuzzyMatch(string2: String?): Boolean {
  return this == string2?.trim()?.replace(Regex("[^A-Za-z0-9_ ]"), "")?.replace(Regex("\\s+"), "_")?.uppercase()
}

fun String.convertNameToTitleCase(): String = WordUtils.capitalizeFully(this).trim()
