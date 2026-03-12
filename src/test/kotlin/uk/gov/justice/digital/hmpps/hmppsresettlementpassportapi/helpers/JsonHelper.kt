package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

fun jsonMapper(): JsonMapper = jacksonMapperBuilder()
  .addModule(JavaTimeModule())
  .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  .build()

val jsonMapper = jsonMapper()

fun readFile(file: String): String = jsonMapper.javaClass.getResource("/$file")!!.readText()

inline fun <reified T> readFileAsObject(filename: String): T = readStringAsObject(readFile(filename))
inline fun <reified T> readStringAsObject(string: String): T = jsonMapper.readValue(string, jacksonTypeRef<T>())
