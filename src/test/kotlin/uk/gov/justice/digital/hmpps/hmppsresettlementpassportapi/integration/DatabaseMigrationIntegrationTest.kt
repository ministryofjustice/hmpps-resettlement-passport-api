package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDateTime
import javax.sql.DataSource

class DatabaseMigrationIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var dataSource: DataSource

  @Autowired
  lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Test
  fun `test database migration - remove ref data`() {
    // Drop database
    val dropDatabasePopulator = ResourceDatabasePopulator(ClassPathResource("testdata/sql/drop-database.sql"))
    dropDatabasePopulator.execute(dataSource)

    // Re-run flyway script up to version 1.37
    Flyway.configure().dataSource(dataSource).target("1.37").load().migrate()

    // Seed database with old data
    val seedDatabasePopulator = ResourceDatabasePopulator(ClassPathResource("testdata/sql/test-database-v1-37.sql"))
    seedDatabasePopulator.execute(dataSource)

    // Re-run flyway script up to latest version
    Flyway.configure().dataSource(dataSource).load().migrate()

    // Assert results
    val prisoner1 = PrisonerEntity(id = 1, nomsId = "G1458GV", creationDate = LocalDateTime.parse("2023-05-17T12:21:44"), crn = "CRN1", prisonId = "xyz", releaseDate = null)
    val prisoner2 = PrisonerEntity(id = 2, nomsId = "G6628UE", creationDate = LocalDateTime.parse("2023-05-17T12:21:44"), crn = "CRN2", prisonId = "xyz", releaseDate = null)
    val prisoner3 = PrisonerEntity(id = 3, nomsId = "G6335VX", creationDate = LocalDateTime.parse("2023-05-17T12:21:44"), crn = "CRN3", prisonId = "xyz", releaseDate = null)
    val prisoner4 = PrisonerEntity(id = 4, nomsId = "G6933GF", creationDate = LocalDateTime.parse("2023-05-17T12:21:44"), crn = "CRN4", prisonId = "xyz", releaseDate = null)
    val prisoner5 = PrisonerEntity(id = 5, nomsId = "A8339DY", creationDate = LocalDateTime.parse("2023-05-17T12:21:44"), crn = "CRN5", prisonId = "xyz", releaseDate = null)

    val actualPathwayStatuses = pathwayStatusRepository.findAll()
    val expectedPathwayStatuses = listOf(
      PathwayStatusEntity(id = 1, prisoner = prisoner1, pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-05-17T12:21:44")),
      PathwayStatusEntity(id = 2, prisoner = prisoner1, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2023-05-18T12:21:44")),
      PathwayStatusEntity(id = 3, prisoner = prisoner1, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2023-05-19T12:21:44")),
      PathwayStatusEntity(id = 4, prisoner = prisoner1, pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2023-05-20T12:21:44")),
      PathwayStatusEntity(id = 5, prisoner = prisoner1, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.DONE, updatedDate = LocalDateTime.parse("2023-05-21T12:21:44")),
      PathwayStatusEntity(id = 6, prisoner = prisoner1, pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-05-22T12:21:44")),
      PathwayStatusEntity(id = 7, prisoner = prisoner1, pathway = Pathway.HEALTH, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2023-05-23T12:21:44")),
      PathwayStatusEntity(id = 8, prisoner = prisoner2, pathway = Pathway.ACCOMMODATION, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2023-05-24T12:21:44")),
      PathwayStatusEntity(id = 9, prisoner = prisoner2, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2023-05-25T12:21:44")),
      PathwayStatusEntity(id = 10, prisoner = prisoner2, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.DONE, updatedDate = LocalDateTime.parse("2023-05-26T12:21:44")),
      PathwayStatusEntity(id = 11, prisoner = prisoner2, pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-05-27T12:21:44")),
      PathwayStatusEntity(id = 12, prisoner = prisoner2, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2023-05-28T12:21:44")),
      PathwayStatusEntity(id = 13, prisoner = prisoner2, pathway = Pathway.FINANCE_AND_ID, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2023-05-29T12:21:44")),
      PathwayStatusEntity(id = 14, prisoner = prisoner2, pathway = Pathway.HEALTH, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2023-05-30T12:21:44")),
      PathwayStatusEntity(id = 15, prisoner = prisoner3, pathway = Pathway.ACCOMMODATION, status = Status.DONE, updatedDate = LocalDateTime.parse("2023-05-31T12:21:44")),
      PathwayStatusEntity(id = 16, prisoner = prisoner3, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-06-01T12:21:44")),
      PathwayStatusEntity(id = 17, prisoner = prisoner3, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2023-06-02T12:21:44")),
      PathwayStatusEntity(id = 18, prisoner = prisoner3, pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2023-06-03T12:21:44")),
      PathwayStatusEntity(id = 19, prisoner = prisoner3, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2023-06-04T12:21:44")),
      PathwayStatusEntity(id = 20, prisoner = prisoner3, pathway = Pathway.FINANCE_AND_ID, status = Status.DONE, updatedDate = LocalDateTime.parse("2023-06-05T12:21:44")),
      PathwayStatusEntity(id = 21, prisoner = prisoner3, pathway = Pathway.HEALTH, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-06-06T12:21:44")),
      PathwayStatusEntity(id = 22, prisoner = prisoner4, pathway = Pathway.ACCOMMODATION, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2023-06-07T12:21:44")),
      PathwayStatusEntity(id = 23, prisoner = prisoner4, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2023-06-08T12:21:44")),
      PathwayStatusEntity(id = 24, prisoner = prisoner4, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2023-06-09T12:21:44")),
      PathwayStatusEntity(id = 25, prisoner = prisoner4, pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.DONE, updatedDate = LocalDateTime.parse("2023-06-10T12:21:44")),
      PathwayStatusEntity(id = 26, prisoner = prisoner4, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-06-11T12:21:44")),
      PathwayStatusEntity(id = 27, prisoner = prisoner4, pathway = Pathway.FINANCE_AND_ID, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2023-06-12T12:21:44")),
      PathwayStatusEntity(id = 28, prisoner = prisoner4, pathway = Pathway.HEALTH, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2023-06-13T12:21:44")),
      PathwayStatusEntity(id = 29, prisoner = prisoner5, pathway = Pathway.ACCOMMODATION, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2023-06-14T12:21:44")),
      PathwayStatusEntity(id = 30, prisoner = prisoner5, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.DONE, updatedDate = LocalDateTime.parse("2023-06-15T12:21:44")),
      PathwayStatusEntity(id = 31, prisoner = prisoner5, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-06-16T12:21:44")),
      PathwayStatusEntity(id = 32, prisoner = prisoner5, pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2023-06-17T12:21:44")),
      PathwayStatusEntity(id = 33, prisoner = prisoner5, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2023-06-18T12:21:44")),
      PathwayStatusEntity(id = 34, prisoner = prisoner5, pathway = Pathway.FINANCE_AND_ID, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2023-06-19T12:21:44")),
      PathwayStatusEntity(id = 35, prisoner = prisoner5, pathway = Pathway.HEALTH, status = Status.DONE, updatedDate = LocalDateTime.parse("2023-06-20T12:21:44")),
    )
    Assertions.assertEquals(expectedPathwayStatuses, actualPathwayStatuses)

    val actualResettlementAssessments = resettlementAssessmentRepository.findAll()
    val expectedResettlementAssessments = listOf(
      ResettlementAssessmentEntity(id = 1, prisoner = prisoner1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 2, prisoner = prisoner1, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 3, prisoner = prisoner1, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 4, prisoner = prisoner1, pathway = Pathway.DRUGS_AND_ALCOHOL, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 5, prisoner = prisoner1, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 6, prisoner = prisoner1, pathway = Pathway.FINANCE_AND_ID, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 7, prisoner = prisoner1, pathway = Pathway.HEALTH, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 8, prisoner = prisoner1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 9, prisoner = prisoner1, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 11, prisoner = prisoner1, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 12, prisoner = prisoner1, pathway = Pathway.DRUGS_AND_ALCOHOL, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 13, prisoner = prisoner1, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 14, prisoner = prisoner1, pathway = Pathway.FINANCE_AND_ID, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
      ResettlementAssessmentEntity(id = 15, prisoner = prisoner1, pathway = Pathway.HEALTH, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-10-16T12:21:38.709"), createdBy = "Prison Officer", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "UNKNOWN", submissionDate = null),
    )
    Assertions.assertEquals(expectedResettlementAssessments, actualResettlementAssessments)
  }
}
