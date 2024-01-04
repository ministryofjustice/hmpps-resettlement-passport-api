package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

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

class AnswerDeserializer : JsonDeserializer<Answer<*>> {
  override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Answer<*>? {
    if (json?.isJsonArray == true) {
      if (json.asJsonArray[0].isJsonObject) {
        val listOfMaps: MutableList<Map<String, String>> = mutableListOf()
        for (jsonObj in json.asJsonArray) {
          val map: MutableMap<String, String> = linkedMapOf()
          for (key in jsonObj.asJsonObject.keySet()) {
            map[key] = jsonObj.asJsonObject[key].asString
          }
          listOfMaps.add(map)
        }
        return MapAnswer(listOfMaps)
      }
      return ListAnswer(json.asJsonArray.map { it.asString })
    }

    return StringAnswer(json?.asString?.replace("\"\"", ""))
  }
}
