package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AppointmentsDataTag
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AppointmentsList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceTag
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MissingFieldScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserAppointmentCountMetric
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserAppointmentCountMetrics
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserCountMetrics
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserLicenceCountMetric
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import java.time.LocalDate

@Service
class PoPUserMetricsService(
  private val registry: MeterRegistry,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val licenceConditionService: LicenceConditionService,
  private val popUserApiService: PoPUserApiService,
  private val prisonerRepository: PrisonerRepository,
  private val appointmentsService: AppointmentsService,
) {
  private val popUserLicenceCountMetric = PopUserCountMetrics()
  private val popUserAppointmentMetrics = PopUserAppointmentCountMetrics()

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun recordCustomMetrics() {
    recordProbationUsersLicenceConditionMetrics()
    recordProbationUsersAppointmentsMetrics()
  }

  fun recordProbationUsersLicenceConditionMetrics() {
    log.info("Started running scheduled POP User metrics job - LicenceCondition")
    val popUserList = popUserApiService.getAllVerifiedPopUsers()
    val totalPopUser = popUserList.size
    if (totalPopUser > 0) {
      val prisonList = prisonRegisterApiService.getActivePrisonsList()
      var percentStdLicenceCondition: Double
      var percentOtherLicenceCondition: Double
      for (prison in prisonList) {
        var stdLicenceConditionCount = 0
        var otherLicenceConditionCount = 0
        var noLicenceUserCount = 0
        val prisonersList = prisonerRepository.findByPrisonId(prison.id)
        try {
          var popUserExists = false
          for (popUser in popUserList) {
            val prisoner = prisonersList.filter { it.nomsId == popUser.nomsId }
            if (prisoner.isNotEmpty() && prisoner.size == 1) {
              popUserExists = true
              var licencesConditions: LicenceConditions
              try {
                licencesConditions = licenceConditionService.getLicenceConditionsByNomsId(prisoner[0].nomsId)!!
                if (licencesConditions.standardLicenceConditions.isNullOrEmpty() || licencesConditions.otherLicenseConditions.isNullOrEmpty()) {
                  noLicenceUserCount += 1
                }
                if (licencesConditions.standardLicenceConditions.isNullOrEmpty()) {
                  stdLicenceConditionCount += 1
                }
                if (licencesConditions.otherLicenseConditions.isNullOrEmpty()) {
                  otherLicenceConditionCount += 1
                }
              } catch (_: ResourceNotFoundException) {
                log.info("Unable to get the licence condition for nomsId ${prisoner[0].nomsId}")
                stdLicenceConditionCount += 1
                otherLicenceConditionCount += 1
                noLicenceUserCount += 1
                continue
              }
            }
          }
          if (popUserExists) {
            percentStdLicenceCondition = if (stdLicenceConditionCount > 0) {
              calculatePercentage(stdLicenceConditionCount, totalPopUser)
            } else {
              0.0
            }
            percentOtherLicenceCondition = if (otherLicenceConditionCount > 0) {
              calculatePercentage(otherLicenceConditionCount, totalPopUser)
            } else {
              0.0
            }

            val metrics = listOf(
              PopUserLicenceCountMetric(LicenceTag.MISSING_STANDARD_PERCENTAGE, percentStdLicenceCondition),
              PopUserLicenceCountMetric(LicenceTag.MISSING_OTHERS_PERCENTAGE, percentOtherLicenceCondition),
              PopUserLicenceCountMetric(LicenceTag.MISSING_STANDARD_COUNT, stdLicenceConditionCount.toDouble()),
              PopUserLicenceCountMetric(LicenceTag.MISSING_OTHERS_COUNT, otherLicenceConditionCount.toDouble()),
              PopUserLicenceCountMetric(LicenceTag.NO_LICENCE_USER_COUNT, noLicenceUserCount.toDouble()),

            )
            popUserLicenceCountMetric.metrics[prison] = metrics
            val prisonTag = Tags.of("prison", prison.name)

            popUserLicenceCountMetric.metrics[prison]?.forEachIndexed { i, metric ->
              registry.gauge(
                "missing_licence_conditions",
                prisonTag
                  .and("metricType", metric.licenceType.label),
                popUserLicenceCountMetric,
              ) {
                it.metrics[prison]?.get(i)?.value
                  ?: throw RuntimeException("Can't find value for metric $metric. This is likely a coding error!")
              }
            }
          }
        } catch (ex: Exception) {
          log.warn("Error collecting metrics for popUser Missing Licence Conditions ${prison.name}", ex)
        }
      }
    }
    log.info("Finished running scheduled POP User metrics job - LicenceCondition")
  }

  fun recordProbationUsersAppointmentsMetrics() {
    log.info("Started running scheduled POP User metrics job - Appointments")
    val popUserList = popUserApiService.getAllVerifiedPopUsers()
    val totalPopUser = popUserList.size
    if (totalPopUser > 0) {
      val prisonList = prisonRegisterApiService.getActivePrisonsList()
      var scoreDate: Int
      var scoreTime: Int
      var scoreType: Int
      var scoreLocation: Int
      var scorePO: Int
      var scoreEmail: Int
      var percentDate: Double
      var percentTime: Double
      var percentType: Double
      var percentLocation: Double
      var percentPO: Double
      var percentEmail: Double

      for (prison in prisonList) {
        var missingDateCount = 0
        var missingTimeCount = 0
        var missingTypeCount = 0
        var missingLocationCount = 0
        var missingPOCount = 0
        var missingEmailCount = 0
        var totalAppointments = 0
        var noAppointmentsUserCount = 0

        val prisonersList = prisonerRepository.findByPrisonId(prison.id)
        try {
          var popUserExists = false
          for (popUser in popUserList) {
            val prisoner = prisonersList.filter { it.nomsId == popUser.nomsId }
            if (prisoner.isNotEmpty() && prisoner.size == 1) {
              popUserExists = true
              var appointmentsList: AppointmentsList
              try {
                appointmentsList = appointmentsService.getAppointmentsByNomsId(prisoner[0].nomsId, LocalDate.now(), LocalDate.now().plusDays(365), false)
                if (appointmentsList.results.isNotEmpty()) {
                  totalAppointments += appointmentsList.results.size
                  missingDateCount += appointmentsList.results.filter { it.date == null }.size
                  missingTimeCount += appointmentsList.results.filter { it.time == null }.size
                  missingTypeCount = appointmentsList.results.filter { it.type == null }.size
                  missingLocationCount += appointmentsList.results.size - appointmentsList.results.filter { it.location != null && (it.location.postcode != null || it.location.streetName != null) }.size
                  missingPOCount += appointmentsList.results.filter { it.contact == null }.size
                  missingEmailCount += appointmentsList.results.filter { it.contactEmail == null }.size
                } else {
                  totalAppointments += 1
                  missingDateCount += 1
                  missingTimeCount += 1
                  missingTypeCount += 1
                  missingLocationCount += 1
                  missingPOCount += 1
                  missingEmailCount += 1
                  noAppointmentsUserCount += 1
                }
              } catch (_: ResourceNotFoundException) {
                log.info("Unable to get the appointments for nomsId ${prisoner[0].nomsId}")
                continue
              }
            }
          }
          if (popUserExists) {
            scoreDate = missingDateCount * MissingFieldScore.TWO.score
            scoreTime = missingTimeCount * MissingFieldScore.TWO.score
            scoreType = missingTypeCount * MissingFieldScore.ONE.score
            scoreLocation = missingLocationCount * MissingFieldScore.TWO.score
            scorePO = missingPOCount * MissingFieldScore.ONE.score
            scoreEmail = missingPOCount * MissingFieldScore.ONE.score
            percentDate = if (missingDateCount in 1..totalAppointments) {
              calculatePercentage(missingDateCount, totalAppointments)
            } else {
              0.0
            }

            percentTime = if (missingTimeCount in 1..totalAppointments) {
              calculatePercentage(missingTimeCount, totalAppointments)
            } else {
              0.0
            }

            percentType = if (missingTypeCount in 1..totalAppointments) {
              calculatePercentage(missingTypeCount, totalAppointments)
            } else {
              0.0
            }

            percentLocation = if (missingLocationCount in 1..totalAppointments) {
              calculatePercentage(missingLocationCount, totalAppointments)
            } else {
              0.0
            }

            percentPO = if (missingPOCount in 1..totalAppointments) {
              calculatePercentage(missingPOCount, totalAppointments)
            } else {
              0.0
            }

            percentEmail = if (missingEmailCount in 1..totalAppointments) {
              calculatePercentage(missingEmailCount, totalAppointments)
            } else {
              0.0
            }

            val metrics = listOf(
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_DATE_SCORE, scoreDate.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_TIME_SCORE, scoreTime.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_TYPE_SCORE, scoreType.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_LOCATION_SCORE, scoreLocation.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_PROBATION_OFFICER_SCORE, scorePO.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_EMAIL_SCORE, scoreEmail.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_DATE_PERCENTAGE, percentDate),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_TIME_PERCENTAGE, percentTime),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_TYPE_PERCENTAGE, percentType),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_LOCATION_PERCENTAGE, percentLocation),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_PROBATION_OFFICER_PERCENTAGE, percentPO),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_EMAIL_PERCENTAGE, percentEmail),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_DATE_COUNT, missingDateCount.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_TIME_COUNT, missingTimeCount.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_TYPE_COUNT, missingTypeCount.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_LOCATION_COUNT, missingLocationCount.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_PROBATION_OFFICER_COUNT, missingPOCount.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.MISSING_EMAIL_COUNT, missingEmailCount.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.NO_APPOINTMENTS_USER_COUNT, noAppointmentsUserCount.toDouble()),
            )
            popUserAppointmentMetrics.metrics[prison] = metrics
            val prisonTag = Tags.of("prison", prison.name)

            popUserAppointmentMetrics.metrics[prison]?.forEachIndexed { i, metric ->
              registry.gauge(
                "missing_appointments_data",
                prisonTag
                  .and("metricType", metric.appointmentFieldType.label),
                popUserAppointmentMetrics,
              ) {
                it.metrics[prison]?.get(i)?.value
                  ?: throw RuntimeException("Can't find value for metric $metric. This is likely a coding error!")
              }
            }
          }
        } catch (ex: Exception) {
          log.warn("Error collecting metrics for popUser Missing Appointments ${prison.name}", ex)
        }
      }
    }
    log.info("Finished running scheduled POP User metrics job - Appointments")
  }

  fun recordReleaseDayProbationUserAppointmentsMetrics() {
    log.info("Started running scheduled POP User metrics job - Release day Appointments")
    val popUserList = prisonerRepository.findByReleaseDateGreaterThanEqualAndReleaseDateLessThanEqual(LocalDate.now(), LocalDate.now().plusDays(1))
    val totalPopUser = popUserList.size
    val versionRegex = "Initial Appointment".toRegex()
    if (totalPopUser > 0) {
      val prisonList = prisonRegisterApiService.getActivePrisonsList()
      for (prison in prisonList) {
        var zeroAnyAppointmentsCount = 0
        var zeroProbationAppointmentsCount = 0
        try {
          var popUserExists = false
          for (prisoner in popUserList) {
            if (prisoner.prisonId.equals(prison.id)) {
              popUserExists = true
              var appointmentsList: AppointmentsList
              try {
                appointmentsList = appointmentsService.getAppointmentsByNomsId(
                  prisoner.nomsId,
                  LocalDate.now(),
                  LocalDate.now().plusDays(365),
                  false,
                )
                if (appointmentsList.results.isNotEmpty()) {
                  if (!appointmentsList.results.any { versionRegex.matchesAt(it.title, 5) }) {
                    zeroProbationAppointmentsCount += 1
                  }
                } else {
                  zeroAnyAppointmentsCount += 1
                  zeroProbationAppointmentsCount += 1
                }
              } catch (_: ResourceNotFoundException) {
                log.info("Unable to get the appointments for nomsId ${prisoner.nomsId}")
                continue
              }
            }
          }
          if (popUserExists) {
            val metrics = listOf(
              PopUserAppointmentCountMetric(AppointmentsDataTag.RELEASE_DAY_ZERO_COUNT, zeroAnyAppointmentsCount.toDouble()),
              PopUserAppointmentCountMetric(AppointmentsDataTag.RELEASE_DAY_PROBATION_ZERO_COUNT, zeroProbationAppointmentsCount.toDouble()),
            )
            popUserAppointmentMetrics.metrics[prison] = metrics
            val prisonTag = Tags.of("prison", prison.name)

            popUserAppointmentMetrics.metrics[prison]?.forEachIndexed { i, metric ->
              registry.gauge(
                "release_day_appointments_data",
                prisonTag
                  .and("metricType", metric.appointmentFieldType.label),
                popUserAppointmentMetrics,
              ) {
                it.metrics[prison]?.get(i)?.value
                  ?: throw RuntimeException("Can't find value for metric $metric. This is likely a coding error!")
              }
            }
          }
        } catch (ex: Exception) {
          log.warn("Error collecting metrics for popUser Release Day Appointments ${prison.name}", ex)
        }
      }
    }
    log.info("Finished running scheduled POP User metrics job - Release Day Appointments")
  }

  fun calculatePercentage(count: Int, total: Int): Double {
    return if (count > 0) {
      ((count.toDouble() / total) * 100)
    } else {
      0.0
    }
  }
}
