package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase

@SpringBootTest
@ActiveProfiles("test")
class IdTypeRepositoryTest : TestBase() {
  @Autowired
  lateinit var idTypeRepository: IdTypeRepository

  @Test
  fun `test get saved id types`() {
    val idTypes = idTypeRepository.findAll()

    assertThat(idTypes).isNotEmpty()
  }
}
