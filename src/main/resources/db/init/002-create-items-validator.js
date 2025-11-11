// MongoDB validation rules for items collection

db = db.getSiblingDB('cpwarehouse_db');

// Create collection with validator
db.createCollection("items", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["sku", "name", "basePrice", "isActive", "createdAt", "updatedAt"],
            properties: {
                _id: {
                    bsonType: "objectId",
                    description: "Primary key - must be an ObjectId"
                },
                sku: {
                    bsonType: "string",
                    maxLength: 100,
                    pattern: "^[A-Z0-9-]+$",
                    description: "SKU must be a string, max 100 characters, uppercase alphanumeric with hyphens"
                },
                name: {
                    bsonType: "string",
                    minLength: 3,
                    maxLength: 255,
                    description: "Name must be a string between 3 and 255 characters"
                },
                description: {
                    bsonType: ["string", "null"],
                    maxLength: 2000,
                    description: "Description must be a string, max 2000 characters or null"
                },
                basePrice: {
                    bsonType: ["double", "decimal"],
                    minimum: 0,
                    description: "Base price must be a number >= 0"
                },
                isActive: {
                    bsonType: "bool",
                    description: "isActive must be a boolean"
                },
                createdAt: {
                    bsonType: "long",
                    description: "createdAt must be a long (epoch milliseconds)"
                },
                updatedAt: {
                    bsonType: "long",
                    description: "updatedAt must be a long (epoch milliseconds)"
                }
            }
        }
    },
    validationLevel: "moderate",
    validationAction: "error"
});

print("Items collection validator created successfully");
