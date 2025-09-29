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

// Create infusion_sets collection for infusion sets management
const infusionSetsCollection = 'infusion_sets';
if (!db.getCollectionNames().includes(infusionSetsCollection)) {
    db.createCollection(infusionSetsCollection, {
        validator: {
            $jsonSchema: {
                bsonType: "object",
                properties: {
                    bodyLocation: { bsonType: "string" }, // enum stored as string
                    userId: { bsonType: "string" },
                    insertionDate: { bsonType: "date" },
                    removalDeadline: { bsonType: "date" },
                    removalDate: { bsonType: ["date", "null"] },
                    isActive: { bsonType: "bool" },
                    createdAt: { bsonType: "date" },
                    updatedAt: { bsonType: "date" }
                }
            }
        },
        validationLevel: "moderate"
    });

    print("✅ Collection 'infusion_sets' has been created.");

    // Helpful indexes for app queries
    db.infusion_sets.createIndex({ userId: 1 });
    db.infusion_sets.createIndex({ userId: 1, isActive: 1 }); // for findActiveByUserId
    db.infusion_sets.createIndex({ removalDate: 1 }); // for findByRemovalDate

    print("✅ Indexes for 'infusion_sets' have been created.");
} else {
    print("ℹ️ Collection 'infusion_sets' already exists.");
}
