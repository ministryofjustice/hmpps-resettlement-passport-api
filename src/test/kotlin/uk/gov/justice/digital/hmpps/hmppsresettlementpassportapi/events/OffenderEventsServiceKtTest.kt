package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events.MovementReasonType.RECALL

class OffenderEventsServiceKtTest {
  @TestFactory
  fun `checks whether event should be considered recall`() =
    listOf(
      "Y" to RECALL,
      "PD" to RECALL,
      "ELR" to RECALL,
      "B" to RECALL,
      "24" to RECALL,
      "ETRLR" to RECALL,
      "L" to RECALL,
      "O" to null,
      "Z" to null,
    ).map { (reasonCode, reasonType) ->
      dynamicTest("$reasonCode should be $reasonType") {
        assertThat(toReasonType(reasonCode)).isEqualTo(reasonType)
      }
    }
}
