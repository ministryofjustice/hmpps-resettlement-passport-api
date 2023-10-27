package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferral
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferralResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferralsWithPathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PathwayMap
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.ReferralDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.InterventionsApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService

@Service
class CRSReferralService(
  private val prisonerRepository: PrisonerRepository,
  private val interventionsApiService: InterventionsApiService,
  private val offenderSearchApiService: OffenderSearchApiService,
  private val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
) {

  suspend fun getAllPathwayCRSReferralsByNomsId(
    nomsId: String,
  ): CRSReferralResponse {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val crn = prisonerEntity.crn ?: throw ResourceNotFoundException("Prisoner with id $nomsId has no CRN in database")

    var crsReferralsResponse = CRSReferralResponse(emptyList())
    val referrals = interventionsApiService.fetchProbationCaseReferrals(nomsId, crn)
    referrals.collect {
      crsReferralsResponse = objectMapper(it, null, nomsId)
    }
    return crsReferralsResponse
  }

  suspend fun getCRSReferralsByPathway(
    nomsId: String,
    pathway: String,
  ): CRSReferralResponse {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }
    if (!PathwayMap.values().any { it.id == pathway } || pathway == "RESET") {
      throw NoDataWithCodeFoundException("Pathway", pathway)
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val crn = prisonerEntity.crn ?: throw ResourceNotFoundException("Prisoner with id $nomsId has no CRN in database")

    var crsReferralsResponse = CRSReferralResponse(emptyList())
    val referrals = interventionsApiService.fetchProbationCaseReferrals(nomsId, crn)
    referrals.collect {
      crsReferralsResponse = objectMapper(it, pathway, nomsId)
    }
    return crsReferralsResponse
  }

  private suspend fun objectMapper(referralList: List<ReferralDTO>, pathway: String? = null, nomsId: String): CRSReferralResponse {
    val crsReferralACCOMList = mutableListOf<CRSReferral>()
    val crsReferralATBList = mutableListOf<CRSReferral>()
    val crsReferralCHDFAMCOMList = mutableListOf<CRSReferral>()
    val crsReferralDAList = mutableListOf<CRSReferral>()
    val crsReferralESKList = mutableListOf<CRSReferral>()
    val crsReferralHELList = mutableListOf<CRSReferral>()
    val crsReferralFINList = mutableListOf<CRSReferral>()

    val crsReferralResponse = CRSReferralResponse(emptyList())
    val crsReferralWithPathwaysList = mutableListOf<CRSReferralsWithPathway>()

    referralList.forEach {
      val crsReferral: CRSReferral?
      crsReferral = CRSReferral(
        it.serviceCategories,
        it.contractType,
        it.referralCreatedAt,
        it.referralSentAt,
        it.interventionTitle,
        it.referringOfficer,
        it.responsibleOfficer,
        it.serviceProviderUser,
        it.serviceProviderLocation,
        it.serviceProviderName,
        it.isDraft,
      )
      if (it.contractType.startsWith("Accommodation")) {
        crsReferralACCOMList.add(crsReferral)
      } else if (it.contractType.startsWith("Dependency and Recovery")) {
        crsReferralDAList.add(crsReferral)
      } else if (it.contractType.startsWith("Education, Training and Employment")) {
        crsReferralESKList.add(crsReferral)
      } else if (it.contractType.startsWith("Finance, Benefit and Debt")) {
        crsReferralFINList.add(crsReferral)
      } else if (it.contractType.startsWith("Mentoring")) {
        crsReferralATBList.add(crsReferral)
      } else if (it.contractType.startsWith("Personal Wellbeing") && (it.serviceCategories.contains("Family and Significant Others") ||
              it.serviceCategories.contains("Family and Significant Others (GM)"))
      ) {
        crsReferralCHDFAMCOMList.add(crsReferral)
      } else if (it.contractType.startsWith("Personal Wellbeing") && (!it.serviceCategories.contains("Family and Significant Others") ||
              !it.serviceCategories.contains("Family and Significant Others (GM)"))
      ) {
        crsReferralATBList.add(crsReferral)
      } else if (it.contractType.startsWith("Women's Support Services (GM)") || it.contractType.startsWith("Women's Services")) {
        crsReferralHELList.add(crsReferral)
      }
    }

    when (pathway) {
      null -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ACCOM.id, crsReferralACCOMList, getAlternateMessage(nomsId, PathwayMap.ACCOM.id, crsReferralACCOMList)))
        crsReferralWithPathwaysList.add(
          CRSReferralsWithPathway(
            PathwayMap.CHDFAMCOM.id,
            crsReferralCHDFAMCOMList,
            getAlternateMessage(nomsId, PathwayMap.CHDFAMCOM.id, crsReferralCHDFAMCOMList),
          ),
        )
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.DRUG_ALCOHOL.id, crsReferralDAList, getAlternateMessage(nomsId, PathwayMap.DRUG_ALCOHOL.id, crsReferralDAList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ED_SKL_WRK.id, crsReferralESKList, getAlternateMessage(nomsId, PathwayMap.ED_SKL_WRK.id, crsReferralESKList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.FINANCE_ID.id, crsReferralFINList, getAlternateMessage(nomsId, PathwayMap.FINANCE_ID.id, crsReferralFINList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.HEALTH.id, crsReferralHELList, getAlternateMessage(nomsId, PathwayMap.HEALTH.id, crsReferralHELList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ATB.id, crsReferralATBList, getAlternateMessage(nomsId, PathwayMap.ATB.id, crsReferralATBList)))
      }
      PathwayMap.ACCOM.id -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ACCOM.id, crsReferralACCOMList, getAlternateMessage(nomsId, PathwayMap.ACCOM.id, crsReferralACCOMList)))
      }
      PathwayMap.CHDFAMCOM.id -> {
        crsReferralWithPathwaysList.add(
          CRSReferralsWithPathway(
            PathwayMap.CHDFAMCOM.id,
            crsReferralCHDFAMCOMList,
            getAlternateMessage(nomsId, PathwayMap.CHDFAMCOM.id, crsReferralCHDFAMCOMList),
          ),
        )
      }
      PathwayMap.DRUG_ALCOHOL.id -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.DRUG_ALCOHOL.id, crsReferralDAList, getAlternateMessage(nomsId, PathwayMap.DRUG_ALCOHOL.id, crsReferralDAList)))
      }
      PathwayMap.ED_SKL_WRK.id -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ED_SKL_WRK.id, crsReferralESKList, getAlternateMessage(nomsId, PathwayMap.ED_SKL_WRK.id, crsReferralESKList)))
      }
      PathwayMap.FINANCE_ID.id -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.FINANCE_ID.id, crsReferralFINList, getAlternateMessage(nomsId, PathwayMap.FINANCE_ID.id, crsReferralFINList)))
      }
      PathwayMap.HEALTH.id -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.HEALTH.id, crsReferralHELList, getAlternateMessage(nomsId, PathwayMap.HEALTH.id, crsReferralHELList)))
      }
      PathwayMap.ATB.id -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ATB.id, crsReferralATBList, getAlternateMessage(nomsId, PathwayMap.ATB.id, crsReferralATBList)))
      }
    }
    crsReferralResponse.results = crsReferralWithPathwaysList
    return crsReferralResponse
  }

  private suspend fun getAlternateMessage(nomsId: String, pathway: String, crsReferralList: MutableList<CRSReferral>): String {
    var message = ""
    if (crsReferralList.isEmpty()) {
      val prisoner = offenderSearchApiService.findPrisonerPersonalDetails(nomsId)
      var prisonerName = ""
      prisonerName =
        "${prisoner.firstName} ${prisoner.lastName}".convertNameToTitleCase()
      val comName = resettlementPassportDeliusApiService.getComByNomsId(nomsId) ?: ""
      message =
        "No ${pathway.convertEnumStringToLowercaseContent()} referral currently exists for $prisonerName. If you think this incorrect, please contact their COM, ${comName.convertNameToTitleCase()}."
    }
    return message
  }
}
