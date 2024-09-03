package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auditable(
  val beforeAction: AuditAction,
  val afterAction: AuditAction,
  val id: String,
  val username: String = "",
  val token: String = ""
)