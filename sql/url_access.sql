-- Insert URL access data only if role doesn't exist

USE javatechie;

INSERT INTO url_access (role, url)
SELECT 'ALLOW_URL', '/auth,/ws'
    WHERE NOT EXISTS (SELECT 1 FROM url_access WHERE role = 'ALLOW_URL');

INSERT INTO url_access (role, url)
SELECT 'ADMIN', '/users,/product,/orders'
    WHERE NOT EXISTS (SELECT 1 FROM url_access WHERE role = 'ADMIN');

INSERT INTO url_access (role, url)
SELECT 'USER', '/orders,/product'
    WHERE NOT EXISTS (SELECT 1 FROM url_access WHERE role = 'USER');