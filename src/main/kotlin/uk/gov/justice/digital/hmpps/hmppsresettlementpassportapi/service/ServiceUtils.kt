package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

private val log = LoggerFactory.getLogger(object {}::class.java.`package`.name)

fun <T : Enum<*>> convertStringToEnum(enumClass: KClass<T>, stringValue: String?) : T? {
  val enum = enumClass.java.enumConstants.firstOrNull { it.name.fuzzyMatch(stringValue) }
  if (enum == null) {
    log.warn("Conversion error reading string [{}] into enum [{}]", stringValue, enumClass)
    // TODO - should we throw an exception here instead?
  }
  return enum
}

fun String.fuzzyMatch(string2: String?): Boolean {
  return this == string2?.trim()?.replace(Regex("[^A-Za-z0-9_ ]"), "")?.replace(Regex("\\s+"), "_")?.uppercase()
}

//fun <T : Enum<*>> KClass<T>.findByString(stringValue: String?) = this.java.enumConstants.firstOrNull { it.name.fuzzyMatch(stringValue) }
