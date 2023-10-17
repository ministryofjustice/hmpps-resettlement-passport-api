package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate

data class WorkReadinessStatusAndDetails(
  val workReadinessStatus: String?,
  val workReadinessStatusLastUpdated: LocalDate?,
  val details: WorkReadinessDetails?,
)

data class WorkReadinessDetails(
  val lastUpdated: LocalDate?,
  val workInterests: Set<String>?,
  val abilityToWork: Set<String>?,
  val reasonsToNotGetWork: Set<String>?,
  val volunteeringAndExperience: Set<VolunteeringAndExperience>?,
  val educationalCoursesAndQualifications: EducationalCoursesAndQualifications?,
  val inPrisonWorkAndEducation: InPrisonWork?,
  val skillsAndInterests: SkillsAndInterests?,
)

data class VolunteeringAndExperience(
  val typeOfWorkExperience: String?,
  val otherWork: String?,
  val role: String?,
  val details: String?,
)

data class EducationalCoursesAndQualifications(
  val qualifications: Set<Qualification>?,
  val additionalTraining: Set<String>?,
)

data class Qualification(
  val subject: String?,
  val grade: String?,
  val level: String?,
)

data class InPrisonWork(
  val inPrisonWork: Set<String>?,
  val inPrisonEducation: Set<String>?,
)

data class SkillsAndInterests(
  val skills: Set<String>?,
  val personalInterests: Set<String>?,
)
