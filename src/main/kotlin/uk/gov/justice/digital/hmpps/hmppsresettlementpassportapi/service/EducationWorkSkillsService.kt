package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.WorkReadinessStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.EducationEmploymentApiService

@Service
class EducationWorkSkillsService(
  val educationEmploymentApiService: EducationEmploymentApiService,
) {
  fun getWorkReadinessData(nomsId: String): WorkReadinessStatus{
    val readinessProfile = educationEmploymentApiService.getReadinessProfileByNomsId(nomsId)

    return WorkReadinessStatus(
      workReadinessStatus = getLabelFromEnum(readinessProfile.profileData?.status),
      workReadinessStatusLastUpdated = readinessProfile.profileData?.statusChangeDate?.toLocalDate(),
    )
  }
}
