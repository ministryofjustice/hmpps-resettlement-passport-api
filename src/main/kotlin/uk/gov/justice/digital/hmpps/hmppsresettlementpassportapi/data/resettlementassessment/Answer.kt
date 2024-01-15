package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "@class",
)
@JsonSubTypes(
  JsonSubTypes.Type(value = StringAnswer::class),
  JsonSubTypes.Type(value = ListAnswer::class),
  JsonSubTypes.Type(value = MapAnswer::class),
)
interface Answer<T> {
  var answer: T?
}

data class StringAnswer(override var answer: String? = null) : Answer<String>

data class ListAnswer(override var answer: List<String>? = null) : Answer<List<String>>

data class MapAnswer(override var answer: List<Map<String, String>>? = null) : Answer<List<Map<String, String>>>
