package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Accommodation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OfficerInfo
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime
import java.time.ZoneId

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

    var msg: String? = null
    if (accommodation.mainAddress?.noFixedAbode == true) {
      msg =
        prisonerDetails.firstName.convertNameToTitleCase() + " " + prisonerDetails.lastName.convertNameToTitleCase() + " is currently of no fixed abode. They may require assistance finding accommodation. If a CRS referral or duty to refer have been made, details will be shown above."
    }
    var startDateTimeVal: LocalDateTime?= null
    if (accommodation.startDateTime!=null){
      startDateTimeVal = LocalDateTime.ofInstant(accommodation.startDateTime.toInstant(), ZoneId.of("Europe/London"))
    }

    val mainAddress = constructAddress(
      arrayOf(
        accommodation.mainAddress?.buildingName,
        accommodation.mainAddress?.addressNumber,
        accommodation.mainAddress?.streetName,
        accommodation.mainAddress?.district,
        accommodation.mainAddress?.town,
        accommodation.mainAddress?.county,
        accommodation.mainAddress?.postcode,
      ),
    )
    val officerInfo = OfficerInfo(
      accommodation.officer?.forename,
      accommodation.officer?.surname,
      accommodation.officer?.middlename,
    )
    return Accommodation(
      accommodation.referralDate,
      accommodation.provider,
      accommodation.team,
      officerInfo,
      accommodation.status,
      startDateTimeVal,
      accommodation.notes,
      mainAddress,
      msg,
    )
  }
}
