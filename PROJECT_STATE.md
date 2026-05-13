# Current Project State

## What is Working
- Jenkins CI/CD pipeline: 6 stages (clone → test → build JAR → docker build → push to DockerHub → ansible deploy), triggered manually
- Docker Compose: all services start with `docker-compose up` (mysql, app, elasticsearch, logstash, kibana)
- Ansible roles: mysql, app_server (force-removes container, starts via docker run on stockportfolio-network), elk_stack, kubernetes
- Kubernetes: Minikube cluster, all manifests apply, ELK and Filebeat pods created; app pods had connectivity issues (MySQL not on cluster network — fixed by adding SPRING_DATASOURCE_URL to ConfigMap pointing to Minikube host IP)
- Docker Hub image: 3piradians/stock-portfolio-manager (latest + build-number tags)
- App UI: Thymeleaf, register/login/logout, portfolio CRUD, holdings with live Finnhub quotes, market search

## Current Architecture
- **Type**: Monolith
- Single Spring Boot app on port 8081
- Single MySQL database (stockportfolio)

### Controllers
| Controller | Path | Description |
|---|---|---|
| AuthController | /auth/** | Register, login, logout |
| PortfolioController | /portfolio/** | Portfolio CRUD |
| HoldingController | /portfolio/{id}/holdings/** | Holdings CRUD with live prices |
| MarketController | /market/** | Stock search via Finnhub |

### Services
| Service | Description |
|---|---|
| AuthService | User registration, JWT login |
| PortfolioService | Portfolio CRUD, getCurrentUser() via SecurityContext |
| HoldingService | Holdings add/remove, transaction log |
| MarketService | Finnhub API calls for stock quotes |

### Entities / Repositories
- User (users table) → UserRepository
- Portfolio (portfolios table) → PortfolioRepository
- Holding (holdings table) → HoldingRepository
- Transaction (transactions table) → TransactionRepository

### Security
- JwtUtil: generates and validates HS256 tokens, secret from JWT_SECRET env var
- JwtAuthFilter: reads 'jwt' cookie, validates, sets SecurityContext
- CustomUserDetailsService: loads User from DB by username

## Current DevOps Stack
- **Jenkins**: Pipeline on localhost, credentials stored (DB_USERNAME, DB_PASSWORD, JWT_SECRET, FINNHUB_API_KEY, dockerhub-credentials)
- **Docker**: Multi-stage Dockerfile (maven build → eclipse-temurin runtime), image 3piradians/stock-portfolio-manager
- **Docker Compose**: mysql (healthcheck), app (depends_on mysql+ES), elasticsearch, logstash (file input from app_logs volume), kibana
- **Ansible**: inventory localhost (ansible_connection=local), secrets from env vars, 4 roles (mysql, app_server, elk_stack, kubernetes)
- **Kubernetes**: Minikube (docker driver), configmap, secrets, deployment (2 replicas), NodePort service (30081), HPA (2–5, CPU 70%), Nginx ingress, ELK on K8s, Filebeat DaemonSet
- **ELK**: Logstash grok parses Spring Boot log format, tags by service (auth_activity, holding_activity, market_activity, portfolio_activity), Kibana on 5601

## Current Problems / Limitations
- Single monolith: all auth + business logic in one JAR
- K8s pods can't reach MySQL (MySQL runs as Docker container on host; fixed with SPRING_DATASOURCE_URL=jdbc:mysql://192.168.49.1:3306 in ConfigMap)
- HPA shows `<unknown>` CPU (metrics-server addon enabled but metrics not yet scraping)
- Ingress has no ADDRESS (nginx-ingress addon enabled but may need time)
- Logstash reads only from a single app_logs volume
- No service-level log tagging (only controller-level tagging)

## Migration Status
- [ ] auth-service created
- [ ] api-gateway created
- [ ] app-service refactored (port 8082, auth classes removed)
- [ ] Docker Compose updated
- [ ] Jenkins pipelines created (3 Jenkinsfiles)
- [ ] Kubernetes manifests updated (3 deployments, updated HPA/ingress)
- [ ] ELK updated (3 log sources)
- [ ] Ansible updated (3 containers in app_server role)
