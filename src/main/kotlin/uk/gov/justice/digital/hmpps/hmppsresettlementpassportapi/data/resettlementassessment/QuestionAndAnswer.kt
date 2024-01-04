package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

interface IResettlementAssessmentQuestionAndAnswer {
  val question: IResettlementAssessmentQuestion
  val answer: Answer<*>?
}

data class ResettlementAssessmentRequestQuestionAndAnswer<T> (
  val question: String,
  val answer: Answer<T>,
)

data class ResettlementAssessmentQuestionAndAnswer(
  override val question: IResettlementAssessmentQuestion,
  override val answer: Answer<*>? = null,
) : IResettlementAssessmentQuestionAndAnswer

class ResettlementAssessmentRequestQuestionAndAnswerSerialize : JsonSerializer<ResettlementAssessmentRequestQuestionAndAnswer<*>> {
  override fun serialize(src: ResettlementAssessmentRequestQuestionAndAnswer<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
    val jsonObj = JsonObject()
    jsonObj.addProperty("question", src?.question)
    if (src?.answer is StringAnswer) {
      jsonObj.addProperty("answer", src?.answer.answer as String)
      jsonObj.addProperty("type", "string")
    }
    else if (src?.answer is ListAnswer) {
      val arr = JsonArray()
      for(ans in src?.answer.answer as List<String>)
      {
        arr.add(ans)
      }
      jsonObj.add("answer", arr)
      jsonObj.addProperty("type", "list")
    }
    else if (src?.answer is MapAnswer) {
      val arr = JsonArray()
      for(ans in src?.answer.answer as List<Map<String,String>>) {
        val nestedObj = JsonObject()
        ans.forEach { (k, v) -> nestedObj.addProperty(k,v) }
        arr.add(nestedObj)
        jsonObj.add("answer", arr)
        jsonObj.addProperty("type", "map")
      }
    }
    return jsonObj
  }

}