package pl.diabetix.diabetix.api.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.diabetix.diabetix.application.InfusionSetService
import pl.diabetix.diabetix.domain.InfusionSet
import pl.diabetix.diabetix.domain.InfusionSetCommand
import pl.diabetix.diabetix.domain.UserId
import java.net.URI
import java.time.LocalDate

@RestController
@RequestMapping("/infusion-sets")
class InfusionSetController(
    private val infusionSetService: InfusionSetService
) {

    @PostMapping
    fun createInfusionSet(@RequestBody request: InfusionSetRequest): ResponseEntity<InfusionSetResponse> {
        val infusionSet = infusionSetService.create(request.toCommand())
        return ResponseEntity.created(URI.create("/user/infusion-sets/${infusionSet.userId}")).build()
    }

    // TODO: Implement endpoint to get list of infusion set by userId with pagination

    private fun InfusionSet.toResponse() = InfusionSetResponse(
        id = this.id,
        bodyLocation = this.bodyLocation,
        userId = this.userId,
        insertionDate = this.insertionDate,
        removalDeadline = this.removalDeadline,
        removalDate = this.removalDate
    )

    private fun InfusionSetRequest.toCommand() = InfusionSetCommand(
        bodyLocation = this.bodyLocation,
        userId = this.userId,
        insertionDate = this.insertionDate
    )
}

data class InfusionSetRequest(
    val bodyLocation: String,
    val userId: String,
    val insertionDate: LocalDate
)

data class InfusionSetResponse(
    val id: String,
    val bodyLocation: String,
    val userId: UserId,
    val insertionDate: LocalDate,
    val removalDeadline: LocalDate,
    val removalDate: LocalDate?
)


