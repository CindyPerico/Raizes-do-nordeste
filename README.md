# Rede de Lanchonetes Raízes do Nordeste

## Projeto Multidisciplinar - Trilha Back-End

API REST desenvolvida para gerenciamento de uma rede de lanchonetes, permitindo o controle de clientes, produtos, unidades, pedidos, pagamentos e relatórios administrativos.

---

# Tecnologias Utilizadas

- Java
- Spring Boot
- Spring Data JPA
- Banco de Dados MySQL/PostgreSQL
- Maven
- Swagger/OpenAPI
- PowerShell para testes da API

---

# Funcionalidades Implementadas

## Clientes

- Cadastro de clientes;
- Atualização de dados;
- Controle de consentimento LGPD;
- Sistema de pontos de fidelidade.

## Produtos

- Cadastro de produtos;
- Controle de disponibilidade por unidade;
- Controle de estoque.

## Pedidos

- Criação de pedidos;
- Controle de status:

  - Criado
  - Em preparo
  - Pronto
  - Finalizado

## Pagamentos

- Solicitação de pagamento;
- Confirmação de pagamento.

## Relatórios

- Produtos mais vendidos;
- Relatório financeiro.

---

# Como Executar o Projeto

## Pré-requisitos

Necessário instalar:

- Java JDK 17 ou superior;
- Maven;
- Banco de dados configurado.

---

# Instalação

Clonar o projeto:

```bash
git clone URL_DO_PROJETO
```

Entrar na pasta:

```bash
cd nome-do-projeto
```

Instalar dependências:

```bash
mvn clean install
```

---

# Configuração do Banco de Dados

Configurar o acesso ao banco no arquivo:

```
src/main/resources/application.properties
```

Exemplo:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/raizes_nordeste
spring.datasource.username=usuario
spring.datasource.password=senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

# Executando a Aplicação

Executar:

```bash
mvn spring-boot:run
```

A API ficará disponível em:

```
http://localhost:8082
```

---

# Documentação da API

A documentação dos endpoints está disponível através do Swagger:

```
http://localhost:8082/swagger-ui/index.html
```

O Swagger permite visualizar e testar os recursos disponíveis na API.

---

# Testes da API

Os testes foram realizados utilizando:

```
PowerShell
```

Com o comando:

```powershell
Invoke-WebRequest
```

Foram validados os seguintes recursos:

- Cadastro de clientes;
- Produtos por unidade;
- Criação de pedidos;
- Fluxo de pagamento;
- Atualização de status dos pedidos;
- Relatórios administrativos;
- Consentimento LGPD;
- Pontos de fidelidade.

As evidências dos testes estão documentadas no arquivo:

```
Relatorio_Evidencias_Testes_API
```

---

# Estrutura do Projeto

Exemplo da organização:

```
src/main/java

├── controller
│   └── Endpoints da API
│
├── service
│   └── Regras de negócio
│
├── repository
│   └── Comunicação com banco de dados
│
├── model
│   └── Entidades do sistema
│
└── dto
    └── Objetos de transferência de dados
```

---

# Endpoints Principais

## Clientes

Cadastro de clientes:

```
POST /api/clientes
```

---

## Produtos por Unidade

Disponibilização de produtos:

```
PUT /api/unidades/{id}/produtos/{id}
```

---

## Pedidos

Criação de pedidos:

```
POST /api/pedidos
```

---

## Pagamento

Solicitação:

```
POST /api/pedidos/{id}/pagamento/solicitar
```

Confirmação:

```
POST /api/pedidos/{id}/pagamento/confirmar
```

---

## Relatórios

Produtos mais vendidos:

```
GET /api/relatorios/mais-vendidos
```

Relatório financeiro:

```
GET /api/relatorios/financeiro
```

---

# Autor

Cindy Perico

Curso: Análise e Desenvolvimento de Sistemas

Projeto Multidisciplinar - Trilha Back-End

Ano: 2026