package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferral
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferralResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferralsWithPathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ContractTypeAndServiceCategories
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.ReferralDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.InterventionsApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class CRSReferralService(
  private val prisonerRepository: PrisonerRepository,
  private val interventionsApiService: InterventionsApiService,
  private val offenderSearchApiService: OffenderSearchApiService,
  private val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
) {

  fun getAllPathwayCRSReferralsByNomsId(
    nomsId: String,
  ): CRSReferralResponse {
    return getCRSReferralsByPathway(nomsId, Pathway.getAllPathways())
  }

  fun getCRSReferralsByPathway(
    nomsId: String,
    pathways: Set<Pathway>,
  ): CRSReferralResponse {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val crn = prisonerEntity.crn ?: throw ResourceNotFoundException("Prisoner with id $nomsId has no CRN in database")

    val referrals = interventionsApiService.fetchProbationCaseReferrals(nomsId, crn)
    return objectMapper(referrals, pathways, nomsId)
  }

  private fun objectMapper(
    referralList: List<ReferralDTO>,
    pathways: Set<Pathway>,
    nomsId: String,
  ): CRSReferralResponse {
    val pathwayToCrsReferralMap = HashMap<Pathway, MutableList<CRSReferral>>()

    Pathway.values().forEach {
      pathwayToCrsReferralMap[it] = mutableListOf()
    }

    referralList.forEach {
      val crsReferral: CRSReferral?
      crsReferral = CRSReferral(
        it.serviceCategories,
        it.contractType,
        if (it.referralCreatedAt != null) LocalDateTime.ofInstant(it.referralCreatedAt.toInstant(), ZoneId.of("Europe/London")) else null,
        if (it.referralSentAt != null) LocalDateTime.ofInstant(it.referralSentAt.toInstant(), ZoneId.of("Europe/London")) else null,
        it.interventionTitle,
        it.referringOfficer,
        it.responsibleOfficer,
        it.serviceProviderUser,
        it.serviceProviderLocation,
        it.serviceProviderName,
        it.isDraft,
      )
      if (it.contractType.startsWith("Accommodation")) {
        pathwayToCrsReferralMap[Pathway.ACCOMMODATION]?.add(crsReferral)
      } else if (it.contractType.startsWith("Dependency and Recovery")) {
        pathwayToCrsReferralMap[Pathway.DRUGS_AND_ALCOHOL]?.add(crsReferral)
      } else if (it.contractType.startsWith("Education, Training and Employment")) {
        pathwayToCrsReferralMap[Pathway.EDUCATION_SKILLS_AND_WORK]?.add(crsReferral)
      } else if (it.contractType.startsWith("Finance, Benefit and Debt")) {
        pathwayToCrsReferralMap[Pathway.FINANCE_AND_ID]?.add(crsReferral)
      } else if (it.contractType.startsWith("Mentoring")) {
        pathwayToCrsReferralMap[Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR]?.add(crsReferral)
      } else if (it.contractType.startsWith("Personal Wellbeing") && (
        it.serviceCategories.contains("Family and Significant Others") ||
          it.serviceCategories.contains("Family and Significant Others (GM)")
        )
      ) {
        pathwayToCrsReferralMap[Pathway.CHILDREN_FAMILIES_AND_COMMUNITY]?.add(crsReferral)
      } else if (it.contractType.startsWith("Personal Wellbeing") && (
        !it.serviceCategories.contains("Family and Significant Others") ||
          !it.serviceCategories.contains("Family and Significant Others (GM)")
        )
      ) {
        pathwayToCrsReferralMap[Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR]?.add(crsReferral)
      } else if (it.contractType.startsWith("Women's Support Services (GM)") || it.contractType.startsWith("Women's Services")) {
        pathwayToCrsReferralMap[Pathway.HEALTH]?.add(crsReferral)
      }
    }

    val crsReferralWithPathwaysList = mutableListOf<CRSReferralsWithPathway>()

    pathways.forEach { pathway ->
      val crsReferralList = pathwayToCrsReferralMap[pathway]
        ?: throw IllegalArgumentException("Cannot find Pathway in pathwayToCrsReferralMap - this is likely a coding error!")
      val finalDuplicateReferrals = removeDuplicateReferrals(crsReferralList)
      crsReferralWithPathwaysList.add(
        CRSReferralsWithPathway(
          pathway,
          finalDuplicateReferrals,
          getAlternateMessage(nomsId, pathway, finalDuplicateReferrals),
        ),
      )
    }

    return CRSReferralResponse(crsReferralWithPathwaysList)
  }

  private fun getAlternateMessage(nomsId: String, pathway: Pathway, crsReferralList: List<CRSReferral>): String {
    var message = ""
    if (crsReferralList.isEmpty()) {
      val prisoner = offenderSearchApiService.findPrisonerPersonalDetails(nomsId)
      val prisonerName: String = "${prisoner.firstName} ${prisoner.lastName}".convertNameToTitleCase()
      val comName = resettlementPassportDeliusApiService.getComByNomsId(nomsId) ?: "NO DATA"
      message =
        "No ${pathway.toString().convertEnumStringToLowercaseContent()} referral currently exists for $prisonerName. If you think this incorrect, please contact their COM, ${comName.convertNameToTitleCase()}."
    }
    return message
  }

  fun removeDuplicateReferrals(crsReferralList: List<CRSReferral>): List<CRSReferral> {
    // RP2-753 We should only display the latest referral for each Contract Type/Service Category combination
    val duplicateCRSReferralsMap = mutableMapOf<ContractTypeAndServiceCategories, MutableList<CRSReferral>>()
    crsReferralList.forEach { crsReferral ->
      val existingEntry = duplicateCRSReferralsMap[ContractTypeAndServiceCategories(crsReferral.contractType, crsReferral.serviceCategories.sorted())]
      if (existingEntry != null) {
        existingEntry.add(crsReferral)
      } else {
        duplicateCRSReferralsMap[ContractTypeAndServiceCategories(crsReferral.contractType, crsReferral.serviceCategories.sorted())] =
          mutableListOf(crsReferral)
      }
    }

    return duplicateCRSReferralsMap.map { entry -> entry.value.sortedByDescending { it.referralCreatedAt }.first() }
  }
}
