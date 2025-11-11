// MongoDB initialization script for items collection
// Create indexes for items collection

db = db.getSiblingDB('cpwarehouse_db');

// Create unique index on sku field
db.items.createIndex(
    { "sku": 1 }, 
    { 
        unique: true,
        name: "idx_items_sku"
    }
);

// Create index on isActive field for filtering
db.items.createIndex(
    { "isActive": 1 }, 
    { 
        name: "idx_items_is_active"
    }
);

// Create text index for search functionality on name and description
db.items.createIndex(
    { 
        "name": "text", 
        "description": "text" 
    },
    {
        name: "idx_items_text_search",
        weights: {
            name: 10,
            description: 5
        }
    }
);

// Create compound index for common queries
db.items.createIndex(
    { "isActive": 1, "createdAt": -1 },
    {
        name: "idx_items_active_created"
    }
);

print("Items collection indexes created successfully");
