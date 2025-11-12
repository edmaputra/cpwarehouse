// MongoDB initialization script for stock collection
// Create indexes for stock collection

db = db.getSiblingDB('cpwarehouse');

// Create unique compound index on itemId and variantId (one stock record per item-variant combination)
db.stock.createIndex(
    { "itemId": 1, "variantId": 1 }, 
    { 
        unique: true,
        name: "idx_stock_item_variant",
        partialFilterExpression: { "variantId": { $type: "string" } }
    }
);

// Create unique index on itemId where variantId is null (for items without variants)
db.stock.createIndex(
    { "itemId": 1 }, 
    { 
        unique: true,
        name: "idx_stock_item_only",
        partialFilterExpression: { "variantId": { $type: "null" } }
    }
);

// Create index on variantId for efficient stock lookups by variant
db.stock.createIndex(
    { "variantId": 1 }, 
    { 
        name: "idx_stock_variant_id"
    }
);

// Create index on warehouseLocation for warehouse management queries
db.stock.createIndex(
    { "warehouseLocation": 1 }, 
    { 
        name: "idx_stock_warehouse_location"
    }
);

// Create compound index for low stock alerts
db.stock.createIndex(
    { "quantity": 1, "warehouseLocation": 1 },
    {
        name: "idx_stock_quantity_warehouse"
    }
);

// Create index on version for optimistic locking queries
db.stock.createIndex(
    { "version": 1 }, 
    { 
        name: "idx_stock_version"
    }
);

print("Stock collection indexes created successfully");
