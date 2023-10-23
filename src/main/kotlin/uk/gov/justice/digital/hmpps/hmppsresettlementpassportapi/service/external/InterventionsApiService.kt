package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.ReferralDTO

@Service
class InterventionsApiService(
  private val interventionsWebClientCredentials: WebClient,

) {

  fun fetchProbationCaseReferrals(nomsId: String, crn: String): Flow<List<ReferralDTO>> = flow {
    val data = interventionsWebClientCredentials.get()
      .uri(
        "/probation-case/{crn}/referral",
        mapOf(
          "crn" to crn,
        ),
      )
      .retrieve().onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("NomsId $nomsId / CRN  $crn not found in InterventionService for Probation Case Referral") })
    val pageOfData = data.awaitBodyOrNull<List<ReferralDTO>>()
    if (pageOfData != null) {
      emit(pageOfData)
    }
  }
}
