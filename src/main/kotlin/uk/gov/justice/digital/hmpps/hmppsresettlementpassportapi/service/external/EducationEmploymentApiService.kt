package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.educationemploymentapi.ReadinessProfileDTO

@Service
class EducationEmploymentApiService(
  val educationEmploymentWebClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Cacheable("education-employment-api-get-readiness-profile-by-noms-id")
  fun getReadinessProfileByNomsId(nomsId: String): ReadinessProfileDTO = educationEmploymentWebClientCredentials.get()
    .uri("/readiness-profiles/$nomsId")
    .retrieve()
    .bodyToMono(ReadinessProfileDTO::class.java)
    .onErrorReturn(
      {
        log.warn("Unexpected error from Education Employment API - ignoring but data will be missing from response!", it)
        it is WebClientException
      },
      ReadinessProfileDTO(profileData = null),
    )
    .block() ?: throw RuntimeException("Unexpected null returned from request.")
}
