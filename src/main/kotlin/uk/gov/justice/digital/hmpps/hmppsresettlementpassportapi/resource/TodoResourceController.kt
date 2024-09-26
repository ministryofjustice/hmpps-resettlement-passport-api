package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.TodoService
import java.time.LocalDate
import java.util.UUID

@RestController
@Validated
@RequestMapping("/resettlement-passport/person", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('RESETTLEMENT_PASSPORT_EDIT')")
class TodoResourceController(private val todoService: TodoService) {
  @PostMapping("/{nomsId}/todo", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Create todo entry")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Entry added to person's todo list",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createTodo(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @RequestBody
    createRequest: TodoCreateRequest,
  ) = todoService.createEntry(nomsId, createRequest)

  @GetMapping("/{nomsId}/todo", produces = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(summary = "Get todo list for a person")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "List of items in person's todo lsit",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getTodoList(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
  ) = todoService.getList(nomsId)

  @DeleteMapping("/{nomsId}/todo/{id}")
  @Operation(summary = "Delete a todo list item for a person")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Item is deleted",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person or item not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteItem(
    @Schema(example = "AXXXS", required = true)
    @PathVariable("nomsId")
    nomsId: String,
    @PathVariable("id") id: UUID,
  ) = todoService.deleteItem(nomsId, id)
}

data class TodoCreateRequest(
  val urn: String,
  val task: String,
  val notes: String? = null,
  val dueDate: LocalDate? = null,
)
