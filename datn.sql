CREATE DATABASE sd_73;
GO

USE sd_73;
GO

CREATE TABLE role (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    create_date DATETIME2 (7) NULL,
    update_date DATETIME2 (7) NULL
);
GO

CREATE TABLE employee(
    id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    code VARCHAR(255) NULL,
    name NVARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    birth_day DATETIME2 (7) NULL,
    gender BIT, -- 1: Nam, 0: Nữ
    cccd VARCHAR(20),
    tinh_thanh NVARCHAR(100),
    quan_huyen NVARCHAR(100),
    xa_phuong NVARCHAR(100),
    so_nha NVARCHAR(255),
    status INT NOT NULL DEFAULT 1
);
GO

CREATE TABLE customer (
    id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    code VARCHAR(255) NULL,
    name NVARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    phone_number VARCHAR(255),
    birth_day DATETIME2 (7) NULL,
    gender BIT, -- 1: Nam, 0: Nữ
    cccd VARCHAR(20),
    tinh_thanh NVARCHAR(100),
    quan_huyen NVARCHAR(100),
    xa_phuong NVARCHAR(100),
    so_nha NVARCHAR(255),
	is_guest BIT DEFAULT 0,
    status INT DEFAULT 1
);
GO


CREATE TABLE account(
    id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    code VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    birth_day DATETIME2 (7) NULL,
    create_date DATETIME2 (7),
    update_date DATETIME2 (7),
    is_non_locked BIT NOT NULL,
    role_id BIGINT,
    customer_id BIGINT NULL,
    employee_id BIGINT NULL,
    CONSTRAINT FK_account_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT FK_account_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT FK_account_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);
GO

CREATE TABLE address_shipping (
    id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    customer_id BIGINT NULL,
    tinh_thanh NVARCHAR(100),
    quan_huyen NVARCHAR(100),
    xa_phuong NVARCHAR(100),
    address_detail NVARCHAR(255),
    is_default BIT DEFAULT 0,
    create_date DATETIME2 (7),
    update_date DATETIME2 (7),
    CONSTRAINT FK_shipping_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);
GO

CREATE TABLE verification_code (
    id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    code VARCHAR(255) NULL,
    expiry_time DATETIME2 (7),
    account_id BIGINT,
    CONSTRAINT FK_verification_account FOREIGN KEY (account_id) REFERENCES account(id)
);
GO


CREATE TABLE category(
	id bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
	code VARCHAR(255) NULL,
	name NVARCHAR(255) NULL,
	status INT NOT NULL,
	delete_flag BIT NULL,
);
GO

CREATE TABLE material(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	code VARCHAR(255) NULL,
	name NVARCHAR(255) NULL,
	status INT NOT NULL,
	delete_flag BIT NULL,
);
GO

CREATE TABLE brand(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	code VARCHAR(255) NULL,
	name NVARCHAR(255) NULL,
	status INT NOT NULL,
	delete_flag BIT NULL,
);
GO

CREATE TABLE size(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	code VARCHAR(255) NULL,
	name NVARCHAR(255) NULL,
	delete_flag BIT NULL,
);
GO

CREATE TABLE color(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	code VARCHAR(255) NULL,
	name NVARCHAR(255) NULL,
	delete_flag BIT NULL,
);
GO

CREATE TABLE payment_method (
    id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    name NVARCHAR(100) NULL,
    status INT NOT NULL
);
GO

CREATE TABLE discount_code(
	id BIGINT PRIMARY KEY NOT NULL,
	code NVARCHAR(255) NULL,
	delete_flag BIT NOT NULL,
	detail NVARCHAR(255) NULL,
	discount_amount FLOAT NULL,
	end_date DATETIME2(7) NULL,
	maximum_amount INT NULL,
	maximum_usage INT NULL,
	minimum_amount_in_cart FLOAT NULL,
	percentage INT NULL,
	start_date DATETIME2(7) NULL,
	status INT NOT NULL,
	type INT NOT NULL,
);
GO

CREATE TABLE product(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	code VARCHAR(255) NULL,
	create_date DATETIME2(7) NULL,
	delete_flag BIT NOT NULL,
	describe NVARCHAR(255) NULL,
	gender INT NOT NULL,
	name NVARCHAR(255) NULL,
	price FLOAT NOT NULL,
	status INT NOT NULL,
	updated_date DATETIME2(7) NULL,
	brand_id BIGINT NULL,
	category_id BIGINT NULL,
	material_id BIGINT NULL,
	CONSTRAINT FK_product_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
	CONSTRAINT FK_product_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT FK_product_material FOREIGN KEY (material_id) REFERENCES material(id),
);
GO

CREATE TABLE image(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	create_date DATETIME2(7) NULL,
	file_type VARCHAR(255) NULL,
	link VARCHAR(255) NULL,
	name NVARCHAR(255) NULL,
	update_date DATETIME2(7) NULL,
	product_id BIGINT NULL,
	CONSTRAINT FK_image_product FOREIGN KEY (product_id) REFERENCES product(id)
);
GO

CREATE TABLE product_detail(
	id bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
	barcode varchar(255) NULL,
	price float NOT NULL,
	quantity int NOT NULL,
	color_id bigint NULL,
	product_id bigint NULL,
	size_id bigint NULL,
	CONSTRAINT FK_product_detail_product FOREIGN KEY (product_id) REFERENCES product(id),
	CONSTRAINT FK_product_detail_color FOREIGN KEY (color_id) REFERENCES color(id),
    CONSTRAINT FK_product_detail_size FOREIGN KEY (size_id) REFERENCES size(id)
);
GO

CREATE TABLE product_discount(
    id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
    code VARCHAR(50) NULL, -- Mã giảm giá (duy nhất cho mỗi lần giảm)
    name NVARCHAR(255) NULL,   -- Tên chương trình giảm giá
    closed BIT NOT NULL,
    discounted_amount FLOAT NULL,
    start_date DATETIME2(7) NULL,
    end_date DATETIME2(7) NULL,
    product_detail_id BIGINT NULL,
    CONSTRAINT FK_product_discount_detail FOREIGN KEY (product_detail_id) REFERENCES product_detail(id)
);
GO

CREATE TABLE cart(
	id bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
	create_date datetime2(7) NULL,
	quantity int NOT NULL,
	update_date datetime2(7) NULL,
	account_id bigint NULL,
	product_detail_id bigint NULL,
	CONSTRAINT FK_cart_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT FK_cart_product_detail FOREIGN KEY (product_detail_id) REFERENCES product_detail(id)
);
GO

CREATE TABLE bill(
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	amount FLOAT NULL,
	billing_address NVARCHAR(255) NULL,
	code VARCHAR(50) NOT NULL,
	create_date DATETIME2(7) NULL,
	invoice_type VARCHAR(255) NULL,
	promotion_price FLOAT NOT NULL,
	return_status BIT NULL,
	status VARCHAR(255) NULL,
	update_date DATETIME2(7) NULL,
	customer_id BIGINT NULL,
	discount_code_id BIGINT NULL,
	payment_method_id BIGINT NULL,
	CONSTRAINT FK_bill_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
	CONSTRAINT FK_bill_discount FOREIGN KEY (discount_code_id) REFERENCES discount_code(id),
    CONSTRAINT FK_bill_payment FOREIGN KEY (payment_method_id) REFERENCES payment_method(id)
);
GO

CREATE TABLE payment(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	amount VARCHAR(255) NULL,
	order_id VARCHAR(255) NULL,
	order_status VARCHAR(255) NULL,
	payment_date DATETIME2(7) NULL,
	status_exchange INT NULL,
	bill_id BIGINT NULL,
	CONSTRAINT FK_payment_bill FOREIGN KEY (bill_id) REFERENCES bill(id)
);
GO

CREATE TABLE bill_detail(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	moment_price FLOAT NULL,
	quantity INT NULL,
	return_quantity INT NULL,
	bill_id BIGINT NULL,
	product_detail_id BIGINT NULL,
	CONSTRAINT FK_billdetail_bill FOREIGN KEY (bill_id) REFERENCES bill(id),
    CONSTRAINT FK_billdetail_product_detail FOREIGN KEY (product_detail_id) REFERENCES product_detail(id)
);
GO

CREATE TABLE bill_return(
	id BIGINT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	code VARCHAR(255) NULL,
	is_cancel BIT NOT NULL,
	percent_fee_exchange INT NULL,
	return_date DATETIME2(7) NULL,
	return_money FLOAT NULL,
	return_reason NVARCHAR(255) NULL,
	return_status INT NOT NULL,
	bill_id BIGINT NULL,
	CONSTRAINT FK_billreturn_bill FOREIGN KEY (bill_id) REFERENCES bill(id)
);
GO

CREATE TABLE return_detail(
	id bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
	moment_price_refund float NULL,
	quantity_return int NULL,
	return_id bigint NULL,
	product_detail_id bigint NULL,
	CONSTRAINT FK_return_detail_return FOREIGN KEY (return_id) REFERENCES bill_return(id),
	CONSTRAINT FK_return_detail_product FOREIGN KEY (product_detail_id) REFERENCES product_detail(id)
);
GO