package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ciagapi

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.EnumWithLabel
import java.time.LocalDateTime

data class CIAGProfileDTO(
  var modifiedDateTime: LocalDateTime?,
  var reasonToNotGetWorkOther: String?,
  var abilityToWorkOther: String?,
  var abilityToWork: Set<AbilityToWorkImpactedBy>?,
  var reasonToNotGetWork: Set<ReasonToNotGetWork>?,
  var workExperience: PreviousWork?,
  var skillsAndInterests: SkillsAndInterests?,
  var qualificationsAndTraining: EducationAndQualification?,
  var inPrisonInterests: PrisonWorkAndEducation?,
  )

enum class AbilityToWorkImpactedBy : EnumWithLabel {
  CARING_RESPONSIBILITIES,
  LIMITED_BY_OFFENSE {
    override fun customLabel() = "Limited by offence"
  },
  HEALTH_ISSUES,
  NO_RIGHT_TO_WORK,
  OTHER,
  NONE,
}

enum class ReasonToNotGetWork : EnumWithLabel  {
  LIMIT_THEIR_ABILITY,
  FULL_TIME_CARER,
  LACKS_CONFIDENCE_OR_MOTIVATION,
  HEALTH,
  RETIRED,
  NO_RIGHT_TO_WORK,
  NOT_SURE,
  OTHER,
  NO_REASON,
}

data class PreviousWork(
  val hasWorkedBefore: Boolean?,
  val typeOfWorkExperience: Set<WorkType>?,
  val typeOfWorkExperienceOther: String?,
  val workExperience: Set<WorkExperience>?,
  val workInterests: WorkInterests?,
)

data class SkillsAndInterests(
  val skills: Set<Skills>?,
  val skillsOther: String?,
  val personalInterests: Set<PersonalInterests>?,
  val personalInterestsOther: String?,
)

enum class WorkType : EnumWithLabel {
  OUTDOOR,
  CONSTRUCTION,
  DRIVING,
  BEAUTY,
  HOSPITALITY,
  TECHNICAL,
  MANUFACTURING,
  OFFICE,
  RETAIL,
  SPORTS,
  WAREHOUSING,
  WASTE_MANAGEMENT,
  EDUCATION_TRAINING,
  CLEANING_AND_MAINTENANCE,
  OTHER,
}

data class EducationAndQualification(
  var educationLevel: EducationLevels?,
  var qualifications: Set<AchievedQualification>?,
  var additionalTraining: Set<OtherQualification>?,
  var additionalTrainingOther: String?,
)

data class PrisonWorkAndEducation(
  var inPrisonWork: MutableSet<PrisonWork>?,
  var inPrisonWorkOther: String?,
  var inPrisonEducation: MutableSet<PrisonTraining>?,
  var inPrisonEducationOther: String?,
)

enum class Skills : EnumWithLabel {
  COMMUNICATION,
  POSITIVE_ATTITUDE,
  RESILIENCE,
  SELF_MANAGEMENT,
  TEAMWORK,
  THINKING_AND_PROBLEM_SOLVING,
  WILLINGNESS_TO_LEARN,
  OTHER,
  NONE,
}

enum class PersonalInterests : EnumWithLabel {
  COMMUNITY,
  CRAFTS,
  CREATIVE,
  DIGITAL,
  KNOWLEDGE_BASED,
  MUSICAL,
  OUTDOOR,
  NATURE_AND_ANIMALS,
  SOCIAL,
  SOLO_ACTIVITIES,
  SOLO_SPORTS,
  TEAM_SPORTS,
  WELLNESS,
  OTHER,
  NONE,
}

enum class EducationLevels : EnumWithLabel {
  PRIMARY_SCHOOL,
  SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS {
    override fun customLabel() = "Secondary school - left before taking exams"
  },
  SECONDARY_SCHOOL_TOOK_EXAMS {
    override fun customLabel() = "Secondary school - took exams"
  },
  FURTHER_EDUCATION_COLLEGE,
  UNDERGRADUATE_DEGREE_AT_UNIVERSITY,
  POSTGRADUATE_DEGREE_AT_UNIVERSITY,
  NOT_SURE,
}

data class WorkExperience(
  var typeOfWorkExperience: WorkType?,
  var otherWork: String?,
  var role: String?,
  var details: String?,
)

data class WorkInterests(
  var workInterests: Set<WorkType>?,
  var workInterestsOther: String?,
  var particularJobInterests: Set<WorkInterestDetail>?,
  var previousWork: PreviousWork?,
)

data class AchievedQualification(
  var subject: String?,
  var grade: String?,
  var level: QualificationLevel?,
)

enum class QualificationLevel : EnumWithLabel {
  ENTRY_LEVEL_2,
  ENTRY_LEVEL_3,
  LEVEL_1,
  LEVEL_2,
  LEVEL_3,
  LEVEL_4,
  LEVEL_5,
  LEVEL_6,
  LEVEL_7,
  LEVEL_8,
}

data class WorkInterestDetail(
  var workInterest: WorkType?,
  var role: String?,
)

enum class PrisonWork : EnumWithLabel {
  CLEANING_AND_HYGIENE,
  COMPUTERS_OR_DESK_BASED,
  GARDENING_AND_OUTDOORS,
  KITCHENS_AND_COOKING,
  MAINTENANCE,
  PRISON_LAUNDRY,
  PRISON_LIBRARY,
  TEXTILES_AND_SEWING,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}

enum class OtherQualification : EnumWithLabel {
  CSCS_CARD {
    override fun customLabel() = "CSCS card"
  },
  FIRST_AID_CERTIFICATE,
  FOOD_HYGIENE_CERTIFICATE,
  FULL_UK_DRIVING_LICENCE {
    override fun customLabel() = "Full UK driving licence"
  },
  HEALTH_AND_SAFETY,
  HGV_LICENCE {
    override fun customLabel() = "HGV licence"
  },
  MACHINERY_TICKETS,
  MANUAL_HANDLING,
  TRADE_COURSE,
  OTHER,
  NONE,
}

enum class PrisonTraining : EnumWithLabel  {
  BARBERING_AND_HAIRDRESSING,
  CATERING,
  COMMUNICATION_SKILLS,
  ENGLISH_LANGUAGE_SKILLS,
  FORKLIFT_DRIVING,
  INTERVIEW_SKILLS,
  MACHINERY_TICKETS,
  NUMERACY_SKILLS,
  RUNNING_A_BUSINESS,
  SOCIAL_AND_LIFE_SKILLS,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}