package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonerImage

@Service
class PrisonApiService(val prisonWebClientCredentials: WebClient) {

  fun getPrisonerImageData(nomsId: String, imageId: Int): ByteArray? {
    var image: ByteArray? = null
    val prisonerImageDetailsList = findPrisonerImageDetails(nomsId)
    var imageIdExists = false
    prisonerImageDetailsList.forEach {
      if (it.imageId.toInt() == imageId) {
        imageIdExists = true
        image = prisonWebClientCredentials
          .get()
          .uri(
            "/api/images/$imageId/data",
          )
          .retrieve()
          .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Image not found") })
          .bodyToMono<ByteArray>()
          .block()
      }
    }
    if (!imageIdExists) {
      throw ResourceNotFoundException("Image not found")
    }
    return image
  }

  fun findPrisonerImageDetails(nomsId: String): List<PrisonerImage> {
    return prisonWebClientCredentials
      .get()
      .uri(
        "/api/images/offenders/{nomsId}",
        mapOf(
          "nomsId" to nomsId,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomsId not found") })
      .bodyToFlux<PrisonerImage>()
      .collectList()
      .onErrorComplete(ResourceNotFoundException::class.java)
      .block() ?: emptyList()
  }
}
