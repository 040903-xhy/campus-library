-- 校园图书管理系统建表脚本（H2 LEGACY 模式与 MySQL 8 均兼容）

CREATE TABLE IF NOT EXISTS user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(32)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(32),
    role        VARCHAR(16)  NOT NULL DEFAULT 'READER',
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS category (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(32)  NOT NULL,
    description VARCHAR(200),
    CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS book (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn         VARCHAR(20)  NOT NULL,
    title        VARCHAR(100) NOT NULL,
    author       VARCHAR(64),
    publisher    VARCHAR(64),
    category_id  BIGINT,
    total        INT NOT NULL DEFAULT 1,
    available    INT NOT NULL DEFAULT 1,
    publish_date DATE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_book_isbn UNIQUE (isbn),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS borrow_record (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT    NOT NULL,
    book_id     BIGINT    NOT NULL,
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date    TIMESTAMP,
    return_date TIMESTAMP,
    status      VARCHAR(16) NOT NULL DEFAULT 'BORROWED',
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book(id)
);

CREATE INDEX IF NOT EXISTS idx_borrow_user ON borrow_record(user_id, status);
CREATE INDEX IF NOT EXISTS idx_book_title ON book(title);
