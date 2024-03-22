package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase

@SpringBootTest
@Transactional
@Sql(scripts = ["classpath:testdata/sql/clear-all-data.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RepositoryTestBase : TestBase()
