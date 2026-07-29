# Plano de testes (manual)

## 1) Autenticação

- Acessar `GET /api/clientes` sem Basic Auth -> deve retornar 401.
- Acessar com Basic Auth `matriz/matriz123` -> deve retornar 200.

## 2) Clientes + LGPD

- Criar cliente (POST)
- Atualizar consentimento (PUT `/consentimento`)
- Adicionar pontos (POST `/pontos`)
- Anonimizar (POST `/anonimizar`)
- Verificar se operações apareceram em `audit_logs`.

## 3) Unidades

- Criar unidade (POST)
- Listar unidades (GET)

## 4) Produtos + Unidade

- Criar produto (POST)
- Vincular produto na unidade com disponivel=true (PUT `/api/unidades/{unidadeId}/produtos/{produtoId}`)

## 5) Estoque

- Setar estoque inicial do produto na unidade (PUT `/api/unidades/{unidadeId}/estoque/produtos/{produtoId}`)
- Ajustar estoque (POST ajuste)
- Testar erro: ajuste que deixaria negativo -> deve retornar 400.

## 6) Pedido

- Criar pedido com itens (POST `/api/pedidos`)
- Solicitar pagamento (POST `/pagamento/solicitar`)
- Confirmar pagamento (POST `/pagamento/confirmar` com confirmado=true)
- Verificar baixa de estoque.

## 7) Cancelamento e desconto

- Criar novo pedido
- Aplicar desconto
- Cancelar pedido

## 8) Relatórios

- Consultar `GET /api/relatorios/mais-vendidos`
- Consultar `GET /api/relatorios/financeiro`
- Consultar os mesmos com `?unidadeId=`

## 9) Auditoria de acessos

- Fazer algumas chamadas e verificar tabela `access_logs`.
