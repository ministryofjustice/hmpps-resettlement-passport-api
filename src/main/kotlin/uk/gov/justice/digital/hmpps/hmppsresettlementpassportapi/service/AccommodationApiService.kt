package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Accommodation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OfficerInfo
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

@Service
class AccommodationApiService(
  private val prisonerRepository: PrisonerRepository,
  private val rpDeliusApiService: ResettlementPassportDeliusApiService,
  private val offenderSearchApiService: OffenderSearchApiService,
) {

  suspend fun getAccommodationMainAddressByNomisId(
    nomisId: String,
  ): Accommodation {
    if (nomisId.isBlank() || nomisId.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoner", nomisId)
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomisId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomisId not found in database")

    var crn = prisonerEntity.crn

    crn = if (crn.isBlank() or crn.isEmpty()) {
      rpDeliusApiService.getCrn(nomisId).toString()
    } else {
      prisonerEntity.crn
    }

    val prisonerDetails = offenderSearchApiService.findPrisonerPersonalDetails(nomisId)

    val accommodation = rpDeliusApiService.fetchAccommodation(nomisId, crn)

    var msg = ""
    if (accommodation.mainAddress?.noFixedAbode == true) {
      msg =
        prisonerDetails.firstName.convertNameToTitleCase() + " " + prisonerDetails.lastName.convertNameToTitleCase() + " is currently of no fixed abode. They may require assistance finding accommodation. If a CRS referral or duty to refer have been made, details will be shown above."
    }
    val mainAddress = constructAddress(
        arrayOf(
            accommodationAddress.mainAddress?.buildingName,
            accommodationAddress.mainAddress?.addressNumber,
            accommodationAddress.mainAddress?.streetName,
            accommodationAddress.mainAddress?.district,
            accommodationAddress.mainAddress?.town,
            accommodationAddress.mainAddress?.county,
            accommodationAddress.mainAddress?.postcode,
        ),
    )
    val officeInfo = OfficerInfo(
      accommodation.officer?.forename,
      accommodation.officer?.surname,
      accommodation.officer?.middlename,
    )
    return Accommodation(
      accommodation.referralDate,
      accommodation.provider,
      accommodation.team,
      officeInfo,
      accommodation.status,
      accommodation.startDateTime,
      accommodation.notes,
      addressInfo,
    )
  }
}
