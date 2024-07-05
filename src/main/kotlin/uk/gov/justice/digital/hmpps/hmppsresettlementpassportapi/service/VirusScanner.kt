package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.VirusScanResult.NoVirusFound
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult.OK
import xyz.capybara.clamav.commands.scan.result.ScanResult.VirusFound

fun interface VirusScanner {
  fun scan(documentBytes: ByteArray): VirusScanResult
}

class ClamavVirusScanner(@Autowired private val clamavClient: ClamavClient) : VirusScanner {
  override fun scan(documentBytes: ByteArray): VirusScanResult =
    when (val scanResult = clamavClient.scan(documentBytes.inputStream())) {
      OK -> NoVirusFound
      is VirusFound -> VirusScanResult.VirusFound(scanResult.foundViruses)
    }
}

sealed class VirusScanResult {
  object NoVirusFound : VirusScanResult()
  data class VirusFound(val foundViruses: Map<String, Collection<String>>) : VirusScanResult()
}
