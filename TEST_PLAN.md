# Plano de testes — API Raízes do Nordeste

## 1. Como reproduzir

**Testes automatizados (JUnit 5 + MockMvc)**

```bash
mvn test
```

São 17 testes que sobem o contexto completo da aplicação com banco H2 e seed próprios
(perfil `test`), sem dependência de serviços externos.

**Testes manuais (Postman)**

1. Suba a API: `mvn spring-boot:run` (porta 8082, seed habilitado).
2. Importe `docs/postman/RaizesDoNordeste.postman_collection.json` e
   `docs/postman/RaizesDoNordeste.postman_environment.json`.
3. Selecione o environment **Raizes do Nordeste - Local**.
4. Execute as pastas na ordem: `Auth` → `Cardapio` → `Pedidos` → `Pagamento` →
   `Fidelidade e LGPD` → `Relatorios` → `Erros`.
   O login salva `token` e `tokenCliente` automaticamente; a criação do pedido salva `pedidoId`.
5. Alternativa por linha de comando:
   `newman run docs/postman/RaizesDoNordeste.postman_collection.json -e docs/postman/RaizesDoNordeste.postman_environment.json`

Pré-condição comum a todos os cenários: aplicação em execução com o seed aplicado
(2 unidades, 3 produtos com estoque, 1 cliente e 3 usuários).

## 2. Cenários

### 2.1 Positivos

| ID | Cenário | Endpoint | Pré-condição | Entrada | Esperado | Evidência |
| --- | --- | --- | --- | --- | --- | --- |
| T01 | Login válido | `POST /api/auth/login` | Usuário do seed | `{"email":"admin@raizes.com","senha":"admin12345"}` | `200` + `accessToken` e `role: ADMIN` | Postman `Auth/T01`; teste `AutenticacaoTest.loginValido` |
| T02 | Consulta do usuário autenticado | `GET /api/auth/me` | Token válido | — | `200` + e-mail do usuário do token | Postman `Auth/T03` |
| T03 | Cardápio por unidade | `GET /api/unidades/1/produtos` | Produtos vinculados à unidade | path `unidadeId=1` | `200` + itens com preço e `quantidadeEmEstoque` | Postman `Cardapio/T04`; teste `PedidoFluxoTest.cardapioPorUnidade` |
| T04 | Criar pedido com itens válidos | `POST /api/pedidos` | Estoque suficiente | `{"canalPedido":"TOTEM","clienteId":1,"unidadeId":1,"itens":[{"produtoId":1,"quantidade":2}]}` | `201` + `canalPedido: TOTEM`, `status: CRIADO`, `total` calculado | Postman `Pedidos/T06`; teste `PedidoFluxoTest.fluxoPedidoPagamentoStatus` |
| T05 | Filtrar pedidos por canal | `GET /api/pedidos?canalPedido=TOTEM` | Pedido criado em T04 | query `canalPedido=TOTEM&page=0&size=10` | `200` + resposta paginada só com pedidos `TOTEM` | Postman `Pedidos/T07`; teste `PedidoFluxoTest.filtrarPorCanal` |
| T06 | Pagamento mock aprovado atualiza o pedido | `POST /api/pedidos/{id}/pagamento/solicitar` e `/confirmar` | Pedido criado | `{"confirmado":true}` | `200` + `status: PAGO`, `statusPagamentoExterno: CONFIRMADO` e estoque decrementado | Postman `Pagamento/T08` e `T09`; teste `PedidoFluxoTest.fluxoPedidoPagamentoStatus` |
| T07 | Atualizar status do pedido | `PUT /api/pedidos/{id}/status` | Pedido pago | `{"status":"EM_PREPARO"}` | `200` + `status: EM_PREPARO` | Postman `Pagamento/T10` |
| T08 | Fidelidade com consentimento | `PUT /api/clientes/1/consentimento` + `POST /api/clientes/1/pontos` | Cliente cadastrado | `{"consentido":true}` e `{"pontos":20}` | `200` + `lgpdConsentido: true` e saldo de pontos atualizado | Postman `Fidelidade e LGPD/T12` e `T13`; teste `FidelidadeLgpdTest.fidelidadeComConsentimento` |
| T09 | Relatório financeiro | `GET /api/relatorios/financeiro?unidadeId=1` | Perfil ADMIN | query `unidadeId=1` | `200` + `totalVendido` | Postman `Relatorios/T16` |

### 2.2 Negativos

| ID | Cenário | Endpoint | Pré-condição | Entrada | Esperado | Evidência |
| --- | --- | --- | --- | --- | --- | --- |
| T10 | Acesso sem token | `GET /api/pedidos` | — | Sem `Authorization` | `401` + erro padronizado (`timestamp`, `status`, `error`, `message`, `path`) | Postman `Erros/T17`; teste `AutenticacaoTest.semToken` |
| T11 | Perfil sem permissão | `GET /api/relatorios/financeiro` | Token de `CLIENTE` | `Authorization: Bearer <tokenCliente>` | `403` + "Acesso negado" | Postman `Erros/T18`; teste `AutenticacaoTest.clienteSemPermissao` |
| T12 | Credenciais inválidas | `POST /api/auth/login` | — | Senha incorreta | `401` | Teste `AutenticacaoTest.loginInvalido` |
| T13 | Campo obrigatório ausente | `POST /api/pedidos` | Token válido | Body sem `canalPedido` | `400` + mensagem citando `canalPedido` | Postman `Erros/T19`; teste `PedidoFluxoTest.pedidoSemCanal` |
| T14 | Formato/valor inválido | `POST /api/pedidos` e `POST /api/clientes` | Token válido | `quantidade: -3`; `email: "nao-eh-email"` | `400` + erro de validação | Postman `Erros/T22`; teste `FidelidadeLgpdTest.emailInvalido` |
| T15 | Produto inexistente | `POST /api/pedidos` | Token válido | `produtoId: 9999` | `404` | Postman `Erros/T20`; teste `PedidoFluxoTest.pedidoProdutoInexistente` |
| T16 | Estoque insuficiente | `POST /api/pedidos` | Estoque menor que o pedido | `quantidade: 9999` | `409` + "Estoque insuficiente" | Postman `Erros/T21`; teste `PedidoFluxoTest.pedidoEstoqueInsuficiente` |
| T17 | Pagamento mock recusado | `POST /api/pedidos/{id}/pagamento/confirmar` | Pagamento solicitado | `{"confirmado":false}` | `200` + `statusPagamentoExterno: RECUSADO`, pedido segue `AGUARDANDO_PAGAMENTO` e estoque intacto | Postman `Pagamento/T11`; teste `PedidoFluxoTest.pagamentoRecusado` |
| T18 | Fidelidade sem consentimento / sem saldo | `POST /api/clientes/{id}/pontos` e `/pontos/resgatar` | Cliente sem consentimento | `{"pontos":10}` e resgate acima do saldo | `409` nos dois casos | Teste `FidelidadeLgpdTest.fidelidadeComConsentimento` |

### 2.3 Logs e auditoria

| ID | Cenário | Verificação | Evidência |
| --- | --- | --- | --- |
| T19 | Ação sensível gera registro de auditoria | A criação de pedido grava a ação `PEDIDO_CRIADO` em `audit_logs`; toda requisição gera registro em `access_logs` | Teste `PedidoFluxoTest.auditoriaDoPedido`; consulta `SELECT * FROM audit_logs` no H2 Console |

## 3. Resumo

- Total de cenários documentados: **19** (9 positivos, 9 negativos e 1 de auditoria).
- Cobertura automatizada: 17 testes, todos executados por `mvn test`.
- Cobertura manual: 22 requisições na coleção Postman, com asserções (`pm.test`) em cada uma.
