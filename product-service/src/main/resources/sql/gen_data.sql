-- SAMPLE DATA GENERATOR FOR E-COMMERCE DATABASE
-- Tạo dữ liệu mẫu ngẫu nhiên cho tất cả các bảng

-- ================================
-- UTILITY FUNCTIONS
-- ================================

DELIMITER $$

-- Function to generate random string
CREATE FUNCTION RANDOM_STRING(length INT)
    RETURNS VARCHAR(255)
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE chars VARCHAR(62) DEFAULT 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    DECLARE result VARCHAR(255) DEFAULT '';
    DECLARE i INT DEFAULT 0;

    WHILE i < length DO
        SET result = CONCAT(result, SUBSTRING(chars, FLOOR(1 + RAND() * 62), 1));
        SET i = i + 1;
END WHILE;

RETURN result;
END$$

-- Function to generate random email
CREATE FUNCTION RANDOM_EMAIL()
    RETURNS VARCHAR(255)
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE domains TEXT DEFAULT 'gmail.com,yahoo.com,hotmail.com,outlook.com,company.com';
    DECLARE domain VARCHAR(50);
    DECLARE domain_count INT;

    SET domain_count = (LENGTH(domains) - LENGTH(REPLACE(domains, ',', '')) + 1);
    SET domain = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(domains, ',', FLOOR(1 + RAND() * domain_count)), ',', -1));

RETURN CONCAT(RANDOM_STRING(8), '@', domain);
END$$

-- Function to generate random phone
CREATE FUNCTION RANDOM_PHONE()
    RETURNS VARCHAR(20)
    READS SQL DATA
    DETERMINISTIC
BEGIN
RETURN CONCAT('+84', LPAD(FLOOR(RAND() * 1000000000), 9, '0'));
END$$

DELIMITER ;

-- ================================
-- MAIN DATA GENERATION PROCEDURES
-- ================================

DELIMITER $$

-- 1. Generate Categories
CREATE PROCEDURE GenerateCategories(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE category_names TEXT DEFAULT 'Electronics,Fashion,Home & Garden,Sports,Books,Toys,Beauty,Automotive,Food,Health,Jewelry,Pets,Art,Music,Gaming,Travel,Office,Baby,Outdoor,Tools';
    DECLARE name_count INT;
    DECLARE category_name VARCHAR(100);

    SET name_count = (LENGTH(category_names) - LENGTH(REPLACE(category_names, ',', '')) + 1);

    WHILE i < record_count DO
        SET category_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(category_names, ',', FLOOR(1 + RAND() * name_count + i) % name_count + 1), ',', -1));

INSERT INTO categories (name, description, parent_id, slug, image_url, is_active, sort_order, created_at, updated_at)
VALUES (
           CONCAT(category_name, ' ', RANDOM_STRING(3)),
           CONCAT('Description for ', category_name, ' category'),
           CASE WHEN RAND() > 0.7 AND i > 5 THEN FLOOR(1 + RAND() * (i-1)) ELSE NULL END,
           LOWER(REPLACE(CONCAT(category_name, '-', RANDOM_STRING(3)), ' ', '-')),
           CONCAT('https://images.example.com/category_', i+1, '.jpg'),
           RAND() > 0.1,
           i + 1,
           DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
           NOW()
       );
SET i = i + 1;
END WHILE;
END$$

-- 2. Generate Brands
CREATE PROCEDURE GenerateBrands(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE brand_names TEXT DEFAULT 'Apple,Samsung,Nike,Adidas,Sony,LG,Dell,HP,Canon,Toyota,Honda,Mercedes,BMW,Coca-Cola,Pepsi,McDonald,KFC,Zara,H&M,Uniqlo';
    DECLARE name_count INT;
    DECLARE brand_name VARCHAR(100);

    SET name_count = (LENGTH(brand_names) - LENGTH(REPLACE(brand_names, ',', '')) + 1);

    WHILE i < record_count DO
        SET brand_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(brand_names, ',', FLOOR(1 + RAND() * name_count + i) % name_count + 1), ',', -1));

INSERT INTO brands (name, description, logo_url, website, is_active, created_at, updated_at)
VALUES (
           CONCAT(brand_name, CASE WHEN i > 19 THEN CONCAT(' ', RANDOM_STRING(2)) ELSE '' END),
           CONCAT('Official ', brand_name, ' brand store'),
           CONCAT('https://logos.example.com/', LOWER(brand_name), '.png'),
           CONCAT('https://www.', LOWER(brand_name), '.com'),
           RAND() > 0.05,
           DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 1000) DAY),
           NOW()
       );
SET i = i + 1;
END WHILE;
END$$

-- 3. Generate Users (Updated to match User class)
CREATE PROCEDURE GenerateUsers(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE first_names TEXT DEFAULT 'Nguyen,Tran,Le,Pham,Hoang,Vu,Vo,Dang,Bui,Do,Ho,Ngo,Duong,Ly,Truong,Phan,Vang,Tang,Lam,Huynh';
    DECLARE last_names TEXT DEFAULT 'An,Binh,Cuong,Dung,Em,Phong,Giang,Hao,Khoa,Linh,Minh,Nam,Quan,Son,Tuan,Uyen,Van,Xuan,Yen,Duc';
    DECLARE first_count INT;
    DECLARE last_count INT;
    DECLARE first_name VARCHAR(50);
    DECLARE last_name VARCHAR(50);
    DECLARE full_name VARCHAR(100);

    SET first_count = (LENGTH(first_names) - LENGTH(REPLACE(first_names, ',', '')) + 1);
    SET last_count = (LENGTH(last_names) - LENGTH(REPLACE(last_names, ',', '')) + 1);

    WHILE i < record_count DO
            SET first_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(first_names, ',', FLOOR(1 + RAND() * first_count)), ',', -1));
            SET last_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(last_names, ',', FLOOR(1 + RAND() * last_count)), ',', -1));
            SET full_name = CONCAT(first_name, ' ', last_name);

            INSERT INTO user (name, email, password, status, role)
            VALUES (
                       full_name,
                       RANDOM_EMAIL(),
                       MD5(CONCAT('password', i)),
                       RAND() > 0.05,  -- 95% chance status = true
                       CASE
                           WHEN RAND() > 0.9 THEN 'ADMIN'
                           WHEN RAND() > 0.7 THEN 'MANAGER'
                           ELSE 'USER'
                           END
                   );

            SET i = i + 1;
        END WHILE;
END$$

-- 4. Generate Products (Updated to match Product class)
CREATE PROCEDURE GenerateProducts(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE product_names TEXT DEFAULT 'Smartphone,Laptop,Headphones,Watch,Camera,Tablet,Speaker,Monitor,Keyboard,Mouse,Shoes,Shirt,Pants,Dress,Jacket,Bag,Sunglasses,Perfume,Book,Toy';
    DECLARE name_count INT;
    DECLARE product_name VARCHAR(100);

    SET name_count = (LENGTH(product_names) - LENGTH(REPLACE(product_names, ',', '')) + 1);

    WHILE i < record_count DO
            SET product_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(product_names, ',', FLOOR(1 + RAND() * name_count)), ',', -1));

            INSERT INTO products (name, price, image, quantity, description, status)
            VALUES (
                       CONCAT(product_name, ' ', RANDOM_STRING(4)),
                       ROUND(50 + RAND() * 1950, 2),  -- Random price between 50-2000
                       CONCAT('https://images.example.com/products/', LOWER(product_name), '_', i+1, '.jpg'),
                       FLOOR(1 + RAND() * 1000),  -- Random quantity between 1-1000
                       CONCAT('High quality ', product_name, ' with advanced features and modern design. Perfect for daily use with excellent performance and durability.'),
                       RAND() > 0.1  -- 90% chance status = true (active)
                   );

            SET i = i + 1;
        END WHILE;
END$$

-- 5. Generate Product Categories
CREATE PROCEDURE GenerateProductCategories()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE product_count INT;
    DECLARE category_count INT;
    DECLARE categories_per_product INT;
    DECLARE j INT;
    DECLARE random_category_id INT;

SELECT COUNT(*) INTO product_count FROM products;
SELECT COUNT(*) INTO category_count FROM categories;

WHILE i < product_count DO
        SET categories_per_product = FLOOR(1 + RAND() * 3);
        SET j = 0;

        WHILE j < categories_per_product DO
            SET random_category_id = FLOOR(1 + RAND() * category_count);

            INSERT IGNORE INTO product_categories (product_id, category_id, is_primary, created_at)
            VALUES (
                i + 1,
                random_category_id,
                j = 0,
                NOW()
            );
            SET j = j + 1;
END WHILE;
        SET i = i + 1;
END WHILE;
END$$

-- 6. Generate Product Images
CREATE PROCEDURE GenerateProductImages()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE product_count INT;
    DECLARE images_per_product INT;
    DECLARE j INT;

SELECT COUNT(*) INTO product_count FROM products;

WHILE i < product_count DO
        SET images_per_product = FLOOR(2 + RAND() * 6);
        SET j = 0;

        WHILE j < images_per_product DO
            INSERT INTO product_images (product_id, image_url, alt_text, is_primary, sort_order, created_at)
            VALUES (
                i + 1,
                CONCAT('https://images.example.com/product_', i+1, '_image_', j+1, '.jpg'),
                CONCAT('Product image ', j+1),
                j = 0,
                j + 1,
                NOW()
            );
            SET j = j + 1;
END WHILE;
        SET i = i + 1;
END WHILE;
END$$

-- 7. Generate Tags
CREATE PROCEDURE GenerateTags(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE tag_names TEXT DEFAULT 'bestseller,new,sale,premium,eco-friendly,wireless,waterproof,lightweight,durable,stylish,comfortable,trendy,limited-edition,handmade,organic,vintage,modern,classic,sport,casual';
    DECLARE name_count INT;
    DECLARE tag_name VARCHAR(50);

    SET name_count = (LENGTH(tag_names) - LENGTH(REPLACE(tag_names, ',', '')) + 1);

    WHILE i < record_count DO
        SET tag_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(tag_names, ',', FLOOR(1 + RAND() * name_count + i) % name_count + 1), ',', -1));

INSERT INTO tags (name, slug, description, color, created_at, updated_at)
VALUES (
           CONCAT(tag_name, CASE WHEN i >= name_count THEN CONCAT('-', RANDOM_STRING(2)) ELSE '' END),
           LOWER(REPLACE(CONCAT(tag_name, CASE WHEN i >= name_count THEN CONCAT('-', RANDOM_STRING(2)) ELSE '' END), ' ', '-')),
           CONCAT('Tag for ', tag_name, ' products'),
           CONCAT('#', SUBSTR(MD5(RAND()), 1, 6)),
           DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY),
           NOW()
       );
SET i = i + 1;
END WHILE;
END$$

-- 8. Generate Product Tags
CREATE PROCEDURE GenerateProductTags()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE product_count INT;
    DECLARE tag_count INT;
    DECLARE tags_per_product INT;
    DECLARE j INT;
    DECLARE random_tag_id INT;

SELECT COUNT(*) INTO product_count FROM products;
SELECT COUNT(*) INTO tag_count FROM tags;

WHILE i < product_count DO
        SET tags_per_product = FLOOR(1 + RAND() * 5);
        SET j = 0;

        WHILE j < tags_per_product DO
            SET random_tag_id = FLOOR(1 + RAND() * tag_count);

            INSERT IGNORE INTO product_tags (product_id, tag_id, created_at)
            VALUES (
                i + 1,
                random_tag_id,
                NOW()
            );
            SET j = j + 1;
END WHILE;
        SET i = i + 1;
END WHILE;
END$$

-- 9. Generate Orders (Updated to match Order class)
CREATE PROCEDURE GenerateOrders(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE user_count INT;
    DECLARE product_count INT;
    DECLARE random_user_id INT;
    DECLARE total_price DOUBLE;
    DECLARE order_date DATETIME;
    DECLARE cart_items_json TEXT;
    DECLARE num_items INT;
    DECLARE j INT;
    DECLARE product_id INT;
    DECLARE product_name VARCHAR(255);
    DECLARE product_price DOUBLE;
    DECLARE item_quantity INT;

    SELECT COUNT(*) INTO user_count FROM user;
    SELECT COUNT(*) INTO product_count FROM products;

    WHILE i < record_count DO
            SET random_user_id = FLOOR(1 + RAND() * user_count);
            SET order_date = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY);
            SET total_price = 0;
            SET cart_items_json = '[';
            SET num_items = FLOOR(1 + RAND() * 5); -- Random 1-5 items per order
            SET j = 0;

            -- Generate cart items JSON
            WHILE j < num_items DO
                    SET product_id = FLOOR(1 + RAND() * product_count);

                    -- Get product info (assuming we have products table)
                    SELECT name, price INTO product_name, product_price
                    FROM products
                    WHERE id = product_id
                    LIMIT 1;

                    SET item_quantity = FLOOR(1 + RAND() * 3); -- 1-3 quantity per item
                    SET total_price = total_price + (product_price * item_quantity);

                    -- Add item to JSON
                    IF j > 0 THEN
                        SET cart_items_json = CONCAT(cart_items_json, ',');
                    END IF;

                    SET cart_items_json = CONCAT(cart_items_json,
                                                 '{',
                                                 '"productId":', product_id, ',',
                                                 '"productName":"', REPLACE(product_name, '"', '\\"'), '",',
                                                 '"price":', product_price, ',',
                                                 '"quantity":', item_quantity, ',',
                                                 '"subtotal":', (product_price * item_quantity),
                                                 '}'
                                          );

                    SET j = j + 1;
                END WHILE;

            SET cart_items_json = CONCAT(cart_items_json, ']');

            INSERT INTO orders (userId, orderDate, totalPrice, status, voucherMess, cartItemsJson)
            VALUES (
                       random_user_id,
                       order_date,
                       ROUND(total_price, 2),
                       CASE
                           WHEN RAND() > 0.9 THEN 'CANCELLED'
                           WHEN RAND() > 0.8 THEN 'PENDING'
                           WHEN RAND() > 0.3 THEN 'COMPLETED'
                           ELSE 'PROCESSING'
                           END,
                       CASE
                           WHEN RAND() > 0.7 THEN CONCAT('Voucher applied: ',
                                                         CASE
                                                             WHEN RAND() > 0.6 THEN 'DISCOUNT10'
                                                             WHEN RAND() > 0.3 THEN 'FREESHIP'
                                                             ELSE 'WELCOME20'
                                                             END)
                           ELSE NULL
                           END,
                       cart_items_json
                   );

            SET i = i + 1;
        END WHILE;
END$$

-- 10. Generate Order Items
CREATE PROCEDURE GenerateOrderItems()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE order_count INT;
    DECLARE product_count INT;
    DECLARE items_per_order INT;
    DECLARE j INT;
    DECLARE random_product_id INT;
    DECLARE unit_price DECIMAL(10,2);
    DECLARE quantity INT;
    DECLARE total_price DOUBLE;

SELECT COUNT(*) INTO order_count FROM orders;
SELECT COUNT(*) INTO product_count FROM products;

WHILE i < order_count DO
        SET items_per_order = FLOOR(1 + RAND() * 5);
        SET j = 0;

        WHILE j < items_per_order DO
            SET random_product_id = FLOOR(1 + RAND() * product_count);
            SET quantity = FLOOR(1 + RAND() * 3);

SELECT price INTO unit_price FROM products WHERE id = random_product_id;
SET unit_price = unit_price * (0.8 + RAND() * 0.4); -- Add some price variation
            SET total_price = unit_price * quantity;

INSERT INTO order_items (order_id, product_id, product_name, sku, quantity, unit_price, total_price, discount_amount, tax_amount, weight, fulfillment_status, created_at)
SELECT
    i + 1,
    random_product_id,
    p.name,
    'p.sku',
    quantity,
    unit_price,
    total_price,
    CASE WHEN RAND() > 0.8 THEN ROUND(total_price * RAND() * 0.2, 2) ELSE 0 END,
    ROUND(total_price * 0.1, 2),
    ROUND(0.1 + RAND() * 2, 2),
    CASE
        WHEN RAND() > 0.8 THEN 'delivered'
        WHEN RAND() > 0.6 THEN 'shipped'
        WHEN RAND() > 0.4 THEN 'processing'
        ELSE 'unfulfilled'
        END,
    NOW()
FROM products p WHERE p.id = random_product_id;

SET j = j + 1;
END WHILE;
        SET i = i + 1;
END WHILE;
END$$

-- 11. Generate Reviews
CREATE PROCEDURE GenerateReviews(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE user_count INT;
    DECLARE product_count INT;
    DECLARE order_count INT;
    DECLARE random_user_id INT;
    DECLARE random_product_id INT;
    DECLARE random_order_id INT;
    DECLARE rating INT;
    DECLARE review_titles TEXT DEFAULT 'Great product!,Love it!,Excellent quality,Good value,Not bad,Could be better,Amazing!,Perfect,Satisfied,Recommended';
    DECLARE title_count INT;
    DECLARE review_title VARCHAR(100);

SELECT COUNT(*) INTO user_count FROM user;
SELECT COUNT(*) INTO product_count FROM products;
SELECT COUNT(*) INTO order_count FROM orders;
SET title_count = (LENGTH(review_titles) - LENGTH(REPLACE(review_titles, ',', '')) + 1);

    WHILE i < record_count DO
        SET random_user_id = FLOOR(1 + RAND() * user_count);
        SET random_product_id = FLOOR(1 + RAND() * product_count);
        SET random_order_id = CASE WHEN RAND() > 0.3 THEN FLOOR(1 + RAND() * order_count) ELSE NULL END;
        SET rating = FLOOR(1 + RAND() * 5);
        SET review_title = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(review_titles, ',', FLOOR(1 + RAND() * title_count)), ',', -1));

INSERT INTO reviews (product_id, user_id, order_id, rating, title, content, is_verified_purchase, is_approved, helpful_count, created_at, updated_at)
VALUES (
           random_product_id,
           random_user_id,
           random_order_id,
           rating,
           review_title,
           CONCAT('This product is ', CASE
                                          WHEN rating >= 4 THEN 'really good and I recommend it to everyone. '
                                          WHEN rating >= 3 THEN 'decent and worth the price. '
                                          ELSE 'not what I expected. '
               END, 'The quality is ', CASE
                                           WHEN rating >= 4 THEN 'excellent'
                                           WHEN rating >= 3 THEN 'good'
                                           ELSE 'poor'
                      END, ' and delivery was fast.'),
           random_order_id IS NOT NULL,
           RAND() > 0.1,
           FLOOR(RAND() * 20),
           DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 120) DAY),
           NOW()
       );
SET i = i + 1;
END WHILE;
END$$

-- 12. Generate Locations
CREATE PROCEDURE GenerateLocations(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE cities TEXT DEFAULT 'Ho Chi Minh City,Hanoi,Da Nang,Can Tho,Hai Phong,Nha Trang,Hue,Vung Tau,Phan Thiet,Quy Nhon';
    DECLARE city_count INT;
    DECLARE city_name VARCHAR(100);

    SET city_count = (LENGTH(cities) - LENGTH(REPLACE(cities, ',', '')) + 1);

    WHILE i < record_count DO
        SET city_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(cities, ',', FLOOR(1 + RAND() * city_count)), ',', -1));

INSERT INTO locations (name, address1, address2, city, country, province, zip, phone, active, fulfills_online_orders, legacy, created_at, updated_at)
VALUES (
           CONCAT('Warehouse ', city_name, ' ', i+1),
           CONCAT(FLOOR(1 + RAND() * 999), ' Main Street'),
           CASE WHEN RAND() > 0.7 THEN CONCAT('Floor ', FLOOR(1 + RAND() * 10)) ELSE NULL END,
           city_name,
           'Vietnam',
           CASE
               WHEN city_name = 'Ho Chi Minh City' THEN 'Ho Chi Minh'
               WHEN city_name = 'Hanoi' THEN 'Ha Noi'
               ELSE city_name
               END,
           LPAD(FLOOR(100000 + RAND() * 899999), 6, '0'),
           RANDOM_PHONE(),
           RAND() > 0.1,
           RAND() > 0.2,
           RAND() > 0.9,
           DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 500) DAY),
           NOW()
       );
SET i = i + 1;
END WHILE;
END$$

-- 13. Generate Suppliers
CREATE PROCEDURE GenerateSuppliers(IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE company_names TEXT DEFAULT 'Tech Solutions,Global Trade,Quality Parts,Premium Supply,Fast Logistics,Best Materials,Top Manufacturer,Smart Components,Advanced Systems,Modern Factory';
    DECLARE name_count INT;
    DECLARE company_name VARCHAR(100);

    SET name_count = (LENGTH(company_names) - LENGTH(REPLACE(company_names, ',', '')) + 1);

    WHILE i < record_count DO
        SET company_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(company_names, ',', FLOOR(1 + RAND() * name_count)), ',', -1));

INSERT INTO suppliers (name, code, contact_person, email, phone, address, city, country, payment_terms, lead_time_days, is_active, rating, created_at, updated_at)
VALUES (
           CONCAT(company_name, ' Co., Ltd.'),
           CONCAT('SUP-', LPAD(i+1, 4, '0')),
           CONCAT('Manager ', RANDOM_STRING(6)),
           CONCAT('contact@', LOWER(REPLACE(company_name, ' ', '')), '.com'),
           RANDOM_PHONE(),
           CONCAT(FLOOR(1 + RAND() * 999), ' Industrial Road'),
           CASE
               WHEN RAND() > 0.7 THEN 'Ho Chi Minh City'
               WHEN RAND() > 0.4 THEN 'Hanoi'
               ELSE 'Da Nang'
               END,
           'Vietnam',
           CASE
               WHEN RAND() > 0.6 THEN 'NET 30'
               WHEN RAND() > 0.3 THEN 'NET 15'
               ELSE 'COD'
               END,
           FLOOR(3 + RAND() * 28),
           RAND() > 0.05,
           ROUND(2 + RAND() * 3, 1),
           DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 730) DAY),
           NOW()
       );
SET i = i + 1;
END WHILE;
END$$

-- 14. Master procedure to generate all data
CREATE PROCEDURE GenerateAllSampleData()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

-- Clear existing data
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE analytics;
TRUNCATE TABLE product_promotions;
TRUNCATE TABLE promotions;
TRUNCATE TABLE inventory_movements;
TRUNCATE TABLE purchase_order_items;
TRUNCATE TABLE purchase_orders;
TRUNCATE TABLE product_suppliers;
TRUNCATE TABLE suppliers;
TRUNCATE TABLE inventories;
TRUNCATE TABLE locations;
TRUNCATE TABLE carts;
TRUNCATE TABLE wishlists;
TRUNCATE TABLE review_images;
TRUNCATE TABLE reviews;
TRUNCATE TABLE addresses;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE coupons;
TRUNCATE TABLE product_tags;
TRUNCATE TABLE tags;
TRUNCATE TABLE product_images;
TRUNCATE TABLE product_categories;
TRUNCATE TABLE products;
TRUNCATE TABLE users;
TRUNCATE TABLE brands;
TRUNCATE TABLE categories;

SET FOREIGN_KEY_CHECKS = 1;

    -- Generate base data
CALL GenerateCategories(25);
CALL GenerateBrands(30);
CALL GenerateUsers(500);
CALL GenerateTags(50);
# CALL GenerateLocations(10);
CALL GenerateSuppliers(20);

-- Generate products and related data
CALL GenerateProducts(200);
CALL GenerateProductCategories();
# CALL GenerateProductImages();
CALL GenerateProductTags();

-- Generate orders and related data
CALL GenerateOrders(1000);
CALL GenerateOrderItems();
CALL GenerateReviews(800);

-- Generate additional data (you can add more procedures here)
-- CALL GenerateWishlists();
-- CALL GenerateCarts();
-- CALL GenerateInventories();
-- CALL GenerateAnalytics();

COMMIT;

SELECT 'Sample data generation completed successfully!' as message;
END$$

DELIMITER ;

-- ================================
-- EXECUTION INSTRUCTIONS
-- ================================

/*
-- To generate all sample data, run:
CALL GenerateAllSampleData();

-- To generate specific data types:
CALL GenerateCategories(20);
CALL GenerateBrands(25);
CALL GenerateUsers(100);
CALL GenerateProducts(50);
CALL GenerateOrders(200);
CALL GenerateReviews(150);

-- To check generated data:
SELECT 'Categories' as table_name, COUNT(*) as record_count FROM categories
UNION ALL SELECT 'Brands', COUNT(*) FROM brands
UNION ALL SELECT 'Users', COUNT(*) FROM users
UNION ALL SELECT 'Products', COUNT(*) FROM products
UNION ALL SELECT 'Orders', COUNT(*) FROM orders
UNION ALL SELECT 'Order Items', COUNT(*) FROM order_items
UNION ALL SELECT 'Reviews', COUNT(*) FROM reviews
UNION ALL SELECT 'Product Categories', COUNT(*) FROM product_categories
UNION ALL SELECT 'Product Images', COUNT(*) FROM product_images
UNION ALL SELECT 'Product Tags', COUNT(*) FROM product_tags;
*/