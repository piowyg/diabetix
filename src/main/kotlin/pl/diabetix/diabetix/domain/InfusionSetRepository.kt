package pl.diabetix.diabetix.domain

interface InfusionSetRepository {
    fun create(infusionSet: InfusionSet): InfusionSet
    fun update(infusionSet: InfusionSet): InfusionSet
    fun findById(id: String): InfusionSet
    fun findInfusionSetsByUserId(userId: UserId): List<InfusionSet>
    fun findActiveInfusionSetByUserId(userId: UserId): InfusionSet?
}
