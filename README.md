# Document Signature App

Enterprise-style Java project for secure PDF upload, signature placement, tokenized public signing links, signed PDF generation, status workflows, and audit logs.

## Tech Stack

- Backend: Java 17+, Spring Boot, Spring Security, JWT, Spring Data JPA, H2 by default, PostgreSQL/MySQL-ready
- PDF: Apache PDFBox
- Frontend: React, Vite, Tailwind CSS, react-pdf, dnd-kit
- Other: Java Mail Sender, local file storage

## Features

- User registration and login with BCrypt password hashing
- JWT-secured APIs
- PDF upload with owner-based access control
- PDF preview in React
- Drag-and-drop signature placement
- Tokenized public signing URLs
- Sign/reject workflow
- Signed PDF generation with PDFBox
- Audit trail with action, actor, timestamp, and IP address
- Dashboard filters by document status

## Folder Structure

```text
signature-app/
  backend/
    src/main/java/com/signatureapp/
      config/
      controller/
      dto/
      model/
      repository/
      security/
      service/
      util/
    src/main/resources/application.yml
  frontend/
    src/components/
    src/pages/
    src/utils/
```

## Run Locally

Start the backend:

```bash
cd backend
mvn spring-boot:run
```

Start the frontend in another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

H2 JDBC URL:

```text
jdbc:h2:file:./data/signatureapp
```

## API Endpoints

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`

Documents:

- `POST /api/docs/upload`
- `GET /api/docs`
- `GET /api/docs/{id}`
- `GET /api/docs/{id}/file`
- `GET /api/docs/{id}/signed-file`

Signatures:

- `POST /api/signatures`
- `GET /api/signatures/{docId}`
- `POST /api/signatures/finalize/{docId}`

Public Signing:

- `GET /api/public/sign/{token}`
- `GET /api/public/sign/{token}/file`
- `POST /api/public/sign/{token}`
- `POST /api/public/sign/{token}/reject`

Audit:

- `GET /api/audit/{docId}`

## PostgreSQL Example

Replace the datasource section in `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/signature_app
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

## MySQL Example

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/signature_app
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

## Notes

- Email sending is attempted through `localhost:1025`. If no mail server is running, the backend prints the development signing link to the console.
- For production, replace `app.jwt.secret`, use cloud storage, enable HTTPS, and add legally compliant certificate-backed digital signatures if your jurisdiction requires cryptographic signing.
