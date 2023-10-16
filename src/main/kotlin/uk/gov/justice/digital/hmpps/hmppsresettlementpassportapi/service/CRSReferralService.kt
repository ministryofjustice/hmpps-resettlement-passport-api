package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferral
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferralResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferralsWithPathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PathwayMap
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.Referral
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

  suspend fun getAllPathwayCRSReferralsByNomisId(
    nomsId: String,
  ): CRSReferralResponse {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val crn = prisonerEntity.crn

    // val results = CRSReferrals(emptyList())
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
    val crn = prisonerEntity.crn

    var crsReferralsResponse = CRSReferralResponse(emptyList())
    val referrals = interventionsApiService.fetchProbationCaseReferrals(nomsId, crn)
    referrals.collect {
      crsReferralsResponse = objectMapper(it, pathway, nomsId)
    }
    return crsReferralsResponse
  }

  private suspend fun objectMapper(referralList: List<Referral>, pathway: String? = null, nomisId: String): CRSReferralResponse {
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
        it.draft,
      )
      if (it.contractType?.startsWith("Accommodation") == true) {
        crsReferralACCOMList.add(crsReferral)
      } else if (it.contractType?.startsWith("Dependency and Recovery") == true) {
        crsReferralDAList.add(crsReferral)
      } else if (it.contractType?.startsWith("Education, Training and Employment") == true) {
        crsReferralESKList.add(crsReferral)
      } else if (it.contractType?.startsWith("Finance, Benefit and Debt") == true) {
        crsReferralFINList.add(crsReferral)
      } else if (it.contractType?.startsWith("Mentoring") == true) {
        crsReferralATBList.add(crsReferral)
      } else if (it.contractType?.startsWith("Personal Wellbeing") == true &&
        (
          it.serviceCategories.contains("Family and Significant Others") ||
            it.serviceCategories.contains("Family and Significant Others (GM)")
          )
      ) {
        crsReferralCHDFAMCOMList.add(crsReferral)
      } else if (it.contractType?.startsWith("Personal Wellbeing") == true &&
        (
          !it.serviceCategories.contains("Family and Significant Others") ||
            !it.serviceCategories.contains("Family and Significant Others (GM)")
          )
      ) {
        crsReferralATBList.add(crsReferral)
      } else if (it.contractType?.startsWith("Women's Support Services (GM)") == true) {
        crsReferralHELList.add(crsReferral)
      }
    }

    when (pathway) {
      null -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ACCOM.id.toString(), crsReferralACCOMList, getAlternateMessage(nomisId, PathwayMap.ACCOM.id.toString(), crsReferralACCOMList)))
        crsReferralWithPathwaysList.add(
          CRSReferralsWithPathway(
            PathwayMap.CHDFAMCOM.id.toString(),
            crsReferralCHDFAMCOMList,
            getAlternateMessage(nomisId, PathwayMap.CHDFAMCOM.id.toString(), crsReferralCHDFAMCOMList),
          ),
        )
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.DRUG_ALCOHOL.id.toString(), crsReferralDAList, getAlternateMessage(nomisId, PathwayMap.DRUG_ALCOHOL.id.toString(), crsReferralDAList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ED_SKL_WRK.id.toString(), crsReferralESKList, getAlternateMessage(nomisId, PathwayMap.ED_SKL_WRK.id.toString(), crsReferralESKList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.FINANCE_ID.id.toString(), crsReferralFINList, getAlternateMessage(nomisId, PathwayMap.FINANCE_ID.id.toString(), crsReferralFINList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.HEALTH.id.toString(), crsReferralHELList, getAlternateMessage(nomisId, PathwayMap.HEALTH.id.toString(), crsReferralHELList)))
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ATB.id.toString(), crsReferralATBList, getAlternateMessage(nomisId, PathwayMap.ATB.id.toString(), crsReferralATBList)))
      }
      PathwayMap.ACCOM.id.toString() -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ACCOM.id.toString(), crsReferralACCOMList, getAlternateMessage(nomisId, PathwayMap.ACCOM.id.toString(), crsReferralACCOMList)))
      }
      PathwayMap.CHDFAMCOM.id.toString() -> {
        crsReferralWithPathwaysList.add(
          CRSReferralsWithPathway(
            PathwayMap.CHDFAMCOM.id.toString(),
            crsReferralCHDFAMCOMList,
            getAlternateMessage(nomisId, PathwayMap.CHDFAMCOM.id.toString(), crsReferralCHDFAMCOMList),
          ),
        )
      }
      PathwayMap.DRUG_ALCOHOL.id.toString() -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.DRUG_ALCOHOL.id.toString(), crsReferralDAList, getAlternateMessage(nomisId, PathwayMap.DRUG_ALCOHOL.id.toString(), crsReferralDAList)))
      }
      PathwayMap.ED_SKL_WRK.id.toString() -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ED_SKL_WRK.id.toString(), crsReferralESKList, getAlternateMessage(nomisId, PathwayMap.ED_SKL_WRK.id.toString(), crsReferralESKList)))
      }
      PathwayMap.FINANCE_ID.id.toString() -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.FINANCE_ID.id.toString(), crsReferralFINList, getAlternateMessage(nomisId, PathwayMap.FINANCE_ID.id.toString(), crsReferralFINList)))
      }
      PathwayMap.HEALTH.id.toString() -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.HEALTH.id.toString(), crsReferralHELList, getAlternateMessage(nomisId, PathwayMap.HEALTH.id.toString(), crsReferralHELList)))
      }
      PathwayMap.ATB.id.toString() -> {
        crsReferralWithPathwaysList.add(CRSReferralsWithPathway(PathwayMap.ATB.id.toString(), crsReferralATBList, getAlternateMessage(nomisId, PathwayMap.ATB.id.toString(), crsReferralATBList)))
      }
    }
    crsReferralResponse.results = crsReferralWithPathwaysList
    return crsReferralResponse
  }

  private suspend fun getAlternateMessage(nomsId: String, pathway: String?, crsReferralList: MutableList<CRSReferral>): String {
    var message: String = ""
    if (crsReferralList.isEmpty()) {
      val prisoner = offenderSearchApiService.findPrisonerPersonalDetails(nomsId) ?: null
      var prisonerName = ""
      if (prisoner != null) {
        prisonerName =
          "${prisoner.firstName} ${prisoner.lastName}".convertNameToTitleCase()
      }
      val comName = resettlementPassportDeliusApiService.getComByNomsId(nomsId) ?: ""
      message =
        "No $pathway referral currently exists for $prisonerName. If you think this incorrect, please contact their COM, ${comName.convertNameToTitleCase()}."
    }
    return message
  }
}
