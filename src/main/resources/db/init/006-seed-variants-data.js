// MongoDB seed data for variants collection (development only)

db = db.getSiblingDB('cpwarehouse');

// Get item IDs for reference
const itemTshirt = db.items.findOne({ sku: "ITEM-001" });
const itemHoodie = db.items.findOne({ sku: "ITEM-002" });
const itemJeans = db.items.findOne({ sku: "ITEM-003" });

if (itemTshirt && itemHoodie && itemJeans) {
    // Insert sample variants for T-Shirt (sizes and colors)
    db.variants.insertMany([
        // T-Shirt variants (Size + Color combinations)
        {
            itemId: itemTshirt._id.toString(),
            variantSku: "ITEM-001-S-BLACK",
            variantName: "Small / Black",
            attributes: {
                size: "S",
                color: "Black"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemTshirt._id.toString(),
            variantSku: "ITEM-001-M-BLACK",
            variantName: "Medium / Black",
            attributes: {
                size: "M",
                color: "Black"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemTshirt._id.toString(),
            variantSku: "ITEM-001-L-BLACK",
            variantName: "Large / Black",
            attributes: {
                size: "L",
                color: "Black"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemTshirt._id.toString(),
            variantSku: "ITEM-001-M-WHITE",
            variantName: "Medium / White",
            attributes: {
                size: "M",
                color: "White"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemTshirt._id.toString(),
            variantSku: "ITEM-001-L-WHITE",
            variantName: "Large / White",
            attributes: {
                size: "L",
                color: "White"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        
        // Hoodie variants (Size + Color with price adjustments for larger sizes)
        {
            itemId: itemHoodie._id.toString(),
            variantSku: "ITEM-002-M-GREY",
            variantName: "Medium / Grey",
            attributes: {
                size: "M",
                color: "Grey"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemHoodie._id.toString(),
            variantSku: "ITEM-002-L-GREY",
            variantName: "Large / Grey",
            attributes: {
                size: "L",
                color: "Grey"
            },
            priceAdjustment: 5.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemHoodie._id.toString(),
            variantSku: "ITEM-002-XL-GREY",
            variantName: "XL / Grey",
            attributes: {
                size: "XL",
                color: "Grey"
            },
            priceAdjustment: 10.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemHoodie._id.toString(),
            variantSku: "ITEM-002-L-NAVY",
            variantName: "Large / Navy",
            attributes: {
                size: "L",
                color: "Navy"
            },
            priceAdjustment: 5.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        
        // Jeans variants (Size + Fit)
        {
            itemId: itemJeans._id.toString(),
            variantSku: "ITEM-003-30-SLIM",
            variantName: "30W / Slim Fit",
            attributes: {
                waist: "30",
                fit: "Slim"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemJeans._id.toString(),
            variantSku: "ITEM-003-32-SLIM",
            variantName: "32W / Slim Fit",
            attributes: {
                waist: "32",
                fit: "Slim"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemJeans._id.toString(),
            variantSku: "ITEM-003-34-SLIM",
            variantName: "34W / Slim Fit",
            attributes: {
                waist: "34",
                fit: "Slim"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemJeans._id.toString(),
            variantSku: "ITEM-003-32-REGULAR",
            variantName: "32W / Regular Fit",
            attributes: {
                waist: "32",
                fit: "Regular"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        },
        {
            itemId: itemJeans._id.toString(),
            variantSku: "ITEM-003-34-REGULAR",
            variantName: "34W / Regular Fit",
            attributes: {
                waist: "34",
                fit: "Regular"
            },
            priceAdjustment: 0.0,
            isActive: true,
            createdAt: NumberLong(Date.now()),
            updatedAt: NumberLong(Date.now())
        }
    ]);

    print("Sample variants inserted successfully: 14 variants created");
} else {
    print("ERROR: Items not found. Please ensure items are seeded first.");
}
