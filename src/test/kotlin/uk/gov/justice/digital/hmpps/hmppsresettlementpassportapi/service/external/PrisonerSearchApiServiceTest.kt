package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.io.File

class PrisonerSearchApiServiceTest {

  private fun buildClient(): WebClient = WebClient.builder()
    .baseUrl("https://prisoner-search-dev.prison.service.justice.gov.uk")
    .defaultHeaders { headers ->
      headers.setBearerAuth("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImhtcHBzLWF1dGgtZGV2LTIwMjMwMzA2In0.eyJzdWIiOiJobXBwcy1yZXNldHRsZW1lbnQtcGFzc3BvcnQtYXBpLTEiLCJncmFudF90eXBlIjoiY2xpZW50X2NyZWRlbnRpYWxzIiwic2NvcGUiOlsicmVhZCIsIndyaXRlIl0sImF1dGhfc291cmNlIjoibm9uZSIsImlzcyI6Imh0dHBzOi8vc2lnbi1pbi1kZXYuaG1wcHMuc2VydmljZS5qdXN0aWNlLmdvdi51ay9hdXRoL2lzc3VlciIsImV4cCI6MTcyNjA2OTcwNCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9WSUVXX1BPTV9BTExPQ0FUSU9OUyIsIlJPTEVfT05FX1BMQU5fRURJVCIsIlJPTEVfS0VZX1dPUktFUiIsIlJPTEVfUFJJU09ORVJfSU5fUFJJU09OX1NFQVJDSCIsIlJPTEVfUklTS19SRVNFVFRMRU1FTlRfUEFTU1BPUlRfUk8iLCJST0xFX1VTRVJTX19QUklTT05fVVNFUlNfX0ZJTkRfQllfQ0FTRUxPQURfQU5EX1JPTEVfX1JPIiwiUk9MRV9DVVJJT1VTX0FQSSIsIlJPTEVfQ1ZMX0FETUlOIiwiUk9MRV9QUk9CQVRJT05fQVBJX19SRVNFVFRMRU1FTlRfUEFTU1BPUlRfX0NBU0VfREVUQUlMIiwiUk9MRV9WSUVXX0NBU0VfTk9URVMiLCJST0xFX0lOVEVSVkVOVElPTlNfQVBJX1JFQURfQUxMIiwiUk9MRV9JTlRFUlZFTlRJT05TX1JFRkVSX0FORF9NT05JVE9SIiwiUk9MRV9WSUVXX1BSSVNPTkVSX0RBVEEiLCJST0xFX1dPUktfUkVBRElORVNTX1ZJRVciLCJST0xFX1BST0JBVElPTl9BUElfX1JFU0VUVExFTUVOVF9QQVNTUE9SVF9fQVBQT0lOVE1FTlRfUlciLCJST0xFX1BSSVNPTkVSX1NFQVJDSCIsIlJPTEVfUkVTRVRUTEVNRU5UX1BBU1NQT1JUX0VESVQiXSwianRpIjoiS1Z1cjJvZkJlZk1hZnp0RG9fQ2poUVZET2ZFIiwiY2xpZW50X2lkIjoiaG1wcHMtcmVzZXR0bGVtZW50LXBhc3Nwb3J0LWFwaS0xIn0.H0sEaIwfPiUOm0wdQJave2ZPRsRvUfBob4hS9muVxN0vXSD7kxS-PxpEaMyyIVKykfq8vLZPO9teZktpuXDwgA6xB8EdMqb3_jpcZdwwQDQ64SPpaRjvr3KIcef2n6aPQ5CGQE-1w3lTRD4A4Q5THrHx_L3a-4I_31JS4Zhusr0EUEH5AUospOflfYal9OlA_l7dYaUBHchqVLF3qZ4wWlVICA1jloLmRh50iSN6UIHuQFMGwiy7N0ut3pT5sQUBvAK6bl_Js2t6AlSYjL_4UpovZJatrr3v4NGeaosV7vK3ZLZhtY9hDLnzvjcBx-Ymx33VR_CeBLOXzhJX8xRPSg")
    }
    .codecs { codecs ->
      codecs.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)
    }
    .build()

  private val prisonerSearchApiService: PrisonerSearchApiService = PrisonerSearchApiService(buildClient())

  @Test
  fun djfiofdjg() {
    println(
      buildClient().post()
        .uri("/prisoner-search/match-prisoners")
        .bodyValue(
          PrisonerMatchRequest(
            firstName = "Neil",
            lastName = "Youngish",
          ),
        )
        .retrieve()
        .bodyToMono(String::class.java)
        .block(),
    )
  }

  @Test
  fun datame() {
    val message = prisonerSearchApiService.findPrisonersByPrisonId("MDI")
    File("out.json").outputStream().use { s ->
      jacksonMapperBuilder().addModule(JavaTimeModule()).build().writerWithDefaultPrettyPrinter().writeValue(s, message)
    }
  }

  @Test
  fun testy() {
    println(
      prisonerSearchApiService.match(
        PrisonerMatchRequest(
          firstName = "Neil",
          lastName = "Youngish",
        ),
      ),
    )
  }
}
