package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import java.util.stream.Stream

@Service
class POPUserMetricsService(
  private val registry: MeterRegistry,
  private val prisonOTPService: PoPUserOTPService,
  private val poPUserApiService: PoPUserApiService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val prisonerService: PrisonerService,
  @Value("\${cron.release-dates.batch-size:200}") val batchSize: Int,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun recordCustomMetrics() {
    recordProbationUsersAppointmentMetrics()
  }

  fun recordProbationUsersAppointmentMetrics(): Int {
    val otpList = prisonOTPService.getAllOTPs()

    val prisonList = prisonRegisterApiService.getActivePrisonsList()
    var slice = prisonerService.getSliceOfAllPrisoners(PageRequest.of(0, batchSize).withSort(Sort.by(Sort.Direction.ASC, "id")))
    var cnt = countPopUsers(slice.get())
    while (slice.hasNext()) {
      slice = prisonerService.getSliceOfAllPrisoners((slice.nextPageable()))
      cnt+=countPopUsers(slice.get())
    }
    for (prison in prisonList) {
      val cntByPrison = otpList?.size ?: 0
      registry.gauge(
        "total_pack_print_count",
        cnt,
      )
      return cnt
    }
    return 0
  }

  fun countPopUsers(prisonerEntities: Stream<PrisonerEntity>): Int {
    val activeUserList = poPUserApiService.getVerifiedPopUsers()
    var cnt = 0;
    for(popUser in activeUserList) {
      if (prisonerEntities.anyMatch {value -> popUser.nomsId == value.nomsId})
        cnt +=1
    }
      return cnt
  }
}
