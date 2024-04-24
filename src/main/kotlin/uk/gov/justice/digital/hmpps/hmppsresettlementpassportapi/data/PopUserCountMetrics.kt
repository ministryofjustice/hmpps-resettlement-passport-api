package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class PopUserCountMetrics(
  val metrics: MutableMap<Prison, List<PopUserCountMetric>> = mutableMapOf(),
)

data class PopUserCountMetric(
  val metric: MetricTag,
  val value: Int,
)

enum class MetricTag(val label: String) {
  print_count("Print_Count"),
  appointment_quality("Appointment_Quality"),
  licenceCondition_quality("LicenceCondition_Quality"),
}
