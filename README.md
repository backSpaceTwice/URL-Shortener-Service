# URL Shortener Service

A URL-shortening application with a Spring Boot API and a minimal Next.js interface.

## Project structure

- `back-end` — Java 21, Spring Boot, JPA, H2/PostgreSQL, and bearer-token authentication
- `front-end` — React and Next.js URL submission interface

## Run locally

Start the backend:

```shell
cd back-end
./mvnw spring-boot:run
```

In another terminal, start the frontend:

```shell
cd front-end
npm install
npm run dev
```

The frontend runs at `http://localhost:3000` and connects to the backend at
`http://localhost:8080`. Set `BACKEND_URL` for the frontend if the API uses a
different address.

## Test and build

```shell
cd back-end
./mvnw test
```

```shell
cd front-end
npm run build
```
