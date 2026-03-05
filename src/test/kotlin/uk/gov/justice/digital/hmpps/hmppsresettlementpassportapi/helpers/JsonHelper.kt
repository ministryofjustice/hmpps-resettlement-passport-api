package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue

fun jsonMapper(): JsonMapper = jacksonMapperBuilder()
  .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  .build()

val jsonMapper = jsonMapper()

fun readFile(file: String): String = jsonMapper.javaClass.getResource("/$file")!!.readText()

inline fun <reified T> readFileAsObject(filename: String): T = readStringAsObject(readFile(filename))
inline fun <reified T> readStringAsObject(string: String): T = jsonMapper.readValue(string)
