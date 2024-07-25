package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class GotenbergFile(
  val name: String,
  val data: ByteArray?,
)