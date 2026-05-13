# Migration TODO & Requirements

## Project Requirements (from CSE 816 guidelines)
- [x] Git + GitHub version control
- [x] Jenkins CI/CD with GitHub webhook trigger
- [x] Automated tests in pipeline
- [x] Docker images pushed to Docker Hub
- [x] Docker + Docker Compose
- [x] Ansible playbooks with roles
- [x] Kubernetes with HPA
- [x] ELK Stack with Kibana dashboard
- [x] Ansible Vault for secrets
- [ ] Live changes visible after deployment

## Microservices Migration Tasks

### Phase 1: Create Service Projects
- [x] Create auth-service/ directory with full Spring Boot project
  - [x] pom.xml (Spring Boot 3.2, Security, JPA, MySQL, JWT)
  - [x] application.properties
  - [x] All auth-related Java classes extracted from monolith
  - [x] Dockerfile (multi-stage)
  - [x] logback-spring.xml
- [x] Create api-gateway/ directory with Spring Cloud Gateway
  - [x] pom.xml (Spring Cloud Gateway, JWT validation)
  - [x] application.yml with routes configured
  - [x] JWT GlobalFilter for token validation
  - [x] Dockerfile
- [x] Refactor src/ into app-service
  - [x] Remove auth-related classes (AuthController, AuthService, auth DTOs)
  - [x] Update SecurityConfig (no AuthenticationManager — not issuing tokens)
  - [x] Update application.properties port to 8082
  - [x] Update pom.xml artifactId to app-service
  - [x] Update Dockerfile (new JAR name)

### Phase 2: Docker Compose
- [x] Update docker-compose.yml with all 3 app services
- [x] All services on stockportfolio-network
- [x] Health checks for each service
- [x] Depends_on configured correctly
- [x] Volumes for logs (auth_logs, app_logs, gateway_logs)
- [x] Logstash reads from all 3 log volumes

### Phase 3: Jenkins Pipelines
- [x] Jenkinsfile (api-gateway pipeline)
- [x] Jenkinsfile.auth (auth-service pipeline)
- [x] Jenkinsfile.app (app-service pipeline)
- [x] Each pipeline builds and pushes to Docker Hub
- [x] Each pipeline triggers Ansible deploy

### Phase 4: Kubernetes
- [x] Update k8s/configmap.yaml (add service URLs, SPRING_DATASOURCE_URL)
- [x] k8s/deployment.yaml → app-service deployment (port 8082)
- [x] k8s/service.yaml → app-service NodePort (30082)
- [x] Create k8s/auth-service-deployment.yaml (Deployment + Service 30081)
- [x] Create k8s/api-gateway-deployment.yaml (Deployment + Service 30080)
- [x] Update k8s/hpa.yaml (HPA for all 3 services)
- [x] Update k8s/ingress.yaml (/ → api-gateway:8080)
- [x] k8s/elk-stack.yaml unchanged
- [x] k8s/filebeat.yaml unchanged

### Phase 5: Ansible
- [x] Update ansible/roles/app_server/tasks/main.yml (3 containers)
- [x] Update ansible/roles/kubernetes/tasks/main.yml (apply new manifests)
- [x] Update ansible/roles/elk_stack/tasks/main.yml (3 log volumes)
- [x] Keep Ansible Vault / env-var secrets

### Phase 6: ELK
- [x] Update logstash/pipeline/logstash.conf (3 file inputs, service field tagging)
- [ ] Verify Kibana shows logs from all 3 services

## Definition of Done
- [ ] git push triggers all 3 Jenkins pipelines
- [ ] All 3 Docker images pushed to Docker Hub
- [ ] All 3 services running in Kubernetes
- [ ] App accessible via api-gateway on port 8080
- [ ] Register/login works (auth-service)
- [ ] Portfolio/holdings works (app-service via gateway)
- [ ] Logs from all 3 services visible in Kibana
- [ ] kubectl get pods shows all pods Running
- [ ] kubectl get hpa shows HPA active for all 3
- [ ] kubectl get ingress shows ingress configured
