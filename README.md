# 🛒 E-Commerce Platform

Sistema de gerenciamento de pedidos e produtos para e-commerce com autenticação JWT, busca avançada com Elasticsearch e processamento assíncrono com Kafka.

## 📋 Sobre o Projeto

Uma plataforma completa de e-commerce que permite o gerenciamento de produtos, criação de pedidos e geração de relatórios. O sistema implementa diferentes perfis de usuário (Admin e User) com autenticação segura via JWT, busca inteligente de produtos com Elasticsearch e processamento assíncrono de pagamentos usando Kafka.

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 3.3.4**
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Spring Data Elasticsearch
  - Spring Kafka
- **MySQL** - Banco de dados principal
- **Elasticsearch** - Busca avançada de produtos
- **Kafka** - Processamento assíncrono de eventos
- **JWT** - Autenticação e autorização
- **Lombok** - Redução de código boilerplate
- **MapStruct** - Mapeamento de objetos
- **Maven** - Gerenciamento de dependências

## ✨ Funcionalidades

### 👥 Autenticação e Autorização
- Autenticação via JWT
- Dois perfis de usuário:
  - **Admin**: Gerencia produtos e extrai relatórios
  - **User**: Cria pedidos, realiza pagamentos e visualiza produtos

### 📦 Produtos
- CRUD completo de produtos
- Busca avançada com Elasticsearch:
  - Filtro por nome (com tolerância a erros de digitação)
  - Filtro por categoria
  - Filtro por faixa de preço
  - Apenas produtos em estoque
- Sincronização automática entre MySQL e Elasticsearch

### 🛍️ Pedidos
- Criação de pedidos com múltiplos produtos
- Validação de estoque em tempo real
- Cálculo automático de valores
- Processamento de pagamentos
- Status: `PENDENTE`, `PAGO`, `CANCELADO`

### 📊 Relatórios (Admin)
- Top 5 usuários que mais compraram (com filtro por data)
- Ticket médio por usuário (com filtro por data)
- Faturamento total do mês

### 🔄 Processamento Assíncrono
- Eventos Kafka para pagamentos (`order.paid`)
- Atualização automática de estoque via consumer

## 🔧 Pré-requisitos

- Java 21+
- Maven 3.8+
- Docker e Docker Compose

## 🏃 Como Rodar

### 1. Subir a infraestrutura (MySQL, Elasticsearch, Kafka)

```bash
docker-compose up -d
```

### 2. Compilar o projeto

```bash
mvn clean install
```

### 3. Rodar a aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## 📚 Documentação da API

### 🔐 Autenticação

#### Registrar usuário
```http
POST /auth/register
Content-Type: application/json

{
  "username": "usuario",
  "email": "usuario@email.com",
  "password": "senha123",
  "role": "USER"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "usuario",
  "password": "senha123"
}
```

Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "usuario",
  "role": "USER"
}
```

### 📦 Produtos (Requer autenticação)

#### Buscar produtos
```http
GET /products/search?name=notebook&category=eletronicos&minPrice=1000&maxPrice=5000
Authorization: Bearer {token}
```

#### Criar produto (Admin)
```http
POST /products
Authorization: Bearer {token}
Content-Type: application/json

{
  "nome": "Notebook Dell",
  "descricao": "Notebook i7 16GB RAM",
  "preco": 3500.00,
  "categoria": "Eletrônicos",
  "quantidadeEstoque": 10
}
```

#### Atualizar produto (Admin)
```http
PUT /products/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

#### Deletar produto (Admin)
```http
DELETE /products/{id}
Authorization: Bearer {token}
```

### 🛍️ Pedidos

#### Criar pedido
```http
POST /orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "items": [
    {
      "productId": "uuid-do-produto",
      "quantity": 2
    }
  ]
}
```

#### Pagar pedido
```http
POST /orders/{id}/pay
Authorization: Bearer {token}
```

#### Listar meus pedidos
```http
GET /orders/my-orders
Authorization: Bearer {token}
```

### 📊 Relatórios (Admin)

#### Top 5 usuários que mais compraram
```http
GET /reports/top-users?startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer {token}
```

#### Ticket médio por usuário
```http
GET /reports/avg-ticket?startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer {token}
```

#### Faturamento do mês
```http
GET /reports/current-month-revenue
Authorization: Bearer {token}
```

## 🎯 Regras de Negócio

### Pedidos
- Status inicial: `PENDENTE`
- Se algum produto não tiver estoque suficiente, o pedido é criado como `CANCELADO`
- Preços são calculados no momento da criação do pedido
- Após pagamento, um evento `order.paid` é enviado ao Kafka
- Consumer do Kafka atualiza o estoque automaticamente

### Produtos
- Apenas produtos com estoque > 0 aparecem na busca
- Criação/atualização é sincronizada automaticamente com Elasticsearch
- Busca com tolerância a erros de digitação (fuzzy search)

### Segurança
- Todas as rotas (exceto `/auth/**`) requerem autenticação
- Rotas de admin verificam a role do usuário
- JWT expira após 24 horas

## 🏗️ Arquitetura

```
ecommerce-platform/
├── src/main/java/br/com/vcoroa/
│   ├── config/          # Configurações (Security, Kafka, Elasticsearch)
│   ├── controller/      # Controllers REST
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # Entidades JPA
│   ├── document/        # Documentos Elasticsearch
│   ├── repository/      # Repositórios (JPA e Elasticsearch)
│   ├── service/         # Lógica de negócio
│   ├── security/        # JWT e autenticação
│   ├── kafka/           # Producers e Consumers
│   └── exception/       # Tratamento de exceções
└── src/test/           # Testes unitários e de integração
```

## 🧪 Testes

```bash
# Rodar todos os testes
mvn test

# Rodar testes com cobertura
mvn test jacoco:report
```

Consulte o [TESTING_GUIDE.md](TESTING_GUIDE.md) para mais informações sobre testes.

## 📝 Collections

O projeto inclui collections para facilitar o teste das APIs:
- `Postman_Collection.json` - Importar no Postman
- `Insomnia_Collection.json` - Importar no Insomnia

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais como parte de um case técnico.

---

Desenvolvido com ☕ e Java
