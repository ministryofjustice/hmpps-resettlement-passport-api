package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Service
class PoPUserApiService(

  private val popUserWebClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun postPoPUserVerification(oneLoginUrn: String, prisoner: PrisonerEntity): PoPUserResponse = popUserWebClientCredentials.post()
    .uri(
      "/person-on-probation-user/user",
    ).contentType(MediaType.APPLICATION_JSON)
    .bodyValue(
      mapOf(
        "crn" to prisoner.crn,
        "cprId" to "NA",
        "verified" to true,
        "nomsId" to prisoner.nomsId,
        "oneLoginUrn" to oneLoginUrn,
      ),
    )
    .retrieve()
    .bodyToMono<PoPUserResponse>()
    .block() ?: throw RuntimeException("Unexpected null returned from request.")

  fun getAllVerifiedPopUsers(): List<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.PopUserData> =
    popUserWebClientCredentials
      .get()
      .uri("/person-on-probation-user/users/all")
      .retrieve()
      .bodyToFlux<uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.PopUserData>()
      .collectList()
      .block() ?: throw RuntimeException("Unexpected null returned from request.")
}
