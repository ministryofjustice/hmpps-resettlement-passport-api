package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OffenderEventRepository : JpaRepository<OffenderEventEntity, UUID>
