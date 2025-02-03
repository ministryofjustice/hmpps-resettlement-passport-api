package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonerImage

@Service
class PrisonApiService(val prisonWebClientCredentials: WebClient) {

  @Cacheable("prison-api-get-prisoner-image-data", unless = "#result == null")
  fun getPrisonerImageData(nomsId: String, imageId: Int): ByteArray? {
    var image: ByteArray? = null
    val prisonerImageDetailsList = findPrisonerImageDetails(nomsId)
    var imageIdExists = false
    prisonerImageDetailsList.forEach { prisonerImageDetails ->
      if (prisonerImageDetails.imageId.toInt() == imageId) {
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

  @Cacheable("prison-api-find-prisoner-image-details")
  fun findPrisonerImageDetails(nomsId: String): List<PrisonerImage> = prisonWebClientCredentials
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
