package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

interface IdApplicationRepository : JpaRepository<IdApplicationEntity, Long> {
  fun findByPrisonerAndIsDeletedAndCreationDateBetween(prisoner: PrisonerEntity, isDeleted: Boolean = false,
                                                       fromDate: LocalDateTime = LocalDateTime.now().minusYears(50),
                                                       toDate: LocalDateTime = LocalDateTime.now()): List<IdApplicationEntity?>
  fun findByIdAndIsDeleted(id: Long, isDeleted: Boolean = false): IdApplicationEntity?
  fun findByPrisonerAndIdTypeAndIsDeleted(id: PrisonerEntity, idTypeEntity: IdTypeEntity, isDeleted: Boolean = false): IdApplicationEntity?
}
