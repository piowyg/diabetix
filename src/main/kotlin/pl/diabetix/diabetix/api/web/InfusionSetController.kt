package pl.diabetix.diabetix.api.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.diabetix.diabetix.application.InfusionSetService
import pl.diabetix.diabetix.domain.InfusionSet
import pl.diabetix.diabetix.domain.InfusionSetCommand
import pl.diabetix.diabetix.domain.InfusionSetUpdateCommand
import pl.diabetix.diabetix.domain.UserId
import java.net.URI
import java.time.LocalDate

@RestController
@RequestMapping("/infusion-sets")
class InfusionSetController(
    private val infusionSetService: InfusionSetService
) {

    @PostMapping
    fun createInfusionSet(@RequestBody request: InfusionSetCreateRequest): ResponseEntity<Void> {
        val infusionSet = infusionSetService.create(request.toCommand())
        return ResponseEntity.created(URI.create("/infusion-sets/${infusionSet.id}")).build()
    }

    @GetMapping("/{id}")
    fun getInfusionSet(@PathVariable id: String): ResponseEntity<InfusionSetResponse> =
        infusionSetService.getById(id).let { ResponseEntity.ok(it.toResponse()) }

    @GetMapping("/user/{userId}")
    fun getInfusionSetsByUser(@PathVariable userId: String): ResponseEntity<List<InfusionSetResponse>> =
        infusionSetService.getInfusionSetsByUserId(userId)
            .map { it.toResponse() }
            .let { ResponseEntity.ok(it) }

    @PutMapping("/{id}")
    fun updateInfusionSet(
        @PathVariable id: String,
        @RequestBody request: InfusionSetUpdateRequest
    ): ResponseEntity<InfusionSetResponse> =
        infusionSetService.update(id, request.toCommand())
            .let { ResponseEntity.ok(it.toResponse()) }

    private fun InfusionSet.toResponse() = InfusionSetResponse(
        id = this.id,
        bodyLocation = this.bodyLocation,
        userId = this.userId,
        insertionDate = this.insertionDate,
        removalDeadline = this.removalDeadline,
        removalDate = this.removalDate
    )

    private fun InfusionSetCreateRequest.toCommand() = InfusionSetCommand(
        bodyLocation = this.bodyLocation,
        userId = this.userId,
        insertionDate = this.insertionDate
    )

    private fun InfusionSetUpdateRequest.toCommand() = InfusionSetUpdateCommand(
        bodyLocation = this.bodyLocation,
        removalDate = this.removalDate,
        insertionDate = null
    )
}

data class InfusionSetCreateRequest(
    val bodyLocation: String,
    val userId: String,
    val insertionDate: LocalDate
)

data class InfusionSetUpdateRequest(
    val bodyLocation: String?,
    val removalDate: LocalDate?
)

data class InfusionSetResponse(
    val id: String,
    val bodyLocation: String,
    val userId: UserId,
    val insertionDate: LocalDate,
    val removalDeadline: LocalDate,
    val removalDate: LocalDate?
)
