# Raízes do Nordeste - Back-end (API)

## 1. Introdução

Este projeto é o back-end do sistema da rede de lanchonetes **Raízes do Nordeste**. A ideia foi cobrir o básico que uma franquia precisa para operar: cadastro, estoque por unidade, pedidos e alguns relatórios.

Eu escolhi fazer como **API REST (JSON)** em **Java 17** com **Spring Boot 3**. Para facilitar os testes, usei **H2 em memória** (assim não precisa instalar banco para rodar).

Informações de execução (padrão do projeto):

- Base URL: `http://localhost:8082`
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- H2 Console: `http://localhost:8082/h2-console`

O passo a passo de execução está no [`README.md`](README.md).

---

## 2. Requisitos Funcionais (RF)

- **RF01 - Manter cadastros**: clientes, unidades, funcionários e produtos (CRUD).
- **RF02 - Fidelidade**: acumular e ajustar pontos por cliente.
- **RF03 - LGPD**: registrar consentimento explícito e permitir anonimização do cliente.
- **RF04 - Produto por unidade**: controlar disponibilidade por unidade e permitir variação simples (override) de nome/descrição/preço.
- **RF05 - Sazonalidade**: bloquear venda de produto fora do período configurado.
- **RF06 - Estoque por unidade**: controlar quantidade e permitir ajustes com motivo.
- **RF07 - Pedidos**: registrar pedido com itens, canal e status; validar disponibilidade e estoque.
- **RF08 - Pagamento externo**: solicitar pagamento e receber confirmação/recusa para atualizar o pedido (sem processar pagamento).
- **RF09 - Cancelamento/Desconto/Ajustes**: registrar motivo e auditar operações.
- **RF10 - Relatórios**: consolidado (matriz) e por unidade; produtos mais vendidos e financeiro.
- **RF11 - Auditoria**: registrar operações sensíveis e acessos HTTP.
- **RF12 - Autenticação e perfis**: login com e-mail e senha, emissão de token JWT e autorização por perfil (`ADMIN`, `GERENTE`, `CLIENTE`).
- **RF13 - Cardápio por unidade**: consultar os produtos disponíveis em cada unidade, com preço praticado e saldo de estoque.
- **RF14 - Multicanalidade**: registrar o canal de origem (`canalPedido`) em todo pedido e permitir consulta/filtro por canal.

Observação: no cadastro de cliente, o consentimento LGPD começa como `false` e pode ser atualizado pelo endpoint de consentimento.

---

## 3. Requisitos Não Funcionais (RNF)

- **LGPD**: existe consentimento e também anonimização de cliente.
- **Validação e erros**: validações nos DTOs e retorno de erros em JSON.
- **Auditoria**: operações sensíveis e acessos ficam registrados no banco.
- **Segurança**: autenticação por token JWT (HS256, stateless), senhas com hash BCrypt e autorização por perfil em cada rota.
- **Documentação**: contrato OpenAPI gerado pelo springdoc e exposto no Swagger UI.
- **Tolerância a falhas na integração de pagamento**: o pagamento externo é desacoplado do pedido; a recusa ou a ausência de retorno mantêm o pedido em `AGUARDANDO_PAGAMENTO`, sem baixa de estoque, permitindo nova tentativa.
- **Desempenho em pico**: listagens paginadas (`page`/`size`) e consultas por índice de chave estrangeira; a baixa de estoque ocorre dentro de transação.

---

## 4. Casos de Uso (resumo)

- **UC01 - Manter cadastros** (clientes, unidades, funcionários, produtos).
- **UC02 - Registrar consentimento LGPD e anonimizar cliente**.
- **UC03 - Configurar produto por unidade** (disponibilidade e overrides regionais simples).
- **UC04 - Controlar estoque por unidade** (definir quantidade e ajustar com motivo).
- **UC05 - Registrar pedido** (itens, canal, validações) e acompanhar status.
- **UC06 - Pagamento externo** (solicitar e confirmar/recusar).
- **UC07 - Cancelar pedido e aplicar desconto** (com motivo).
- **UC08 - Consultar relatórios** (consolidado e por unidade).

---

Os diagramas estão em [`docs/DIAGRAMAS/CASOS_DE_USO.md`](docs/DIAGRAMAS/CASOS_DE_USO.md),
junto com a descrição detalhada da feature crítica (Realizar Pedido + Solicitar Pagamento).

---

## 5. Diagrama de Classes (descrição)

Principais entidades (bem resumido):

- **Cliente**: dados pessoais + pontos + flags LGPD.
- **Unidade**: identificação e localização.
- **Funcionario**: pertence a uma unidade.
- **Produto**: preço base + sazonalidade.
- **ProdutoUnidade**: vínculo produto/unidade (disponível, overrides regionais simples).
- **EstoqueItem**: quantidade por unidade/produto.
- **Usuario**: credenciais de acesso (e-mail, hash da senha) e perfil.
- **Pedido**: unidade/cliente + `canalPedido` + status + status do pagamento externo + totais.
- **PedidoItem**: itens do pedido.
- **PedidoEvento**: histórico (status/cancelamento/desconto/ajuste/pagamento).
- **AuditLog**: auditoria de operações sensíveis.
- **AccessLog**: auditoria de acessos HTTP.

Diagrama completo (classes e sequência do fluxo crítico): [`docs/DIAGRAMAS/CLASSES.md`](docs/DIAGRAMAS/CLASSES.md).

---

## 6. DER (resumo)

Relacionamentos principais:

- **Cliente (1) -> (N) Pedido**
- **Unidade (1) -> (N) Pedido**
- **Pedido (1) -> (N) PedidoItem**
- **Produto (1) -> (N) PedidoItem**
- **Pedido (1) -> (N) PedidoEvento**
- **Unidade (1) -> (N) Funcionario**
- **Unidade (1) -> (N) EstoqueItem**
- **Produto (1) -> (N) EstoqueItem**
- **Produto (1) -> (N) ProdutoUnidade**
- **Unidade (1) -> (N) ProdutoUnidade**
- **Usuario (N) -> (1) Unidade** e **Usuario (1) -> (1) Cliente** (vínculos opcionais)

DER completo: [`docs/DIAGRAMAS/DER.md`](docs/DIAGRAMAS/DER.md).

---

## 7. Arquitetura da Solução

Eu usei uma arquitetura em camadas (padrão do Spring):

- **Controllers**: endpoints.
- **Services**: regras.
- **Repositories**: acesso ao banco.
- **DTOs**: entrada/saída e validação.
- **Security**: autenticação JWT, autorização por perfil e handlers de 401/403.
- **Middleware**: auditoria de acessos.
- **Handler de erros**: respostas de erro padronizadas.

Detalhamento das camadas: [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md).

---

## 8. Endpoints (resumo)

Contrato completo (parâmetros, exemplos de request/response, status e padrão de erro):
[`docs/ENDPOINTS.md`](docs/ENDPOINTS.md) e Swagger UI.

- `GET /` (status)

### Autenticação
- `POST /api/auth/login`
- `POST /api/auth/registrar`
- `GET /api/auth/me`

### Clientes
- `POST /api/clientes`
- `GET /api/clientes`
- `GET /api/clientes/{id}`
- `PUT /api/clientes/{id}`
- `DELETE /api/clientes/{id}`
- `PUT /api/clientes/{id}/consentimento`
- `POST /api/clientes/{id}/pontos`
- `POST /api/clientes/{id}/pontos/resgatar`
- `POST /api/clientes/{id}/anonimizar`

### Unidades
- `POST /api/unidades`
- `GET /api/unidades`
- `GET /api/unidades/{id}`
- `PUT /api/unidades/{id}`
- `DELETE /api/unidades/{id}`

### Funcionários
- `POST /api/funcionarios`
- `GET /api/funcionarios` (opcional `?unidadeId=`)
- `GET /api/funcionarios/{id}`
- `PUT /api/funcionarios/{id}`
- `DELETE /api/funcionarios/{id}`

### Produtos
- `POST /api/produtos`
- `GET /api/produtos`
- `GET /api/produtos/{id}`
- `PUT /api/produtos/{id}`
- `DELETE /api/produtos/{id}`

### Cardápio por unidade
- `GET /api/unidades/{unidadeId}/produtos`
- `PUT /api/unidades/{unidadeId}/produtos/{produtoId}`

### Estoque
- `GET /api/unidades/{unidadeId}/estoque/produtos/{produtoId}`
- `PUT /api/unidades/{unidadeId}/estoque/produtos/{produtoId}`
- `POST /api/unidades/{unidadeId}/estoque/produtos/{produtoId}/ajuste`

### Pedidos
- `POST /api/pedidos`
- `GET /api/pedidos` (filtros `?canalPedido=`, `?status=`, `?unidadeId=` e paginação `?page=&size=`)
- `GET /api/pedidos/{id}`
- `PUT /api/pedidos/{id}/status`
- `POST /api/pedidos/{id}/pagamento/solicitar`
- `POST /api/pedidos/{id}/pagamento/confirmar`
- `POST /api/pedidos/{id}/desconto`
- `POST /api/pedidos/{id}/cancelar`
- `POST /api/pedidos/{id}/ajuste-pontos`

### Relatórios
- `GET /api/relatorios/mais-vendidos` (opcional `?unidadeId=`)
- `GET /api/relatorios/financeiro` (opcional `?unidadeId=`)

---

## 9. Modelo de Persistência

Persistência feita com **Spring Data JPA**.

- Banco: H2 em memória (portável para PostgreSQL/MySQL por variáveis de ambiente).
- As tabelas são criadas automaticamente quando a aplicação inicia.
- A carga inicial (`config/DataSeeder`) cria unidades, produtos, estoque, cliente e os três usuários de demonstração.

---

## 10. LGPD, privacidade e segurança

Dados pessoais tratados, finalidade, base legal, consentimento, anonimização e controles técnicos
estão descritos em [`docs/LGPD.md`](docs/LGPD.md). Em resumo: senha com hash BCrypt,
autenticação por token JWT, autorização por perfil, consentimento explícito para o programa de
fidelidade, anonimização do cliente e trilha de auditoria de ações sensíveis e de acessos HTTP.

---

## 11. Plano de Testes

Arquivo: [`TEST_PLAN.md`](TEST_PLAN.md), com 19 cenários (9 positivos, 9 negativos e 1 de auditoria).

Evidências reproduzíveis:

- 17 testes automatizados (JUnit 5 + MockMvc) executados por `mvn test`;
- coleção Postman em [`docs/postman/`](docs/postman) com 22 requisições e asserções automáticas.

---

## 12. Conclusão

O projeto entrega o fluxo crítico completo exigido pelo roteiro — criar pedido, validar itens e
estoque, solicitar o pagamento ao gateway externo simulado e atualizar o status do pedido —
com persistência real em banco.

As decisões de priorização seguiram o MVP obrigatório: primeiro o fluxo
Pedido → Pagamento mock → Status, depois os controles transversais (autenticação, autorização,
padronização de erros e auditoria) e, por fim, os cadastros de apoio e relatórios.

Os artefatos de modelagem se conectam diretamente ao código: as entidades do DER correspondem
às tabelas geradas pelo mapeamento JPA em `model/`; o diagrama de classes reflete o domínio e
seus enums (`CanalAtendimento`, `StatusPedido`, `StatusPagamentoExterno`); e cada caso de uso
tem um endpoint correspondente — UC05 em `POST /api/pedidos`, UC04/UC05 no par
`pagamento/solicitar` e `pagamento/confirmar`, UC06 em `PUT /api/pedidos/{id}/status`.

O pagamento foi tratado como integração simulada: a solicitação gera uma referência
`MOCK-<uuid>` e coloca o pedido em `AGUARDANDO_PAGAMENTO`; o retorno do gateway é recebido pelo
endpoint de confirmação, aprovando (pedido `PAGO`, baixa de estoque e crédito de pontos) ou
recusando (estoque preservado, permitindo nova tentativa). Todos os erros seguem um único
formato JSON, com 400/422 para validação, 401 para não autenticado, 403 para perfil sem
permissão, 404 para recurso inexistente e 409 para conflito de regra de negócio.

Ficaram como proposta de evolução: campanhas promocionais automáticas (hoje aplicadas como
desconto manual no pedido), refresh token e migrations versionadas para bancos de produção.

---

## 13. Referências

BRASIL. **Lei nº 13.709, de 14 de agosto de 2018**. Lei Geral de Proteção de Dados Pessoais (LGPD). Brasília, DF: Presidência da República, 2018.

FIELDING, Roy Thomas. **Architectural styles and the design of network-based software architectures**. 2000. Tese (Doutorado) — University of California, Irvine, 2000.

FOWLER, Martin. **Patterns of enterprise application architecture**. Boston: Addison-Wesley, 2003.

H2 DATABASE ENGINE. **H2 database documentation**. Disponível em: https://www.h2database.com/html/main.html. Acesso em: 9 ago. 2026.

OPENAPI INITIATIVE. **OpenAPI specification v3.1.0**. Disponível em: https://spec.openapis.org/oas/latest.html. Acesso em: 9 ago. 2026.

SPRING. **Spring Boot reference documentation**. Disponível em: https://docs.spring.io/spring-boot/documentation.html. Acesso em: 9 ago. 2026.

SPRING. **Spring Security reference documentation**. Disponível em: https://docs.spring.io/spring-security/reference/. Acesso em: 9 ago. 2026.
