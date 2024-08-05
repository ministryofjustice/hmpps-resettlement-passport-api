package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LearnersCourse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LearnersCourseList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.curiousapi.LearnersEducationList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CuriousApiService

@Service
class LearnersEducationService(
  val learnersEducationApiService: CuriousApiService,
  private val prisonerRepository: PrisonerRepository,
) {
  fun getLearnersEducationCourseData(nomsId: String, pageNumber: Int, pageSize: Int): LearnersCourseList {
    if (nomsId.isBlank() || nomsId.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoners", nomsId)
    }

    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException(
        "Prisoner with id $nomsId not found in the DB",
      )

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }

    val courses = learnersEducationApiService.getLearnersEducation(prisoner.nomsId)

    val startIndex = (pageNumber * pageSize)
    if (startIndex >= courses.content?.size!!) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber",
      )
    }
    val courseList = objectMapper(courses)

    val endIndex = (pageNumber * pageSize) + (pageSize)
    if (startIndex < endIndex && endIndex <= courseList.size) {
      val cList = courseList.subList(startIndex, endIndex)
      return LearnersCourseList(cList, cList.toList().size, pageNumber, courseList.size, (endIndex == courses.size))
    } else if (startIndex < endIndex) {
      val cList = courseList.subList(startIndex, courseList.size)
      return LearnersCourseList(cList, cList.toList().size, pageNumber, courseList.size, true)
    }
    return LearnersCourseList(emptyList(), 0, 0, 0, true)
  }

  fun objectMapper(courses: LearnersEducationList): List<LearnersCourse> {
    val courseList = mutableListOf<LearnersCourse>()
    courses.content?.forEach { course ->
      val learnersCourse = LearnersCourse(
        course.nomsId,
        course.establishmentId,
        course.establishmentName,
        course.courseName,
        course.courseCode,
        course.isAccredited,
        course.aimSequenceNumber,
        course.learningStartDate,
        course.learningPlannedEndDate,
        course.learningActualEndDate,
        course.learnersAimType,
        course.completionStatus,
      )
      courseList.add(learnersCourse)
    }
    return courseList
  }
}
