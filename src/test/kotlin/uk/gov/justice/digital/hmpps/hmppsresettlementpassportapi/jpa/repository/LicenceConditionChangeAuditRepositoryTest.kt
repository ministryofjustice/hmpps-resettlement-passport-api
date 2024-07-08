package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.LicenceConditionChangeAuditEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate
import java.time.LocalDateTime

class LicenceConditionChangeAuditRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var licenceConditionsChangeAuditRepository: LicenceConditionsChangeAuditRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    licenceConditionsChangeAuditRepository.deleteAll()
    prisonerRepository.deleteAll()
  }

  @Test
  fun `test persist new licence condition change audit`() {
    val prisoner = prisonerRepository.save(PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23")))

    val licenceConditionChangeAuditEntity = LicenceConditionChangeAuditEntity(prisonerId = prisoner.id!!, licenceConditions = LicenceConditions(1), confirmationDate = null)

    licenceConditionsChangeAuditRepository.save(licenceConditionChangeAuditEntity)

    val assessmentFromDatabase = licenceConditionsChangeAuditRepository.findAll()[0]

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(licenceConditionChangeAuditEntity)
  }
}
