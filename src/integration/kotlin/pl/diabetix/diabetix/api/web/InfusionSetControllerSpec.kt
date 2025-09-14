package pl.diabetix.diabetix.api.web

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import pl.diabetix.diabetix.BaseIntegrationTest
import pl.diabetix.diabetix.InfusionSetBuilder
import pl.diabetix.diabetix.UserBuilder
import pl.diabetix.diabetix.api.web.ability.InfusionSetEndpointAbility
import pl.diabetix.diabetix.application.UserService
import pl.diabetix.diabetix.application.InfusionSetService
import pl.diabetix.diabetix.domain.BodyLocation
import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserId
import pl.diabetix.diabetix.domain.InfusionSetRepository
import java.time.LocalDate

class InfusionSetControllerSpec: BaseIntegrationTest() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var infusionSetRepository: InfusionSetRepository

    @Autowired
    private lateinit var infusionSetService: InfusionSetService

    @Autowired
    private lateinit var infusionSetAbility: InfusionSetEndpointAbility

    @Test
    fun `should create infusion set`() {
        // given
        val userId = "1234"
        givenTime(DATE)
        givenUser(userId)

        // when
        val response = infusionSetAbility.callCreateInfusionSet(
            request = InfusionSetCreateRequest(
                bodyLocation = BodyLocation.ARM,
                userId = userId,
                insertionDate = DATE,
            )
        )

        // then
        response.expectStatus().isCreated

        val infusionSet = infusionSetRepository.findActiveInfusionSetByUserId(userId)
        assertThat(infusionSet).isNotNull()
        assertThat(infusionSet!!.id).isNotNull()
        assertThat(infusionSet.userId).equals(userId)
        assertThat(infusionSet.bodyLocation).equals(BodyLocation.ARM)
        assertThat(infusionSet.insertionDate).equals(DATE)
        assertThat(infusionSet.removalDate).isNull()
        assertThat(infusionSet.removalDeadline).equals(DATE.plusDays(3))
        assertThat(infusionSet.isActive).equals(true)

        val locationHeader = response.expectStatus().isCreated
            .returnResult(Void::class.java)
            .responseHeaders
            .location
        assertThat(locationHeader).isNotNull()
        val locationString = locationHeader!!.toString()
        assertThat(locationString.contains("/infusion-sets/")).isEqualTo(true)
    }

    @Test
    fun `should mark actual infusion set as removed when create new one on the same user`() {
        // given
        givenTime(DATE)
        val userId = "1234"
        val existingInfusionSetId = "1234"
        givenUser(userId)
        givenInfusionSet(id = existingInfusionSetId, userId = userId)

        // when
        givenTime(DAY_AFTER_DATE)
        val response = infusionSetAbility.callCreateInfusionSet(
            request = InfusionSetCreateRequest(
                bodyLocation = BodyLocation.LEG,
                userId = userId,
                insertionDate = DAY_AFTER_DATE,
            )
        )

        // then
        response.expectStatus().isCreated

        val activeInfusionSet = infusionSetRepository.findActiveInfusionSetByUserId(userId)
        assertThat(activeInfusionSet).isNotNull()
        assertThat(activeInfusionSet!!.bodyLocation).isEqualTo(BodyLocation.LEG)
        assertThat(activeInfusionSet.insertionDate).isEqualTo(DAY_AFTER_DATE)
        assertThat(activeInfusionSet.removalDate).isNull()
        assertThat(activeInfusionSet.isActive).isEqualTo(true)

        val allInfusionSets = infusionSetService.getInfusionSetsByUserId(userId)
        assertThat(allInfusionSets).hasSize(2)
        val removedInfusionSet = allInfusionSets.first { it.id == existingInfusionSetId }
        assertThat(removedInfusionSet.isActive).isEqualTo(false)
        assertThat(removedInfusionSet.removalDate).isEqualTo(DAY_AFTER_DATE)
    }

    @Test
    fun `should throw exception when user is not found`() = runTest {
        // given
        givenTime(DATE)
        val infusionSetCreateRequest = InfusionSetCreateRequest(
            bodyLocation = BodyLocation.ARM,
            userId = "1234",
            insertionDate = DATE,
        )

        // when
        val response = infusionSetAbility.callCreateInfusionSet(infusionSetCreateRequest)

        // then
        response.expectStatus().isNotFound
    }

    @Test
    fun `should get infusion sets base on userId`() {
        // given
        val userId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = "1234", userId = userId)

        // when
        val response = infusionSetAbility.callGetInfusionSetsByUser(userId)

        // then
        val body = response.expectStatus().isOk
            .expectBodyList(InfusionSetResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(body).hasSize(1)
        val infusionSet = body.first()
        assertThat(infusionSet.id).isEqualTo("1234")
        assertThat(infusionSet.bodyLocation).isEqualTo(BodyLocation.ARM)
        assertThat(infusionSet.userId).isEqualTo(userId)
        assertThat(infusionSet.insertionDate).isEqualTo(DATE)
        assertThat(infusionSet.removalDeadline).isEqualTo(DATE.plusDays(3))
        assertThat(infusionSet.removalDate).isNull()
    }

    @Test
    fun `should update infusion set`() {
        // given
        val userId = "user-update"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when
        val removalDate = DATE.plusDays(1)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            infusionSetId,
            InfusionSetUpdateRequest(
                bodyLocation = BodyLocation.LEG,
                removalDate = removalDate
            )
        )

        // then
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.id).equals(infusionSetId)
        assertThat(updatedBody.bodyLocation).equals(BodyLocation.LEG)
        assertThat(updatedBody.removalDate).equals(removalDate)

        // repo check
        val fromRepo = infusionSetRepository.findById(infusionSetId)
        assertThat(fromRepo.bodyLocation).equals(BodyLocation.LEG)
        assertThat(fromRepo.removalDate).equals(removalDate)
        assertThat(fromRepo.isActive).equals(false)
    }

    @Test
    fun `should throw exception when removalDate is before insertionDate`() {
        // given
        val userId = "user-invalid-update"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when
        val response = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = DATE.minusDays(1) // before insertion
            )
        )

        // then
        response.expectStatus().isBadRequest
    }

    @Test
    fun `should throw exception when try to update not existing infusion set`() {
        // given
        val userId = "user-not-found-update"
        givenTime(DATE)
        givenUser(userId)

        // when
        val response = infusionSetAbility.callUpdateInfusionSet(
            id = "does-not-exist",
            request = InfusionSetUpdateRequest(bodyLocation = BodyLocation.LEG, removalDate = DATE.plusDays(1))
        )

        // then
        response.expectStatus().isNotFound
    }

    @Test
    fun `should get single infusion set by id`() {
        // given
        val userId = "user-get-single"
        val infusionSetId = "single-test-id"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when
        val response = infusionSetAbility.callGetInfusionSet(infusionSetId)

        // then
        val body = response.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!

        assertThat(body.id).isEqualTo(infusionSetId)
        assertThat(body.bodyLocation).isEqualTo(BodyLocation.ARM)
        assertThat(body.userId).isEqualTo(userId)
        assertThat(body.insertionDate).isEqualTo(DATE)
        assertThat(body.removalDeadline).isEqualTo(DATE.plusDays(3))
        assertThat(body.removalDate).isNull()
    }

    @Test
    fun `should throw exception when get infusion set with not existing id`() {
        // given
        val nonExistingId = "non-existing-id"

        // when
        val response = infusionSetAbility.callGetInfusionSet(nonExistingId)

        // then
        response.expectStatus().isNotFound
    }

    @Test
    fun `should return empty list when get infusion sets for user with no infusion sets`() {
        // given
        val userId = "user-no-infusion-sets"
        givenTime(DATE)
        givenUser(userId)

        // when
        val response = infusionSetAbility.callGetInfusionSetsByUser(userId)

        // then
        val body = response.expectStatus().isOk
            .expectBodyList(InfusionSetResponse::class.java)
            .returnResult()
            .responseBody!!

        assertThat(body).hasSize(0)
    }

    @Test
    fun `should update only body location when removal date is not provided`() {
        // given
        val userId = "user-update-location-only"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = BodyLocation.STOMACH,
                removalDate = null
            )
        )

        // then
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.id).isEqualTo(infusionSetId)
        assertThat(updatedBody.bodyLocation).isEqualTo(BodyLocation.STOMACH)
        assertThat(updatedBody.removalDate).isEqualTo(null)

        // repo check
        val fromRepo = infusionSetRepository.findById(infusionSetId)
        assertThat(fromRepo.bodyLocation).isEqualTo(BodyLocation.STOMACH)
        assertThat(fromRepo.removalDate).isEqualTo(null)
        assertThat(fromRepo.isActive).isEqualTo(true) // should still be active
    }

    @Test
    fun `should update only removal date when body location is not provided`() {
        // given
        val userId = "user-update-removal-only"
        val infusionSetId = "update-removal-id"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when
        val removalDate = DATE.plusDays(2)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = removalDate
            )
        )

        // then
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.id).isEqualTo(infusionSetId)
        assertThat(updatedBody.bodyLocation).isEqualTo(BodyLocation.ARM) // should keep original location
        assertThat(updatedBody.removalDate).isEqualTo(removalDate)

        // repo check
        val fromRepo = infusionSetRepository.findById(infusionSetId)
        assertThat(fromRepo.bodyLocation).isEqualTo(BodyLocation.ARM)
        assertThat(fromRepo.removalDate).isEqualTo(removalDate)
        assertThat(fromRepo.isActive).isEqualTo(false) // should be inactive when removal date is set
    }

    @Test
    fun `should update every infusion set field in single request`() {
        // given
        val userId = "user-update-both"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when
        val removalDate = DATE.plusDays(2)
        givenTime(removalDate)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = BodyLocation.THIGH,
                removalDate = removalDate,
                insertionDate = DATE.plusDays(1)
            )
        )

        // then
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.id).isEqualTo(infusionSetId)
        assertThat(updatedBody.bodyLocation).isEqualTo(BodyLocation.THIGH)
        assertThat(updatedBody.removalDate).isEqualTo(removalDate)

        // repo check
        val fromRepo = infusionSetRepository.findById(infusionSetId)
        assertThat(fromRepo.bodyLocation).isEqualTo(BodyLocation.THIGH)
        assertThat(fromRepo.removalDate).isEqualTo(removalDate)
        assertThat(fromRepo.isActive).isEqualTo(false) // should be inactive due to removal date
    }

    @Test
    fun `should handle edge case when removal date equals insertion date`() {
        // given
        val userId = "user-edge-case"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when - removal date same as insertion date (edge case)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = DATE // same day
            )
        )

        // then - should be allowed
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.removalDate).isEqualTo(DATE)
    }

    @Test
    fun `should allow null value for body location`() {
        // given
        val userId = "user-null-validation"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when - update with null body location (should be allowed)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null, // null should be allowed
                removalDate = DATE.plusDays(1)
            )
        )

        // then - should succeed
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.bodyLocation).isEqualTo(BodyLocation.ARM) // should keep original value
    }

    @Test
    fun `should create infusion set with future insertion date`() {
        // given
        val userId = "user-future-date"
        givenTime(DATE)
        givenUser(userId)

        val futureDate = DATE.plusDays(10)
        val infusionSetCreateRequest = InfusionSetCreateRequest(
            bodyLocation = BodyLocation.STOMACH,
            userId = userId,
            insertionDate = futureDate,
        )

        // when
        val response = infusionSetAbility.callCreateInfusionSet(infusionSetCreateRequest)

        // then
        response.expectStatus().isCreated

        val infusionSet = infusionSetRepository.findActiveInfusionSetByUserId(userId)
        assertThat(infusionSet).isNotNull()
        assertThat(infusionSet!!.insertionDate).isEqualTo(futureDate)
        assertThat(infusionSet.removalDeadline).isEqualTo(futureDate.plusDays(3))
    }

    @Test
    fun `should update insertion date and recalculate removal deadline`() {
        // given
        val userId = "user-update-insertion-date"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when - update insertion date to one day later
        val newInsertionDate = DATE.plusDays(1)
        givenTime(newInsertionDate)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = null,
                insertionDate = newInsertionDate
            )
        )

        // then
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.insertionDate).isEqualTo(newInsertionDate)
        assertThat(updatedBody.removalDeadline).isEqualTo(newInsertionDate.plusDays(3)) // recalculated

        // repo check
        val fromRepo = infusionSetRepository.findById(infusionSetId)
        assertThat(fromRepo.insertionDate).isEqualTo(newInsertionDate)
        assertThat(fromRepo.removalDeadline).isEqualTo(newInsertionDate.plusDays(3))
    }

    @Test
    fun `should reject future insertion date`() {
        // given
        val userId = "user-future-insertion"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when - try to update with future insertion date
        val futureDate = DATE.plusDays(10)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = null,
                insertionDate = futureDate
            )
        )

        // then - should fail validation
        updateResponse.expectStatus().isBadRequest
    }

    @Test
    fun `should reject insertion date after removal date`() {
        // given
        val userId = "user-insertion-after-removal"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when - try to update insertion date to be after removal date
        val removalDate = DATE.plusDays(2)
        val insertionDate = DATE.plusDays(3) // after removal
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = removalDate,
                insertionDate = insertionDate
            )
        )

        // then - should fail validation
        updateResponse.expectStatus().isBadRequest
    }

    @Test
    fun `should update all fields including insertion date in single request`() {
        // given
        val userId = "user-update-all-fields"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // when - update all fields at once
        val newInsertionDate = DATE.minusDays(1) // past date
        val newRemovalDate = DATE.plusDays(1)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = BodyLocation.BACK,
                removalDate = newRemovalDate,
                insertionDate = newInsertionDate
            )
        )

        // then
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.bodyLocation).isEqualTo(BodyLocation.BACK)
        assertThat(updatedBody.insertionDate).isEqualTo(newInsertionDate)
        assertThat(updatedBody.removalDate).isEqualTo(newRemovalDate)
        assertThat(updatedBody.removalDeadline).isEqualTo(newInsertionDate.plusDays(3))

        // repo check
        val fromRepo = infusionSetRepository.findById(infusionSetId)
        assertThat(fromRepo.bodyLocation).isEqualTo(BodyLocation.BACK)
        assertThat(fromRepo.insertionDate).isEqualTo(newInsertionDate)
        assertThat(fromRepo.removalDate).isEqualTo(newRemovalDate)
        assertThat(fromRepo.removalDeadline).isEqualTo(newInsertionDate.plusDays(3))
        assertThat(fromRepo.isActive).isEqualTo(false) // should be inactive due to removal date
    }

    @Test
    fun `should handle edge case when updating insertion date equals existing removal date`() {
        // given
        val userId = "user-insertion-equals-removal"
        val infusionSetId = "1234"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = infusionSetId, userId = userId)

        // First set a removal date
        infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = DATE.plusDays(2),
                insertionDate = null
            )
        ).expectStatus().isOk

        // when - update insertion date to equal removal date (edge case)
        val equalDate = DATE.plusDays(2)
        givenTime(equalDate)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            id = infusionSetId,
            request = InfusionSetUpdateRequest(
                bodyLocation = null,
                removalDate = null,
                insertionDate = equalDate
            )
        )

        // then - should be allowed (same day is valid)
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.insertionDate).isEqualTo(equalDate)
        assertThat(updatedBody.removalDate).isEqualTo(equalDate)
    }

    private fun givenUser(userId: UserId): User {
        val user = UserBuilder().id(userId).login("testuser").build()
        return userService.create(user)
    }

    private fun givenInfusionSet(id: String, userId: UserId) {
        val infusionSet = InfusionSetBuilder()
            .id(id)
            .userId(userId)
            .build()
        infusionSetRepository.create(infusionSet)
    }

    companion object {
        private val DATE = LocalDate.of(2025, 6, 8)
        private val DAY_AFTER_DATE = DATE.plusDays(1)
    }
}
