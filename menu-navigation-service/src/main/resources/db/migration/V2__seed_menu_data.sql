-- Seed regular user menu items (from COMEN02Y.cpy)
INSERT INTO menu_items (option_number, option_name, program_name, user_type, menu_group, display_order) VALUES
(1,  'Account View',        'COACTVWC', 'U', 'REGULAR', 1),
(2,  'Account Update',      'COACTUPC', 'U', 'REGULAR', 2),
(3,  'Credit Card List',    'COCRDLIC', 'U', 'REGULAR', 3),
(4,  'Credit Card View',    'COCRDSLC', 'U', 'REGULAR', 4),
(5,  'Credit Card Update',  'COCRDUPC', 'U', 'REGULAR', 5),
(6,  'Transaction List',    'COTRN00C', 'U', 'REGULAR', 6),
(7,  'Transaction View',    'COTRN01C', 'U', 'REGULAR', 7),
(8,  'Transaction Add',     'COTRN02C', 'U', 'REGULAR', 8),
(9,  'Transaction Reports', 'CORPT00C', 'U', 'REGULAR', 9),
(10, 'Bill Payment',        'COBIL00C', 'U', 'REGULAR', 10);

-- Seed admin menu items (from COADM02Y.cpy)
INSERT INTO menu_items (option_number, option_name, program_name, user_type, menu_group, display_order) VALUES
(1, 'User List (Security)',   'COUSR00C', 'A', 'ADMIN', 1),
(2, 'User Add (Security)',    'COUSR01C', 'A', 'ADMIN', 2),
(3, 'User Update (Security)', 'COUSR02C', 'A', 'ADMIN', 3),
(4, 'User Delete (Security)', 'COUSR03C', 'A', 'ADMIN', 4);
