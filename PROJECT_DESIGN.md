## 1. Mục tiêu & phạm vi

- Xây dựng hệ thống quản lý đại lý xe điện (EV Dealer Management System) cho:
  - Quản lý sản phẩm (model xe, brand, thông số).
  - Quản lý kho (vehicle unit, trạng thái).
  - Quản lý nhập xe từ hãng (purchase order từ hãng + sinh `VehicleUnit` vào kho).
  - Quản lý đơn hàng khách + voucher khuyến mãi + phiếu giao xe.
  - Quản lý người dùng admin, phân quyền qua JWT.

Hệ thống hướng tới:
- Backend RESTful API (Spring Boot).
- Frontend web (React) cho nhân viên/admin đại lý.

---

## 2. Kiến trúc tổng thể

- **Kiểu kiến trúc**: Layered Architecture (Controller → Service → Repository → Database).
- **Backend**: Spring Boot 3, Spring Security, Spring Data JPA.
- **Frontend**: React (Vite), TypeScript.
- **Database**:
  - Dev/test: H2 in‑memory DB.
  - Prod: Microsoft SQL Server.
- **Giao tiếp**:
  - HTTP/REST + JSON.
  - Auth: JWT Bearer token.

### 2.1 Sơ đồ lớp logic (nhóm chính)

- **User & Auth**
  - User/Admin, Role (nếu có).
  - JWT Token (không lưu trong DB, quản lý bằng secret + exp).
- **Sản phẩm & kho**
  - Brand → ElectricVehicle (model) → VehicleUnit (từng xe trong kho).
- **Nhập hàng từ hãng**
  - ManufacturerOrder (purchase order) → VehicleUnit (ON_ORDER → AT_DEALER → DELIVERED).
- **Đơn khách & khuyến mãi**
  - Order (đơn khách) → ElectricVehicle (model); khi giao xe thì Order được gắn với VehicleUnit thông qua Delivery.
  - Voucher → áp dụng cho Order.

---

## 3. Thiết kế module backend

### 3.1 Controller layer

Trách nhiệm:
- Nhận HTTP request, validate input cơ bản.
- Gọi service xử lý nghiệp vụ.
- Trả về DTO (không trả entity trực tiếp).

Các controller chính:
- `AuthController` – đăng ký, login, refresh token (nếu có).
- `AdminUserController` – quản lý user/admin.
- `VehicleController` – danh sách model xe (VD: `GET /api/vehicles` trả `VehicleResponse`).
- `VehicleUnitController` – quản lý xe cụ thể trong kho.
- `ModelController` – quản lý model xe/line up.
- `PurchaseOrderController`, `DeliveryController`, `OrderController` – quy trình nhập & bán.
- `VoucherController` – CRUD voucher, apply voucher.

### 3.2 Service layer

Trách nhiệm:
- Chứa toàn bộ business logic.
- Kiểm tra điều kiện nghiệp vụ (trạng thái đơn, tồn kho, áp dụng voucher…).
- Giao tiếp với nhiều repository trong cùng một use case.

Ví dụ:
- `DeliveryService`:
  - Tạo phiếu giao xe (`Delivery`) cho một `Order` cụ thể và 1 `VehicleUnit` đã về đại lý.
  - Kiểm tra: mỗi order chỉ có tối đa 1 delivery, vehicle unit phải đang `AT_DEALER` và model trùng với `Order.vehicle`.
  - Cập nhật trạng thái delivery (PENDING → DELIVERED/COMPLETED) và đồng bộ `VehicleUnit.status` + `Order.status`.
- `PurchaseOrderService`:
  - Tạo/list/cập nhật purchase order từ hãng (dùng entity `ManufacturerOrder`).
  - Khi chuyển trạng thái sang `CONFIRMED`, sinh ra các `VehicleUnit` mới với trạng thái `ON_ORDER` gắn với purchase order đó.
- `OrderService`:
  - Tạo order cho khách hàng, gắn với `User` hiện tại và `ElectricVehicle` mà khách chọn.
  - Áp voucher (nếu có), tính `discountApplied` và `priceAfter`.
  - Cung cấp API xem danh sách/chi tiết/cập nhật trạng thái/xóa order (không gắn trực tiếp với `VehicleUnit`).
- `JwtService`, `JpaUserDetailsService`:
  - Tạo/validate JWT.
  - Load user/role từ DB cho Spring Security.

### 3.3 Repository layer

Trách nhiệm:
- Truy cập DB thông qua JPA.
- Không chứa logic nghiệp vụ, chỉ chứa query.

Ví dụ repos:
- `ElectricVehicleRepository`, `VehicleUnitRepository`.
- `ManufacturerOrderRepository`, `DeliveryRepository`.
- `VoucherRepository`, `OrderRepository`, `UserRepository`.

### 3.4 Entity & DTO

- Entity:
  - Map với bảng trong DB, có annotation `@Entity`.
  - Khai báo quan hệ (`@ManyToOne`, `@OneToMany`, `@ManyToMany`…).
- DTO:
  - Response: chỉ chứa field FE thực sự cần (VD: `VehicleResponse(id, model, brandName, status, batteryCapacity)`).
  - Request: input từ FE khi tạo/cập nhật.
- Mapping:
  - Có thể dùng MapStruct hoặc ModelMapper (project đã thêm dependency).

---

## 4. Thiết kế bảo mật

### 4.1 Authentication

- Cơ chế: JWT Bearer token.
- Flow:
  1. User gọi `POST /api/auth/login`.
  2. Backend check username/password (BCrypt, `PasswordEncoder`).
  3. Nếu hợp lệ, tạo JWT với:
     - Subject: username hoặc userId.
     - Claims: role/permission cơ bản.
     - Expiration: config bằng `jwt.exp-minutes`.
  4. FE lưu token, gửi kèm header `Authorization: Bearer <token>`.

### 4.2 Authorization

- `SecurityConfig`:
  - Public:
    - `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/error`, OPTIONS.
  - Các API khác: yêu cầu authenticated.
- Có thể bổ sung:
  - `@PreAuthorize("hasRole('ADMIN')")` ở các controller/service nhạy cảm.

### 4.3 CORS

- Cho phép origin:
  - `http://localhost:*`, `https://localhost:*`, `http://127.0.0.1:*`
  - `https://swddb.vercel.app`, `https://*.vercel.app`
- Cho phép header: `Authorization`, `Content-Type`,…

---

## 5. Thiết kế data & quan hệ (high-level)

> Tên bảng/quan hệ thực tế xem chi tiết ở `schema-h2.sql`, dưới đây là mô tả khái niệm.

- **Brand**
  - 1 brand – N electric vehicle (model).
- **ElectricVehicle**
  - Thuộc 1 brand.
  - N vehicle unit.
- **VehicleUnit**
  - Thuộc 1 `ElectricVehicle` (model).
  - Thuộc 0..1 `ManufacturerOrder` (purchase order sinh ra xe).
  - Có thể được tham chiếu bởi 0..1 `Delivery` (phiếu giao xe cho khách).
  - Có trạng thái: `ON_ORDER`, `AT_DEALER`, `DELIVERED`.
- **ManufacturerOrder (PurchaseOrder)**
  - Lưu thông tin PO gửi hãng: `orderNo`, `status`, `etaAtDealer`, `model`, `quantity`.
  - Khi `status = CONFIRMED`, backend sinh ra `quantity` vehicle unit (`VehicleUnit`) gắn với order, trạng thái ban đầu `ON_ORDER`.
- **Delivery**
  - Phiếu giao xe cho khách:
    - Gắn với 1 `Order`.
    - Gắn với 1 `VehicleUnit` (xe thực tế giao).
    - Gắn với 1 `ElectricVehicle` (model).
- **Order**
  - Thuộc 1 `User` (nhân viên tạo đơn).
  - Gắn với 1 `ElectricVehicle` (model khách chọn).
  - Có thể gắn 0..1 voucher (qua `voucherCode` và các field giảm giá).
  - Có thể gắn 0..1 delivery (xe đã được giao cho khách).
- **Voucher**
  - Thuộc loại giảm (percent/fixed).
  - Có ngày hiệu lực, điều kiện đơn tối thiểu, số lần dùng (nếu có).

---

## 6. Flow chính (sequence high-level)

### 6.1 Nhập hàng từ hãng về kho

1. Manager tạo purchase order (`ManufacturerOrder`) từ màn hình Orders (FE gọi `POST /api/po`).
2. Sau khi chốt với hãng, manager cập nhật `status` PO sang `CONFIRMED` (`PUT /api/po/{id}/status?status=CONFIRMED`).
3. Khi `CONFIRMED`, backend tự sinh ra các `VehicleUnit` tương ứng (`status = ON_ORDER`) gắn với purchase order.
4. Khi xe thực tế về đại lý, bộ phận kho dùng `PATCH /api/vehicle-units/{id}/arrive` để:
   - Chuyển trạng thái `VehicleUnit` từ `ON_ORDER` → `AT_DEALER`.
   - Set `arrivedAt`.
5. Kho có thể gán VIN cho từng xe bằng `PATCH /api/vehicle-units/{id}/vin?vin=...`.

### 6.2 Bán xe cho khách

1. Nhân viên bán hàng tạo `Order` cho khách (`POST /api/orders`) với:
   - Model xe (`vehicleId` → `ElectricVehicle`).
   - Thông tin khách (`customerInfo`), giá dự kiến, ngày giao dự kiến, voucher (tùy chọn).
   - `OrderService` tính `priceAfter` và `discountApplied` nếu có voucher.
2. Khi có xe thực tế tại đại lý (các `VehicleUnit` đang ở trạng thái `AT_DEALER`) và khách sẵn sàng nhận:
   - Nhân viên tạo `Delivery` cho đơn đó (`POST /api/deliveries`) và chọn một `vehicleUnitId` phù hợp.
3. `DeliveryService` kiểm tra:
   - Đơn chưa có delivery nào khác.
   - Vehicle unit đang `AT_DEALER`.
   - Model của vehicle unit trùng với `Order.vehicle`.
   - Nếu request có voucher và order chưa tính giảm giá, có thể tính/áp dụng voucher tại đây.
4. Khi giao xe xong, cập nhật `Delivery.status` sang `DELIVERED`/`COMPLETED`:
   - `VehicleUnit.status` → `DELIVERED`, set `deliveredAt`.
   - `Order.status` → `COMPLETED`.

### 6.3 Quản lý voucher

1. Admin tạo voucher mới qua `VoucherController`.
2. Khi khách đặt hoặc lập phiếu giao:
   - FE gửi kèm `voucherCode` trong request tạo `Order` hoặc `Delivery`.
   - Service kiểm tra:
     - Hạn sử dụng (`usableFrom` / `usableTo`).
     - Giá tối thiểu (`minPrice`).
     - Trạng thái `active`.
   - Nếu hợp lệ, tính số tiền giảm (`amount` hoặc `percent` với `maxDiscount`) và cập nhật `discountApplied` + `priceAfter`.

---

## 7. Triển khai & môi trường

### 7.1 Dev

- DB: H2 in‑memory.
- Cấu hình: `application-h2.properties`.
- Chạy bằng: `./gradlew bootRun`.
- FE trỏ về backend: `http://localhost:8080`.

### 7.2 Prod (gợi ý)

- DB: SQL Server (Azure/AWS/on-prem).
- Tạo `application-prod.properties` với:
  - `spring.datasource.url=jdbc:sqlserver://...`
  - `spring.datasource.username`, `spring.datasource.password`.
  - `spring.jpa.hibernate.ddl-auto=validate` (không tự đổi schema).
- Sử dụng env:
  - `SPRING_PROFILES_ACTIVE=prod`
  - `JWT_SECRET`, `JWT_EXP_MINUTES`.

---

## 8. Định hướng mở rộng

- Thêm phân quyền chi tiết:
  - Role: ADMIN, SALES, WAREHOUSE, ACCOUNTANT…
  - `@PreAuthorize` cho từng use case.
- Thêm logging & audit:
  - Lưu lịch sử thay đổi trạng thái vehicle/order.
- Thêm report:
  - Doanh thu theo thời gian.
  - Tồn kho từng model/brand.
- Tách microservice (nếu cần scale lớn):
  - Inventory service, Order service, Auth service…
