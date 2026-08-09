# Contrato da API — endpoints

Base URL local: `http://localhost:8082`
Todos os endpoints, exceto os indicados como públicos, exigem o cabeçalho
`Authorization: Bearer <accessToken>`.

## Padrão de erro

Todas as falhas retornam o mesmo formato:

```json
{
  "timestamp": "2026-08-09T22:10:00.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Estoque insuficiente",
  "path": "/api/pedidos"
}
```

| Status | Quando ocorre |
| --- | --- |
| `200` | Consulta ou atualização bem-sucedida |
| `201` | Recurso criado |
| `204` | Remoção bem-sucedida, sem corpo |
| `400` | Payload inválido: campo obrigatório ausente, tipo ou formato incorreto |
| `401` | Token ausente, inválido ou expirado; credenciais incorretas |
| `403` | Perfil sem permissão para o recurso |
| `404` | Recurso inexistente |
| `409` | Conflito de regra de negócio (estoque, transição de status, saldo de pontos) |
| `422` | Violação de regra de validação de domínio |
| `500` | Erro inesperado |

## /auth

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | público | Autentica e devolve o token JWT |
| POST | `/api/auth/registrar` | público | Cadastra um usuário com perfil `CLIENTE` |
| GET | `/api/auth/me` | autenticado | Dados do usuário do token |

**POST /api/auth/login**

```json
{ "email": "admin@raizes.com", "senha": "admin12345" }
```

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "nome": "Administrador da Rede",
  "email": "admin@raizes.com",
  "role": "ADMIN"
}
```

Status: `200` sucesso · `400` payload inválido · `401` credenciais incorretas.

## /unidades e cardápio

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| POST | `/api/unidades` | ADMIN, GERENTE | Cadastra unidade |
| GET | `/api/unidades` | autenticado | Lista unidades |
| GET | `/api/unidades/{id}` | autenticado | Consulta unidade |
| PUT | `/api/unidades/{id}` | ADMIN, GERENTE | Atualiza unidade |
| DELETE | `/api/unidades/{id}` | ADMIN, GERENTE | Remove unidade |
| GET | `/api/unidades/{unidadeId}/produtos` | autenticado | **Cardápio da unidade** com preço praticado e estoque |
| PUT | `/api/unidades/{unidadeId}/produtos/{produtoId}` | ADMIN, GERENTE | Disponibiliza/atualiza item no cardápio |

**GET /api/unidades/1/produtos** → `200`

```json
[
  {
    "produtoId": 1,
    "nome": "Baião de Dois",
    "descricao": "Arroz, feijão de corda, queijo coalho e carne seca",
    "preco": 32.90,
    "disponivel": true,
    "quantidadeEmEstoque": 50
  }
]
```

## /produtos

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| POST | `/api/produtos` | ADMIN, GERENTE | Cadastra produto |
| GET | `/api/produtos?page=0&size=10` | autenticado | Lista paginada |
| GET | `/api/produtos/{id}` | autenticado | Consulta produto |
| PUT | `/api/produtos/{id}` | ADMIN, GERENTE | Atualiza produto |
| DELETE | `/api/produtos/{id}` | ADMIN, GERENTE | Remove produto |

## /estoque

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| GET | `/api/unidades/{unidadeId}/estoque/produtos/{produtoId}` | autenticado | Saldo do produto na unidade |
| PUT | `/api/unidades/{unidadeId}/estoque/produtos/{produtoId}` | ADMIN, GERENTE | Define a quantidade |
| POST | `/api/unidades/{unidadeId}/estoque/produtos/{produtoId}/ajuste` | ADMIN, GERENTE | Entrada/saída com motivo (`delta` positivo ou negativo) |

Ajuste que deixaria o saldo negativo retorna `409`.

## /pedidos

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| POST | `/api/pedidos` | autenticado | Cria pedido (exige `canalPedido`) |
| GET | `/api/pedidos` | autenticado | Lista paginada com filtros |
| GET | `/api/pedidos/{id}` | autenticado | Consulta pedido |
| PUT | `/api/pedidos/{id}/status` | autenticado | Atualiza o status |
| POST | `/api/pedidos/{id}/desconto` | ADMIN, GERENTE | Aplica desconto promocional |
| POST | `/api/pedidos/{id}/cancelar` | autenticado | Cancela com motivo |

Query params de `GET /api/pedidos`: `canalPedido` (`APP`, `TOTEM`, `BALCAO`, `PICKUP`, `WEB`),
`status`, `unidadeId`, `page` (padrão `0`) e `size` (padrão `10`).

Exemplo: `GET /api/pedidos?canalPedido=TOTEM&status=PAGO&page=0&size=10`

**POST /api/pedidos**

```json
{
  "canalPedido": "TOTEM",
  "clienteId": 1,
  "unidadeId": 1,
  "itens": [{ "produtoId": 1, "quantidade": 2 }]
}
```

`201 Created`:

```json
{
  "id": 1,
  "clienteId": 1,
  "unidadeId": 1,
  "canalPedido": "TOTEM",
  "status": "CRIADO",
  "statusPagamentoExterno": "NAO_SOLICITADO",
  "referenciaPagamentoExterno": null,
  "criadoEm": "2026-08-09T22:10:00Z",
  "subtotal": 65.80,
  "desconto": 0.00,
  "total": 65.80
}
```

Status: `201` criado · `400` `canalPedido` ausente/inválido ou quantidade inválida ·
`404` unidade, cliente ou produto inexistente · `409` produto indisponível, fora de sazonalidade
ou estoque insuficiente.

**Resposta paginada de `GET /api/pedidos`**

```json
{
  "content": [ { "id": 1, "canalPedido": "TOTEM", "status": "PAGO" } ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

## /pagamentos (mock)

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| POST | `/api/pedidos/{id}/pagamento/solicitar` | autenticado | Envia a cobrança ao gateway simulado |
| POST | `/api/pedidos/{id}/pagamento/confirmar` | autenticado | Recebe o retorno do gateway |

A solicitação move o pedido para `AGUARDANDO_PAGAMENTO`, define
`statusPagamentoExterno = SOLICITADO` e gera a referência `MOCK-<uuid>`.

Confirmação:

```json
{ "confirmado": true, "referenciaExterna": "MOCK-APROVADO-001" }
```

- `confirmado: true` → pedido `PAGO`, baixa de estoque e crédito de pontos de fidelidade.
- `confirmado: false` → `statusPagamentoExterno = RECUSADO`, pedido permanece
  `AGUARDANDO_PAGAMENTO` e o estoque não é movimentado.
- Confirmar antes de solicitar retorna `409`.

## /fidelidade (clientes)

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| POST | `/api/clientes` | autenticado | Cadastra cliente |
| GET | `/api/clientes?page=0&size=10` | autenticado | Lista paginada |
| GET | `/api/clientes/{id}` | autenticado | Consulta cliente |
| PUT | `/api/clientes/{id}` | autenticado | Atualiza cliente |
| DELETE | `/api/clientes/{id}` | ADMIN | Remove cliente |
| PUT | `/api/clientes/{id}/consentimento` | autenticado | Registra/revoga o consentimento LGPD |
| POST | `/api/clientes/{id}/pontos` | autenticado | Credita pontos (exige consentimento) |
| POST | `/api/clientes/{id}/pontos/resgatar` | autenticado | Resgata pontos (exige saldo) |
| POST | `/api/clientes/{id}/anonimizar` | ADMIN | Anonimiza os dados pessoais |
| POST | `/api/pedidos/{id}/ajuste-pontos` | ADMIN, GERENTE | Ajuste manual de pontos do cliente do pedido |

Regra de acúmulo automático: a cada R$ 10,00 pagos, 1 ponto é creditado ao cliente do pedido,
somente quando o pagamento é aprovado e o cliente consentiu com o programa.

## /relatorios

| Método | Rota | Perfil | Descrição |
| --- | --- | --- | --- |
| GET | `/api/relatorios/mais-vendidos?unidadeId=1` | ADMIN, GERENTE | Produtos mais vendidos |
| GET | `/api/relatorios/financeiro?unidadeId=1` | ADMIN, GERENTE | Consolidação financeira |

## Promoções e campanhas (regra documentada)

Campanhas promocionais são aplicadas hoje pelo endpoint de desconto
(`POST /api/pedidos/{id}/desconto`), que registra valor e motivo em `pedido_eventos` e recalcula
o total. A evolução proposta é uma entidade `Campanha` (vigência, canal, unidade, percentual e
valor mínimo) avaliada automaticamente na criação do pedido, reutilizando o mesmo cálculo de
desconto já implementado.
