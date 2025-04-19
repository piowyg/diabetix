db = db.getSiblingDB('your_database_name');

const collection = 'users';

// Sprawdź, czy kolekcja już istnieje
if (!db.getCollectionNames().includes(collection)) {
    db.createCollection(collection, {
        validator: {
            $jsonSchema: {
                bsonType: "object",
                required: ["email", "login", "name", "surname", "password", "birthdate"],
                properties: {
                    id: {
                        bsonType: "string",
                        description: "must be a string and is auto-generated"
                    },
                    email: {
                        bsonType: "string",
                        description: "must be a string and is required"
                    },
                    login: {
                        bsonType: "string",
                        description: "must be a string and is required"
                    },
                    name: {
                        bsonType: "string"
                    },
                    surname: {
                        bsonType: "string"
                    },
                    password: {
                        bsonType: "string"
                    },
                    birthdate: {
                        bsonType: "date",
                        description: "must be a date"
                    },
                    createdAt: {
                        bsonType: "date",
                        description: "date when the document was created"
                    }
                }
            }
        },
        validationLevel: "moderate"
    });

    print("✅ Kolekcja 'users' została utworzona z walidacją schematu i polem createdAt.");

    db.users.createIndex({ email: 1 }, { unique: true });
    db.users.createIndex({ login: 1 }, { unique: true });

    print("✅ Indeksy na 'email' i 'login' zostały utworzone.");
} else {
    print("ℹ️ Kolekcja 'users' już istnieje.");
}
