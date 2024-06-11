package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "watchlist")
class WatchlistEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val prisonerId: Long,

  @Column(name = "staff_username")
  val staffUsername: String,

  @Column(name = "creation_date")
  val creationDate: LocalDateTime,
)
