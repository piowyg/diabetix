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

        val infusionSetCreateRequest = InfusionSetCreateRequest(
            bodyLocation = "arm",
            userId = userId,
            insertionDate = DATE,
        )

        // when
        val response = infusionSetAbility.callCreateInfusionSet(infusionSetCreateRequest)

        // then
        response.expectStatus().isCreated

        val infusionSet = infusionSetRepository.findActiveInfusionSetByUserId(userId)
        assertThat(infusionSet).isNotNull()
        assertThat(infusionSet!!.id).isNotNull()
        assertThat(infusionSet.userId).equals(userId)
        assertThat(infusionSet.bodyLocation).equals("arm")
        assertThat(infusionSet.insertionDate).equals(DATE)
        assertThat(infusionSet.removalDate).isNull()
        assertThat(infusionSet.removalDeadline).equals(DATE.plusDays(3))
        assertThat(infusionSet.isActive).equals(true)
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
        val request = InfusionSetCreateRequest(
            bodyLocation = "leg",
            userId = userId,
            insertionDate = DAY_AFTER_DATE,
        )
        val response = infusionSetAbility.callCreateInfusionSet(request)

        // then
        response.expectStatus().isCreated

        val activeInfusionSet = infusionSetRepository.findActiveInfusionSetByUserId(userId)
        assertThat(activeInfusionSet).isNotNull()
        assertThat(activeInfusionSet!!.bodyLocation).isEqualTo("leg")
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
            bodyLocation = "arm",
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
        assertThat(infusionSet.bodyLocation).isEqualTo("arm")
        assertThat(infusionSet.userId).isEqualTo(userId)
        assertThat(infusionSet.insertionDate).isEqualTo(DATE)
        assertThat(infusionSet.removalDeadline).isEqualTo(DATE.plusDays(3))
        assertThat(infusionSet.removalDate).isNull()
    }

    @Test
    fun `should update infusion set`() {
        // given
        val userId = "user-update"
        givenTime(DATE)
        givenUser(userId)
        givenInfusionSet(id = "1234", userId = userId)
        val created = infusionSetRepository.findActiveInfusionSetByUserId(userId)!!

        // when
        val removalDate = DATE.plusDays(1)
        val updateResponse = infusionSetAbility.callUpdateInfusionSet(
            created.id,
            InfusionSetUpdateRequest(
                bodyLocation = "leg",
                removalDate = removalDate
            )
        )

        // then
        val updatedBody = updateResponse.expectStatus().isOk
            .expectBody(InfusionSetResponse::class.java)
            .returnResult().responseBody!!
        assertThat(updatedBody.id).equals(created.id)
        assertThat(updatedBody.bodyLocation).equals("leg")
        assertThat(updatedBody.removalDate).equals(removalDate)

        // repo check
        val fromRepo = infusionSetRepository.findById(created.id)
        assertThat(fromRepo.bodyLocation).equals("leg")
        assertThat(fromRepo.removalDate).equals(removalDate)
        assertThat(fromRepo.isActive).equals(false)
    }

    @Test
    fun `should throw exception when removalDate is before insertionDate`() {
        // given
        val userId = "user-invalid-update"
        givenTime(DATE)
        givenUser(userId)
        infusionSetAbility.callCreateInfusionSet(
            InfusionSetCreateRequest(
                bodyLocation = "arm",
                userId = userId,
                insertionDate = DATE,
            )
        ).expectStatus().isCreated
        val created = infusionSetRepository.findActiveInfusionSetByUserId(userId)!!

        // when
        val response = infusionSetAbility.callUpdateInfusionSet(
            created.id,
            InfusionSetUpdateRequest(
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
            request = InfusionSetUpdateRequest(bodyLocation = "leg", removalDate = DATE.plusDays(1))
        )

        // then
        response.expectStatus().isNotFound
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
