/**
 * These are mock extensions for static mocks among tests:
 * - LocalDateTime.now()
 * - UUID.randomUUID()
 * - getClaimFromJWTToken(token, claimName)
 */
package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.randomAlphaNumericString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

class CurrentDateTimeMockExtension :
  MockkMockExtension(),
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val testDate: LocalDateTime = LocalDateTime.parse("2023-08-16T12:00:00")
    val fakeNow: LocalDateTime = LocalDateTime.parse("2023-08-17T12:00:01")

    fun mockCurrentTime(now: LocalDateTime = fakeNow) = every { LocalDateTime.now() } returns now
  }

  override fun beforeAll(context: ExtensionContext) {
    mockkStatic(LocalDateTime::class)
  }

  override fun beforeEach(context: ExtensionContext) {
    mockCurrentTime()
    every { LocalDateTime.parse(any<String>()) } answers { callOriginal() }
  }

  override fun afterAll(context: ExtensionContext) {
    unmockkStatic(LocalDateTime::class)
  }
}

class CurrentOffsetDateTimeMockExtension :
  MockkMockExtension(),
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val fakeNowOffset: OffsetDateTime = OffsetDateTime.parse("2024-06-04T09:16:04+01:00")

    fun mockCurrentOffsetTime(now: OffsetDateTime = fakeNowOffset) = every { OffsetDateTime.now() } returns now
  }

  override fun beforeAll(context: ExtensionContext) {
    mockkStatic(OffsetDateTime::class)
    every { OffsetDateTime.parse(any<String>()) } answers { callOriginal() }
  }

  override fun beforeEach(context: ExtensionContext) {
    mockCurrentOffsetTime()
  }

  override fun afterAll(context: ExtensionContext) {
    unmockkStatic(OffsetDateTime::class)
  }
}

class CurrentDateMockExtension :
  MockkMockExtension(),
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val testDate: LocalDate = LocalDate.parse("2024-08-05")

    fun mockCurrentDate(now: LocalDate = testDate) = every { LocalDate.now() } returns now
  }

  override fun beforeAll(context: ExtensionContext) {
    mockkStatic(LocalDate::class)
  }

  override fun beforeEach(context: ExtensionContext) {
    mockCurrentDate()
    every { LocalDate.parse(any<String>()) } answers { callOriginal() }
    every { any<Int>().let { LocalDate.of(it, it, it) } } answers { callOriginal() }
  }

  override fun afterAll(context: ExtensionContext) {
    unmockkStatic(LocalDate::class)
  }
}

class UUIDMockExtension :
  MockkMockExtension(),
  BeforeAllCallback,
  AfterAllCallback {
  companion object {
    fun mockRandomUUID(uuid: UUID) = every { UUID.randomUUID() }.returns(uuid)
  }

  override fun beforeAll(context: ExtensionContext) {
    mockkStatic(UUID::class)
  }

  override fun afterAll(context: ExtensionContext) {
    unmockkStatic(UUID::class)
  }
}

class JWTTokenMockExtension :
  MockkMockExtension(),
  BeforeAllCallback,
  AfterAllCallback {
  companion object {
    fun mockClaimFromJWTToken(
      token: String = "auth",
      claimValue: String? = "USERNAME",
      claimName: String = "sub",
    ) = every { getClaimFromJWTToken(token, claimName) } returns claimValue
  }

  override fun beforeAll(context: ExtensionContext) {
    mockkStatic(::getClaimFromJWTToken)
  }

  override fun afterAll(context: ExtensionContext) {
    unmockkStatic(::getClaimFromJWTToken)
  }
}

class ServiceUtilsMockExtension :
  MockkMockExtension(),
  BeforeAllCallback,
  AfterAllCallback,
  AfterEachCallback {
  companion object {
    fun mockRandomAlphaNumericString(value: String) = every { randomAlphaNumericString() } returns value
  }

  override fun beforeAll(context: ExtensionContext) {
    mockkStatic(::randomAlphaNumericString)
  }

  override fun afterAll(context: ExtensionContext) {
    unmockkStatic(::randomAlphaNumericString)
  }
}

abstract class MockkMockExtension : AfterEachCallback {
  override fun afterEach(context: ExtensionContext) {
    clearAllMocks()
  }
}
