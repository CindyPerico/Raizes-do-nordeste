# Raízes do Nordeste — API Back-end

API REST do projeto multidisciplinar da trilha Back-end para a rede de lanchonetes
**Raízes do Nordeste**. A solução entrega o fluxo crítico completo
**Pedido → Pagamento externo (mock) → Atualização de status**, com persistência em banco,
autenticação JWT com perfis, controle de estoque por unidade, programa de fidelidade com
consentimento LGPD, auditoria e documentação OpenAPI.

## 1. Requisitos

| Item | Versão |
| --- | --- |
| Java (JDK) | 17 ou superior |
| Maven | 3.9+ (ou o wrapper `./mvnw`) |
| Banco de dados | H2 em memória (padrão, já embarcado). Compatível com PostgreSQL/MySQL via variáveis de ambiente |

Não é necessário instalar banco de dados para executar o projeto.

## 2. Configuração (variáveis de ambiente)

Todas as configurações têm valor padrão, então a API roda sem nenhuma variável definida.
Para personalizar, copie o arquivo de exemplo e ajuste os valores:

```bash
cp .env.example .env
```

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `SERVER_PORT` | `8082` | Porta HTTP da API |
| `DB_URL` / `DB_DRIVER` / `DB_USERNAME` / `DB_PASSWORD` | H2 em memória | Conexão do banco |
| `JPA_DDL_AUTO` | `update` | Estratégia de criação do schema |
| `JWT_SECRET` | chave de desenvolvimento | Chave HMAC do token (use no mínimo 32 caracteres e troque em produção) |
| `JWT_EXPIRATION_MS` | `3600000` | Validade do token (1 hora) |
| `SEED_ENABLED` | `true` | Carga inicial de dados de demonstração |

No Linux/macOS as variáveis podem ser exportadas antes de iniciar a API:

```bash
export $(grep -v '^#' .env | xargs)
```

## 3. Instalar dependências e compilar

```bash
mvn clean package
```

## 4. Banco de dados e carga inicial (seed)

O schema é criado automaticamente pelo Hibernate na inicialização (não há migrations manuais).
Com `SEED_ENABLED=true`, a classe `config/DataSeeder` popula na primeira execução:

- 2 unidades (Recife e Fortaleza);
- 3 produtos com preço por unidade e estoque;
- 1 cliente de demonstração;
- 3 usuários, um por perfil.

| Perfil | E-mail | Senha |
| --- | --- | --- |
| ADMIN | `admin@raizes.com` | `admin12345` |
| GERENTE | `gerente@raizes.com` | `gerente12345` |
| CLIENTE | `maria.souza@example.com` | `cliente12345` |

> Credenciais apenas para avaliação local. Em produção, defina `SEED_ENABLED=false`.

Console do banco H2: <http://localhost:8082/h2-console> (JDBC URL `jdbc:h2:mem:raizesdb`, usuário `sa`, senha em branco).

## 5. Iniciar a API

```bash
mvn spring-boot:run
```

Ou, após o `package`:

```bash
java -jar target/raizes-do-nordeste-api-0.0.1-SNAPSHOT.jar
```

A API sobe em <http://localhost:8082> (health check em `GET /`).

## 6. Documentação da API (Swagger/OpenAPI)

- Swagger UI: <http://localhost:8082/swagger-ui/index.html> (rota curta: `/swagger-ui.html`)
- Contrato OpenAPI em JSON: <http://localhost:8082/v3/api-docs>

Para usar endpoints protegidos no Swagger: faça `POST /api/auth/login`, copie o `accessToken`
e informe-o no botão **Authorize** (esquema `bearerAuth`).

## 7. Autenticação e perfis

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@raizes.com","senha":"admin12345"}'
```

A resposta traz `accessToken`, que deve ser enviado em `Authorization: Bearer <token>`.

| Perfil | Permissões |
| --- | --- |
| `ADMIN` | Acesso total, incluindo exclusão e anonimização de clientes |
| `GERENTE` | Cadastros da rede, estoque, cardápio e relatórios |
| `CLIENTE` | Cardápio, próprios pedidos e pagamento; sem acesso a relatórios e cadastros |

## 8. Rodar os testes

```bash
mvn test
```

São 17 testes automatizados (JUnit 5 + MockMvc) cobrindo autenticação, autorização, validações,
fluxo de pedido, pagamento mock aprovado e recusado, estoque, fidelidade/LGPD e auditoria.

## 9. Coleção Postman

- Coleção: [`docs/postman/RaizesDoNordeste.postman_collection.json`](docs/postman/RaizesDoNordeste.postman_collection.json)
- Environment: [`docs/postman/RaizesDoNordeste.postman_environment.json`](docs/postman/RaizesDoNordeste.postman_environment.json)

Importe os dois arquivos no Postman, selecione o environment **Raizes do Nordeste - Local**
e execute as pastas na ordem: `Auth` → `Cardapio` → `Pedidos` → `Pagamento` →
`Fidelidade e LGPD` → `Relatorios` → `Erros`. O login já salva o token nas variáveis de ambiente
automaticamente. Também é possível rodar tudo pelo Collection Runner ou por linha de comando:

```bash
newman run docs/postman/RaizesDoNordeste.postman_collection.json \
  -e docs/postman/RaizesDoNordeste.postman_environment.json
```

## 10. Documentação complementar

| Documento | Conteúdo |
| --- | --- |
| [`docs/DIAGRAMAS/DER.md`](docs/DIAGRAMAS/DER.md) | Diagrama entidade-relacionamento |
| [`docs/DIAGRAMAS/CLASSES.md`](docs/DIAGRAMAS/CLASSES.md) | Diagrama de classes e sequência do fluxo crítico |
| [`docs/DIAGRAMAS/CASOS_DE_USO.md`](docs/DIAGRAMAS/CASOS_DE_USO.md) | Casos de uso e descrição da feature crítica |
| [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md) | Camadas e separação de responsabilidades |
| [`docs/ENDPOINTS.md`](docs/ENDPOINTS.md) | Contrato detalhado dos endpoints |
| [`docs/LGPD.md`](docs/LGPD.md) | Dados pessoais, base legal, consentimento e anonimização |
| [`TEST_PLAN.md`](TEST_PLAN.md) | Plano de testes com os cenários positivos e negativos |
| [`DOCUMENTACAO.md`](DOCUMENTACAO.md) | Documento acadêmico consolidado |
| [`docs/evidencias-execucao.txt`](docs/evidencias-execucao.txt) | Saída completa dos cenários executados contra a API |
| [`docs/imagens/`](docs/imagens) | Diagramas exportados em PNG e prints do Swagger e da auditoria |
| [`docs/pdf/`](docs/pdf) | PDF único de entrega (ABNT) e script que o gera |

### Gerar o PDF de entrega

O documento final em PDF (capa, sumário, seções do roteiro, conclusão e referências) é gerado
a partir dos diagramas e evidências do próprio repositório:

```bash
pip install reportlab pillow
python3 docs/pdf/gerar_pdf.py --aluno "Seu Nome" --ru 1234567 \
  --saida "docs/pdf/1234567_Projeto_Back_End.pdf"
```

Para uma versão editável (Word, Word online ou Google Docs), com o mesmo conteúdo e
sumário automático:

```bash
pip install python-docx pillow
python3 docs/pdf/gerar_docx.py --aluno "Seu Nome" --ru 1234567 \
  --saida "docs/pdf/1234567_Projeto_Back_End.docx"
```

## 11. Estrutura do projeto

```
src/main/java/br/com/cindyperico/raizesdonordeste
├── config/         # OpenAPI, segurança e carga inicial (seed)
├── controller/     # Camada de API (rotas e contratos)
├── dto/            # Contratos de request/response e validações
├── exception/      # Exceções de domínio e handler global de erros
├── middleware/     # Filtro de auditoria de acessos
├── model/          # Entidades de domínio e enums
├── repository/     # Persistência (Spring Data JPA)
├── security/       # JWT, filtro de autenticação e handlers 401/403
└── service/        # Casos de uso e regras de negócio
```

## 12. Limitações conhecidas

- O pagamento é **simulado**: não há integração com provedor real; a aprovação/recusa é enviada
  manualmente ao endpoint de confirmação, representando o callback do gateway.
- O banco padrão é H2 em memória, portanto os dados são perdidos ao reiniciar a aplicação.
- Promoções/campanhas estão implementadas apenas como desconto manual no pedido
  (`POST /api/pedidos/{id}/desconto`); regras automáticas ficaram como proposta.
