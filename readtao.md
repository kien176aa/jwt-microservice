# Hướng dẫn cấu hình & kiểm tra hệ thống

## 1. Cấu hình `ConstantValue` trong module `common`
- Khi **chạy local**: dùng `localhost` cho các URL.
- Khi **chạy Docker**: dùng `container name` (tên service trong `docker-compose.yml`), ví dụ: `http://identity-service:8080`.

## 2. Clear cache Redis
- Vào container Redis:
  ```bash
  docker exec -it redis-container-name redis-cli
  ```
- Xóa toàn bộ cache:
  ```bash
  FLUSHALL
  ```

## 3. Kiểm tra URL các service trong API Gateway
- Mở file `application.yml` (hoặc `application.properties`) của gateway.
- Đảm bảo các route URL đúng:
  ```yaml
  routes:
    - id: identity-service
      uri: http://identity-service:8080
      predicates:
        - Path=/api/v1/auth/**
    - id: order-service
      uri: http://order-service:8081
      predicates:
        - Path=/api/v1/order/**
    - id: notification-service
      uri: http://notification-service:8082
      predicates:
        - Path=/api/v1/notification/**
  ```

## 4. Kafka Topics
Hệ thống sử dụng các topic Kafka sau:
- `identity-topic`
- `order-topic`
- `notification-topic`

> Đảm bảo các service đã cấu hình đúng `bootstrap.servers`, và các consumer/subscriber đã subscribe đúng topic.

## 5. Notification Service kết nối MySQL
- Notification service hiện đã **kết nối cơ sở dữ liệu MySQL**.
- Cấu hình cần kiểm tra:
  ```properties
  spring.datasource.url=jdbc:mysql://<host>:<port>/<dbname>
  spring.datasource.username=<user>
  spring.datasource.password=<password>
  spring.jpa.hibernate.ddl-auto=update
  ```
- Đảm bảo MySQL container đã được khởi động và database/tables đã được tạo (nếu không dùng `ddl-auto=create` ở lần đầu).

## 6. Check lại file docker-compose.yml tại đang config để chạy local

---
