db = db.getSiblingDB('diabetix');

const collection = 'users';

// Check if collection exists
if (!db.getCollectionNames().includes(collection)) {
    db.createCollection(collection, {
        validator: {
            $jsonSchema: {
                bsonType: "object",
                properties: {
                    email: {
                        bsonType: "string",
                    },
                    login: {
                        bsonType: "string",
                    },
                    name: {
                        bsonType: "string"
                    },
                    surname: {
                        bsonType: "string"
                    },
                    activated: {
                        bsonType: "bool"
                    },
                    birthdate: {
                        bsonType: "date",
                    },
                    createdAt: {
                        bsonType: "date",
                    },
                    updatedAt: {
                        bsonType: "date",
                    }
                }
            }
        },
        validationLevel: "moderate"
    });

    print("✅ Collection 'users' has been created.");

    db.users.createIndex({ login: 1 }, { unique: true });
    db.users.createIndex({ email: 1 }, { unique: true });

    print("✅ Indexes has been created.");
} else {
    print("ℹ️ Collection 'users' already exists.");
}
