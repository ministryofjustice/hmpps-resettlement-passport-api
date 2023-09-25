package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate
import java.time.LocalTime

data class Appointment(
  val title: String,
  val contact: String,
  val date: LocalDate?,
  val time: LocalTime?,
  val location: Address?,
)

data class Address(
  val buildingName: String,
  val buildingNumber: String,
  val streetName: String,
  val district: String,
  val town: String,
  val county: String,
  val postcode: String,
)

data class AppointmentsList(
  val results: List<Appointment> = listOf(),
  val totalElements: Int,
  val totalPages: Int,
  val page: Int,
  val size: Int,
)
