## 1. Mục tiêu dự án

- Hệ thống quản lý đại lý xe điện (EV Dealer Management System - `evdms`).
- Hỗ trợ quản lý:
  - Xe điện (model, brand, battery, trạng thái…)
  - Đơn nhập xe từ hãng (purchase order `ManufacturerOrder` + các `VehicleUnit` tạo ra)
  - Đơn hàng + giao xe cho khách (`Order`, `Delivery`)
  - Kho xe/vehicle unit, voucher khuyến mãi
  - Người dùng admin và phân quyền truy cập qua JWT.

Backend dùng Spring Boot 3 + Spring Security + Spring Data JPA + H2/MSSQL, expose REST API cho frontend React (`dealership-manager-simple`).

---

## 2. Công nghệ chính

- Java 21
- Spring Boot 3 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`)
- CSDL:
  - Dev/test: H2 in‑memory (`application-h2.properties`)
  - Prod: SQL Server (driver `mssql-jdbc`)
- Auth:
  - JWT (`io.jsonwebtoken:jjwt-*`)
  - `Spring Security` với filter `JwtAuthenticationFilter`
- ORM:
  - JPA/Hibernate
  - MapStruct / ModelMapper cho mapping DTO ↔ entity

---

## 3. Cấu trúc thư mục backend

`src/main/java/com/swd/evdms`

- `EvdmsApplication.java` – class khởi động Spring Boot.
- `config/` – cấu hình chung (nếu có: CORS, mapper, mail…).
- `controller/` – REST controller, định nghĩa endpoint:
  - `AuthController` – đăng ký, đăng nhập, trả về JWT.
  - `AdminUserController` – quản lý admin user.
  - `VehicleController`, `VehicleUnitController` – xe và từng unit xe trong kho.
  - `ModelController` – model xe.
  - `PurchaseOrderController`, `OrderController`, `DeliveryController` – quy trình nhập & bán xe.
  - `VoucherController` – voucher khuyến mãi.
- `dto/` – DTO request/response trả cho FE (ví dụ: `VehicleResponse`, `DeliveryResponse`, `PurchaseOrderResponse`…).
- `entity/` – entity JPA map với bảng DB (`ElectricVehicle`, `VehicleUnit`, `ManufacturerOrder`, `Voucher`, …).
- `repository/` – interface `JpaRepository` để truy vấn DB (ví dụ `VehicleUnitRepository`, `ManufacturerOrderRepository`, `VoucherRepository`…).
- `service/` – business logic:
  - `DeliveryService`, `PurchaseOrderService`, … xử lý quy trình nghiệp vụ.
  - `JpaUserDetailsService` – load UserDetails cho Spring Security.
  - `JwtService` – tạo/validate JWT.
- `security/` – cấu hình bảo mật:
  - `SecurityConfig` – cấu hình filter chain, CORS, endpoint public/secure.
  - `JwtAuthenticationFilter`, `ApiExceptionHandler`, ….

`src/main/resources`

- `application.properties` – cấu hình chung, set profile đang dùng (`spring.profiles.active=h2`).
- `application-h2.properties` – cấu hình cho profile `h2` (H2 in‑memory).
- `schema-h2.sql` – script tạo bảng khi dùng H2.
- `data-h2.sql` – script seed dữ liệu demo.

---

## 4. Luồng auth & bảo mật (JWT)

1. User gọi `POST /api/auth/login` với username/password.
2. `AuthController` dùng `AuthenticationManager` + `JpaUserDetailsService` để xác thực.
3. Nếu thành công, `JwtService` tạo JWT (dựa trên `jwt.secret`, `jwt.exp-minutes` trong `application-h2.properties`) và trả về cho FE.
4. FE lưu JWT (localStorage, memory) và gửi trong header `Authorization: Bearer <token>`.
5. `JwtAuthenticationFilter`:
   - Đọc JWT từ header.
   - Validate chữ ký, hạn.
   - Nếu hợp lệ, set `Authentication` vào `SecurityContext`.
6. `SecurityConfig`:
   - Cho phép một số path public:
     - `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/error`, `OPTIONS /**`.
   - Các endpoint khác yêu cầu JWT hợp lệ.

CORS được cấu hình cho localhost và domain deploy FE trên Vercel (ví dụ `https://swddb.vercel.app`), cho phép header `Authorization`.

---

## 5. Luồng nghiệp vụ chính

### 5.1 Quản lý xe & kho

- **ElectricVehicle**: thông tin model xe: brand, tên model, dung lượng pin, status…
- **VehicleUnit**: một chiếc xe cụ thể trong kho (VIN/ID riêng, trạng thái `ON_ORDER` → `AT_DEALER` → `DELIVERED`).
- FE gọi:
  - `GET /api/vehicles` – danh sách model xe (controller: `VehicleController` trả `VehicleResponse`).
  - `GET /api/vehicle-units` (hoặc tương tự) – danh sách vehicle unit trong kho.

Business logic nằm ở service tương ứng, controller chỉ nhận request, gọi service, trả DTO.

### 5.2 Đơn nhập hàng từ hãng (purchase order)

- **ManufacturerOrder**:
  - Thực tế được dùng như purchase order đặt xe từ hãng (entity `ManufacturerOrder` + service `PurchaseOrderService`).
  - Trường chính: `orderNo`, `status` (`DRAFT`, `SUBMITTED`, `CONFIRMED`, `CANCELLED`), `quantity`, `model`.
- Khi PO được **CONFIRMED**:
  - `PurchaseOrderService.updateStatus` sinh ra các `VehicleUnit` tương ứng với số lượng, trạng thái ban đầu `ON_ORDER` và gắn với purchase order đó.
- Khi xe về đại lý:
  - Staff dùng `VehicleUnitController.markArrived` (`PATCH /api/vehicle-units/{id}/arrive`) để chuyển `VehicleUnit` từ `ON_ORDER` → `AT_DEALER` và set `arrivedAt`.
  - Có thể gán VIN qua `PATCH /api/vehicle-units/{id}/vin?vin=...`.
- Repo & service chính:
  - `ManufacturerOrderRepository`, `VehicleUnitRepository`, `PurchaseOrderService`.
- FE gọi:
  - `Orders.tsx` sử dụng `api.listOrders`, `api.createOrder`, `api.updateOrderStatus` tương ứng với API `/api/po` (purchase order).
  - `Inventory.tsx` dùng `/api/vehicle-units` để xem các xe được sinh ra từ PO.

### 5.3 Đơn khách hàng, voucher & phiếu giao xe

- **Order**:
  - Đơn hàng khách, gắn với `User` (nhân viên tạo đơn) và `ElectricVehicle` (model khách mua).
  - Tính toán giá, voucher, `priceAfter` trong `OrderService`.
- **Delivery**:
  - "Phiếu giao xe" cho một `Order` cụ thể.
  - Gắn **1 order** với **1 vehicle unit** thực tế (`Delivery.order` + `Delivery.vehicleUnit`).
  - Khi cập nhật trạng thái sang `DELIVERED`/`COMPLETED`, service đổi trạng thái:
    - `VehicleUnit.status` → `DELIVERED`, set `deliveredAt`.
    - `Order.status` → `COMPLETED`.
- **Voucher**:
  - Lưu mã, phần trăm / số tiền giảm giá, điều kiện sử dụng, ngày hết hạn.
  - Được áp dụng khi tạo đơn (`OrderService`) hoặc tạo phiếu giao (`DeliveryService`) tùy luồng sử dụng.
  - `VoucherRepository`, `VoucherController` expose API để:
    - Liệt kê voucher còn hiệu lực.
    - Bật/tắt hoạt động của voucher.

---

## 6. Cấu hình database & môi trường

- Mặc định app chạy với profile `h2` (config tại `application.properties`):
  - `spring.profiles.active=h2`
- Trong `application-h2.properties`:
  - `spring.datasource.url=jdbc:h2:mem:evdms;MODE=MSSQLServer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - H2 chạy in‑memory, rất phù hợp cho dev/test nhanh.
  - `spring.jpa.hibernate.ddl-auto=update` + `schema-h2.sql` + `data-h2.sql` giúp:
    - Tự tạo bảng & seed dữ liệu mẫu mỗi lần run.

Khi deploy thật với SQL Server, cần:
- Tạo `application-prod.properties` (hoặc profile khác) với URL SQL Server.
- Đổi `spring.profiles.active` tương ứng hoặc dùng biến môi trường.

---

## 7. Cách chạy backend

### 7.1 Chạy bằng Gradle

- Vào thư mục `SWD_Backend`:
  - `./gradlew bootRun`
- Mặc định:
  - Port: `8080` (override bằng `PORT` env).
  - DB: H2 in‑memory (profile `h2`).

### 7.2 Kiểm tra swagger

- Sau khi chạy app:
  - Mở `http://localhost:8080/swagger-ui/index.html`
  - Có thể test nhanh các API (auth, vehicle, orders, vouchers…).

---

## 8. Cách backend phục vụ frontend

- Frontend (`dealership-manager-simple`) gọi API backend qua:
  - Base URL thường là `import.meta.env.VITE_API_URL` hoặc tương tự (xem trong `api.ts`).
- Các trang FE tương ứng:
  - `Dashboard.tsx` – tổng quan: thường call nhiều endpoint thống kê (orders/deliveries/inventory).
  - `Inventory.tsx` – dùng API `VehicleController` & `VehicleUnitController`.
  - `Deliveries.tsx` – dùng API `DeliveryController`.
  - `Orders.tsx` – màn hình đặt **purchase order từ hãng**, dùng `PurchaseOrderController` (`/api/po`).
  - `CustomerOrders.tsx` – đơn hàng khách, dùng `OrderController` (`/api/orders`).
  - `Vouchers.tsx` – dùng `VoucherController`.

FE gửi JWT ở header `Authorization` cho mọi API cần login, backend validate ở `JwtAuthenticationFilter`.

---

## 9. Hướng dẫn đọc code nhanh

- **Bắt đầu từ controller**:
  - Xem endpoint mà FE đang gọi (ví dụ `/api/vehicles`).
- **Lần xuống service**:
  - Tìm file service tương ứng để xem logic nghiệp vụ.
- **Kiểm tra entity/repository**:
  - Xem bảng nào được dùng, quan hệ giữa các entity (OneToMany, ManyToOne…).
- **Đọc DTO / mapper**:
  - Xem cách entity được chuyển thành response trả cho FE.

Với flow mới, hãy:
1. Tạo/điều chỉnh entity + repository.
2. Viết service xử lý nghiệp vụ.
3. Expose API trong controller.
4. Cập nhật DTO & mapper.
5. Thêm dữ liệu mẫu vào `data-h2.sql` nếu cần.
