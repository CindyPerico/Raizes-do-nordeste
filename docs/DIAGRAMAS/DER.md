# DER — Modelo de dados

Diagrama entidade-relacionamento das tabelas criadas pelo mapeamento JPA
(compatível com o banco usado pela API: H2 em modo PostgreSQL, e portável para PostgreSQL/MySQL).

```mermaid
erDiagram
    USUARIOS ||--o| UNIDADES : "lotado em"
    USUARIOS ||--o| CLIENTES : "vinculado a"
    CLIENTES ||--o{ PEDIDOS : realiza
    UNIDADES ||--o{ PEDIDOS : recebe
    UNIDADES ||--o{ FUNCIONARIOS : possui
    UNIDADES ||--o{ ESTOQUE_ITENS : mantem
    UNIDADES ||--o{ PRODUTO_UNIDADE : oferece
    PRODUTOS ||--o{ PRODUTO_UNIDADE : "disponivel em"
    PRODUTOS ||--o{ ESTOQUE_ITENS : "controlado em"
    PRODUTOS ||--o{ PEDIDO_ITENS : compoe
    PEDIDOS ||--|{ PEDIDO_ITENS : contem
    PEDIDOS ||--o{ PEDIDO_EVENTOS : registra

    USUARIOS {
        bigint id PK
        varchar nome
        varchar email UK "unico"
        varchar senha_hash "BCrypt"
        varchar role "ADMIN | GERENTE | CLIENTE"
        bigint unidade_id FK
        bigint cliente_id FK
        boolean ativo
        timestamp criado_em
    }

    CLIENTES {
        bigint id PK
        varchar nome
        varchar cpf "dado pessoal"
        varchar email "dado pessoal"
        varchar telefone "dado pessoal"
        int pontos_fidelidade
        boolean lgpd_consentido
        timestamp lgpd_consentido_em
        boolean anonimizado
        timestamp anonimizado_em
    }

    UNIDADES {
        bigint id PK
        varchar nome
        varchar uf
        varchar cidade
        varchar endereco
    }

    FUNCIONARIOS {
        bigint id PK
        varchar nome
        varchar cargo
        bigint unidade_id FK
        boolean ativo
    }

    PRODUTOS {
        bigint id PK
        varchar nome
        varchar descricao
        decimal preco_base
        int mes_inicio_sazonal
        int mes_fim_sazonal
        boolean ativo
    }

    PRODUTO_UNIDADE {
        bigint id PK
        bigint produto_id FK
        bigint unidade_id FK
        boolean disponivel
        decimal preco_override
        varchar nome_override
        varchar descricao_override
    }

    ESTOQUE_ITENS {
        bigint id PK
        bigint unidade_id FK
        bigint produto_id FK
        int quantidade "nunca negativo"
    }

    PEDIDOS {
        bigint id PK
        bigint cliente_id FK "opcional (venda avulsa)"
        bigint unidade_id FK
        varchar canal_pedido "APP | TOTEM | BALCAO | PICKUP | WEB"
        varchar status "CRIADO ... FINALIZADO | CANCELADO"
        varchar status_pagamento_externo "NAO_SOLICITADO | SOLICITADO | CONFIRMADO | RECUSADO"
        varchar referencia_pagamento_externo
        timestamp criado_em
        decimal subtotal
        decimal desconto
        decimal total
    }

    PEDIDO_ITENS {
        bigint id PK
        bigint pedido_id FK
        bigint produto_id FK
        int quantidade
        decimal preco_unitario
        decimal total_item
    }

    PEDIDO_EVENTOS {
        bigint id PK
        bigint pedido_id FK
        varchar tipo
        varchar motivo
        decimal valor
        varchar criado_por
        timestamp criado_em
    }

    AUDIT_LOGS {
        bigint id PK
        varchar acao
        varchar entidade
        bigint entidade_id
        varchar detalhes
        varchar usuario
        varchar ip
        timestamp criado_em
    }

    ACCESS_LOGS {
        bigint id PK
        varchar usuario
        varchar metodo
        varchar path
        int status
        varchar ip
        bigint duracao_ms
        timestamp criado_em
    }
```

`AUDIT_LOGS` e `ACCESS_LOGS` não possuem chave estrangeira: são tabelas de trilha de auditoria,
preservadas mesmo quando o registro de origem é removido ou anonimizado.
