package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.OneLoginData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.util.*

@Service
class PoPUserApiService(

  private val popUserWebClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun postPoPUserVerification(oneLoginData: OneLoginData, prisoner: Optional<PrisonerEntity>?, prisonerSearch: PrisonersSearch): PoPUserResponse? {
    if (prisoner != null) {
      return popUserWebClientCredentials.post()
        .uri(
          "/person-on-probation-user/user",
        ).contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          mapOf(
            "crn" to prisoner.get().crn,
            "cprId" to "NA",
            "verified" to true,
            "nomsId" to prisoner.get().nomsId,
            "oneLoginUrn" to oneLoginData.urn,
          ),
        )
        .retrieve()
        .bodyToMono<PoPUserResponse>()
        .block()
    } else {
      throw ResourceNotFoundException("Person On Probation User verification already done.")
    }
  }

  fun getVerifiedPopUsers(): List<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.PopUserData> {
    return popUserWebClientCredentials
      .get()
      .uri("/person-on-probation-user/users/all")
      .retrieve()
      .bodyToFlux<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.PopUserData>()
      .collectList()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
  }
}
