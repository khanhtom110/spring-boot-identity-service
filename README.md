# Spring Boot Identity Service

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green) ![Build](https://img.shields.io/badge/Build-Maven-blue)

Dự án Backend API chuyên biệt xử lý xác thực (Authentication) và phân quyền (Authorization) tập trung, được xây dựng theo kiến trúc Microservices.

## 🚀 Công nghệ sử dụng (Tech Stack)

* **Java Development Kit (JDK):** Version 21
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security, JWT (JSON Web Token)
* **Database:** MySQL (hoặc H2/PostgreSQL tùy cấu hình của bạn)
* **Build Tool:** Maven

## 🔑 Các tính năng chính (Key Features)

* **Authentication (Xác thực):**
    * Đăng ký (Register), Đăng nhập (Login), Đăng xuất (Logout).
    * Cấp phát Access Token & Refresh Token.
    * Xác thực token (Introspect).
* **Authorization (Phân quyền):**
    * Quản lý Vai trò (Role-based Access Control - RBAC).
    * Quản lý Quyền hạn (Permissions).
* **User Management:**
    * CRUD người dùng.
    * Lấy thông tin người dùng hiện tại (My Info).
* **Validation:** Kiểm tra dữ liệu đầu vào chặt chẽ (ngày sinh, độ mạnh mật khẩu...).
* **Exception Handling:** Xử lý lỗi tập trung với `GlobalExceptionHandler`.

## 🛠️ Cài đặt và Chạy ứng dụng

### Yêu cầu tiên quyết
* Java 21 đã được cài đặt.
* Maven.
* MySQL Server (đang chạy).

### Các bước thực hiện

1.  **Clone dự án:**
    ```bash
    git clone [https://github.com/khanhtom110/spring-boot-identity-service.git](https://github.com/khanhtom110/spring-boot-identity-service.git)
    cd spring-boot-identity-service
    ```

2.  **Cấu hình Database:**
    * Mở file `src/main/resources/application.yaml`.
    * Chỉnh sửa `url`, `username`, `password` của Database cho phù hợp với máy bạn.
    * *Lưu ý: Nên đặt `signerKey` (Secret Key JWT) vào biến môi trường để bảo mật.*

3.  **Chạy ứng dụng:**
    ```bash
    mvn spring-boot:run
    ```

## 🔌 API Endpoints (Ví dụ)

| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| POST | `/auth/token` | Đăng nhập lấy token |
| POST | `/auth/introspect` | Kiểm tra token hợp lệ |
| POST | `/auth/logout` | Đăng xuất |
| POST | `/users` | Đăng ký người dùng mới |
| GET | `/users/myInfo` | Lấy thông tin bản thân |

---
*Author: [khanhtom110](https://github.com/khanhtom110)*
