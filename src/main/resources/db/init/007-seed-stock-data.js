// MongoDB seed data for stock collection (development only)

db = db.getSiblingDB('cpwarehouse');

// Get variants for reference
const variants = db.variants.find().toArray();

if (variants && variants.length > 0) {
    const stockRecords = [];
    
    // Create stock for each variant with realistic quantities
    variants.forEach((variant, index) => {
        // Vary quantities based on variant index for realistic data
        let quantity;
        let warehouseLocation;
        
        // Distribute stock across different warehouse locations
        if (index % 3 === 0) {
            quantity = 150;
            warehouseLocation = "WH-A-01-01";
        } else if (index % 3 === 1) {
            quantity = 100;
            warehouseLocation = "WH-A-01-02";
        } else {
            quantity = 75;
            warehouseLocation = "WH-B-01-01";
        }
        
        stockRecords.push({
            itemId: variant.itemId,
            variantId: variant._id.toString(),
            quantity: quantity,
            reservedQuantity: 0,
            warehouseLocation: warehouseLocation,
            version: NumberLong(0),
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        });
    });
    
    // Insert all stock records
    if (stockRecords.length > 0) {
        db.stock.insertMany(stockRecords);
        print("Sample stock inserted successfully: " + stockRecords.length + " stock records created");
    }
} else {
    print("ERROR: No variants found. Please ensure variants are seeded first.");
}
