package pl.diabetix.diabetix.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "application.keycloak")
class KeycloakTestProperties {
    lateinit var version: String
    var admin: Admin = Admin()
    var realm: Realm = Realm()

    class Admin {
        lateinit var username: String
        lateinit var password: String
    }


    class Realm {
        lateinit var name: String
        var client: Client = Client()

        class Client {
            lateinit var id: String
            lateinit var secret: String
        }
    }
}
