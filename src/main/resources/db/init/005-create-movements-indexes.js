// MongoDB initialization script for stock_movements collection
// Create indexes for stock_movements collection

db = db.getSiblingDB('cpwarehouse');

// Create index on stockId for efficient movement lookup by stock
db.stock_movements.createIndex(
    { "stockId": 1 }, 
    { 
        name: "idx_movements_stock_id"
    }
);

// Create index on movementType for filtering by type
db.stock_movements.createIndex(
    { "movementType": 1 }, 
    { 
        name: "idx_movements_type"
    }
);

// Create compound index for stock movement history
db.stock_movements.createIndex(
    { "stockId": 1, "createdAt": -1 },
    {
        name: "idx_movements_stock_created"
    }
);

// Create index on referenceNumber for lookup by order/checkout reference
db.stock_movements.createIndex(
    { "referenceNumber": 1 }, 
    { 
        name: "idx_movements_reference"
    }
);

// Create index on createdBy for audit trails
db.stock_movements.createIndex(
    { "createdBy": 1 }, 
    { 
        name: "idx_movements_created_by"
    }
);

// Create index on relatedMovementId for tracking related movements
db.stock_movements.createIndex(
    { "relatedMovementId": 1 }, 
    { 
        name: "idx_movements_related_id"
    }
);

// Create index on releaseMovementId for tracking releases
db.stock_movements.createIndex(
    { "releaseMovementId": 1 }, 
    { 
        name: "idx_movements_release_id"
    }
);

// Create compound index for movement analytics by type and date
db.stock_movements.createIndex(
    { "movementType": 1, "createdAt": -1 },
    {
        name: "idx_movements_type_created"
    }
);

// Create index on version for optimistic locking queries
db.stock_movements.createIndex(
    { "version": 1 }, 
    { 
        name: "idx_movements_version"
    }
);

print("Stock movements collection indexes created successfully");
