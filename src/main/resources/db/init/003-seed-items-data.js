// MongoDB seed data for items collection (development only)

db = db.getSiblingDB('cpwarehouse_db');

// Insert sample items for development/testing
db.items.insertMany([
    {
        sku: "ITEM-001",
        name: "Basic T-Shirt",
        description: "100% Cotton Basic T-Shirt",
        basePrice: 19.99,
        isActive: true,
        createdAt: NumberLong(Date.now()),
        updatedAt: NumberLong(Date.now())
    },
    {
        sku: "ITEM-002",
        name: "Premium Hoodie",
        description: "Premium quality hoodie with fleece lining",
        basePrice: 49.99,
        isActive: true,
        createdAt: NumberLong(Date.now()),
        updatedAt: NumberLong(Date.now())
    },
    {
        sku: "ITEM-003",
        name: "Casual Jeans",
        description: "Comfortable denim jeans",
        basePrice: 39.99,
        isActive: true,
        createdAt: NumberLong(Date.now()),
        updatedAt: NumberLong(Date.now())
    },
    {
        sku: "ITEM-004",
        name: "Discontinued Item",
        description: "This item is no longer available",
        basePrice: 9.99,
        isActive: false,
        createdAt: NumberLong(Date.now()),
        updatedAt: NumberLong(Date.now())
    }
]);

print("Sample items inserted successfully");
