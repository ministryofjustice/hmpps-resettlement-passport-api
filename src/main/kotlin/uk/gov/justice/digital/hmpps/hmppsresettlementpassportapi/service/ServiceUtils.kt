package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.nimbusds.jwt.JWTParser
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

fun <T> getLabelFromEnum(enum: T?): String? where T : Enum<T>, T : EnumWithLabel {
  return if (enum != null) {
    if (enum.customLabel() != null) {
      enum.customLabel()
    } else {
      enum.name.convertEnumToContent()
    }
  } else {
    null
  }
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

inline fun <reified E : Enum<E>> enumIncludes(name: String): Boolean {
  return enumValues<E>().any { it.name == name }
}

fun getClaimFromJWTTOken(token: String, claimName: String): String? {
  val jwtClaimsSet = JWTParser.parse(token.replaceFirst("Bearer ", "")).jwtClaimsSet
  return jwtClaimsSet.getStringClaim(claimName)
}
