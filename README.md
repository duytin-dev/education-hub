# LearnHub Backend

API Spring Boot cho nền tảng học trực tuyến LearnHub: khóa học, ghi danh, thanh toán VNPay, JWT, chat, proxy gia sư AI.

Repo này **chỉ là backend**. Frontend và service AI là hai repo riêng.

| Service | Cổng | Repo |
|---------|------|------|
| Backend (repo này) | 8080 | — |
| Frontend Next.js | 3000 | `education-fe` |
| Gia sư AI FastAPI | 8000 | `education-ai` |

Frontend gọi REST `http://localhost:8080`. Backend gọi AI `http://127.0.0.1:8000`. Client **không** gọi Python trực tiếp.

## Stack

- Java 17, Spring Boot 4
- Spring Security + JWT
- Spring Data JPA, PostgreSQL, Flyway
- WebSocket STOMP (chat)
- JavaMail, Cloudinary, VNPay sandbox
- springdoc OpenAPI

## Cấu trúc

```
education/
├── pom.xml
├── .env.example
└── src/main/
    ├── java/com/iTech/education/
    │   ├── EducationApplication.java
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   ├── CourseController.java
    │   │   ├── LessonController.java
    │   │   ├── EnrollmentController.java
    │   │   ├── PaymentController.java
    │   │   ├── CommentController.java
    │   │   ├── UserController.java
    │   │   ├── CategoryController.java
    │   │   ├── AdminController.java
    │   │   ├── ChatController.java / ChatWsController.java / AdminChatController.java
    │   │   └── AiTutorController.java      POST /api/v1/ai/explain|quiz|grade
    │   ├── service/ + impl/
    │   ├── repository/
    │   ├── entity/
    │   ├── dto/
    │   ├── security/                       JWT filter, SecurityConfig
    │   ├── config/                         CORS, WebSocket, Cloudinary
    │   ├── payment/VnPayUtils.java
    │   ├── websocket/
    │   └── exception/
    └── resources/
        ├── application.properties
        ├── application-dev.yml
        └── db/migration/
            ├── V1__init_schema.sql
            ├── V2__chat.sql
            └── V3__email_verification.sql
```

## Vai trò API

| Role | Quyền chính |
|------|-------------|
| `STUDENT` | Ghi danh, học, gia sư AI, chat |
| `INSTRUCTOR` | CRUD khóa / bài **của mình** — không enroll, không `/learn` |
| `ADMIN` | Dashboard, user, khóa, danh mục, inbox chat |

`GET /api/v1/courses`: INSTRUCTOR chỉ thấy khóa `instructor_id = self`.

## Chạy

Yêu cầu: Java 17, Maven, PostgreSQL (database `education`).

```powershell
copy .env.example .env
# sửa POSTGRES_*, JWT_SECRET, MAIL_*, VNPAY_*, Cloudinary
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc IntelliJ: profile `dev`, cổng 8080.

- Swagger: http://localhost:8080/swagger-ui.html
- AI (tuỳ chọn): chạy `education-ai` trước khi dùng gia sư; không có thì endpoint AI trả lỗi rõ.

## Cấu hình

| Biến | Ý nghĩa |
|------|---------|
| `POSTGRES_*` | JDBC |
| `JWT_SECRET` | ≥ 32 ký tự |
| `PAYMENT_MODE` | `vnpay` hoặc `mock` |
| `VNPAY_*` | Sandbox TMN + hash |
| `MAIL_*` | Gmail App Password |
| `CLOUDINARY_*` | Avatar / video |
| `app.ai.base-url` | `http://127.0.0.1:8000` |
| `FRONTEND_URL` | `http://localhost:3000` (redirect verify / VNPay) |

Không commit `.env`.

## Gia sư AI

`AiTutorController` (`@PreAuthorize STUDENT` + đã enrollment) forward JSON sang FastAPI:

- `POST /api/v1/ai/explain`
- `POST /api/v1/ai/quiz`
- `POST /api/v1/ai/grade`

Hết quota Gemini / timeout được map thành message tiếng Việt, không trả 401 giả.

## Chat

REST + STOMP `ws://localhost:8080/ws/chat`. Guest: header `X-Chat-Token`. Admin inbox.
