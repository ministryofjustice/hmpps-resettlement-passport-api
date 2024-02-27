package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.util.*

@Repository
interface PrisonerRepository : JpaRepository<PrisonerEntity, Long> {

  fun findByNomsId(nomsId: String): PrisonerEntity?
}
