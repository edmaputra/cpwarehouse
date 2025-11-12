// MongoDB initialization script for variants collection
// Create indexes for variants collection

db = db.getSiblingDB('cpwarehouse_db');

// Create unique index on variantSku field
db.variants.createIndex(
    { "variantSku": 1 }, 
    { 
        unique: true,
        name: "idx_variants_sku"
    }
);

// Create index on itemId for efficient variant lookups by item
db.variants.createIndex(
    { "itemId": 1 }, 
    { 
        name: "idx_variants_item_id"
    }
);

// Create index on isActive field for filtering
db.variants.createIndex(
    { "isActive": 1 }, 
    { 
        name: "idx_variants_is_active"
    }
);

// Create compound index for common queries (active variants by item)
db.variants.createIndex(
    { "itemId": 1, "isActive": 1 },
    {
        name: "idx_variants_item_active"
    }
);

// Create text index for search functionality on variantName
db.variants.createIndex(
    { "variantName": "text" },
    {
        name: "idx_variants_text_search"
    }
);

print("Variants collection indexes created successfully");
