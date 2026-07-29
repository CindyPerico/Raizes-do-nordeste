# Raízes do Nordeste - Back-end (API)

## 1. Introdução

Este projeto é o back-end do sistema da rede de lanchonetes **Raízes do Nordeste**. A ideia foi cobrir o básico que uma franquia precisa para operar: cadastro, estoque por unidade, pedidos e alguns relatórios.

Eu escolhi fazer como **API REST (JSON)** em **Java 17** com **Spring Boot 3**. Para facilitar os testes, usei **H2 em memória** (assim não precisa instalar banco para rodar).

Informações de execução (padrão do projeto):

- Base URL: `http://localhost:8082`
- H2 Console: `http://localhost:8082/h2-console`

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

Observação: no cadastro de cliente, o consentimento LGPD começa como `false` e pode ser atualizado pelo endpoint de consentimento.

---

## 3. Requisitos Não Funcionais (RNF)

- **LGPD**: existe consentimento e também anonimização de cliente.
- **Validação e erros**: validações nos DTOs e retorno de erros em JSON.
- **Auditoria**: operações sensíveis e acessos ficam registrados no banco.
- **Segurança (simplificada)**: para o trabalho e os testes, a API pode rodar sem autenticação.

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

## 5. Diagrama de Classes (descrição)

Principais entidades (bem resumido):

- **Cliente**: dados pessoais + pontos + flags LGPD.
- **Unidade**: identificação e localização.
- **Funcionario**: pertence a uma unidade.
- **Produto**: preço base + sazonalidade.
- **ProdutoUnidade**: vínculo produto/unidade (disponível, overrides regionais simples).
- **EstoqueItem**: quantidade por unidade/produto.
- **Pedido**: unidade/cliente + canal + status + totais.
- **PedidoItem**: itens do pedido.
- **PedidoEvento**: histórico (status/cancelamento/desconto/ajuste/pagamento).
- **AuditLog**: auditoria de operações sensíveis.
- **AccessLog**: auditoria de acessos HTTP.

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

---

## 7. Arquitetura da Solução

Eu usei uma arquitetura em camadas (padrão do Spring):

- **Controllers**: endpoints.
- **Services**: regras.
- **Repositories**: acesso ao banco.
- **DTOs**: entrada/saída e validação.
- **Middleware**: auditoria de acessos.
- **Handler de erros**: respostas de erro padronizadas.

---

## 8. Endpoints (resumo)

- `GET /` (status)

### Clientes
- `POST /api/clientes`
- `GET /api/clientes`
- `GET /api/clientes/{id}`
- `PUT /api/clientes/{id}`
- `DELETE /api/clientes/{id}`
- `PUT /api/clientes/{id}/consentimento`
- `POST /api/clientes/{id}/pontos`
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

### Produto por unidade
- `PUT /api/unidades/{unidadeId}/produtos/{produtoId}`

### Estoque
- `GET /api/unidades/{unidadeId}/estoque/produtos/{produtoId}`
- `PUT /api/unidades/{unidadeId}/estoque/produtos/{produtoId}`
- `POST /api/unidades/{unidadeId}/estoque/produtos/{produtoId}/ajuste`

### Pedidos
- `POST /api/pedidos`
- `GET /api/pedidos`
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

- Banco: H2 em memória.
- As tabelas são criadas automaticamente quando a aplicação inicia.

---

## 10. Plano de Testes

Arquivo: `TEST_PLAN.md`.

---

## 11. Evidências (checklist)

Sugestão de evidências (prints):

- Log do terminal mostrando `Tomcat started`.
- `GET /` retornando `Raizes do Nordeste API - OK`.
- CRUD Unidade: criar + listar.
- Cadastro de produto + vincular na unidade.
- Setar estoque.
- Criar pedido.
- Solicitar pagamento e confirmar.
- Relatórios: `mais-vendidos` e `financeiro`.
- H2 Console mostrando tabelas `audit_logs` e `access_logs`.

---

## 12. Conclusão

O projeto atende o que foi pedido no enunciado e funciona localmente. Além dos cadastros, dá para simular um fluxo completo de pedido (com estoque e pagamento externo) e gerar relatórios simples.

---

## 13. Referências

- Spring Boot Reference Documentation
- Maven Documentation
- H2 Database Documentation
- LGPD (Lei nº 13.709/2018)
