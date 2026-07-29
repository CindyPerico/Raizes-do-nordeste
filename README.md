
# Raízes do Nordeste - API (Back-end)

Projeto acadêmico (ADS) para a disciplina de Back-end.

Criadora: Cindy Perico

## Como rodar

```bash
mvn spring-boot:run
```

API: `http://localhost:8082`

Teste rápido: `GET http://localhost:8082/`

## H2 Console

- URL: `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:mem:raizesdb`
- User: `sa`
- Password: (vazio)

## Endpoints (resumo)

- `Clientes`: `/api/clientes`
- `Unidades`: `/api/unidades`
- `Funcionários`: `/api/funcionarios`
- `Produtos`: `/api/produtos`
- `Produto por unidade`: `PUT /api/unidades/{unidadeId}/produtos/{produtoId}`
- `Estoque`: `/api/unidades/{unidadeId}/estoque/produtos/{produtoId}`
- `Pedidos`: `/api/pedidos`
- `Relatórios`: `/api/relatorios/mais-vendidos` e `/api/relatorios/financeiro`
