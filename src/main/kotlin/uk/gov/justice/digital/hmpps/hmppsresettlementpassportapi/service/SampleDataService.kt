package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SampleDataDTO
import java.time.LocalDateTime

@Service
class SampleDataService() {

  fun getSampleData(sampleId: String): SampleDataDTO {
    val sampleDataDTO: SampleDataDTO = SampleDataDTO(sampleId, LocalDateTime.now(), "Data Generated as " + sampleId + " requested @ " + LocalDateTime.now())
    return sampleDataDTO
  }
}
