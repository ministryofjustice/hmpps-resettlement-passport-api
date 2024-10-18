package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate
import java.time.LocalDateTime

class PoPUserOTPRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var popUserOTPRepository: PoPUserOTPRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    popUserOTPRepository.deleteAll()
    prisonerRepository.deleteAll()
  }

  @Test
  fun `test get saved PoP User OTP `() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1")
    prisonerRepository.save(prisoner)

    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisoner.id(),
      LocalDateTime.now(),
      LocalDateTime.now().plusDays(7).withHour(11).withMinute(59).withSecond(59),
      "1X3456",
      LocalDate.parse("2000-01-01"),
    )
    popUserOTPRepository.save(popUserOTPEntity)

    val popUserOTPResult = popUserOTPRepository.findAll()[0]

    assertThat(popUserOTPResult).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(popUserOTPEntity)
  }

  @Test
  fun `test create PoP User OTP `() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1")
    prisonerRepository.save(prisoner)

    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisoner.id(),
      LocalDateTime.now(),
      LocalDateTime.now().plusDays(7).withHour(11).withMinute(59).withSecond(59),
      "1X3456",
      LocalDate.parse("1982-10-24"),
    )

    val popUserOTPResult = popUserOTPRepository.save(popUserOTPEntity)
    val popUserOTP = popUserOTPRepository.findByPrisonerId(prisoner.id())

    assertThat(popUserOTPResult).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(popUserOTP)
  }
}
