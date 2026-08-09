# LGPD, privacidade e segurança

## 1. Dados pessoais tratados

| Dado | Entidade | Finalidade | Base legal (Lei nº 13.709/2018) |
| --- | --- | --- | --- |
| Nome | `Cliente`, `Usuario`, `Funcionario` | Identificação no pedido e no atendimento | Execução de contrato (art. 7º, V) |
| CPF | `Cliente` | Identificação fiscal do pedido e do programa de fidelidade | Execução de contrato / obrigação legal (art. 7º, II e V) |
| E-mail | `Cliente`, `Usuario` | Autenticação e comunicação sobre o pedido | Execução de contrato (art. 7º, V) |
| Telefone | `Cliente` | Contato operacional sobre o pedido | Execução de contrato (art. 7º, V) |
| Pontos de fidelidade | `Cliente` | Programa de benefícios | Consentimento (art. 7º, I) |
| IP e rota acessada | `AccessLog`, `AuditLog` | Segurança da informação e trilha de auditoria | Legítimo interesse (art. 7º, IX) |

Não são coletados dados sensíveis (art. 5º, II) nem dados de cartão: o pagamento é delegado
ao gateway externo simulado, e a API armazena apenas a referência da transação.

## 2. Consentimento

O consentimento é registrado explicitamente por `PUT /api/clientes/{id}/consentimento`,
que grava `lgpdConsentido` e a data/hora `lgpdConsentidoEm`. O cadastro do cliente sempre
inicia com o consentimento em `false`.

O programa de fidelidade só funciona com consentimento ativo: créditos e resgates de pontos
retornam `409 Conflict` quando o cliente não consentiu, e o crédito automático após o pagamento
aprovado é simplesmente ignorado nesse caso.

## 3. Revogação, retenção e anonimização

- A revogação é feita pelo mesmo endpoint de consentimento, com `{"consentido": false}`.
- `POST /api/clientes/{id}/anonimizar` implementa o direito de eliminação (art. 18, VI):
  nome, CPF, e-mail e telefone são substituídos/removidos, e o registro é marcado como
  `anonimizado` com a data da operação.
- Os pedidos históricos são preservados sem dados pessoais identificáveis, garantindo a
  integridade contábil e operacional da rede.

## 4. Controles técnicos de segurança

| Controle | Implementação |
| --- | --- |
| Hash de senha | BCrypt (`PasswordEncoder`); somente o hash é persistido em `usuarios.senha_hash` |
| Autenticação | Token JWT assinado em HS256, com expiração configurável e sessão stateless |
| Autorização | Perfis `ADMIN`, `GERENTE` e `CLIENTE` aplicados por rota no `SecurityConfig` |
| Exposição de dados | Respostas por DTO; a senha nunca é retornada pela API |
| Logs de acesso | `AccessAuditFilter` registra usuário, método, rota, status, IP e duração |
| Auditoria de ações sensíveis | `AuditService` registra login, criação de pedido, pagamento, alteração de status, consentimento e anonimização |
| Erros | Formato JSON padronizado, sem stack trace nem detalhes internos |
