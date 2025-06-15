UPDATE url_access
SET url = '/auth,/ws'
WHERE role = 'ALLOW_URL';

INSERT INTO url_access (role, url)
SELECT 'ALLOW_URL', '/auth,/ws'
    WHERE ROW_COUNT() = 0;

-- For ADMIN role
UPDATE url_access
SET url = '/users,/product,/orders,/vouchers'
WHERE role = 'ADMIN';

INSERT INTO url_access (role, url)
SELECT 'ADMIN', '/users,/product,/orders,/vouchers'
    WHERE ROW_COUNT() = 0;

-- For USER role
UPDATE url_access
SET url = '/orders,/product,/vouchers'
WHERE role = 'USER';

INSERT INTO url_access (role, url)
SELECT 'USER', '/orders,/product,/vouchers'
    WHERE ROW_COUNT() = 0;