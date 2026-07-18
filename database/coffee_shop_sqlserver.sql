-- ============================================================
--  COFFEE SHOP DATABASE  (SQL Server / SSMS)
--  Generated from ERD (Draw.io)
-- ============================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'coffee_shop')
BEGIN
    ALTER DATABASE coffee_shop SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE coffee_shop;
END
GO

CREATE DATABASE coffee_shop
    COLLATE Vietnamese_CI_AS;
GO

USE coffee_shop;
GO

-- ============================================================
-- 1. roles
-- ============================================================
CREATE TABLE roles (
    role_id   INT          NOT NULL IDENTITY(1,1),
    role_name NVARCHAR(50) NOT NULL,

    CONSTRAINT pk_roles      PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE      (role_name)
);
GO

-- ============================================================
-- 2. users
-- ============================================================
CREATE TABLE users (
    user_id       INT            NOT NULL IDENTITY(1,1),
    role_id       INT            NOT NULL,
    first_name    NVARCHAR(100)  NOT NULL,
    last_name     NVARCHAR(100)  NOT NULL,
	username	  NVARCHAR(50)   NOT NULL,
    email         NVARCHAR(150)  NOT NULL,
    phone         NVARCHAR(20)   NOT NULL,
    password_hash NVARCHAR(255)  NOT NULL,
    status        BIT			 NOT NULL DEFAULT 1,   -- 'ACTIVE' | 'LOCKED'
    avatar_url    NVARCHAR(255)  NULL,
    created_at    DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at    DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_users				PRIMARY KEY (user_id),
	CONSTRAINT uq_users_username	UNIQUE      (username),
    CONSTRAINT uq_users_email		UNIQUE      (email),
	CONSTRAINT uq_users_phone		UNIQUE      (phone),
    CONSTRAINT fk_users_role		FOREIGN KEY  (role_id) REFERENCES roles (role_id)
);
GO

-- ============================================================
-- 3. categories
-- ============================================================
CREATE TABLE categories (
    category_id INT            NOT NULL IDENTITY(1,1),
    name        NVARCHAR(100)  NOT NULL,
    is_active   BIT            NOT NULL DEFAULT 1,
    created_at  DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_categories PRIMARY KEY (category_id)
);
GO

-- ============================================================
-- 4. products
-- ============================================================
CREATE TABLE products (
    product_id   INT            NOT NULL IDENTITY(1,1),
    category_id  INT            NOT NULL,
    name         NVARCHAR(150)  NOT NULL,
    description  NVARCHAR(MAX)  NULL,
    is_available BIT            NOT NULL DEFAULT 1,
    is_active    BIT            NOT NULL DEFAULT 1,
    created_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_products     PRIMARY KEY (product_id),
    CONSTRAINT fk_products_cat FOREIGN KEY (category_id) REFERENCES categories (category_id)
);
GO

-- ============================================================
-- 5. product_variants
-- ============================================================
CREATE TABLE product_variants (
    variant_id   INT            NOT NULL IDENTITY(1,1),
    product_id   INT            NOT NULL,
    variant_name NVARCHAR(100)  NOT NULL,
    size         NVARCHAR(5)    NULL,          -- 'S' | 'M' | 'L' | 'XL'
    temperature  NVARCHAR(10)   NULL,          -- 'HOT' | 'COLD' | 'ROOM'
    price        DECIMAL(10,2)  NOT NULL DEFAULT 0,
    is_available BIT            NOT NULL DEFAULT 1,

    CONSTRAINT pk_product_variants  PRIMARY KEY (variant_id),
    CONSTRAINT ck_variant_size      CHECK (size        IN ('S','M','L','XL')),
    CONSTRAINT ck_variant_temp      CHECK (temperature IN ('HOT','COLD','ROOM')),
    CONSTRAINT fk_variants_product  FOREIGN KEY (product_id) REFERENCES products (product_id)
);
GO

-- ============================================================
-- 6. product_images
-- ============================================================
CREATE TABLE product_images (
    image_id   INT            NOT NULL IDENTITY(1,1),
    product_id INT            NOT NULL,
    variant_id INT            NOT NULL,
    image_url  NVARCHAR(255)  NOT NULL,
    is_primary BIT            NOT NULL DEFAULT 0,
    created_at DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_product_images PRIMARY KEY (image_id),
    CONSTRAINT fk_images_product FOREIGN KEY (product_id) REFERENCES products         (product_id),
    CONSTRAINT fk_images_variant FOREIGN KEY (variant_id) REFERENCES product_variants (variant_id)
);
GO

-- ============================================================
-- 7. payment_methods
-- ============================================================
CREATE TABLE payment_methods (
    payment_method_id INT           NOT NULL IDENTITY(1,1),
    name              NVARCHAR(100) NOT NULL,

    CONSTRAINT pk_payment_methods     PRIMARY KEY (payment_method_id),
    CONSTRAINT uq_payment_method_name UNIQUE      (name)
);
GO

-- ============================================================
-- 8. tables  (bàn trong quán)
-- ============================================================
CREATE TABLE tables (
    table_id  INT           NOT NULL IDENTITY(1,1),
    capacity  INT           NOT NULL,
--  status    NVARCHAR(15)  NOT NULL DEFAULT 'AVAILABLE',  -- 'AVAILABLE'|'OCCUPIED'|'RESERVED'|'MAINTENANCE'
    is_active BIT           NOT NULL DEFAULT 1,

    CONSTRAINT pk_tables    PRIMARY KEY (table_id),
--  CONSTRAINT ck_tbl_status CHECK (status IN ('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE'))
);
GO

-- ============================================================
-- 20. customer_addresses  (địa chỉ giao hàng của Customer)
-- ============================================================
CREATE TABLE customer_addresses (
    address_id    INT            NOT NULL IDENTITY(1,1),
    customer_id   INT            NOT NULL,   -- FK → users (chỉ role Customer)
    label         NVARCHAR(50)   NULL,       -- 'Nhà', 'Cơ quan', v.v.
    full_address  NVARCHAR(500)  NOT NULL,
    recipient_name  NVARCHAR(150) NOT NULL,
    recipient_phone NVARCHAR(20)  NOT NULL,
    created_at    DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at    DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
 
    CONSTRAINT pk_customer_addresses   PRIMARY KEY (address_id),
    CONSTRAINT fk_addr_customer        FOREIGN KEY (customer_id) REFERENCES users (user_id)
);
GO

-- ============================================================
-- 9. orders
-- ============================================================
CREATE TABLE orders (
    order_id			INT            NOT NULL IDENTITY(1,1),
    user_id				INT				   NOT NULL,        
    table_id			INT            NULL,        -- NULL nếu mang về / online
    order_type			NVARCHAR(10)   NOT NULL,    -- 'ONLINE' | 'COUNTER'
    order_status		NVARCHAR(15)   NOT NULL DEFAULT 'PENDING',
    subtotal			DECIMAL(10,2)  NOT NULL DEFAULT 0,
    discount_amount		DECIMAL(10,2)  NOT NULL DEFAULT 0,
    total_amount		DECIMAL(10,2)  NOT NULL DEFAULT 0,
    points_earned		INT            NOT NULL DEFAULT 0,
    note				NVARCHAR(MAX)  NULL,
	delivery_address_id INT			   NULL,   -- NULL nếu table_id NOT NULL
    shipping_fee        DECIMAL(10,2)  NOT NULL DEFAULT 0,
    created_at			DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at			DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_orders			PRIMARY KEY (order_id),
    CONSTRAINT ck_order_type		CHECK (order_type   IN ('ONLINE','COUNTER')),
    CONSTRAINT ck_order_status		CHECK (order_status IN ('PENDING','CONFIRMED','PREPARING','READY','COMPLETED','CANCELLED')),
    CONSTRAINT fk_orders_user		FOREIGN KEY (user_id) REFERENCES users  (user_id),
    CONSTRAINT fk_orders_table		FOREIGN KEY (table_id)    REFERENCES tables (table_id),
	CONSTRAINT fk_orders_address	FOREIGN KEY (delivery_address_id) REFERENCES customer_addresses (address_id)
);
GO

-- ============================================================
-- 10. order_details
-- ============================================================
CREATE TABLE order_details (
    item_id               INT            NOT NULL IDENTITY(1,1),
    order_id              INT            NOT NULL,
    product_id            INT            NOT NULL,
    variant_id            INT            NOT NULL,
    product_name_snapshot NVARCHAR(150)  NOT NULL,
    variant_name_snapshot NVARCHAR(100)  NULL,
    price_snapshot        DECIMAL(10,2)  NOT NULL,
    quantity              INT            NOT NULL,
    item_total            DECIMAL(10,2)  NOT NULL,
    special_note          NVARCHAR(MAX)  NULL,
    item_status           NVARCHAR(15)   NOT NULL DEFAULT 'PENDING',
    cancel_reason         NVARCHAR(MAX)  NULL,

    CONSTRAINT pk_order_details  PRIMARY KEY (item_id),
    CONSTRAINT ck_od_quantity    CHECK (quantity    > 0),
    CONSTRAINT ck_od_status      CHECK (item_status IN ('PENDING','PREPARING','COMPLETED','CANCELLED')),
    CONSTRAINT fk_od_order       FOREIGN KEY (order_id)   REFERENCES orders           (order_id),
    CONSTRAINT fk_od_product     FOREIGN KEY (product_id) REFERENCES products          (product_id),
    CONSTRAINT fk_od_variant     FOREIGN KEY (variant_id) REFERENCES product_variants  (variant_id)
);
GO

-- ============================================================
-- 11. payments
-- ============================================================
CREATE TABLE payments (
    payment_id        INT            NOT NULL IDENTITY(1,1),
    order_id          INT            NOT NULL,
    payment_method_id INT            NOT NULL,
    amount            DECIMAL(10,2)  NOT NULL,
    payment_status    NVARCHAR(10)   NOT NULL DEFAULT 'PENDING', -- 'PENDING'|'SUCCESS'|'FAILED'|'REFUNDED'
    transaction_ref   NVARCHAR(100)  NULL,
    gateway_response  NVARCHAR(MAX)  NULL,
    paid_at           DATETIME2      NULL,
    created_at        DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_payments				PRIMARY KEY (payment_id),
    CONSTRAINT ck_pay_status			CHECK (payment_status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    CONSTRAINT fk_payments_order		FOREIGN KEY (order_id)          REFERENCES orders          (order_id),
    CONSTRAINT fk_payments_payments_method		FOREIGN KEY (payment_method_id) REFERENCES payment_methods (payment_method_id),
	CONSTRAINT uq_payments_order_id 				UNIQUE      (order_id)
);
GO

-- ============================================================
-- 12. reservations
-- ============================================================
CREATE TABLE reservations (
    reservation_id      INT            NOT NULL IDENTITY(1,1),
    customer_id         INT            NOT NULL,   
    cancelled_by        INT            NULL,   -- FK → users, ai hủy
    order_id            INT            NULL,   -- đơn hàng phát sinh từ reservation
    party_size          INT            NOT NULL,
    reservation_date    DATE           NOT NULL,
    reservation_time    TIME           NOT NULL,
    duration_minutes    INT            NOT NULL,
    status              NVARCHAR(15)   NOT NULL DEFAULT 'PENDING',
    cancellation_reason NVARCHAR(MAX)  NULL,
    cancelled_at        DATETIME2      NULL,
    note                NVARCHAR(MAX)  NULL,
    created_at          DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at          DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_reservations          PRIMARY KEY (reservation_id),
    CONSTRAINT ck_res_status            CHECK (status IN ('PENDING','CONFIRMED','ARRIVED','COMPLETED','CANCELLED','NO_SHOW')),
    CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id)  REFERENCES users  (user_id),
    CONSTRAINT fk_reservations_cancel   FOREIGN KEY (cancelled_by) REFERENCES users  (user_id),
    CONSTRAINT fk_reservations_order    FOREIGN KEY (order_id)     REFERENCES orders (order_id)
);
GO

CREATE UNIQUE NONCLUSTERED INDEX uq_reservations_order_id 
ON reservations(order_id) 
WHERE order_id IS NOT NULL;
GO

-- ============================================================
-- 13. reservation_tables  (bảng trung gian: 1 reservation nhiều bàn)
-- ============================================================
CREATE TABLE reservation_tables (
    id             INT NOT NULL IDENTITY(1,1),
    reservation_id INT NOT NULL,
    table_id       INT NOT NULL,

    CONSTRAINT pk_reservation_tables PRIMARY KEY (id),
    CONSTRAINT uq_reservation_table  UNIQUE      (reservation_id, table_id),
    CONSTRAINT fk_rt_reservation     FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id),
    CONSTRAINT fk_rt_table           FOREIGN KEY (table_id)       REFERENCES tables        (table_id)
);
GO

-- ============================================================
-- 14. reservation_deposits
-- ============================================================
CREATE TABLE reservation_deposits (
    deposit_id      INT            NOT NULL IDENTITY(1,1),
    reservation_id  INT            NOT NULL,
    deposit_amount  DECIMAL(10,2)  NOT NULL,
    payment_method_id INT          NULL,
    payment_status  NVARCHAR(15)   NOT NULL DEFAULT 'PENDING',
    transaction_ref NVARCHAR(100)  NULL,
    refund_amount   DECIMAL(10,2)  NOT NULL DEFAULT 0,
    refund_status   NVARCHAR(10)   NOT NULL DEFAULT 'NONE',  -- 'NONE'|'PARTIAL'|'FULL'
    refund_note     NVARCHAR(MAX)  NULL,
    applied_to_order BIT           NOT NULL DEFAULT 0,
    created_at      DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at      DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_reservation_deposits PRIMARY KEY (deposit_id),
    CONSTRAINT uq_deposit_reservation  UNIQUE      (reservation_id),
    CONSTRAINT ck_deposit_pay_status   CHECK (payment_status IN ('PENDING','PAID','REFUNDED','CANCELLED','FORFEITED')),
    CONSTRAINT ck_deposit_refund_status CHECK (refund_status IN ('NONE','PARTIAL','FULL')),
    CONSTRAINT fk_deposit_reservation  FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id),
	CONSTRAINT fk_reservation_deposits_payments_method		FOREIGN KEY (payment_method_id) REFERENCES payment_methods (payment_method_id)
);
GO

-- ============================================================
-- 15. reviews
-- ============================================================
CREATE TABLE reviews (
    review_id     INT            NOT NULL IDENTITY(1,1),
    customer_id   INT            NOT NULL,
    order_id      INT            NOT NULL,
    product_id    INT            NOT NULL,   
    rating        TINYINT        NOT NULL,
    comment       NVARCHAR(MAX)  NULL,
    is_visible    BIT            NOT NULL DEFAULT 1,
    points_earned   INT          NOT NULL DEFAULT 0,
    created_at    DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_reviews          PRIMARY KEY (review_id),
    CONSTRAINT ck_review_rating    CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_review_per_item  UNIQUE      (customer_id, order_id, product_id),
    CONSTRAINT fk_reviews_customer FOREIGN KEY (customer_id) REFERENCES users    (user_id),
    CONSTRAINT fk_reviews_order    FOREIGN KEY (order_id)    REFERENCES orders   (order_id),
    CONSTRAINT fk_reviews_product  FOREIGN KEY (product_id)  REFERENCES products (product_id)
);
GO

-- ============================================================
-- 16. loyalty_points
-- ============================================================
CREATE TABLE loyalty_points (
    point_id         INT            NOT NULL IDENTITY(1,1),
    customer_id      INT            NOT NULL,
    transaction_type NVARCHAR(10)   NOT NULL,   -- 'EARN'|'REDEEM'|'ADJUST'
    points           INT            NOT NULL,   -- dương = cộng, âm = trừ
    balance_after    INT            NOT NULL,
    reference_type   NVARCHAR(10)   NULL,       -- 'ORDER'|'REVIEW'
    reference_id     INT            NULL,       -- order_id hoặc review_id
    note             NVARCHAR(255)  NULL,
    created_at       DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_loyalty_points    PRIMARY KEY (point_id),
    CONSTRAINT ck_lp_trans_type     CHECK (transaction_type IN ('EARN','REDEEM', 'ADJUST')),
    CONSTRAINT ck_lp_ref_type       CHECK (reference_type   IN ('ORDER','REVIEW')),
    CONSTRAINT fk_lp_customer       FOREIGN KEY (customer_id) REFERENCES users (user_id)
);
GO

-- ============================================================
-- 17. policies  (chính sách tích điểm / giảm giá)
-- ============================================================
CREATE TABLE policies (
    policy_id      INT            NOT NULL IDENTITY(1,1),
    comment        NVARCHAR(MAX)  NULL,
    currency_value DECIMAL(10,2)  NOT NULL DEFAULT 0,
    unit           NVARCHAR(50)   NULL,
    status         BIT				NOT NULL DEFAULT 1,  -- 'ACTIVE'|'INACTIVE'

	action_type	   NVARCHAR(15)   NOT NULL,		-- 'DISCOUNT'|'ORDER'|'REVIEW'
	policy_type    NVARCHAR(15)   NOT NULL,		-- 'EARN'|'REDEEM'
	policy_name    NVARCHAR(150)   NOT NULL,
	created_at    DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at    DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_policies   PRIMARY KEY (policy_id),
    CONSTRAINT ck_pol_action_type CHECK (action_type IN ('DISCOUNT','ORDER','REVIEW')),
	CONSTRAINT ck_pol_policy_type CHECK (policy_type IN ('EARN','REDEEM'))
);
GO

-- ============================================================
-- 18. map  (sơ đồ mặt bằng quán)
-- ============================================================
CREATE TABLE map (
    map_id   INT            NOT NULL IDENTITY(1,1),
    map_name NVARCHAR(100)  NOT NULL,
    url_map  NVARCHAR(255)  NOT NULL,

    CONSTRAINT pk_map PRIMARY KEY (map_id)
);
GO

-- ============================================================
-- 19. system_logs
-- ============================================================
CREATE TABLE system_logs (
    log_id      INT            NOT NULL IDENTITY(1,1),
    user_id     INT            NULL,           -- NULL nếu hành động hệ thống
    action      NVARCHAR(100)  NOT NULL,
    target_type NVARCHAR(50)   NULL,           -- tên bảng bị tác động
    target_id   INT            NULL,           -- id bản ghi bị tác động
    description NVARCHAR(MAX)  NULL,
    ip_address  NVARCHAR(45)   NULL,
    created_at  DATETIME2      NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT pk_system_logs PRIMARY KEY (log_id),
    CONSTRAINT fk_logs_user   FOREIGN KEY (user_id) REFERENCES users (user_id)
);
GO

-- ============================================================
-- 21. carts  (giỏ hàng — 1 customer 1 giỏ active)
-- ============================================================
CREATE TABLE carts (
    cart_id     INT       NOT NULL IDENTITY(1,1),
    customer_id INT       NOT NULL,
    created_at  DATETIME2 NOT NULL DEFAULT SYSDATETIME()
 
    CONSTRAINT pk_carts         PRIMARY KEY (cart_id),
    CONSTRAINT uq_carts_user    UNIQUE      (customer_id),   -- 1 user 1 giỏ
    CONSTRAINT fk_carts_user    FOREIGN KEY (customer_id) REFERENCES users (user_id)
);
GO

-- ============================================================
-- 22. cart_items  (các món trong giỏ hàng)
-- ============================================================
CREATE TABLE cart_items (
    cart_item_id INT            NOT NULL IDENTITY(1,1),
    cart_id      INT            NOT NULL,
    product_id   INT            NOT NULL,
    variant_id   INT            NOT NULL,
    quantity     INT            NOT NULL DEFAULT 1,
    special_note NVARCHAR(MAX)  NULL,       -- ít đường, không đá, v.v.
    added_at     DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
 
    CONSTRAINT pk_cart_items         PRIMARY KEY (cart_item_id),
    CONSTRAINT uq_cart_variant       UNIQUE      (cart_id, variant_id),  -- 1 variant 1 dòng, tăng qty thay vì thêm dòng mới
    CONSTRAINT ck_cart_item_qty      CHECK       (quantity > 0),
    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES carts            (cart_id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products         (product_id),
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants (variant_id)
);
GO

-- ============================================================
-- 23. system_configs (Cấu hình hệ thống)
-- ============================================================
CREATE TABLE system_configs (
    config_id INT IDENTITY(1,1) PRIMARY KEY,
    config_key NVARCHAR(100) NOT NULL UNIQUE,
    config_value NVARCHAR(MAX) NULL,
    config_group NVARCHAR(50) NOT NULL DEFAULT 'general',
    is_active BIT NOT NULL DEFAULT 1,
    description NVARCHAR(500) NULL,
    updated_by INT NULL,  -- FK → users (ai cập nhật cuối)
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT fk_configs_updated_by FOREIGN KEY (updated_by) REFERENCES users(user_id)
);
GO

-- Tạo trigger cập nhật updated_at
CREATE TRIGGER trg_system_configs_updated_at
ON system_configs AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE system_configs 
    SET updated_at = SYSDATETIME()
    WHERE config_id IN (SELECT config_id FROM inserted);
END;
GO


-- ============================================================
-- TRIGGERS: tự động cập nhật updated_at
-- ============================================================
CREATE TRIGGER trg_users_updated_at
ON users AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE users SET updated_at = SYSDATETIME()
    WHERE user_id IN (SELECT user_id FROM inserted);
END;
GO

CREATE TRIGGER trg_products_updated_at
ON products AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE products SET updated_at = SYSDATETIME()
    WHERE product_id IN (SELECT product_id FROM inserted);
END;
GO

CREATE TRIGGER trg_orders_updated_at
ON orders AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE orders SET updated_at = SYSDATETIME()
    WHERE order_id IN (SELECT order_id FROM inserted);
END;
GO

CREATE TRIGGER trg_reservations_updated_at
ON reservations AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE reservations SET updated_at = SYSDATETIME()
    WHERE reservation_id IN (SELECT reservation_id FROM inserted);
END;
GO

CREATE TRIGGER trg_reservation_deposits_updated_at
ON reservation_deposits AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE reservation_deposits SET updated_at = SYSDATETIME()
    WHERE deposit_id IN (SELECT deposit_id FROM inserted);
END;
GO

CREATE TRIGGER trg_policies_updated_at
ON policies AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE policies SET updated_at = SYSDATETIME()
    WHERE policy_id IN (SELECT policy_id FROM inserted);
END;
GO

CREATE TRIGGER trg_customer_addresses_updated_at
ON customer_addresses AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE customer_addresses
    SET    updated_at = SYSDATETIME()
    WHERE  address_id IN (SELECT address_id FROM inserted);
END;
GO
 
CREATE TRIGGER trg_cart_items_updated_at
ON cart_items AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE cart_items
    SET    updated_at = SYSDATETIME()
    WHERE  cart_item_id IN (SELECT cart_item_id FROM inserted);
END;
GO

-- ============================================================
-- SEED DATA: roles & payment_methods
-- ============================================================
-- 1. Bảng ROLES (Phân quyền)
INSERT INTO roles (role_name) VALUES
(N'Admin'), (N'Manager'), (N'Cashier'), (N'Barista'), (N'Customer');
GO

-- 2. Bảng USERS (Người dùng - Giả định status 1 là Active)
INSERT INTO users (first_name, last_name, username, email, phone, password_hash, status, avatar_url, role_id) VALUES
(N'Nguyễn', N'Văn A', 'User1', 'nguyenvana@gmail.com', '0901234561', 'hashed_pw_1', 1, 'avatar1.png', 1),
(N'Trần', N'Thị B', 'User2', 'tranthib@gmail.com', '0901234562', 'hashed_pw_2', 1, 'avatar2.png', 2),
(N'Lê', N'Văn C', 'User3', 'levanc@gmail.com', '0901234563', 'hashed_pw_3', 1, 'avatar3.png',3 ),
(N'Phạm', N'Thị D', 'User4', 'phamthid@gmail.com', '0901234564', 'hashed_pw_4', 1, 'avatar4.png', 4),
(N'Hoàng', N'Văn E', 'User5', 'hoangvane@gmail.com', '0901234565', 'hashed_pw_5', 0, 'avatar5.png', 5),
(N'Đỗ', N'Thị F', 'User6', 'dothif@gmail.com', '0901234566', 'hashed_pw_6', 1, 'avatar6.png', 1),
(N'Vũ', N'Văn G', 'User7', 'vuvang@gmail.com', '0901234567', 'hashed_pw_7', 1, 'avatar7.png', 2),
(N'Ngô', N'Thị H', 'User8', 'ngothih@gmail.com', '0901234568', 'hashed_pw_8', 1, 'avatar8.png', 3),
(N'Bùi', N'Văn I', 'User9', 'buivani@gmail.com', '0901234569', 'hashed_pw_9', 1, 'avatar9.png', 4),
(N'Đặng', N'Thị K', 'User10', 'dangthik@gmail.com', '0901234570', 'hashed_pw_10', 1, 'avatar10.png', 5),
(N'Đặng', N'Thị K', 'User11', 'namvipnhatgt@gmail.com', '0901294570', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, 'avatar10.png', 5),
(N'Phan', N'Văn L', 'User12', 'phanvanl@gmail.com', '0901234571', 'hashed_pw_11', 1, 'avatar11.png', 5),
(N'Trịnh', N'Thị M', 'User13', 'trinhthim@gmail.com', '0901234572', 'hashed_pw_12', 1, 'avatar12.png', 5),
(N'Lý', N'Văn N', 'User14', 'lyvann@gmail.com', '0901234573', 'hashed_pw_13', 1, 'avatar13.png', 5),
(N'Dương', N'Thị O', 'User15', 'duongthio@gmail.com', '0901234574', 'hashed_pw_14', 1, 'avatar14.png', 5),
(N'Mai', N'Văn P', 'User16', 'maivanp@gmail.com', '0901234575', 'hashed_pw_15', 0, 'avatar15.png', 5),
(N'Đinh', N'Thị Q', 'User17', 'dinhthiq@gmail.com', '0901234576', 'hashed_pw_16', 1, 'avatar16.png', 5),
(N'Tô', N'Văn R', 'User18', 'tovanr@gmail.com', '0901234577', 'hashed_pw_17', 1, 'avatar17.png', 5),
(N'Chu', N'Thị S', 'User19', 'chuthis@gmail.com', '0901234578', 'hashed_pw_18', 1, 'avatar18.png', 5),
(N'Lâm', N'Văn T', 'User20', 'lamvant@gmail.com', '0901234579', 'hashed_pw_19', 1, 'avatar19.png', 5),
(N'Kiều', N'Thị U', 'User21', 'kieuthiu@gmail.com', '0901234580', 'hashed_pw_20', 1, 'avatar20.png', 5);
GO

-- 3. Bảng CATEGORIES (Danh mục sản phẩm)
INSERT INTO categories (name, is_active) VALUES
(N'Cà phê pha máy', 1), (N'Cà phê truyền thống', 1), 
(N'Trà trái cây', 1), (N'Trà sữa', 1), 
(N'Đá xay (Frappuccino)', 1), (N'Bánh ngọt', 1), 
(N'Bánh mặn', 1), (N'Nước ép tươi', 1), 
(N'Sữa chua', 1), (N'Hạt cà phê / Merchandise', 1);
GO


-- ================================================================

-- THẾ HIẾU - DÒNG 1
-- ============================================================
-- SẢN PHẨM CÀ PHÊ
-- product_id từ 1 đến 6
-- ============================================================

-- Sản phẩm 1: Espresso (product_id = 1)
INSERT INTO products (category_id, name, description, is_active) VALUES
(1, N'Espresso', N'Cà phê espresso đậm đà, nguyên chất với lớp crema thơm mịn', 1);

-- Sản phẩm 2: Cappuccino (product_id = 2)
INSERT INTO products (category_id, name, description, is_active) VALUES
(1, N'Cappuccino', N'Sự kết hợp cân bằng giữa espresso, sữa nóng và lớp bọt sữa béo mịn', 1);

-- Sản phẩm 3: Iced Latte (product_id = 3)
INSERT INTO products (category_id, name, description, is_active) VALUES
(1, N'Iced Latte', N'Espresso hòa quyện cùng sữa tươi và đá, mang hương vị nhẹ nhàng, mát lạnh', 1);

-- Sản phẩm 4: Cà Phê Đen (product_id = 4)
INSERT INTO products (category_id, name, description, is_active) VALUES
(2, N'Cà Phê Đen', N'Cà phê phin truyền thống đậm đà, nguyên chất và thơm nồng', 1);

-- Sản phẩm 5: Cà Phê Sữa Đá (product_id = 5)
INSERT INTO products (category_id, name, description, is_active) VALUES
(2, N'Cà Phê Sữa Đá', N'Hài hòa giữa vị đắng của cà phê và vị béo ngọt của sữa đặc', 1);

-- Sản phẩm 6: Bạc Xỉu (product_id = 6)
INSERT INTO products (category_id, name, description, is_active) VALUES
(2, N'Bạc Xỉu', N'Vị cà phê nhẹ nhàng hòa cùng sữa đặc béo ngậy, phù hợp với người thích vị ngọt', 1);


-- ============================================================
-- BIẾN THỂ CHO ESPRESSO (product_id = 1)
-- variant_id bắt đầu từ 1
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(1, N'Espresso Size S', 'S', 30000, 'HOT', 1),  -- variant_id = 1
(1, N'Espresso Size M', 'M', 40000, 'HOT', 1),  -- variant_id = 2
(1, N'Espresso Size L', 'L', 50000, 'HOT', 1);  -- variant_id = 3

-- ============================================================
-- BIẾN THỂ CHO CAPPUCCINO (product_id = 2)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(2, N'Cappuccino Size S', 'S', 40000, 'HOT', 1),  -- variant_id = 4
(2, N'Cappuccino Size M', 'M', 50000, 'HOT', 1),  -- variant_id = 5
(2, N'Cappuccino Size L', 'L', 60000, 'HOT', 1);  -- variant_id = 6

-- ============================================================
-- BIẾN THỂ CHO ICED LATTE (product_id = 3)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(3, N'Iced Latte Size S', 'S', 45000, 'COLD', 1),  -- variant_id = 7
(3, N'Iced Latte Size M', 'M', 55000, 'COLD', 1),  -- variant_id = 8
(3, N'Iced Latte Size L', 'L', 65000, 'COLD', 1);  -- variant_id = 9

-- ============================================================
-- BIẾN THỂ CHO CÀ PHÊ ĐEN (product_id = 4)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(4, N'Cà Phê Đen Size S', 'S', 25000, 'HOT', 1),  -- variant_id = 10
(4, N'Cà Phê Đen Size M', 'M', 35000, 'HOT', 1),  -- variant_id = 11
(4, N'Cà Phê Đen Size L', 'L', 45000, 'HOT', 1);  -- variant_id = 12

-- ============================================================
-- BIẾN THỂ CHO CÀ PHÊ SỮA ĐÁ (product_id = 5)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(5, N'Cà Phê Sữa Đá Size S', 'S', 30000, 'COLD', 1),  -- variant_id = 13
(5, N'Cà Phê Sữa Đá Size M', 'M', 40000, 'COLD', 1),  -- variant_id = 14
(5, N'Cà Phê Sữa Đá Size L', 'L', 50000, 'COLD', 1);  -- variant_id = 15

-- ============================================================
-- BIẾN THỂ CHO BẠC XỈU (product_id = 6)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(6, N'Bạc Xỉu Size S', 'S', 30000, 'HOT', 1),  -- variant_id = 16
(6, N'Bạc Xỉu Size M', 'M', 40000, 'HOT', 1),  -- variant_id = 17
(6, N'Bạc Xỉu Size L', 'L', 50000, 'HOT', 1);  -- variant_id = 18


-- ============================================================
-- HÌNH ẢNH CHO ESPRESSO (product_id = 1)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(1, 'images/products/EspressoS.png', 1, 1),
(1, 'images/products/EspressoM.png', 0, 2),
(1, 'images/products/EspressoL.png', 0, 3);

-- ============================================================
-- HÌNH ẢNH CHO CAPPUCCINO (product_id = 2)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(2, 'images/products/CappuccinoS.png', 1, 4),
(2, 'images/products/CappuccinoM.png', 0, 5),
(2, 'images/products/CappuccinoL.png', 0, 6);

-- ============================================================
-- HÌNH ẢNH CHO ICED LATTE (product_id = 3)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(3, 'images/products/IcedLatteS.png', 1, 7),
(3, 'images/products/IcedLatteM.png', 0, 8),
(3, 'images/products/IcedLatteL.png', 0, 9);

-- ============================================================
-- HÌNH ẢNH CHO CÀ PHÊ ĐEN (product_id = 4)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(4, 'images/products/CaphedenS.png', 1, 10),
(4, 'images/products/CaphedenM.png', 0, 11),
(4, 'images/products/CaphedenL.png', 0, 12);

-- ============================================================
-- HÌNH ẢNH CHO CÀ PHÊ SỮA ĐÁ (product_id = 5)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(5, 'images/products/CaphesuadaS.png', 1, 13),
(5, 'images/products/CaphesuadaM.png', 0, 14),
(5, 'images/products/CaphesuadaL.png', 0, 15);

-- ============================================================
-- HÌNH ẢNH CHO BẠC XỈU (product_id = 6)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(6, 'images/products/BacxiuS.png', 1, 16),
(6, 'images/products/BacxiuM.png', 0, 17),
(6, 'images/products/BacxiuL.png', 0, 18);



-- TRUNG HIẾU - DÒNG 2
-- ============================================================
-- SẢN PHẨM CHO DANH MỤC TRÀ VÀ TRÀ SỮA
-- product_id từ 7 đến 12
-- ============================================================

-- Sản phẩm 7: Trà Đào Cam Sả (product_id = 7)
INSERT INTO products (category_id, name, description, is_active) VALUES
(3, N'Trà Đào Cam Sả', N'Trà đào cam sả thanh mát với những lát đào tươi, cam vàng và sả nồng nàn', 1);

-- Sản phẩm 8: Trà Ổi Hồng (product_id = 8)
INSERT INTO products (category_id, name, description, is_active) VALUES
(3, N'Trà Ổi Hồng', N'Trà ổi hồng chua ngọt thanh nhẹ, giải nhiệt mùa hè', 1);

-- Sản phẩm 9: Trà Xoài Chanh Dây (product_id = 9)
INSERT INTO products (category_id, name, description, is_active) VALUES
(3, N'Trà Xoài Chanh Dây', N'Sự hòa quyện tuyệt vời giữa mứt xoài ngọt lịm và chanh dây chua thanh', 1);

-- Sản phẩm 10: Trà Sữa Trân Châu Đường Đen (product_id = 10)
INSERT INTO products (category_id, name, description, is_active) VALUES
(4, N'Trà Sữa Trân Châu Đường Đen', N'Trà sữa béo ngậy kết hợp cùng trân châu đường đen dẻo dai đậm vị', 1);

-- Sản phẩm 11: Trà Sữa Truyền Thống (product_id = 11)
INSERT INTO products (category_id, name, description, is_active) VALUES
(4, N'Trà Sữa Truyền Thống', N'Hương vị trà sữa nguyên bản, sự kết hợp hoàn hảo giữa hồng trà và sữa', 1);

-- Sản phẩm 12: Trà Thái Xanh (product_id = 12)
INSERT INTO products (category_id, name, description, is_active) VALUES
(4, N'Trà Thái Xanh', N'Trà sữa Thái xanh thơm mát đặc trưng, màu sắc bắt mắt', 1);
GO


-- ============================================================
-- BIẾN THỂ CHO TRÀ ĐÀO CAM SẢ (product_id = 7)
-- variant_id bắt đầu từ 19
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(7, N'Trà Đào Cam Sả Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 19
(7, N'Trà Đào Cam Sả Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 20
(7, N'Trà Đào Cam Sả Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 21

-- ============================================================
-- BIẾN THỂ CHO TRÀ ỔI HỒNG (product_id = 8)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(8, N'Trà Ổi Hồng Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 22
(8, N'Trà Ổi Hồng Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 23
(8, N'Trà Ổi Hồng Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 24

-- ============================================================
-- BIẾN THỂ CHO TRÀ XOÀI CHANH DÂY (product_id = 9)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(9, N'Trà Xoài Chanh Dây Size S', 'S', 39000, 'COLD', 1),  -- variant_id = 25
(9, N'Trà Xoài Chanh Dây Size M', 'M', 49000, 'COLD', 1),  -- variant_id = 26
(9, N'Trà Xoài Chanh Dây Size L', 'L', 59000, 'COLD', 1);  -- variant_id = 27

-- ============================================================
-- BIẾN THỂ CHO TRÀ SỮA TRÂN CHÂU ĐƯỜNG ĐEN (product_id = 10)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(10, N'Trà Sữa Trân Châu Đường Đen Size S', 'S', 45000, 'COLD', 1),  -- variant_id = 28
(10, N'Trà Sữa Trân Châu Đường Đen Size M', 'M', 55000, 'COLD', 1),  -- variant_id = 29
(10, N'Trà Sữa Trân Châu Đường Đen Size L', 'L', 65000, 'COLD', 1);  -- variant_id = 30

-- ============================================================
-- BIẾN THỂ CHO TRÀ SỮA TRUYỀN THỐNG (product_id = 11)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(11, N'Trà Sữa Truyền Thống Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 31
(11, N'Trà Sữa Truyền Thống Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 32
(11, N'Trà Sữa Truyền Thống Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 33

-- ============================================================
-- BIẾN THỂ CHO TRÀ THÁI XANH (product_id = 12)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(12, N'Trà Thái Xanh Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 34
(12, N'Trà Thái Xanh Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 35
(12, N'Trà Thái Xanh Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 36
GO


-- ============================================================
-- HÌNH ẢNH CHO TRÀ ĐÀO CAM SẢ (product_id = 7)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(7, 'images/products/tradaocamxaS.jpg', 1, 19),
(7, 'images/products/tradaocamsaM.jpg', 0, 20),
(7, 'images/products/tradaocamXaL.jpg', 0, 21);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ ỔI HỒNG (product_id = 8)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(8, 'images/products/traoihongS.webp', 1, 22),
(8, 'images/products/traoihongM.jpg', 0, 23),
(8, 'images/products/traoihongsizeL.webp', 0, 24);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ XOÀI CHANH DÂY (product_id = 9)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(9, 'images/products/trasoaichanhdaysizeS.jpg', 1, 25),
(9, 'images/products/trasoaichanhdaysizeM.jpg', 0, 26),
(9, 'images/products/trasoaichanhdaysizeL.jpg', 0, 27);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ SỮA TRÂN CHÂU ĐƯỜNG ĐEN (product_id = 10)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(10, 'images/products/trasuachanchauduongdenS.webp', 1, 28),
(10, 'images/products/trasuatranchauduongdenM.jpg', 0, 29),
(10, 'images/products/trasuachanchauduongdenL.jpg', 0, 30);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ SỮA TRUYỀN THỐNG (product_id = 11)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(11, 'images/products/trasuatruyenthongsizeS.webp', 1, 31),
(11, 'images/products/trasuatruyenthongM.jpg', 0, 32),
(11, 'images/products/trasuatruyenthongsizeL.webp', 0, 33);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ THÁI XANH (product_id = 12)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(12, 'images/products/trathaixanhS.jpg', 1, 34),
(12, 'images/products/trathaixxanhM.webp', 0, 35),
(12, 'images/products/trathaixanhL.jpg', 0, 36);
GO

 
-- GIANG - DÒNG 3
-- ============================================================  
-- SẢN PHẨM CHO DANH MỤC ĐÁ XAY (FRAPPUCCINO) - category_id = 5
-- product_id bắt đầu từ 13
-- ============================================================

-- Sản phẩm 1: Caramel Frappuccino (product_id = 13)
INSERT INTO products (category_id, name, description, is_active) VALUES
(5, N'Caramel Frappuccino', N'Đá xay caramel thơm ngon với lớp sốt caramel béo ngậy, kem tươi và siro caramel', 1);

-- Sản phẩm 2: Mocha Frappuccino (product_id = 14)
INSERT INTO products (category_id, name, description, is_active) VALUES
(5, N'Mocha Frappuccino', N'Đá xay mocha với hương vị socola đậm đà kết hợp cùng cà phê espresso', 1);

-- Sản phẩm 3: Strawberry Cream Frappuccino (product_id = 15)
INSERT INTO products (category_id, name, description, is_active) VALUES
(5, N'Strawberry Cream Frappuccino', N'Đá xay kem dâu tây mát lạnh với vị trái cây tươi ngon', 1);
GO
-- ============================================================
-- BIẾN THỂ CHO CARAMEL FRAPPUCCINO (product_id = 13)
-- variant_id: giả sử 2 người trước dùng hết variant_id từ 1-36 (12 sp × 3 variants)
-- nên bắt đầu từ variant_id = 37
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(13, N'Caramel Frappuccino Size S', 'S', 45000, 'COLD', 1),  -- variant_id = 37
(13, N'Caramel Frappuccino Size M', 'M', 55000, 'COLD', 1),  -- variant_id = 38
(13, N'Caramel Frappuccino Size L', 'L', 65000, 'COLD', 1);  -- variant_id = 39

-- ============================================================
-- BIẾN THỂ CHO MOCHA FRAPPUCCINO (product_id = 14)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(14, N'Mocha Frappuccino Size S', 'S', 49000, 'COLD', 1),  -- variant_id = 40
(14, N'Mocha Frappuccino Size M', 'M', 59000, 'COLD', 1),  -- variant_id = 41
(14, N'Mocha Frappuccino Size L', 'L', 69000, 'COLD', 1);  -- variant_id = 42

-- ============================================================
-- BIẾN THỂ CHO STRAWBERRY CREAM FRAPPUCCINO (product_id = 15)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(15, N'Strawberry Cream Frappuccino Size S', 'S', 47000, 'COLD', 1),  -- variant_id = 43
(15, N'Strawberry Cream Frappuccino Size M', 'M', 57000, 'COLD', 1),  -- variant_id = 44
(15, N'Strawberry Cream Frappuccino Size L', 'L', 67000, 'COLD', 1);  -- variant_id = 45
GO

-- ============================================================
-- SẢN PHẨM CHO DANH MỤC BÁNH NGỌT - category_id = 6
-- product_id bắt đầu từ 16
-- ============================================================

-- Sản phẩm 4: Bánh Tiramisu (product_id = 16)
INSERT INTO products (category_id, name, description, is_active) VALUES
(6, N'Bánh Tiramisu', N'Bánh Tiramisu Ý với lớp kem mascarpone mịn màng, bánh xốp thấm cà phê và lớp bột cacao', 1);

-- Sản phẩm 5: Bánh Cheesecake (product_id = 17)
INSERT INTO products (category_id, name, description, is_active) VALUES
(6, N'Bánh Cheesecake', N'Bánh phô mai New York với lớp vỏ bánh quy giòn, nhân phô mai béo ngậy và sốt trái cây', 1);

-- Sản phẩm 6: Bánh Socola (product_id = 18)
INSERT INTO products (category_id, name, description, is_active) VALUES
(6, N'Bánh Socola', N'Bánh socola đen với nhân ganache mịn, phủ lớp kem socola và hạt dẻ rang', 1);
GO

-- ============================================================
-- BIẾN THỂ CHO BÁNH TIRAMISU (product_id = 16)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(16, N'Bánh Tiramisu - Phần lẻ', NULL, 59000, 'ROOM', 1),  -- variant_id = 46
(16, N'Bánh Tiramisu - Cả bánh 20cm', NULL, 290000, 'ROOM', 1),  -- variant_id = 47
(16, N'Bánh Tiramisu - Cả bánh 25cm', NULL, 390000, 'ROOM', 1);  -- variant_id = 48

-- ============================================================
-- BIẾN THỂ CHO BÁNH CHEESECAKE (product_id = 17)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(17, N'Bánh Cheesecake - Phần lẻ', NULL, 65000, 'COLD', 1),  -- variant_id = 49
(17, N'Bánh Cheesecake - Cả bánh 20cm', NULL, 320000, 'COLD', 1),  -- variant_id = 50
(17, N'Bánh Cheesecake - Cả bánh 25cm', NULL, 420000, 'COLD', 1);  -- variant_id = 51

-- ============================================================
-- BIẾN THỂ CHO BÁNH SOCOLA (product_id = 18)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(18, N'Bánh Socola - Phần lẻ', NULL, 55000, 'ROOM', 1),  -- variant_id = 52
(18, N'Bánh Socola - Cả bánh 20cm', NULL, 280000, 'ROOM', 1),  -- variant_id = 53
(18, N'Bánh Socola - Cả bánh 25cm', NULL, 380000, 'ROOM', 1);  -- variant_id = 54
GO

-- ============================================================
-- HÌNH ẢNH CHO ĐÁ XAY - CARAMEL FRAPPUCCINO (product_id = 13)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(13, 'images/products/caramel-frappuccino-main.png', 1, 37),
(13, 'images/products/caramel-frappuccino-side.png', 0, 38),
(13, 'images/products/caramel-frappuccino-top.png', 0, 39);

-- ============================================================
-- HÌNH ẢNH CHO ĐÁ XAY - MOCHA FRAPPUCCINO (product_id = 14)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(14, 'images/products/mocha-frappuccino-main.png', 1, 40),
(14, 'images/products/mocha-frappuccino-detail.png', 0, 41),
(14, 'images/products/mocha-frappuccino-whipped.png', 0, 42);

-- ============================================================
-- HÌNH ẢNH CHO ĐÁ XAY - STRAWBERRY CREAM (product_id = 15)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(15, 'images/products/strawberry-cream-main.png', 1, 43),
(15, 'images/products/strawberry-cream-pink.png', 0, 44),
(15, 'images/products/strawberry-cream-fresh.png', 0, 45);

-- ============================================================
-- HÌNH ẢNH CHO BÁNH NGỌT - TIRAMISU (product_id = 16)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(16, 'images/products/tiramisu-main.png', 1, 46),
(16, 'images/products/tiramisu-slice.png', 0, 47),
(16, 'images/products/tiramisu-whole.png', 0, 48);

-- ============================================================
-- HÌNH ẢNH CHO BÁNH NGỌT - CHEESECAKE (product_id = 17)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(17, 'images/products/cheesecake-main.png', 1, 49),
(17, 'images/products/cheesecake-berry.png', 0, 50),
(17, 'images/products/cheesecake-whole.png', 0, 51);

-- ============================================================
-- HÌNH ẢNH CHO BÁNH NGỌT - SOCOLA (product_id = 18)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(18, 'images/products/socola-main.png', 1, 52),
(18, 'images/products/socola-layer.png', 0, 53),
(18, 'images/products/socola-whole.png', 0, 54);
GO


-- NAM - DÒNG 4
-- ============================================================  
-- SẢN PHẨM CHO DANH MỤC BÁNH MẶN - category_id = 7
-- product_id bắt đầu từ 19
-- ============================================================

-- Sản phẩm 1: Bánh Mì Que Pate (product_id = 19)
INSERT INTO products (category_id, name, description, is_active) VALUES
(7, N'Bánh Mì Que Pate', N'Bánh mì que giòn rụm nhân pate béo ngậy, ăn kèm tương ớt đặc trưng', 1);

-- Sản phẩm 2: Bánh Patê Sô (product_id = 20)
INSERT INTO products (category_id, name, description, is_active) VALUES
(7, N'Bánh Patê Sô', N'Bánh patê sô vỏ phồng nhiều lớp giòn xốp, nhân thịt bằm và patê đậm đà', 1);

-- Sản phẩm 3: Bánh Mì Xúc Xích Phô Mai (product_id = 21)
INSERT INTO products (category_id, name, description, is_active) VALUES
(7, N'Bánh Mì Xúc Xích Phô Mai', N'Bánh mì mềm nhân xúc xích và phô mai tan chảy, nướng nóng thơm phức', 1);
GO

-- ============================================================
-- BIẾN THỂ CHO BÁNH MÌ QUE PATE (product_id = 19)
-- variant_id: bắt đầu từ 55
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(19, N'Bánh Mì Que Pate - 1 cái', NULL, 15000, 'ROOM', 1),   -- variant_id = 55
(19, N'Bánh Mì Que Pate - Set 2 cái', NULL, 28000, 'ROOM', 1), -- variant_id = 56
(19, N'Bánh Mì Que Pate - Set 4 cái', NULL, 52000, 'ROOM', 1); -- variant_id = 57

-- ============================================================
-- BIẾN THỂ CHO BÁNH PATÊ SÔ (product_id = 20)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(20, N'Bánh Patê Sô - 1 cái', NULL, 18000, 'ROOM', 1),   -- variant_id = 58
(20, N'Bánh Patê Sô - Set 2 cái', NULL, 34000, 'ROOM', 1), -- variant_id = 59
(20, N'Bánh Patê Sô - Set 4 cái', NULL, 64000, 'ROOM', 1); -- variant_id = 60

-- ============================================================
-- BIẾN THỂ CHO BÁNH MÌ XÚC XÍCH PHÔ MAI (product_id = 21)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(21, N'Bánh Mì Xúc Xích Phô Mai - 1 cái', NULL, 25000, 'ROOM', 1),   -- variant_id = 61
(21, N'Bánh Mì Xúc Xích Phô Mai - Set 2 cái', NULL, 47000, 'ROOM', 1), -- variant_id = 62
(21, N'Bánh Mì Xúc Xích Phô Mai - Set 4 cái', NULL, 88000, 'ROOM', 1); -- variant_id = 63
GO

-- ============================================================
-- SẢN PHẨM CHO DANH MỤC NƯỚC ÉP TƯƠI - category_id = 8
-- product_id bắt đầu từ 22
-- ============================================================

-- Sản phẩm 4: Nước Cam Ép (product_id = 22)
INSERT INTO products (category_id, name, description, is_active) VALUES
(8, N'Nước Cam Ép', N'Nước cam vắt nguyên chất 100%, vị chua ngọt tự nhiên, giàu vitamin C', 1);

-- Sản phẩm 5: Nước Ép Dưa Hấu (product_id = 23)
INSERT INTO products (category_id, name, description, is_active) VALUES
(8, N'Nước Ép Dưa Hấu', N'Nước ép dưa hấu tươi mát, ngọt thanh tự nhiên, giải khát tức thì', 1);

-- Sản phẩm 6: Nước Ép Ổi (product_id = 24)
INSERT INTO products (category_id, name, description, is_active) VALUES
(8, N'Nước Ép Ổi', N'Nước ép ổi hồng tươi, vị chua ngọt hài hòa, giàu chất xơ và vitamin', 1);
GO

-- ============================================================
-- BIẾN THỂ CHO NƯỚC CAM ÉP (product_id = 22)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(22, N'Nước Cam Ép Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 64
(22, N'Nước Cam Ép Size M', 'M', 42000, 'COLD', 1),  -- variant_id = 65
(22, N'Nước Cam Ép Size L', 'L', 49000, 'COLD', 1);  -- variant_id = 66

-- ============================================================
-- BIẾN THỂ CHO NƯỚC ÉP DƯA HẤU (product_id = 23)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(23, N'Nước Ép Dưa Hấu Size S', 'S', 33000, 'COLD', 1),  -- variant_id = 67
(23, N'Nước Ép Dưa Hấu Size M', 'M', 40000, 'COLD', 1),  -- variant_id = 68
(23, N'Nước Ép Dưa Hấu Size L', 'L', 47000, 'COLD', 1);  -- variant_id = 69

-- ============================================================
-- BIẾN THỂ CHO NƯỚC ÉP ỔI (product_id = 24)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(24, N'Nước Ép Ổi Size S', 'S', 34000, 'COLD', 1),  -- variant_id = 70
(24, N'Nước Ép Ổi Size M', 'M', 41000, 'COLD', 1),  -- variant_id = 71
(24, N'Nước Ép Ổi Size L', 'L', 48000, 'COLD', 1);  -- variant_id = 72
GO

-- ============================================================
-- HÌNH ẢNH CHO BÁNH MẶN - BÁNH MÌ QUE PATE (product_id = 19)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(19, 'images/products/banh-mi-que-pate-main.png', 1, 55),
(19, 'images/products/banh-mi-que-pate-set2.png', 0, 56),
(19, 'images/products/banh-mi-que-pate-set4.png', 0, 57);

-- ============================================================
-- HÌNH ẢNH CHO BÁNH MẶN - BÁNH PATÊ SÔ (product_id = 20)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(20, 'images/products/banh-pate-so-main.png', 1, 58),
(20, 'images/products/banh-pate-so-set2.png', 0, 59),
(20, 'images/products/banh-pate-so-set4.png', 0, 60);

-- ============================================================
-- HÌNH ẢNH CHO BÁNH MẶN - BÁNH MÌ XÚC XÍCH PHÔ MAI (product_id = 21)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(21, 'images/products/banh-mi-xuc-xich-pho-mai-main.png', 1, 61),
(21, 'images/products/banh-mi-xuc-xich-pho-mai-set2.png', 0, 62),
(21, 'images/products/banh-mi-xuc-xich-pho-mai-set4.png', 0, 63);

-- ============================================================
-- HÌNH ẢNH CHO NƯỚC ÉP TƯƠI - NƯỚC CAM ÉP (product_id = 22)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(22, 'images/products/nuoc-cam-ep-main.png', 1, 64),
(22, 'images/products/nuoc-cam-ep-side.png', 0, 65),
(22, 'images/products/nuoc-cam-ep-top.png', 0, 66);

-- ============================================================
-- HÌNH ẢNH CHO NƯỚC ÉP TƯƠI - NƯỚC ÉP DƯA HẤU (product_id = 23)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(23, 'images/products/nuoc-ep-dua-hau-main.png', 1, 67),
(23, 'images/products/nuoc-ep-dua-hau-side.png', 0, 68),
(23, 'images/products/nuoc-ep-dua-hau-top.png', 0, 69);

-- ============================================================
-- HÌNH ẢNH CHO NƯỚC ÉP TƯƠI - NƯỚC ÉP ỔI (product_id = 24)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(24, 'images/products/nuoc-ep-oi-main.png', 1, 70),
(24, 'images/products/nuoc-ep-oi-side.png', 0, 71),
(24, 'images/products/nuoc-ep-oi-top.png', 0, 72);
GO


-- THẾ ANH - DÒNG 5
-- ============================================================
-- SẢN PHẨM CHO DANH MỤC SỮA CHUA VÀ HẠT CÀ PHÊ / MERCHANDISE
-- product_id từ 25 đến 30
-- ============================================================

-- Sản phẩm 25: Sữa Chua Trân Châu Đường Đen (product_id = 25)
INSERT INTO products (category_id, name, description, is_active) VALUES
(9, N'Sữa Chua Trân Châu Đường Đen', N'Sữa chua dẻo mịn kết hợp cùng trân châu đường đen dẻo dai đậm vị', 1);

-- Sản phẩm 26: Sữa Chua Xoài Hoàng Kim (product_id = 26) -> SẢN PHẨM MỚI THAY THẾ
INSERT INTO products (category_id, name, description, is_active) VALUES
(9, N'Sữa Chua Xoài Hoàng Kim', N'Sữa chua dẻo mịn quyện cùng mứt xoài chín mọng ngọt ngào và thạch xoài dai giòn', 1);

-- Sản phẩm 27: Sữa Chua Nếp Cẩm (product_id = 27)
INSERT INTO products (category_id, name, description, is_active) VALUES
(9, N'Sữa Chua Nếp Cẩm', N'Hương vị truyền thống bùi béo từ nếp cẩm hòa quyện cùng sữa chua thanh mát', 1);

-- Sản phẩm 28: Hạt Cà Phê Cầu Đất Arabica (product_id = 28)
INSERT INTO products (category_id, name, description, is_active) VALUES
(10, N'Hạt Cà Phê Cầu Đất Arabica', N'Hạt cà phê Arabica cao cấp từ vùng Cầu Đất, hương thơm trái cây tự nhiên phong phú', 1);

-- Sản phẩm 29: Hạt Cà Phê Robusta Honey (product_id = 29)
INSERT INTO products (category_id, name, description, is_active) VALUES
(10, N'Hạt Cà Phê Robusta Honey', N'Hạt Robusta chế biến theo phương pháp Honey cho vị ngọt hậu đậm đà, ít đắng gắt', 1);

-- Sản phẩm 30: Hạt Cà Phê Moka Thượng Hạng (product_id = 30)
INSERT INTO products (category_id, name, description, is_active) VALUES
(10, N'Hạt Cà Phê Moka Thượng Hạng', N'Dòng hạt Moka quý hiếm với vị chua thanh thoát và hương thơm quý phái', 1);


-- ============================================================
-- BIẾN THỂ CHO SỮA CHUA TRÂN CHÂU ĐƯỜNG ĐEN (product_id = 25)
-- variant_id bắt đầu từ 73
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(25, N'Sữa Chua Trân Châu Đường Đen Size S', 'S', 39000, 'COLD', 1),  -- variant_id = 73
(25, N'Sữa Chua Trân Châu Đường Đen Size M', 'M', 49000, 'COLD', 1),  -- variant_id = 74
(25, N'Sữa Chua Trân Châu Đường Đen Size L', 'L', 59000, 'COLD', 1);  -- variant_id = 75

-- ============================================================
-- BIẾN THỂ CHO SỮA CHUA XOÀI HOÀNG KIM (product_id = 26)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(26, N'Sữa Chua Xoài Hoàng Kim Size S', 'S', 39000, 'COLD', 1),  -- variant_id = 76
(26, N'Sữa Chua Xoài Hoàng Kim Size M', 'M', 49000, 'COLD', 1),  -- variant_id = 77
(26, N'Sữa Chua Xoài Hoàng Kim Size L', 'L', 59000, 'COLD', 1);  -- variant_id = 78

-- ============================================================
-- BIẾN THỂ CHO SỮA CHUA NẾP CẨM (product_id = 27)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(27, N'Sữa Chua Nếp Cẩm Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 79
(27, N'Sữa Chua Nếp Cẩm Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 80
(27, N'Sữa Chua Nếp Cẩm Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 81

-- ============================================================
-- BIẾN THỂ CHO HẠT CÀ PHÊ CẦU ĐẤT ARABICA (product_id = 28)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(28, N'Hạt Cà Phê Cầu Đất Arabica 250g', 'S', 160000, 'ROOM', 1),  -- variant_id = 82
(28, N'Hạt Cà Phê Cầu Đất Arabica 500g', 'M', 300000, 'ROOM', 1),  -- variant_id = 83
(28, N'Hạt Cà Phê Cầu Đất Arabica 1kg',   'L', 580000, 'ROOM', 1);  -- variant_id = 84

-- ============================================================
-- BIẾN THỂ CHO HẠT CÀ PHÊ ROBUSTA HONEY (product_id = 29)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(29, N'Hạt Cà Phê Robusta Honey 250g', 'S', 140000, 'ROOM', 1),  -- variant_id = 85
(29, N'Hạt Cà Phê Robusta Honey 500g', 'M', 260000, 'ROOM', 1),  -- variant_id = 86
(29, N'Hạt Cà Phê Robusta Honey 1kg',   'L', 500000, 'ROOM', 1);  -- variant_id = 87

-- ============================================================
-- BIẾN THỂ CHO HẠT CÀ PHÊ MOKA THƯỢNG HẠNG (product_id = 30)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
(30, N'Hạt Cà Phê Moka Thượng Hạng 250g', 'S', 220000, 'ROOM', 1),  -- variant_id = 88
(30, N'Hạt Cà Phê Moka Thượng Hạng 500g', 'M', 420000, 'ROOM', 1),  -- variant_id = 89
(30, N'Hạt Cà Phê Moka Thượng Hạng 1kg',   'L', 800000, 'ROOM', 1);  -- variant_id = 90


-- ============================================================
-- HÌNH ẢNH CHO SỮA CHUA TRÂN CHÂU ĐƯỜNG ĐEN (product_id = 25)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(25, 'images/products/suachuatranchauduongdenS.jpg', 1, 73),
(25, 'images/products/suachuatranchauduongdenM.jpg', 0, 74),
(25, 'images/products/suachuatranchauduongdenL.jpg', 0, 75);

-- ============================================================
-- HÌNH ẢNH CHO SỮA CHUA XOÀI HOÀNG KIM (product_id = 26)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(26, 'images/products/suachuaxoaiS.jpg', 1, 76),
(26, 'images/products/suachuaxoaiM.jpg', 0, 77),
(26, 'images/products/suachuaxoaiL.jpg', 0, 78);

-- ============================================================
-- HÌNH ẢNH CHO SỮA CHUA NẾP CẨM (product_id = 27)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(27, 'images/products/suachuanepcamS.jpg', 1, 79),
(27, 'images/products/suachuanepcamM.jpg', 0, 80),
(27, 'images/products/suachuanepcamL.jpg', 0, 81);

-- ============================================================
-- HÌNH ẢNH CHO HẠT CÀ PHÊ CẦU ĐẤT ARABICA (product_id = 28)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(28, 'images/products/hatcafecaudatarabicaS.jpg', 1, 82),
(28, 'images/products/hatcafecaudatarabicaM.jpg', 0, 83),
(28, 'images/products/hatcafecaudatarabicaL.jpg', 0, 84);

-- ============================================================
-- HÌNH ẢNH CHO HẠT CÀ PHÊ ROBUSTA HONEY (product_id = 29)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(29, 'images/products/hatcaferobustahoneyS.jpg', 1, 85),
(29, 'images/products/hatcaferobustahoneyM.jpg', 0, 86),
(29, 'images/products/hatcaferobustahoneyL.jpg', 0, 87);

-- ============================================================
-- HÌNH ẢNH CHO HẠT CÀ PHÊ MOKA THƯỢNG HẠNG (product_id = 30)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
(30, 'images/products/hatcafemokathuonghangS.jpg', 1, 88),
(30, 'images/products/hatcafemokathuonghangM.jpg', 0, 89),
(30, 'images/products/hatcafemokathuonghangL.jpg', 0, 90);



-- ================================================================



-- 7. Bảng PAYMENT_METHODS (Phương thức thanh toán)
INSERT INTO payment_methods (name) VALUES
(N'Tiền mặt'), (N'VNPay');
GO

-- 8. Bảng TABLES (Danh sách bàn)
INSERT INTO tables (capacity, is_active) VALUES
(2, 1), (2, 1), (4, 1), (4, 1), (4, 1), (6, 1), (6, 1), (8, 1), (2, 0), (10, 1);
GO

-- 9. Bảng ORDERS (Đơn hàng)
INSERT INTO orders (user_id, table_id, order_type, order_status, subtotal, discount_amount, total_amount, points_earned, note) VALUES
(5, NULL, 'ONLINE', 'COMPLETED', 70000, 0, 70000, 7, NULL),
(6, NULL, 'ONLINE', 'COMPLETED', 45000, 5000, 40000, 4, N'Ít đá'),
(7, NULL, 'ONLINE', 'PREPARING', 150000, 15000, 135000, 0, N'Giao giờ hành chính'), 
(8, NULL, 'ONLINE', 'COMPLETED', 110000, 0, 110000, 11, NULL),
(9, NULL, 'ONLINE', 'CANCELLED', 55000, 0, 55000, 0, NULL), 
(10, NULL, 'ONLINE', 'COMPLETED', 210000, 20000, 190000, 19, N'Khách VIP'),
(5, NULL, 'ONLINE', 'COMPLETED', 90000, 0, 90000, 9, NULL),
(6, NULL, 'ONLINE', 'PENDING', 35000, 0, 35000, 0, NULL),
(7, NULL, 'ONLINE', 'COMPLETED', 130000, 0, 130000, 13, NULL),
(8, NULL, 'ONLINE', 'COMPLETED', 65000, 5000, 60000, 6, NULL),
(5, NULL, 'ONLINE', 'COMPLETED', 100000, 0, 100000, 10, N'Khách gọi thêm'),
(6, NULL, 'ONLINE', 'COMPLETED', 135000, 15000, 120000, 12, NULL),
(7, NULL, 'ONLINE', 'PREPARING', 250000, 0, 250000, 0, N'Giao cẩn thận kẻo hỏng bánh'),
(8, 1, 'COUNTER', 'COMPLETED', 105000, 0, 105000, 10, N'Làm nóng bánh'),
(9, 4, 'COUNTER', 'COMPLETED', 147000, 0, 147000, 14, NULL),
(10, 6, 'COUNTER', 'PENDING', 87000, 0, 87000, 0, N'Đang chờ bạn khách đến'), 
(5, NULL, 'ONLINE', 'COMPLETED', 155000, 0, 155000, 15, N'Giao kèm nhiều ống hút');
GO

-- 10. Bảng ORDER_DETAILS (Chi tiết đơn hàng)
INSERT INTO order_details (order_id, product_id, variant_id, product_name_snapshot, variant_name_snapshot, price_snapshot, quantity, item_total, special_note, item_status) VALUES
(1, 1, 1, N'Espresso', N'Espresso Single', 35000, 2, 70000, NULL, 'COMPLETED'),
(2, 2, 2, N'Latte', N'Latte Nóng Vừa', 45000, 1, 45000, N'Pha bằng sữa hạt macca', 'COMPLETED'), 
(3, 5, 5, N'Trà Đào Cam Sả', N'Trà Đào Cam Sả Lớn', 55000, 1, 55000, N'Ít đá, nhiều đào', 'PREPARING'),
(3, 8, 8, N'Tiramisu', N'Tiramisu Cắt Lát', 45000, 2, 90000, NULL, 'PREPARING'),
(4, 7, 7, N'Matcha Đá Xay', N'Matcha Đá Xay Lớn', 65000, 1, 65000, NULL, 'COMPLETED'),
(4, 8, 8, N'Tiramisu', N'Tiramisu Cắt Lát', 45000, 1, 45000, NULL, 'COMPLETED'),
(5, 5, 5, N'Trà Đào Cam Sả', N'Trà Đào Cam Sả Lớn', 55000, 1, 55000, NULL, 'CANCELLED'),
(7, 6, 6, N'Trà Sữa Trân Châu', N'Trà Sữa Size M', 40000, 1, 40000, N'Không lấy trân châu', 'COMPLETED'),
(8, 3, 3, N'Cà phê Đen Đá', N'Đen Đá Lớn', 35000, 1, 35000, NULL, 'PREPARING'),
(9, 7, 7, N'Matcha Đá Xay', N'Matcha Đá Xay Lớn', 65000, 2, 130000, NULL, 'COMPLETED'),
(6, 6, 6, N'Trà Sữa Trân Châu', N'Trà Sữa Size M', 40000, 3, 120000, NULL, 'COMPLETED'),
(6, 9, 9, N'Bánh Mì Pate', N'Bánh Mì Thập Cẩm', 30000, 3, 90000, N'Cắt làm đôi giúp', 'COMPLETED'),
(7, 10, 10, N'Nước Ép Cam', N'Ép Cam Không Đường', 50000, 1, 50000, NULL, 'COMPLETED'),
(10, 7, 7, N'Matcha Đá Xay', N'Matcha Đá Xay Lớn', 65000, 1, 65000, NULL, 'COMPLETED'),
(11, 6, 21, N'Trà Sữa Trân Châu', N'Trà Sữa Size L', 50000, 2, 100000, N'50% đá, 30% đường', 'COMPLETED'),
(12, 2, 13, N'Latte', N'Latte Đá Vừa', 45000, 3, 135000, NULL, 'COMPLETED'),
(13, 8, 25, N'Tiramisu', N'Tiramisu Nguyên Ổ Nhỏ', 250000, 1, 250000, N'Viết chữ "Happy Birthday Nam" + 1 nến', 'PREPARING'),
(14, 9, 26, N'Bánh Mì Pate', N'Bánh Mì Thêm Pate', 35000, 3, 105000, NULL, 'COMPLETED'),
(15, 4, 17, N'Bạc Xỉu', N'Bạc Xỉu Lớn', 49000, 3, 147000, NULL, 'COMPLETED'),
(16, 3, 15, N'Cà phê Đen Đá', N'Đen Đá Vừa', 29000, 3, 87000, NULL, 'PENDING'),
(17, 10, 30, N'Nước Ép Cam', N'Ép Cam Cà Rốt', 55000, 1, 55000, NULL, 'COMPLETED'),
(17, 10, 28, N'Nước Ép Cam', N'Ép Cam Ít Đường', 50000, 1, 50000, NULL, 'COMPLETED'),
(17, 10, 10, N'Nước Ép Cam', N'Ép Cam Không Đường', 50000, 1, 50000, NULL, 'COMPLETED');
GO

-- 11. Bảng PAYMENTS (Thanh toán đơn hàng)
INSERT INTO payments (order_id, payment_method_id, amount, payment_status, transaction_ref) VALUES
(1, 1, 70000, 'SUCCESS', 'PAY001'),
(2, 2, 40000, 'SUCCESS', 'PAY002'),
(3, 2, 135000, 'PENDING', 'PAY003'),
(4, 1, 110000, 'SUCCESS', 'PAY004'),
(6, 1, 190000, 'SUCCESS', 'PAY005'),
(7, 2, 90000, 'SUCCESS', 'PAY006'),
(9, 1, 130000, 'SUCCESS', 'PAY007'),
(10, 1, 60000, 'SUCCESS', 'PAY008');
GO

-- 12. Bảng RESERVATIONS (Đặt bàn)
INSERT INTO reservations (customer_id, order_id, party_size, reservation_date, reservation_time, duration_minutes, status, cancellation_reason, note) VALUES
(11, 1, 2, '2026-06-14', '18:00:00', 120, 'COMPLETED', NULL, N'Bàn góc yên tĩnh'),
(11, 2, 4, '2026-06-15', '19:00:00', 120, 'CONFIRMED', NULL, N'Tiệc sinh nhật'),
(11, 3, 2, '2026-06-15', '09:00:00', 60, 'PENDING', NULL, NULL),
(11, 4, 6, '2026-06-16', '20:00:00', 180, 'CANCELLED', N'Khách đổi lịch', NULL),
(11, 5, 2, '2026-06-16', '14:00:00', 90, 'CONFIRMED', NULL, N'Gần cửa sổ'),
(11, 6, 8, '2026-06-17', '18:30:00', 150, 'PENDING', NULL, N'Họp nhóm'),
(11, 7, 4, '2026-06-18', '10:00:00', 120, 'CONFIRMED', NULL, NULL),
(11, 8, 2, '2026-06-18', '20:00:00', 60, 'CANCELLED', N'Khách báo bận', NULL),
(11, 9, 6, '2026-06-19', '19:00:00', 120, 'PENDING', NULL, N'Cần ghế em bé'),
(11, NULL, 2, '2026-06-20', '08:00:00', 60, 'CONFIRMED', NULL, NULL),
(11, NULL, 2, '2026-06-20', '10:00:00', 60, 'NO_SHOW', NULL, NULL),
(11, NULL, 2, '2026-06-20', '08:00:00', 60, 'CONFIRMED', NULL, NULL),
(11, NULL, 2, '2026-06-20', '10:00:00', 60, 'NO_SHOW', NULL, NULL),
(11, NULL, 2, '2026-06-20', '08:00:00', 60, 'CONFIRMED', NULL, NULL),
(11, NULL, 2, '2026-06-20', '10:00:00', 60, 'NO_SHOW', NULL, NULL),
(11, NULL, 2, '2026-06-20', '08:00:00', 60, 'CONFIRMED', NULL, NULL),
(11, NULL, 2, '2026-06-20', '10:00:00', 60, 'NO_SHOW', NULL, NULL),
(11, NULL, 2, '2026-06-20', '12:00:00', 60, 'ARRIVED', NULL, NULL);
GO
-- 13. Bảng RESERVATION_TABLES (Bàn được xếp cho đặt bàn)
INSERT INTO reservation_tables (reservation_id, table_id) VALUES
(1, 1), (2, 4), (4, 6), (5, 2), (7, 5), (8, 2), (10, 1);
GO

-- 14. Bảng RESERVATION_DEPOSITS (Tiền cọc đặt bàn)
INSERT INTO reservation_deposits (reservation_id, deposit_amount, payment_status, transaction_ref, refund_amount, refund_status) VALUES
(2, 100000, 'PAID', 'DEP001', 0, 'NONE'),
(4, 200000, 'REFUNDED', 'DEP002', 200000, 'FULL'),
(5, 50000, 'PAID', 'DEP003', 0, 'NONE'),
(7, 100000, 'PAID', 'DEP004', 0, 'NONE'),
(8, 50000, 'FORFEITED', 'DEP005', 0, 'NONE');
GO

-- 15. Bảng REVIEWS (Đánh giá)
INSERT INTO reviews (customer_id, order_id, product_id, rating, comment, is_visible) VALUES
(5, 1, 1, 5, N'Cà phê rất đậm đà, chuẩn vị', 1),
(6, 2, 2, 4, N'Sữa hơi ngọt một chút', 1),
(7, 3, 5, 5, N'Trà đào miếng to, rất ngon', 1),
(8, 4, 7, 5, N'Đá xay mịn, matcha thơm', 1),
(10, 6, 9, 4, N'Bánh mì ngon nhưng hơi nhỏ', 1),
(7, 3, 8, 5, N'Tiramisu béo ngậy, bánh ngon', 1),
(5, 11, 6, 3, N'Trân châu hơi cứng', 1),
(5, 17, 10, 5, N'Nước ép tươi, rất ưng ý', 1),
(8, 14, 9, 4, N'Bánh mì nóng hổi, tuyệt vời', 1),
(9, 15, 4, 5, N'Bạc xỉu ngon nhất tôi từng uống', 1);
GO

-- 16. Bảng LOYALTY_POINTS (Giao dịch điểm tích lũy)
INSERT INTO loyalty_points (customer_id, transaction_type, points, balance_after, reference_type, reference_id, note) VALUES
(11, 'EARN', 10, 10, 'ORDER', 1, N'Tích điểm mua hàng'),
(6, 'EARN', 5, 5, 'ORDER', 2, N'Tích điểm mua hàng'),
(7, 'EARN', 15, 15, 'ORDER', 3, N'Tích điểm mua hàng'),
(8, 'EARN', 10, 10, 'ORDER', 4, N'Tích điểm mua hàng'),
(10, 'EARN', 20, 20, 'ORDER', 6, N'Tích điểm mua hàng'),
(11, 'EARN', 5, 15, 'REVIEW', 1, N'Tích điểm đánh giá'),
(6, 'EARN', 5, 10, 'REVIEW', 2, N'Tích điểm đánh giá'),
(10, 'REDEEM', -10, 10, 'ORDER', 6, N'Đổi điểm lấy giảm giá'),
(7, 'EARN', 10, 25, 'ORDER', 9, N'Tích điểm mua hàng'),
(11, 'EARN', 10, 25, 'ORDER', 7, N'Tích điểm mua hàng'),
(11, 'EARN', 10, 35, 'ORDER', 7, N'Tích điểm mua hàng'),
(11, 'EARN', 10, 45, 'ORDER', 7, N'Tích điểm mua hàng'),
(11, 'REDEEM', -10, 35, 'ORDER', 7, N'Đổi điểm lấy giảm giá'),
(11, 'EARN', 10, 45, 'ORDER', 7, N'Tích điểm mua hàng'),
(11, 'REDEEM', -10, 35, 'ORDER', 7, N'Đổi điểm lấy giảm giá'),
(11, 'EARN', 10, 45, 'ORDER', 7, N'Tích điểm mua hàng'),
(11, 'REDEEM', -10, 35, 'ORDER', 7, N'Đổi điểm lấy giảm giá'),
(11, 'EARN', 10, 45, 'ORDER', 7, N'Tích điểm mua hàng');
GO

-- 17. Bảng POLICIES (Luật / Chính sách điểm)
INSERT INTO policies (policy_name, policy_type, action_type, currency_value, unit, status, comment) VALUES
(N'Đổi điểm', 'REDEEM', 'DISCOUNT', 100, 'VND', 1, N'1 điểm có giá trị'),
(N'Tích điểm hóa đơn', 'EARN', 'ORDER', 0.01, '%', 1, null),
(N'Tặng điểm Review', 'EARN', 'REVIEW', 5, 'points', 1, null);
GO

INSERT INTO map (map_name, url_map) VALUES
('Tang 1', 'url1.png'),
('Tang 1', 'url2.png');
GO

-- 19. Bảng SYSTEMLOG (Nhật ký hệ thống)
INSERT INTO system_logs (target_type, target_id, action, ip_address, description) VALUES
('User', 1, 'Login', '192.168.1.2', N'Admin đăng nhập thành công'),
('Product', 1, 'Update', '192.168.1.3', N'Cập nhật giá Espresso'),
('Order', 5, 'Cancel', '192.168.1.10', N'Khách hủy đơn takeaway'),
('Reservation', 8, 'Cancel', '192.168.1.12', N'Quản lý hủy đặt bàn'),
('User', 2, 'Login', '192.168.1.4', N'Manager đăng nhập'),
('Table', 3, 'Update', '192.168.1.4', N'Chuyển trạng thái bàn sang Occupied'),
('Payment', 5, 'Create', '192.168.1.5', N'Nhận thanh toán qua thẻ'),
('Policy', 1, 'Create', '192.168.1.2', N'Thêm chính sách giảm giá Sinh nhật'),
('Review', 3, 'Hide', '192.168.1.2', N'Ẩn review do vi phạm'),
('User', 10, 'Register', '10.0.0.1', N'Khách hàng mới đăng ký tài khoản');
GO

-- 20. customer_addresses ─────────────────────────────────────
-- Chỉ chèn cho user role Customer (role_id = 5): user_id 5, 6, 7, 8, 9, 10
-- user_id 5 → 2 địa chỉ (nhà + cơ quan)
INSERT INTO customer_addresses (customer_id, label, full_address, recipient_name, recipient_phone)VALUES
-- user 5 (Hoàng Văn E)
(5, N'Nhà',      N'12 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM',          N'Hoàng Văn E',  '0901234565'),
(5, N'Cơ quan',  N'Tòa nhà Vietcombank, 5 Công Trường Mê Linh, Quận 1, TP.HCM', N'Hoàng Văn E', '0901234565'),
 
-- user 6 (Đỗ Thị F) — nhưng user 6 role_id=1 (Admin), để đúng thực tế
-- Ta seed cho user_id 5,9,10 là Customer (role_id=5 theo seed gốc)
-- user 9 (Bùi Văn I)
(9, N'Nhà',      N'45 Lê Văn Việt, Phường Hiệp Phú, TP. Thủ Đức, TP.HCM',    N'Bùi Văn I',   '0901234569'),
(9, N'Cơ quan',  N'Đại học Bách Khoa, 268 Lý Thường Kiệt, Quận 10, TP.HCM',  N'Bùi Văn I',   '0901234569'),
 
-- user 10 (Đặng Thị K)
(10, N'Nhà',     N'88 Đinh Tiên Hoàng, Phường 3, Quận Bình Thạnh, TP.HCM',   N'Đặng Thị K',  '0901234570'),
(10, N'Văn phòng', N'Lầu 3, 391A Nam Kỳ Khởi Nghĩa, Quận 3, TP.HCM',        N'Đặng Thị K',  '0901234570'),

-- user 11 (Đặng Thị K)
(11, N'Nhà',     N'88 Đinh Tiên Hoàng, Phường 3, Quận Bình Thạnh, TP.HCM',   N'Đặng Thị K',  '0901234570'),
(11, N'Văn phòng', N'Lầu 3, 391A Nam Kỳ Khởi Nghĩa, Quận 3, TP.HCM',        N'Đặng Thị K',  '0901234570');
GO
 
-- 21. carts ──────────────────────────────────────────────────
-- Tạo giỏ hàng cho 3 Customer đang active
-- (user_id 5 = Hoàng Văn E, 9 = Bùi Văn I, 10 = Đặng Thị K)
INSERT INTO carts (customer_id) VALUES
(5),
(9),
(10),
(11);
GO
 
-- 22. cart_items ─────────────────────────────────────────────
-- cart_id 1 → customer_id 5
INSERT INTO cart_items (cart_id, product_id, variant_id, quantity, special_note) VALUES
(1, 2,  4,  2, N'Ít đá, nhiều sữa'),        -- Latte Đá Vừa x2
(1, 7,  19, 1, N'Thêm kem cheese'),          -- Matcha Đá Xay Lớn x1
(1, 8,  22, 1, NULL),                       -- Tiramisu Cắt Lát x1
 
-- cart_id 2 → customer_id 9
(2, 3,  7,  1, NULL),                        -- Đen Đá Lớn x1
(2, 9,  24, 2, N'Làm nóng bánh'),           -- Bánh Mì Thêm Pate x2
 
-- cart_id 3 → customer_id 10
(3, 5,  13, 2, N'Ít đá'),                   -- Trà Đào Cam Sả Lớn x2
(3, 6,  16, 1, N'50% đường'),               -- Trà Sữa Size L x1
(3, 10, 28, 1, NULL),                       -- Ép Cam Ít Đường x1

-- cart_id 4 → customer_id 11
(4, 5,  13, 2, N'Ít đá'),                   -- Trà Đào Cam Sả Lớn x2
(4, 6,  16, 1, N'50% đường'),               -- Trà Sữa Size L x1
(4, 10, 28, 1, NULL);
GO

-- ============================================================
-- SEED DATA: system_configs (Cấu hình mặc định)
-- ============================================================
INSERT INTO system_configs (config_key, config_value, config_group, description) VALUES
-- General (Thông tin chung)
(N'site_name', N'BrewMaster Coffee Shop', N'general', N'Tên hiển thị của hệ thống'),
(N'site_phone', N'1900 1234', N'general', N'Số điện thoại hỗ trợ khách hàng'),
(N'site_email', N'support@brewmaster.com', N'general', N'Email hỗ trợ khách hàng'),
(N'site_address', N'123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM', N'general', N'Địa chỉ quán'),
(N'site_hours', N'07:00 - 22:00', N'general', N'Giờ mở cửa'),
(N'site_logo', N'/images/logo.png', N'general', N'Đường dẫn ảnh logo'),
(N'site_favicon', N'/images/favicon.ico', N'general', N'Đường dẫn favicon'),
(N'site_description', N'Hệ thống quản lý quán cà phê BrewMaster', N'general', N'Mô tả hệ thống'),

-- System (Cấu hình hệ thống)
(N'maintenance_mode', N'false', N'system', N'Chế độ bảo trì (true/false)'),
(N'maintenance_message', N'Hệ thống đang bảo trì. Vui lòng quay lại sau!', N'system', N'Thông báo hiển thị khi bảo trì'),
(N'log_retention_days', N'30', N'system', N'Số ngày giữ log (0 = không tự động xóa)'),
(N'default_language', N'vi', N'system', N'Ngôn ngữ mặc định (vi/en)'),
(N'items_per_page', N'12', N'system', N'Số sản phẩm hiển thị trên 1 trang'),

-- Reservation (Cấu hình đặt bàn)
(N'reservation_deposit_amount', N'50000', N'reservation', N'Số tiền cọc đặt bàn mặc định (VNĐ)'),
(N'reservation_hold_minutes', N'15', N'reservation', N'Thời gian giữ bàn sau khi đặt (phút)'),
(N'reservation_max_per_day', N'3', N'reservation', N'Số lần đặt bàn tối đa trong 1 ngày'),
(N'reservation_max_advance_days', N'30', N'reservation', N'Số ngày tối đa được đặt trước'),
(N'reservation_min_advance_hours', N'2', N'reservation', N'Số giờ tối thiểu trước khi đến được đặt bàn'),
(N'reservation_max_party_size', N'10', N'reservation', N'Số khách tối đa cho 1 bàn'),
(N'reservation_cancel_before_minutes', N'60', N'reservation', N'Thời gian tối thiểu trước giờ đến để hủy (phút)');
/*
-- 1. Bảng Phân quyền (Roles)
SELECT * FROM roles;

-- 2. Bảng Người dùng/Khách hàng/Nhân viên (Users)
SELECT * FROM users;

-- 3. Bảng Danh mục sản phẩm (Categories)
SELECT * FROM categories;

-- 4. Bảng Sản phẩm (Products)
SELECT * FROM products;

-- 5. Bảng Biến thể sản phẩm (Product Variants)
SELECT * FROM product_variants;

-- 6. Bảng Hình ảnh sản phẩm (Product Images)
SELECT * FROM product_images;

-- 7. Bảng Phương thức thanh toán (Payment Methods)
SELECT * FROM payment_methods;

-- 8. Bảng Bàn trong quán (Tables)
SELECT * FROM tables;

-- 9. Bảng Đơn hàng (Orders)
SELECT * FROM orders;

-- 10. Bảng Chi tiết đơn hàng (Order Details)
SELECT * FROM order_details;

-- 11. Bảng Thanh toán (Payments)
SELECT * FROM payments;

-- 12. Bảng Đặt bàn (Reservations)
SELECT * FROM reservations;

-- 13. Bảng Liên kết Đặt bàn và Bàn (Reservation Tables)
SELECT * FROM reservation_tables;

-- 14. Bảng Tiền cọc đặt bàn (Reservation Deposits)
SELECT * FROM reservation_deposits;

-- 15. Bảng Đánh giá (Reviews)
SELECT * FROM reviews;

-- 16. Bảng Lịch sử điểm tích lũy (Loyalty Points)
SELECT * FROM loyalty_points;

-- 17. Bảng Chính sách tích điểm/giảm giá (Policies)
SELECT * FROM policies;

-- 18. Bảng Sơ đồ mặt bằng quán (Map)
SELECT * FROM map;

-- 19. Bảng Nhật ký hệ thống (System Logs)
SELECT * FROM system_logs;
*/

select * from users
select * from customer_addresses
select * from reservations
select * from reservation_deposits
select * from tables
select * from reservation_tables

select * from orders
select * from payments
select * from loyalty_points

select * from system_logs

select * from policies
select * from reviews

select * from payment_methods

UPDATE users
SET password_hash = '$2a$10$2.SdZIwqe6nJllpI4MnfQu/GOjqg0G.0kDADLa1DiEMAybWAyyFNO'
WHERE user_id >=1 ;



