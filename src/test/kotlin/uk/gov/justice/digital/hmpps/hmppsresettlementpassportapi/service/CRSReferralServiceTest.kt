package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CRSReferral
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.InterventionsApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDateTime
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class CRSReferralServiceTest {

  private lateinit var crsReferralService: CRSReferralService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var interventionsApiService: InterventionsApiService

  @Mock
  private lateinit var offenderSearchApiService: OffenderSearchApiService

  @Mock
  private lateinit var resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService

  @BeforeEach
  fun beforeEach() {
    crsReferralService = CRSReferralService(
      prisonerRepository,
      interventionsApiService,
      offenderSearchApiService,
      resettlementPassportDeliusApiService,
    )
  }

  @ParameterizedTest
  @MethodSource("test remove duplicate referrals data")
  fun `test remove duplicate referrals`(input: List<CRSReferral>, expectedOutput: List<CRSReferral>) {
    Assertions.assertEquals(expectedOutput, crsReferralService.removeDuplicateReferrals(input))
  }

  private fun `test remove duplicate referrals data`(): Stream<Arguments> = Stream.of(
    // Empty to empty
    Arguments.of(listOf<CRSReferral>(), listOf<CRSReferral>()),
    // Single CRS Referral isn't changed
    Arguments.of(
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
      ),
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50")
        )
      ),
    ),
    // Multiple different referrals not changed
    Arguments.of(
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType2",
          listOf("serviceCategory2"),
          LocalDateTime.parse("2023-05-02T10:55:50"),
        ),
        createCRSReferral(
          "contractType3",
          listOf("serviceCategory3"),
          LocalDateTime.parse("2023-05-01T10:55:50"),
        ),
      ),
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType2",
          listOf("serviceCategory2"),
          LocalDateTime.parse("2023-05-02T10:55:50"),
        ),
        createCRSReferral(
          "contractType3",
          listOf("serviceCategory3"),
          LocalDateTime.parse("2023-05-01T10:55:50"),
        ),
      ),
    ),
    // Contract types are the same but service categories are different so no change
    Arguments.of(
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory2"),
          LocalDateTime.parse("2023-05-02T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory3"),
          LocalDateTime.parse("2023-05-01T10:55:50"),
        ),
      ),
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory2"),
          LocalDateTime.parse("2023-05-02T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory3"),
          LocalDateTime.parse("2023-05-01T10:55:50"),
        ),
      ),
    ),
    // Exact duplicates should be filtered down to one
    Arguments.of(
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
      ),
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
      ),
    ),
    // Duplicates with different times should pick latest time
    Arguments.of(
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-01T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-02T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          null,
        ),
      ),
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
      ),
    ),
    // Exact duplicates should be filtered down to one - service category order doesn't matter
    Arguments.of(
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1", "serviceCategory2", "serviceCategory3"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory3", "serviceCategory2", "serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory2", "serviceCategory3", "serviceCategory1"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
      ),
      listOf(
        createCRSReferral(
          "contractType1",
          listOf("serviceCategory1", "serviceCategory2", "serviceCategory3"),
          LocalDateTime.parse("2023-05-03T10:55:50"),
        ),
      ),
    ),
  )

  private fun createCRSReferral(
    contractType: String,
    serviceCategories: List<String>,
    referralCreatedAt: LocalDateTime?,
  ) = CRSReferral(serviceCategories, contractType, referralCreatedAt, null, null, null, null, null, null, null, null)
}