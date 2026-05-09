# 📈 Stock Portfolio Manager

A full-stack Stock Portfolio Manager built with Java Spring Boot, integrated with Finnhub API for live stock prices, deployed using a complete DevOps pipeline.

## 👥 Team
| Member | Role |
|---|---|
| **Chaitanya Nemade** | Backend APIs, JWT Security, Finnhub Integration, Thymeleaf UI, Jenkins CI/CD, Kubernetes |
| **Pankaj** | Docker, Docker Compose, Ansible Playbooks, Vault, ELK Stack, Kibana Dashboards |

## 🛠️ Tech Stack

### Application
| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT |
| Database | MySQL 8 |
| Frontend | Thymeleaf + HTML/CSS |
| Market Data | Finnhub API |

### DevOps Pipeline
| Stage | Tool |
|---|---|
| Version Control | Git + GitHub |
| CI/CD | Jenkins + GitHub Webhook |
| Containerization | Docker + Docker Compose |
| Configuration Management | Ansible + Vault |
| Orchestration | Kubernetes + HPA |
| Monitoring & Logging | ELK Stack (Elasticsearch, Logstash, Kibana) |

## ✨ Features
- User registration and login with JWT authentication
- Create and manage multiple stock portfolios
- Buy and sell stocks with live price tracking via Finnhub API
- Real-time profit and loss calculation per holding
- Complete transaction history for every portfolio
- Admin dashboard to monitor all users and activity
- Auto-refreshing stock prices every 60 seconds
- Role-based access control (User and Admin roles)

## 🚀 DevOps Pipeline Flow
Code Push to GitHub
↓
Jenkins detects via GitHub Webhook
↓
Run JUnit Tests (Maven)
↓
Build JAR (mvn package)
↓
Build Docker Image
↓
Push to Docker Hub
↓
Ansible deploys to Kubernetes
↓
Kubernetes runs app with HPA auto-scaling
↓
App logs → Logstash → Elasticsearch → Kibana

## ⚙️ Local Setup Instructions

### Prerequisites
- Java 21
- Maven 3.8+
- MySQL 8
- Finnhub API key (free at https://finnhub.io)

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/Chaitanya020403/stock-portfolio-manager.git
cd stock-portfolio-manager
```

**2. Create MySQL database**
```sql
CREATE DATABASE stockportfolio;
CREATE USER 'stockuser'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON stockportfolio.* TO 'stockuser'@'localhost';
FLUSH PRIVILEGES;
```

**3. Set up environment variables**
```bash
cp .env.example .env
nano .env
```
Fill in your actual values in the `.env` file.

**4. Run the application**
```bash
mvn spring-boot:run
```

**5. Open in browser**
http://localhost:8081/auth/register

**6. Create admin account (optional)**

After registering, run this in MySQL:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```
Then visit `http://localhost:8081/admin`

## 📁 Project Structure
stock-portfolio-manager/
├── src/
│   ├── main/
│   │   ├── java/com/stockportfolio/app/
│   │   │   ├── config/          # Security + App config
│   │   │   ├── controller/      # Auth, Portfolio, Holdings, Market, Admin
│   │   │   ├── dto/             # Request/Response objects
│   │   │   ├── entity/          # User, Portfolio, Holding, Transaction
│   │   │   ├── repository/      # JPA database interfaces
│   │   │   ├── security/        # JWT filter + utility
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── templates/       # Thymeleaf HTML pages
│   │       ├── static/css/      # Stylesheet
│   │       ├── application.properties
│   │       └── logback-spring.xml
│   └── test/                    # JUnit tests
├── .env.example                 # Environment variable template
├── .gitignore
└── pom.xml

## 🌿 Branch Strategy
| Branch | Owner | Purpose |
|---|---|---|
| `main` | Both | Production-ready merged code |
| `feature/auth` | Chaitanya | Authentication module |
| `feature/portfolio` | Chaitanya | Portfolio management |
| `feature/market-data` | Chaitanya | Finnhub API integration |
| `feature/jenkins` | Chaitanya | Jenkins CI/CD pipeline |
| `feature/kubernetes` | Chaitanya | Kubernetes deployment configs |
| `feature/docker` | Pankaj | Dockerfile + Docker Compose |
| `feature/ansible` | Pankaj | Ansible playbooks + Vault |
| `feature/elk` | Pankaj | ELK Stack + Kibana dashboards |

## 🔒 Security Notes
- Passwords encrypted with BCrypt
- JWT tokens stored in HTTP-only cookies
- Sensitive credentials stored in `.env` file (never committed to Git)
- Vault used for secrets management in production (Kubernetes)
- `.env` is listed in `.gitignore` — safe from accidental exposure

## 📊 API Endpoints
| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/register` | Register new user | Public |
| POST | `/auth/login` | Login and get JWT | Public |
| GET | `/portfolio` | View all portfolios | User |
| POST | `/portfolio/create` | Create portfolio | User |
| GET | `/portfolio/{id}/holdings` | View holdings + P&L | User |
| POST | `/portfolio/{id}/holdings/add` | Buy a stock | User |
| POST | `/portfolio/{id}/holdings/remove/{hid}` | Sell a stock | User |
| GET | `/market/search?symbol=AAPL` | Live stock price | User |
| GET | `/admin` | Admin dashboard | Admin only |

## 🎓 Course Information
- **Course:** CSE 816 - Software Production Engineering
- **Project:** Final Major Project - DevOps Framework Implementation
- **Institution:** IIIT Banglore

