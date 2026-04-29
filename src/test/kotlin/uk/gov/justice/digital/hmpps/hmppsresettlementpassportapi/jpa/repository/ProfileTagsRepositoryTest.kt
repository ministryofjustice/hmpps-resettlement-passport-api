package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagsEntity
import java.time.LocalDateTime

class ProfileTagsRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var profileTagsRepository: ProfileTagsRepository

  @Test
  fun `test findAllByPrisonerIdOrderByUpdatedDateDesc query`() {
    // Seed database with prisoners and profile tags
    val prisoner1 = prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))

    // save in chronicle order
    val currentTime = LocalDateTime.now()
    val profileTags = listOf(
      listOf("tag 1", "tag 2") to currentTime.minusHours(1),
      listOf("tag 3", "tag 4") to currentTime,
    ).map { (tags, updatedDate) ->
      ProfileTagsEntity(
        id = null,
        prisonerId = prisoner2.id(),
        profileTags = ProfileTagList(tags),
        updatedDate = updatedDate,
      )
    }
    // expected results in reverse chronicle order (order by updatedDate desc)
    val expectedProfileTags = profileTags.reversed()

    profileTagsRepository.saveAll(profileTags)

    // Prisoner 1 has no profile tags
    // Prisoner 2 has profile tags
    Assertions.assertThat(profileTagsRepository.findAllByPrisonerIdOrderByUpdatedDateDesc(prisoner1.id())).isEmpty()
    Assertions.assertThat(profileTagsRepository.findAllByPrisonerIdOrderByUpdatedDateDesc(prisoner2.id())).isEqualTo(expectedProfileTags)
  }
}
