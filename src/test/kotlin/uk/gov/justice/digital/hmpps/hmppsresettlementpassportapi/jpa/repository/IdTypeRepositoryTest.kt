package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class IdTypeRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var idTypeRepository: IdTypeRepository

  @Test
  fun `test get saved id types`() {
    val idTypes = idTypeRepository.findAll()

    assertThat(idTypes).isNotEmpty()
  }
}
