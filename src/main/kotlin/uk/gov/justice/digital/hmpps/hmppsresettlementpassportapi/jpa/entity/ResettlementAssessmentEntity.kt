package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import java.io.IOException
import java.time.LocalDateTime

@Entity
@Table(name = "resettlement_assessment")
data class ResettlementAssessmentEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  val prisonerId: Long,

  @Enumerated(EnumType.STRING)
  val pathway: Pathway,

  @Enumerated(EnumType.STRING)
  @Column(name = "status_changed_to")
  var statusChangedTo: Status?,

  @Column(name = "assessment_type")
  @Enumerated(EnumType.STRING)
  val assessmentType: ResettlementAssessmentType,

  @Column(name = "assessment")
  @JdbcTypeCode(SqlTypes.JSON)
  @Convert(converter = ResettlementAssessmentConverter::class)
  var assessment: ResettlementAssessmentQuestionAndAnswerList,

  @Column(name = "created_date")
  val creationDate: LocalDateTime,

  @Column(name = "created_by")
  val createdBy: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "assessment_status")
  var assessmentStatus: ResettlementAssessmentStatus,

  @Column(name = "case_note_text")
  var caseNoteText: String?,

  @Column(name = "created_by_user_id")
  val createdByUserId: String,

  @Column(name = "version")
  val version: Int,

  @Column(name = "submission_date")
  var submissionDate: LocalDateTime?,

  @Column(name = "user_declaration")
  var userDeclaration: Boolean?,

  @Column(name = "is_deleted")
  var deleted: Boolean = false,

  @Column(name = "deleted_date")
  var deletedDate: LocalDateTime? = null,
) {
  val questionsAndAnswers: List<ResettlementAssessmentSimpleQuestionAndAnswer>
    get() = assessment.assessment
}

enum class ResettlementAssessmentType(val displayName: String, val alternativeDisplayName: String? = null) {
  BCST2("Immediate needs", "BCST2"),
  RESETTLEMENT_PLAN("Pre-release"),
}

data class ResettlementAssessmentQuestionAndAnswerList(
  var assessment: List<ResettlementAssessmentSimpleQuestionAndAnswer>,
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
