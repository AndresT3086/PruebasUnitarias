-- Insert sample packages with different statuses
-- CREATED status packages
INSERT INTO packages (id, recipient_name, recipient_email, recipient_phone, street, city, state, country, postal_code, height, width, depth, weight, status, notes)
VALUES
('LT-CRE000001', 'John Doe', 'john.doe@email.com', '+1234567890', '123 Main St', 'New York', 'NY', 'USA', '10001', 20, 15, 10, 2.5, 'CREATED', 'Fragile items'),
('LT-CRE000002', 'Jane Smith', 'jane.smith@email.com', '+1987654321', '456 Oak Ave', 'Los Angeles', 'CA', 'USA', '90001', 30, 20, 15, 5.0, 'CREATED', 'Electronics'),
('LT-CRE000003', 'Bob Johnson', 'bob.j@email.com', '+1122334455', '789 Pine Rd', 'Chicago', 'IL', 'USA', '60601', 25, 25, 20, 3.2, 'CREATED', NULL),
('LT-CRE000004', 'Alice Brown', 'alice.b@email.com', '+1555666777', '321 Elm St', 'Houston', 'TX', 'USA', '77001', 15, 10, 8, 1.0, 'CREATED', 'Documents');

-- IN_TRANSIT status packages
INSERT INTO packages (id, recipient_name, recipient_email, recipient_phone, street, city, state, country, postal_code, height, width, depth, weight, status, notes)
VALUES
('LT-TRA000001', 'Michael Davis', 'michael.d@email.com', '+1666777888', '654 Birch Ln', 'Phoenix', 'AZ', 'USA', '85001', 35, 30, 25, 8.5, 'IN_TRANSIT', 'Heavy machinery parts'),
('LT-TRA000002', 'Sarah Wilson', 'sarah.w@email.com', '+1777888999', '987 Cedar Dr', 'Philadelphia', 'PA', 'USA', '19101', 18, 12, 10, 1.8, 'IN_TRANSIT', NULL),
('LT-TRA000003', 'Tom Martinez', 'tom.m@email.com', '+1888999000', '147 Spruce Way', 'San Antonio', 'TX', 'USA', '78201', 40, 35, 30, 12.0, 'IN_TRANSIT', 'Furniture'),
('LT-TRA000004', 'Emma Garcia', 'emma.g@email.com', '+1999000111', '258 Maple Ct', 'San Diego', 'CA', 'USA', '92101', 22, 18, 15, 4.3, 'IN_TRANSIT', 'Books');

-- OUT_FOR_DELIVERY status packages
INSERT INTO packages (id, recipient_name, recipient_email, recipient_phone, street, city, state, country, postal_code, height, width, depth, weight, status, notes)
VALUES
('LT-OUT000001', 'David Lee', 'david.l@email.com', '+1333444555', '369 Willow St', 'Dallas', 'TX', 'USA', '75201', 28, 22, 18, 6.2, 'OUT_FOR_DELIVERY', 'Urgent delivery'),
('LT-OUT000002', 'Lisa Anderson', 'lisa.a@email.com', '+1444555666', '741 Ash Blvd', 'San Jose', 'CA', 'USA', '95101', 15, 15, 15, 2.0, 'OUT_FOR_DELIVERY', NULL),
('LT-OUT000003', 'James Taylor', 'james.t@email.com', '+1555666777', '852 Palm Ave', 'Austin', 'TX', 'USA', '73301', 32, 28, 22, 7.8, 'OUT_FOR_DELIVERY', 'Computer equipment'),
('LT-OUT000004', 'Mary Thomas', 'mary.t@email.com', '+1666777888', '963 Beach Rd', 'Jacksonville', 'FL', 'USA', '32099', 20, 16, 12, 3.5, 'OUT_FOR_DELIVERY', 'Clothing');

-- DELIVERED status packages
INSERT INTO packages (id, recipient_name, recipient_email, recipient_phone, street, city, state, country, postal_code, height, width, depth, weight, status, notes)
VALUES
('LT-DEL000001', 'Robert White', 'robert.w@email.com', '+1222333444', '159 River Rd', 'Columbus', 'OH', 'USA', '43085', 25, 20, 15, 4.5, 'DELIVERED', 'Delivered to reception'),
('LT-DEL000002', 'Patricia Harris', 'patricia.h@email.com', '+1333444555', '753 Lake Dr', 'Charlotte', 'NC', 'USA', '28201', 30, 25, 20, 5.5, 'DELIVERED', NULL),
('LT-DEL000003', 'Charles Clark', 'charles.c@email.com', '+1444555666', '951 Mountain View', 'Detroit', 'MI', 'USA', '48201', 18, 14, 10, 2.2, 'DELIVERED', 'Signature obtained'),
('LT-DEL000004', 'Jennifer Lewis', 'jennifer.l@email.com', '+1555666777', '357 Valley Ln', 'El Paso', 'TX', 'USA', '79901', 35, 30, 28, 9.0, 'DELIVERED', 'Left at door');

-- DELIVERY_FAILED status packages
INSERT INTO packages (id, recipient_name, recipient_email, recipient_phone, street, city, state, country, postal_code, height, width, depth, weight, status, notes)
VALUES
('LT-FAI000001', 'William Walker', 'william.w@email.com', '+1777888999', '246 Hill St', 'Memphis', 'TN', 'USA', '38101', 22, 18, 14, 3.8, 'DELIVERY_FAILED', 'No one home'),
('LT-FAI000002', 'Linda Hall', 'linda.h@email.com', '+1888999000', '468 Garden Ave', 'Boston', 'MA', 'USA', '02101', 16, 12, 8, 1.5, 'DELIVERY_FAILED', 'Wrong address'),
('LT-FAI000003', 'Richard Allen', 'richard.a@email.com', '+1999000111', '135 Park Blvd', 'Denver', 'CO', 'USA', '80201', 28, 24, 20, 6.0, 'DELIVERY_FAILED', 'Refused by recipient'),
('LT-FAI000004', 'Susan Young', 'susan.y@email.com', '+1000111222', '579 Forest Way', 'Washington', 'DC', 'USA', '20001', 20, 15, 12, 2.8, 'DELIVERY_FAILED', 'Damaged package');

-- RETURNED status packages
INSERT INTO packages (id, recipient_name, recipient_email, recipient_phone, street, city, state, country, postal_code, height, width, depth, weight, status, notes)
VALUES
('LT-RET000001', 'Joseph King', 'joseph.k@email.com', '+1111222333', '864 Ocean Dr', 'Miami', 'FL', 'USA', '33101', 24, 20, 16, 4.2, 'RETURNED', 'Unclaimed after 3 attempts'),
('LT-RET000002', 'Nancy Wright', 'nancy.w@email.com', '+1222333444', '975 Desert Rd', 'Las Vegas', 'NV', 'USA', '89101', 30, 26, 22, 7.5, 'RETURNED', 'Return to sender requested'),
('LT-RET000003', 'Daniel Lopez', 'daniel.l@email.com', '+1333444555', '531 Snow Ave', 'Portland', 'OR', 'USA', '97201', 18, 15, 12, 2.5, 'RETURNED', 'Incorrect recipient'),
('LT-RET000004', 'Karen Hill', 'karen.h@email.com', '+1444555666', '642 Rain St', 'Seattle', 'WA', 'USA', '98101', 26, 22, 18, 5.8, 'RETURNED', 'Address not found');

-- Insert location history for some packages
INSERT INTO location_history (package_id, city, country, description, latitude, longitude, timestamp)
VALUES
-- For IN_TRANSIT packages
('LT-TRA000001', 'Phoenix', 'USA', 'Package picked up', 33.4484, -112.0740, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('LT-TRA000001', 'Las Vegas', 'USA', 'In transit', 36.1699, -115.1398, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('LT-TRA000002', 'Philadelphia', 'USA', 'Package picked up', 39.9526, -75.1652, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('LT-TRA000002', 'New York', 'USA', 'Sorting facility', 40.7128, -74.0060, CURRENT_TIMESTAMP - INTERVAL '2 days'),
-- For OUT_FOR_DELIVERY packages
('LT-OUT000001', 'Dallas', 'USA', 'Package picked up', 32.7767, -96.7970, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('LT-OUT000001', 'Houston', 'USA', 'Regional hub', 29.7604, -95.3698, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('LT-OUT000001', 'Dallas', 'USA', 'Out for delivery', 32.7767, -96.7970, CURRENT_TIMESTAMP - INTERVAL '1 hour'),
-- For DELIVERED packages
('LT-DEL000001', 'Columbus', 'USA', 'Package picked up', 39.9612, -82.9988, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('LT-DEL000001', 'Cincinnati', 'USA', 'In transit', 39.1031, -84.5120, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('LT-DEL000001', 'Columbus', 'USA', 'Delivered', 39.9612, -82.9988, CURRENT_TIMESTAMP - INTERVAL '2 days');