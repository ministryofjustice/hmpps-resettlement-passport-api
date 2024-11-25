package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.manageusersapi

data class ManageUserList(
  val content: List<ManageUser>,
)
data class ManageUser(
  val staffId: Long,
  val firstName: String? = null,
  val lastName: String? = null,
)

data class CaseLoad(
  val id: String,
  val name: String,
)

data class PagedResponse(
  val content: List<ManageUser>,
  val pageable: PageDetails,
  val last: Boolean,
  val totalPages: Int,
  val totalElements: Long,
  val size: Int,
  val number: Int,
  val sort: PageSort,
  val numberOfElements: Int,
  val first: Boolean,
  val empty: Boolean,
)

data class PageDetails(
  val sort: PageSort,
  val offset: Int,
  val pageNumber: Int,
  val pageSize: Int,
  val paged: Boolean,
  val unpaged: Boolean,
)

data class PageSort(
  val sorted: Boolean,
  val unsorted: Boolean,
  val empty: Boolean,
)
