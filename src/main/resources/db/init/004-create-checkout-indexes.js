// MongoDB initialization script for checkout_items collection
// Create indexes for checkout_items collection

db = db.getSiblingDB('cpwarehouse');

// Create index on customerId for customer order history
db.checkout_items.createIndex(
    { "customerId": 1 }, 
    { 
        name: "idx_checkout_customer_id"
    }
);

// Create index on status for filtering by checkout status
db.checkout_items.createIndex(
    { "status": 1 }, 
    { 
        name: "idx_checkout_status"
    }
);

// Create compound index for customer orders by status
db.checkout_items.createIndex(
    { "customerId": 1, "status": 1 },
    {
        name: "idx_checkout_customer_status"
    }
);

// Create index on checkoutReference for lookup by reference
db.checkout_items.createIndex(
    { "checkoutReference": 1 }, 
    { 
        name: "idx_checkout_reference"
    }
);

// Create index on itemId for item sales analytics
db.checkout_items.createIndex(
    { "itemId": 1 }, 
    { 
        name: "idx_checkout_item_id"
    }
);

// Create index on stockId for stock tracking
db.checkout_items.createIndex(
    { "stockId": 1 }, 
    { 
        name: "idx_checkout_stock_id"
    }
);

// Create index on reservationId for reservation lookup
db.checkout_items.createIndex(
    { "reservationId": 1 }, 
    { 
        name: "idx_checkout_reservation_id"
    }
);

// Create compound index for recent orders
db.checkout_items.createIndex(
    { "createdAt": -1, "status": 1 },
    {
        name: "idx_checkout_created_status"
    }
);

// Create index on version for optimistic locking queries
db.checkout_items.createIndex(
    { "version": 1 }, 
    { 
        name: "idx_checkout_version"
    }
);

print("Checkout items collection indexes created successfully");
