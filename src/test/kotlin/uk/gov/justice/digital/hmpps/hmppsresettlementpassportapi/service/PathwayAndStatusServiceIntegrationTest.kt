package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

class PathwayAndStatusServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Autowired
  private lateinit var prisonerRepository: PrisonerRepository

  @Test
  fun `Loads prisoner when trying to create and getting a unique constraint violation`() {
    val existingPrisoner = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "A123",
        prisonId = "P123",
      ),
    )

    val result = pathwayAndStatusService.createPrisoner(nomsId = "A123", prisonId = "P123")

    assertThat(result.id).isEqualTo(existingPrisoner.id)
  }
}
