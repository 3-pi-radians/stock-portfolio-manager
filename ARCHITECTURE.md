# Microservices Architecture

## Overview
Three services:
1. **api-gateway** (port 8080) — Spring Cloud Gateway, JWT validation, routing
2. **auth-service** (port 8081) — Authentication only (register, login, logout)
3. **app-service** (port 8082) — Portfolio, Holdings, Market, Admin

## Service Breakdown

### api-gateway
- Technology: Spring Cloud Gateway (WebFlux / reactive)
- Port: 8080
- Routes:
  - `/auth/**` → auth-service:8081
  - `/css/**` → app-service:8082 (static assets)
  - `/**` (catch-all) → app-service:8082
- JWT validation via GlobalFilter: allows `/auth/**` and `/css/**` without token; all other paths require valid 'jwt' cookie
- No database, no business logic

### auth-service
- Technology: Spring Boot 3.2, Spring Security, Spring Data JPA
- Port: 8081
- Database: MySQL (stockportfolio db, **users table only**)
- Endpoints:
  - GET  /auth/register → register form
  - POST /auth/register → create user
  - GET  /auth/login    → login form
  - POST /auth/login    → validate credentials, set JWT cookie, redirect to /portfolio
  - GET  /auth/logout   → clear JWT cookie, redirect to /auth/login
- Issues JWT tokens (only service that does)
- Package: com.stockportfolio.auth

### app-service
- Technology: Spring Boot 3.2, Spring Security, Spring Data JPA, Thymeleaf
- Port: 8082
- Database: MySQL (stockportfolio db, all tables including users for JPA FK)
- Endpoints: all portfolio, holdings, market, admin endpoints
- Validates JWT via JwtAuthFilter (does NOT issue tokens)
- Keeps User entity for JPA relationship with Portfolio
- Package: com.stockportfolio.app

## Inter-Service Communication
```
Browser → api-gateway:8080
  ├── /auth/**  → auth-service:8081
  └── /**       → app-service:8082
```
JWT cookie is set by auth-service, forwarded by gateway, validated by app-service using shared JWT_SECRET.

## Database Strategy
- Single MySQL instance, shared database 'stockportfolio'
- auth-service owns: **users** table (schema managed by auth-service's ddl-auto=update)
- app-service owns: **portfolios, holdings, transactions** tables; reads users table for JPA FK
- Both services point to same MySQL host/db with same credentials

## Docker Network
- All services on: `stockportfolio-network`
- MySQL accessible as hostname `mysql` on the network
- ELK on same network (connected via `docker network connect`)

## Port Map
| Service       | Internal Port | Docker External | K8s NodePort |
|---------------|:------------:|:---------------:|:------------:|
| api-gateway   | 8080         | 8080            | 30080        |
| auth-service  | 8081         | 8081            | 30081        |
| app-service   | 8082         | 8082            | 30082        |
| mysql         | 3306         | 3306            | —            |
| elasticsearch | 9200         | 9200            | —            |
| logstash      | 5044/5000    | 5044/5000       | —            |
| kibana        | 5601         | 5601            | —            |

## Kubernetes Architecture
- Namespace: default
- Deployments: api-gateway, auth-service, app-service (+ elasticsearch, logstash, kibana in elk-stack.yaml)
- Services: NodePort for api-gateway (30080), auth-service (30081), app-service (30082)
- HPA: api-gateway, auth-service, app-service — min 2 / max 5, CPU 70%
- Ingress: / → api-gateway:8080, /kibana → kibana:5601
- ConfigMap: DB_HOST, DB_PORT, DB_NAME, SPRING_DATASOURCE_URL, service URLs
- Secrets: DB_USERNAME, DB_PASSWORD, JWT_SECRET, FINNHUB_API_KEY

## CI/CD Architecture
- 3 Jenkins pipelines:
  - `Jenkinsfile`      → api-gateway pipeline  (image: 3piradians/api-gateway)
  - `Jenkinsfile.auth` → auth-service pipeline (image: 3piradians/auth-service)
  - `Jenkinsfile.app`  → app-service pipeline  (image: 3piradians/app-service)
- Each pipeline: clone → test → build JAR → docker build → push → ansible deploy
- Ansible deploy stage passes image_tag + credentials as extra-vars

## Log Flow
```
auth-service  → /app/logs/auth-service.log  → auth_logs volume
app-service   → /app/logs/app-service.log   → app_logs volume
api-gateway   → /app/logs/api-gateway.log   → gateway_logs volume

Docker Compose:
  logstash reads volumes via file input → elasticsearch → kibana

Kubernetes:
  Filebeat DaemonSet reads /var/log/pods → logstash beats:5044 → elasticsearch → kibana
```
