package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OffenderEventRepository : JpaRepository<OffenderEventEntity, UUID> {
  fun findAllByPrisonerId(prisonerId: Long): List<OffenderEventEntity>
}
