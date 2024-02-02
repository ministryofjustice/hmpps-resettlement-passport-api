package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import java.io.IOException
import java.time.LocalDateTime

@Entity
@Table(name = "resettlement_assessment")
data class ResettlementAssessmentEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @ManyToOne(cascade = [CascadeType.MERGE])
  @JoinColumn(name = "prisoner_id", referencedColumnName = "id")
  val prisoner: PrisonerEntity,

  @ManyToOne
  @JoinColumn(name = "pathway_id", referencedColumnName = "id")
  val pathway: PathwayEntity,

  @ManyToOne
  @JoinColumn(name = "status_changed_to_status_id", referencedColumnName = "id")
  var statusChangedTo: StatusEntity? = null,

  @Column(name = "assessment_type")
  @Enumerated(EnumType.STRING)
  val assessmentType: ResettlementAssessmentType,

  @Column(name = "assessment")
  @JdbcTypeCode(SqlTypes.JSON)
  @Convert(converter = ResettlementAssessmentConverter::class)
  val assessment: ResettlementAssessmentQuestionAndAnswerList,

  @Column(name = "created_date")
  val creationDate: LocalDateTime,

  @Column(name = "created_by")
  val createdBy: String,

  @ManyToOne
  @JoinColumn(name = "assessment_status_id", referencedColumnName = "id")
  var assessmentStatus: ResettlementAssessmentStatusEntity,

  @Column(name = "case_note_text")
  var caseNoteText: String? = null,
)

enum class ResettlementAssessmentType {
  BCST2,
  RESETTLEMENT_PLAN,
}

data class ResettlementAssessmentQuestionAndAnswerList(
  val assessment: List<ResettlementAssessmentSimpleQuestionAndAnswer>,
)

data class ResettlementAssessmentSimpleQuestionAndAnswer(
  val questionId: String,
  val answer: Answer<*>,
)

@Converter(autoApply = true)
@Component
class ResettlementAssessmentConverter(
  val objectMapper: ObjectMapper,
) : AttributeConverter<ResettlementAssessmentQuestionAndAnswerList, String> {
  override fun convertToDatabaseColumn(meta: ResettlementAssessmentQuestionAndAnswerList): String {
    return try {
      objectMapper.writeValueAsString(meta)
    } catch (ex: JsonProcessingException) {
      throw RuntimeException("Error serialising data into resettlement assessment")
    }
  }

  override fun convertToEntityAttribute(dbData: String): ResettlementAssessmentQuestionAndAnswerList {
    return try {
      objectMapper.readValue(dbData, ResettlementAssessmentQuestionAndAnswerList::class.java)
    } catch (ex: IOException) {
      throw RuntimeException("Error deserialising data into resettlement assessment")
    }
  }
}
