# Relatório de Evidências de Testes da API

## Projeto

**Sistema:** Rede de Lanchonetes Raízes do Nordeste  
**Tipo:** API REST Back-End  
**Tecnologia:** Java + Spring Boot  
**Ferramenta de testes:** PowerShell utilizando Invoke-WebRequest  
**Data dos testes:** 09/08/2026  

> Este relatório registra a execução manual via PowerShell. As evidências reproduzíveis e
> atualizadas do projeto são os testes automatizados (`mvn test`) e a coleção Postman em
> `docs/postman/`, descritos no plano de testes (`TEST_PLAN.md`).

---

# 1. Objetivo dos Testes

Este documento apresenta as evidências dos testes realizados na API do sistema Raízes do Nordeste.

Os testes foram executados através de requisições HTTP, validando os principais recursos da aplicação:

- Autenticação da API;
- Cadastro e gerenciamento de clientes;
- Controle de estoque;
- Disponibilização de produtos por unidade;
- Criação e gerenciamento de pedidos;
- Fluxo de pagamento;
- Atualização de status dos pedidos;
- Relatórios administrativos.

O objetivo foi verificar se os endpoints implementados funcionam conforme os requisitos definidos no projeto.

---

# 2. Ambiente de Testes

## Servidor

```
http://localhost:8082
```

## Ferramenta utilizada

```
PowerShell
```

Comando utilizado:

```
Invoke-WebRequest
```

## Autenticação

Os endpoints protegidos foram testados utilizando autenticação Basic:

```
Authorization: Basic {token}
```

---

# 3. Resumo dos Testes Executados

| Módulo | Endpoint | Resultado |
|---|---|---|
| Produto por unidade | PUT /api/unidades/1/produtos/2 | Aprovado |
| Pedido | POST /api/pedidos | Aprovado |
| Pagamento | POST /pagamento/solicitar | Aprovado |
| Pagamento | POST /pagamento/confirmar | Aprovado |
| Status Pedido | PUT /status | Aprovado |
| Relatórios | GET /api/relatorios | Aprovado |
| Cliente | POST /api/clientes | Aprovado |
| LGPD | PUT /consentimento | Aprovado |
| Fidelidade | POST /pontos | Aprovado |

---

# 4. Teste de Produto por Unidade

## Disponibilização de produto

Endpoint:

```
PUT /api/unidades/1/produtos/2
```

Dados enviados:

```json
{
 "disponivel": true,
 "precoOverride": 22.00,
 "nomeOverride": "Produto Teste Estoque",
 "descricaoOverride": "Produto disponível na unidade matriz"
}
```

Resultado:

```
200 OK
```

Conclusão:

Produto disponibilizado corretamente para a unidade.

✅ Teste aprovado.

---

# 5. Teste de Criação de Pedido

Endpoint:

```
POST /api/pedidos
```

Dados enviados:

```json
{
 "unidadeId":1,
 "canalPedido":"APP",
 "itens":[
   {
    "produtoId":2,
    "quantidade":2
   }
 ]
}
```

Resultado:

```
201 CREATED
```

Resposta obtida:

```json
{
 "id":4,
 "unidadeId":1,
 "canalPedido":"APP",
 "status":"CRIADO"
}
```

Conclusão:

Pedido criado com sucesso.

✅ Teste aprovado.

---

# 6. Teste do Fluxo de Pagamento

## Solicitação de pagamento

Endpoint:

```
POST /api/pedidos/4/pagamento/solicitar
```

Resultado:

```
200 OK
```

Status alterado para:

```
AGUARDANDO_PAGAMENTO
```

---

## Confirmação de pagamento

Endpoint:

```
POST /api/pedidos/4/pagamento/confirmar
```

Dados enviados:

```json
{
 "confirmado":true,
 "referenciaExterna":"PAG-TESTE-0004"
}
```

Resultado:

```
200 OK
```

Status alterado para:

```
PAGO
```

Conclusão:

Fluxo de pagamento validado.

✅ Teste aprovado.

---

# 7. Teste de Atualização de Status do Pedido

Endpoint:

```
PUT /api/pedidos/4/status
```

Estados testados:

### Em preparo

```json
{
 "status":"EM_PREPARO"
}
```

Resultado:

```
200 OK
```

---

### Pronto

```
PRONTO
```

Resultado:

```
200 OK
```

---

### Finalizado

```
FINALIZADO
```

Resultado:

```
200 OK
```

Conclusão:

O ciclo completo do pedido foi validado.

✅ Teste aprovado.

---

# 8. Testes de Relatórios

## Produtos mais vendidos

Endpoint:

```
GET /api/relatorios/mais-vendidos?unidadeId=1
```

Resultado:

```
200 OK
```

Resposta:

```json
[
 {
  "produtoId":2,
  "produtoNome":"Produto Teste Estoque",
  "quantidadeVendida":2
 }
]
```

---

## Relatório financeiro

Endpoint:

```
GET /api/relatorios/financeiro?unidadeId=1
```

Resultado:

```
200 OK
```

Resposta:

```json
{
 "totalPedidos":4,
 "totalVendido":44.00,
 "totalDescontos":0.00
}
```

Conclusão:

Relatórios funcionando corretamente.

✅ Teste aprovado.

---

# 9. Testes de Cliente e LGPD

## Cadastro de cliente

Endpoint:

```
POST /api/clientes
```

Resultado:

```
201 CREATED
```

Cliente criado:

```json
{
 "id":1,
 "nome":"Cliente Teste API"
}
```

---

## Consentimento LGPD

Endpoint:

```
PUT /api/clientes/1/consentimento
```

Dados:

```json
{
 "consentido":true
}
```

Resultado:

```
200 OK
```

---

## Pontos de fidelidade

Endpoint:

```
POST /api/clientes/1/pontos
```

Dados:

```json
{
 "pontos":100
}
```

Resultado:

```
200 OK
```

Cliente atualizado:

```
pontosFidelidade = 100
```

Conclusão:

Cadastro, LGPD e fidelidade funcionando corretamente.

✅ Teste aprovado.

---

# 10. Conclusão Final

Os testes realizados comprovaram o funcionamento dos principais módulos da API da Rede de Lanchonetes Raízes do Nordeste.

Funcionalidades validadas:

✅ Produtos por unidade  
✅ Pedidos  
✅ Pagamentos  
✅ Controle de status  
✅ Relatórios  
✅ Clientes  
✅ Consentimento LGPD  
✅ Pontos de fidelidade  

A API apresentou respostas esperadas e funcionamento adequado conforme os requisitos definidos para o projeto.