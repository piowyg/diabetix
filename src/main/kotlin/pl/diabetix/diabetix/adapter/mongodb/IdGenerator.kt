package pl.diabetix.diabetix.adapter.mongodb

import java.util.*

object IdGenerator {

    // TODO:
    //  rozwinac to bardziej
    @JvmStatic
    fun generateId(): String = UUID.randomUUID().toString()
}