package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.EducationalCoursesAndQualifications
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.InPrisonWork
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Qualification
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SkillsAndInterests
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.VolunteeringAndExperience
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.WorkReadinessDetails
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.WorkReadinessStatusAndDetails
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ciagapi.AchievedQualification
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ciagapi.WorkExperience
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CiagApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.EducationEmploymentApiService

@Service
class EducationWorkSkillsService(
  val educationEmploymentApiService: EducationEmploymentApiService,
  val ciagApiService: CiagApiService,
) {
  fun getWorkReadinessData(nomsId: String): WorkReadinessStatusAndDetails {
    val readinessProfile = educationEmploymentApiService.getReadinessProfileByNomsId(nomsId)
    val ciagProfile = ciagApiService.getCiagProfileByNomsId(nomsId)

    return WorkReadinessStatusAndDetails(
      workReadinessStatus = getLabelFromEnum(readinessProfile.profileData?.status),
      workReadinessStatusLastUpdated = readinessProfile.profileData?.statusChangeDate?.toLocalDate(),
      details = WorkReadinessDetails(
        lastUpdated = ciagProfile.modifiedDateTime?.toLocalDate(),
        workInterests = convertEnumSetToStringSet(ciagProfile.workExperience?.workInterests?.workInterests, ciagProfile.workExperience?.workInterests?.workInterestsOther),
        abilityToWork = convertEnumSetToStringSet(ciagProfile.abilityToWork, ciagProfile.abilityToWorkOther),
        reasonsToNotGetWork = convertEnumSetToStringSet(ciagProfile.reasonToNotGetWork, ciagProfile.reasonToNotGetWorkOther),
        volunteeringAndExperience = buildVolunteeringAndExperience(ciagProfile.workExperience?.workExperience),
        educationalCoursesAndQualifications = EducationalCoursesAndQualifications(
          qualifications = buildQualifications(ciagProfile.qualificationsAndTraining?.qualifications),
          additionalTraining = convertEnumSetToStringSet(ciagProfile.qualificationsAndTraining?.additionalTraining, ciagProfile.qualificationsAndTraining?.additionalTrainingOther),
        ),
        inPrisonWorkAndEducation = InPrisonWork(
          inPrisonWork = convertEnumSetToStringSet(ciagProfile.inPrisonInterests?.inPrisonWork, ciagProfile.inPrisonInterests?.inPrisonWorkOther),
          inPrisonEducation = convertEnumSetToStringSet(ciagProfile.inPrisonInterests?.inPrisonEducation, ciagProfile.inPrisonInterests?.inPrisonEducationOther),
        ),
        skillsAndInterests = SkillsAndInterests(
          skills = convertEnumSetToStringSet(ciagProfile.skillsAndInterests?.skills, ciagProfile.skillsAndInterests?.skillsOther),
          personalInterests = convertEnumSetToStringSet(ciagProfile.skillsAndInterests?.personalInterests, ciagProfile.skillsAndInterests?.personalInterestsOther),
        ),
      ),
    )
  }

  fun buildVolunteeringAndExperience(workExperience: Set<WorkExperience>?): Set<VolunteeringAndExperience>? {
    var volunteeringAndExperienceSet: Set<VolunteeringAndExperience>? = null
    if (workExperience != null) {
      volunteeringAndExperienceSet = mutableSetOf()
      workExperience.forEach {
        volunteeringAndExperienceSet.add(VolunteeringAndExperience(typeOfWorkExperience = getLabelFromEnum(it.typeOfWorkExperience), otherWork = it.otherWork, role = it.role, details = it.details))
      }
    }
    return volunteeringAndExperienceSet
  }

  private fun buildQualifications(qualifications: Set<AchievedQualification>?): Set<Qualification>? {
    var qualificationsSet: Set<Qualification>? = null
    if (qualifications != null) {
      qualificationsSet = mutableSetOf()
      qualifications.forEach {
        qualificationsSet.add(Qualification(subject = it.subject, grade = it.grade, level = getLabelFromEnum(it.level)))
      }
    }
    return qualificationsSet
  }
}
