package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.OneLoginData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import java.util.*

@Service
class PoPUserApiService(

  private val poPUserOTPRepository: PoPUserOTPRepository,
  private val popUserWebClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun postPoPUserVerification(oneLoginData: OneLoginData, prisoner: Optional<PrisonerEntity>?): PoPUserResponse? {
    if (prisoner != null) {
      return popUserWebClientCredentials.post()
        .uri(
          "/person-on-probation-user/user",
        ).contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          mapOf(
            "crn" to prisoner.get().crn,
            "email" to oneLoginData.email,
            "cprId" to "NA",
            "verified" to true,
            "nomsId" to prisoner.get().nomsId,
            "oneLoginUrn" to oneLoginData.urn,
            "prisonId" to prisoner.get().prisonId,
            "releaseDate" to prisoner.get().releaseDate,
          ),
        )
        .retrieve()
        .bodyToMono<PoPUserResponse>()
        .block()
    } else {
      throw ResourceNotFoundException("Person On Probation User verification already done.")
    }
  }
}
