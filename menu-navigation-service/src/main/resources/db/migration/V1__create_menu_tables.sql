-- Menu items table matching COMEN02Y.cpy and COADM02Y.cpy layouts
CREATE TABLE menu_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    option_number INT          NOT NULL,
    option_name   VARCHAR(35)  NOT NULL,
    program_name  VARCHAR(8)   NOT NULL,
    user_type     VARCHAR(1)   NOT NULL,
    menu_group    VARCHAR(10)  NOT NULL,
    display_order INT          NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_menu_group_option UNIQUE (menu_group, option_number)
);

-- Navigation context table (session-based, mirrors COBOL COMMAREA)
CREATE TABLE navigation_context (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id      VARCHAR(64)  NOT NULL,
    user_id         VARCHAR(8),
    user_type       VARCHAR(1),
    from_tranid     VARCHAR(4),
    from_program    VARCHAR(8),
    to_tranid       VARCHAR(4),
    to_program      VARCHAR(8),
    pgm_context     INT          NOT NULL DEFAULT 0,
    last_updated    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_session UNIQUE (session_id)
);
