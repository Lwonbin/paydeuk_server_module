-- USERS TABLE:
--      ROLE == USER
INSERT INTO users (name, username, password, personal_auth_key, phone, email, payment_pin_code, address, role, birth_date,
                   status, created_at, updated_at)
VALUES ('일반유저', 'user', '$2a$12$2JKJbuJMKQJe0hX5zr9c0e.BgSBjVe2cKnBRis1e4vc1FXpbue0M.', '12345', '010-1234-5678', 'test@test.com',
        NULL, '서울 마포구 월드컵북로 434 상암 IT Tower', 'USER', '1990-01-01', 'active', NOW(),
        NOW());

--      ROLE == ADMIN
INSERT INTO users (name, username, password, personal_auth_key, phone, email, payment_pin_code, address, role, birth_date,
                   status, created_at, updated_at)
VALUES ('관리자', 'admin', '$2a$12$2JKJbuJMKQJe0hX5zr9c0e.BgSBjVe2cKnBRis1e4vc1FXpbue0M.', '1234', '010-0000-0000', 'admin@example.com', NULL, NULL, 'ADMIN',
        '1980-01-01', 'ACTIVE', NOW(), NOW());

-- CARD TABLE
-- card_id == 1
INSERT INTO card (name, type, image_url, annual_fee, company, created_at, updated_at)
VALUES ('현대카드 M', 'credit', 'dummyurl', 30000, 'HYUNDAI', NOW(), NOW()),
('삼성카드 S', 'credit', 'dummyurl2', 20000, 'SAMSUNG', NOW(), NOW()),
       ('우리카드 W', 'credit', 'dummyurl3', 40000, 'WOORI', NOW(), NOW());
-- USER_CARD TABLE
INSERT INTO user_card (user_id, card_id, card_token, card_number, is_default_card, created_at, updated_at)
VALUES (1, 1, 'hyundai_m_sungjun', '1234', 1, NOW(), NOW()),
(1, 2, 'mock_token2', '5678', 0, NOW(), NOW()),
       (1, 3, 'mock_token3', '4321', 0, NOW(), NOW());

-- MERCHANT TABLE
INSERT INTO merchant (name, is_active, commission_rate, business_number, manager_name, phone, manager_phone, category,
                      created_at, updated_at)
VALUES ('스타벅스', 1, '10%', '123-45-67890', '김스벅', '02-1234-5678', '010-8765-4321', 'food_beverage', NOW(), NOW()),
       ('마켓컬리', 1, '10%', '123-45-67890', '이컬리', '02-1234-5678', '010-8765-4321', 'food_beverage', NOW(), NOW()),
       ('배달의민족', 1, '10%', '123-45-67890', '박배민', '02-1234-5678', '010-8765-4321', 'food_beverage', NOW(), NOW()),
       ('넷플릭스', 1, '10%', '123-45-67890', '최넷플', '02-1234-5678', '010-8765-4321', 'subscribe', NOW(), NOW()),
       ('멜론', 1, '10%', '123-45-67890', '정멜론', '02-1234-5678', '010-8765-4321', 'subscribe', NOW(), NOW()),
       ('네이버플러스멤버십', 1, '10%', '123-45-67890', '김네멤', '02-1234-5678', '010-8765-4321', 'subscribe', NOW(), NOW()),
       ('쿠팡', 1, '10%', '123-45-67890', '이쿠팡', '02-1234-5678', '010-8765-4321', 'shopping', NOW(), NOW()),
       ('무신사', 1, '10%', '123-45-67890', '박신사', '02-1234-5678', '010-8765-4321', 'shopping', NOW(), NOW()),
       ('이마트몰', 1, '10%', '123-45-67890', '최마트', '02-1234-5678', '010-8765-4321', 'shopping', NOW(), NOW()),
       ('오늘의집', 1, '10%', '123-45-67890', '정오집', '02-1234-5678', '010-8765-4321', 'shopping', NOW(), NOW()),
       ('CGV', 1, '10%', '123-45-67890', '김지비', '02-1234-5678', '010-8765-4321', 'culture', NOW(), NOW()),
       ('교보문고', 1, '10%', '123-45-67890', '이교보', '02-1234-5678', '010-8765-4321', 'culture', NOW(), NOW()),
       ('롯데월드', 1, '10%', '123-45-67890', '박롯데', '02-1234-5678', '010-8765-4321', 'culture', NOW(), NOW()),
       ('KTX', 1, '10%', '123-45-67890', '최기차', '02-1234-5678', '010-8765-4321', 'transportation', NOW(), NOW()),
       ('고속버스', 1, '10%', '123-45-67890', '정고속', '02-1234-5678', '010-8765-4321', 'transportation', NOW(), NOW()),
       ('항공', 1, '10%', '123-45-67890', '정항공', '02-1234-5678', '010-8765-4321', 'transportation', NOW(), NOW());

-- BENEFIT TABLE
INSERT INTO benefit (title, description, benefit_type, has_additional_condition, merchant_id, created_at, updated_at)
VALUES ('기본혜택', '국내외 가맹점 1.5% M포인트 적립', 'point', 1, NULL, NOW(), NOW()),
       ('추가혜택', '컬리 5% M포인트 적립', 'point', 1, 2, NOW(), NOW()), -- 컬리
       ('추가혜택', '쿠팡 5% M포인트 적립', 'point', 1, 7, NOW(), NOW()), -- 쿠팡
       ('추가혜택', '이마트 5% M포인트 적립', 'point', 1, 9, NOW(), NOW()), -- 이마트
       ('기본혜택', '쿠팡 800원 할인', 'discount', 1, 7, NOW(), NOW()),
       ('기본혜택', 'CGV 10% M포인트 적립', 'point', 1, 11, NOW(), NOW());



-- SPENDING_RANGE TABLE
INSERT INTO spending_range (min_spending, max_spending)
VALUES (500000, NULL),
       (1000000, NULL),
       (300000, 500000),
       (500000, 1000000);

-- BENEFIT_CONDITION TABLE
INSERT INTO benefit_condition (benefit_id, spending_range_id, value, category, created_at, updated_at)
VALUES (2, NULL, 10000, 'MONTHLY_DISCOUNT_LIMIT', NOW(), NOW()),
       (3, NULL, 900, 'MONTHLY_DISCOUNT_LIMIT', NOW(), NOW()),
       (4, NULL, 10000, 'MONTHLY_DISCOUNT_LIMIT', NOW(), NOW());


-- DISCOUNT TABLE
INSERT INTO discount (benefit_id, spending_range_id, apply_type, amount, created_at, updated_at)
VALUES (1, 1, 'rate', 1.5, NOW(), NOW()),
       (1, 2, 'rate', 5, NOW(), NOW()),
       (3, 3, 'rate', 7, NOW(), NOW()),
       (3, 4, 'rate', 10, NOW(), NOW()),
       (5, 3, 'amount', 600, NOW(), NOW()),
       (5, 4, 'amount', 800, NOW(), NOW()),
       (2, 3, 'rate', 3, NOW(), NOW()),
       (2, 4, 'rate', 5, NOW(), NOW());

-- CARD_BENEFIT TABLE
INSERT INTO card_benefit (card_id, benefit_id, created_at, updated_at)
VALUES (1, 1, NOW(), NOW()),
       (1, 2, NOW(), NOW()),
       (1, 3, NOW(), NOW()),
       (1, 4, NOW(), NOW()),
       -- (2,2,NOW(),NOW()),
      -- (2, 3, NOW(), NOW()),
       (3, 5, NOW(), NOW()),
       (3, 6, NOW(), NOW());


-- PAYMENT TABLE
INSERT INTO payment (product_name, amount, payment_success, user_card_id, merchant_id, card_benefit_id, discount_amount, created_at, updated_at)
VALUES 
    ('스타벅스 아메리카노', 4500, true, 1, 1, 1, 450, NOW(), NOW()),
    ('스타벅스 카페라떼', 5000, true, 1, 1, 1, 500, NOW(), NOW()),
    ('컬리 생필품', 25000, true, 1, 2, 2, 1250, NOW(), NOW()),
    ('쿠팡 전자제품', 150000, true, 1, 7, 3, 7500, NOW(), NOW()),
    ('이마트 식료품', 50000, true, 1, 9, 4, 2500, NOW(), NOW()),
    ('스타벅스 케이크', 6000, false, 2, 1, 1, 0, NOW(), NOW()),
    ('컬리 신선식품', 35000, true, 2, 2, 2, 1750, NOW(), NOW()),
    ('쿠팡 의류', 80000, true, 2, 7, 3, 4000, NOW(), NOW()),
    ('이마트 가전제품', 200000, true, 2, 9, 4, 10000, NOW(), NOW()),
    ('스타벅스 디저트', 7000, true, 2, 1, 1, 700, NOW(), NOW());

-- 어제 결제 데이터 추가
INSERT INTO payment (
    product_name, amount, payment_success, user_card_id,
    merchant_id, card_benefit_id, discount_amount, created_at, updated_at
)
VALUES
    ('컬리 어제 채소', 20000, true, 1, 2, 2, 1000, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    ('쿠팡 어제 생필품', 40000, true, 2, 7, 3, 2000, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    ('이마트 어제 식자재', 55000, true, 1, 9, 4, 2750, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    ('스타벅스 어제 음료', 4800, true, 1, 1, 1, 480, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    ('스타벅스 어제 샌드위치', 6200, false, 2, 1, 1, 0, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);
