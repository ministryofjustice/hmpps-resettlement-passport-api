package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

data class PrisonerCountMetrics(
  val metrics: MutableMap<Prison, List<PrisonerCountMetric>> = mutableMapOf(),
)

data class PrisonerCountMetric(
  val status: StatusTag,
  val releaseDate: ReleaseDateTag,
  val value: Int,
)

enum class StatusTag(val label: String) {
  ALL("All"),
  NOT_STARTED("Not Started"),
  IN_PROGRESS("In Progress"),
  DONE("Done"),
}

enum class ReleaseDateTag(val label: String) {
  PAST("Past"),
  ALL_FUTURE("All Future"),
  TWELVE_WEEKS("12 Weeks"),
  TWENTY_FOUR_WEEKS("24 Weeks"),
}

class MetricsMapSerializer : JsonSerializer<Prison>() {

  override fun serialize(value: Prison?, gen: JsonGenerator?, serializers: SerializerProvider?) {
    gen?.writeFieldName(value?.id)
  }
}
