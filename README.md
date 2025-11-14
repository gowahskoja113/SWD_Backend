# EVDMS - Electric Vehicle Delivery Management System

Backend service cho hệ thống quản lý giao hàng xe điện.

## Tính năng

- **Quản lý đơn hàng** (Order Management)
- **Quản lý giao hàng** (Delivery Management)
- **Quản lý voucher** (Voucher Management)
- **Xác thực & Phân quyền** (Authentication & Authorization - JWT)
- **Quản lý thương hiệu** (Brand Management)

## Công nghệ sử dụng

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Data JPA**
- **Spring Security + JWT**
- **SQL Server**
- **Flyway** (Database migration)
- **MapStruct** (DTO mapping)
- **Lombok**
- **Gradle**

## Yêu cầu

- Java 21+
- SQL Server 2019+
- Gradle 8.14.3+

## Cài đặt

### 1. Clone repository
```bash
git clone https://github.com/gowahskoja113/SWD_Backend.git
cd SWD_Backend
```

### 2. Tạo database
Mở SQL Server Management Studio và tạo database:
```sql
CREATE DATABASE ElectricVehicleDB;
```

### 3. Cấu hình kết nối
Sửa file `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ElectricVehicleDB;encrypt=false
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD
```

### 4. Build project
```bash
.\gradlew clean build -x test
```

### 5. Chạy ứng dụng
```bash
.\gradlew bootRun
```

Ứng dụng sẽ chạy tại `http://localhost:xxxx`

## Cấu trúc Project

```
SWD_Backend/
├── src/
│   ├── main/
│   │   ├── java/com/swd/evdms/
│   │   │   ├── controller/       # REST Controllers
│   │   │   ├── service/          # Business logic
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── entity/           # JPA Entities
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── mapper/           # MapStruct mappers
│   │   │   └── security/         # Security config
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/     # Flyway migrations
│   └── test/
├── build.gradle
├── gradlew / gradlew.bat
└── README.md
```

## Authentication

API sử dụng JWT (JSON Web Token) để xác thực. Để gọi protected endpoints, thêm header:
```
Authorization: Bearer <your_jwt_token>
```
## Troubleshooting

### Lỗi kết nối SQL Server
- Kiểm tra SQL Server đang chạy
- Kiểm tra username/password đúng
- Kiểm tra database `ElectricVehicleDB` đã tạo

### Lỗi 403 Forbidden
- Endpoint cần JWT token
- Hoặc endpoint chưa được configure cho phép public access

### Lỗi Flyway migration
- Kiểm tra file migration tên đúng format `V<number>__<name>.sql`
- Kiểm tra file nằm trong `src/main/resources/db/migration`

## Phát triển tiếp theo

- [ ] Thêm Email notification
- [ ] Thêm Payment gateway integration
- [ ] Thêm Real-time tracking
- [ ] Thêm Mobile app API
- [ ] Thêm Analytics dashboard

## Liên hệ

Dự án được phát triển bởi **Team SWD**

## License

MIT License
