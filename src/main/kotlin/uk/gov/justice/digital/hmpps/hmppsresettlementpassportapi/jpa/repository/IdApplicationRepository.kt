package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity

interface IdApplicationRepository : JpaRepository<IdApplicationEntity, Long> {
  fun findByPrisonerIdAndIsDeleted(prisonerId: Long, isDeleted: Boolean = false): List<IdApplicationEntity?>
  fun findByIdAndIsDeleted(id: Long, isDeleted: Boolean = false): IdApplicationEntity?
  fun findByPrisonerIdAndIdTypeAndIsDeleted(prisonerId: Long, idTypeEntity: IdTypeEntity, isDeleted: Boolean = false): IdApplicationEntity?
}
