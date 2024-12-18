package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse

@Service
class PoPUserApiService(

  private val popUserWebClientCredentials: WebClient,
) {

  fun postPoPUserVerification(oneLoginUrn: String, nomsId: String, crn: String?): PoPUserResponse = popUserWebClientCredentials.post()
    .uri(
      "/person-on-probation-user/user",
    ).contentType(MediaType.APPLICATION_JSON)
    .bodyValue(
      mapOf(
        "crn" to crn,
        "cprId" to "NA",
        "verified" to true,
        "nomsId" to nomsId,
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
