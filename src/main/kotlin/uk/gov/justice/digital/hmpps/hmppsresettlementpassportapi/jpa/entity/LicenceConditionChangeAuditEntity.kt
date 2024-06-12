package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import java.time.LocalDateTime

@Entity
@Table(name = "licence_conditions_change_audit")
data class LicenceConditionChangeAuditEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column
  val prisonerId: Long,
  @Column
  val version: Int = 1,

  @Column(name = "licence_conditions")
  @JdbcTypeCode(SqlTypes.JSON)
  @Convert(converter = LicenseConditionsConverter::class)
  val licenceConditions: LicenceConditions,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime = LocalDateTime.now(),

  @Column(name = "seen")
  val seen: Boolean = false,
)

@Converter(autoApply = true)
@Component
class LicenseConditionsConverter(
  val objectMapper: ObjectMapper,
) : AttributeConverter<LicenceConditions, String> {
  override fun convertToDatabaseColumn(conditions: LicenceConditions): String = objectMapper.writeValueAsString(conditions)

  override fun convertToEntityAttribute(dbData: String): LicenceConditions = objectMapper.readValue(dbData)
}
