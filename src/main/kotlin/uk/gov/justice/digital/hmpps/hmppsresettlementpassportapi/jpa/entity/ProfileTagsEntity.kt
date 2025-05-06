package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
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
import java.io.IOException
import java.time.LocalDateTime

@Entity
@Table(name = "profile_tag")
data class ProfileTagsEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "prisoner_id")
  val prisonerId: Long,

  @Column(name = "profile_tags")
  @JdbcTypeCode(SqlTypes.JSON)
  @Convert(converter = ProfileTagConverter::class)
  var profileTags: ProfileTagList,

  @Column(name = "updated_date")
  var updatedDate: LocalDateTime? = null,
)

data class ProfileTagList(
  var tags: List<String>,
)

@Converter(autoApply = true)
@Component
class ProfileTagConverter(
  val objectMapper: ObjectMapper,
) : AttributeConverter<ProfileTagList, String> {
  override fun convertToDatabaseColumn(meta: ProfileTagList): String = try {
    objectMapper.writeValueAsString(meta)
  } catch (ex: JsonProcessingException) {
    throw RuntimeException("Error serialising data into profile tags")
  }

  override fun convertToEntityAttribute(dbData: String): ProfileTagList = try {
    objectMapper.readValue(dbData, ProfileTagList::class.java)
  } catch (ex: IOException) {
    throw RuntimeException("Error deserialising data into profile tags")
  }
}
