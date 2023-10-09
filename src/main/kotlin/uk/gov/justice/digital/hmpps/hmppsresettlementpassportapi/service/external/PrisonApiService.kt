package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonerImage

@Service
class PrisonApiService(val prisonWebClientCredentials: WebClient) {

  fun getPrisonerImageData(nomsId: String, imageId: Int): Flow<ByteArray> = flow {
    val prisonerImageDetailsList = findPrisonerImageDetails(nomsId)
    var imageIdExists = false
    prisonerImageDetailsList.forEach {
      if (it.imageId.toInt() == imageId) {
        imageIdExists = true
        val image = prisonWebClientCredentials
          .get()
          .uri(
            "/api/images/$imageId/data",
          )
          .retrieve()
          .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Image not found") })
          .awaitBody<ByteArray>()
        emit(image)
      }
    }
    if (!imageIdExists) {
      throw ResourceNotFoundException("Image not found")
    }
  }

  suspend fun findPrisonerImageDetails(nomsId: String): List<PrisonerImage> {
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
      .awaitBody<List<PrisonerImage>>()
  }
}
