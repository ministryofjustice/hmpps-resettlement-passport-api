package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import java.time.Duration
import java.time.ZonedDateTime

data class AppointmentDelius(
  val type: Info,
  val dateTime: String?,
  val duration: String?,
  val staff: StaffInfo,
  val location: LocationInfo?,
  val description: String,
  val outcome: Info?,
)

data class Info(
  val code: String,
  val description: String?,
)

data class LocationInfo(
  val code: String,
  val description: String?,
  val address: Address?,
)

data class Address(
  val buildingName: String?,
  val buildingNumber: String?,
  val streetName: String?,
  val district: String?,
  val town: String?,
  val county: String?,
  val postcode: String?,
)

data class StaffInfo(
  val code: String,
  val name: Fullname,
  val email: String?,
)

data class Fullname(
  val forename: String,
  val surname: String,
)

data class AppointmentsDeliusList(
  val results: List<AppointmentDelius> = listOf(),
  val totalElements: Int,
  val totalPages: Int,
  val page: Int,
  val size: Int,
)

data class DeliusCreateAppointment(
  val type: DeliusCreateAppointmentType,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  val start: ZonedDateTime,
  val duration: Duration,
  val notes: String? = null,
)

enum class DeliusCreateAppointmentType {
  Accommodation,
  ThinkingAndBehaviour,
  FamilyAndCommunity,
  DrugsAndAlcohol,
  SkillsAndWork,
  Finance,
  Health,
  Benefits,
  ;

  companion object {
    fun fromCategory(category: Category) = when (category) {
      Category.ACCOMMODATION -> Accommodation
      Category.ATTITUDES_THINKING_AND_BEHAVIOUR -> ThinkingAndBehaviour
      Category.CHILDREN_FAMILIES_AND_COMMUNITY -> FamilyAndCommunity
      Category.DRUGS_AND_ALCOHOL -> DrugsAndAlcohol
      Category.EDUCATION_SKILLS_AND_WORK -> SkillsAndWork
      Category.FINANCE_AND_ID -> Finance
      Category.HEALTH -> Health
      Category.BENEFITS -> Benefits
    }
  }
}
