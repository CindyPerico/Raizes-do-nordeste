"""Gera o PDF unico de entrega (normas ABNT) do Projeto Multidisciplinar - Trilha Back-End.

Uso:
    python3 docs/pdf/gerar_pdf.py --aluno "Nome Completo" --ru 1234567 \
        --saida "1234567_Projeto_Back_End.pdf"
"""

import argparse
import os

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    Image,
    NextPageTemplate,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)
from reportlab.platypus.tableofcontents import TableOfContents

BASE = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
IMG = os.path.join(BASE, "docs", "imagens")

FONT_DIR = "/usr/share/fonts/truetype/liberation"
pdfmetrics.registerFont(TTFont("Serif", f"{FONT_DIR}/LiberationSerif-Regular.ttf"))
pdfmetrics.registerFont(TTFont("Serif-Bold", f"{FONT_DIR}/LiberationSerif-Bold.ttf"))
pdfmetrics.registerFont(TTFont("Serif-Italic", f"{FONT_DIR}/LiberationSerif-Italic.ttf"))
pdfmetrics.registerFont(TTFont("Mono", "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf"))
pdfmetrics.registerFontFamily("Serif", normal="Serif", bold="Serif-Bold", italic="Serif-Italic")

ss = getSampleStyleSheet()
BODY = ParagraphStyle(
    "Corpo", parent=ss["Normal"], fontName="Serif", fontSize=12, leading=18,
    alignment=TA_JUSTIFY, firstLineIndent=1.25 * cm, spaceAfter=6,
)
BODY_NOIND = ParagraphStyle("CorpoSemRecuo", parent=BODY, firstLineIndent=0)
CENTER = ParagraphStyle("Centro", parent=BODY, alignment=TA_CENTER, firstLineIndent=0)
H1 = ParagraphStyle(
    "H1", parent=ss["Normal"], fontName="Serif-Bold", fontSize=12, leading=18,
    spaceBefore=18, spaceAfter=12,
)
H2 = ParagraphStyle("H2", parent=H1, fontName="Serif-Bold", spaceBefore=14, spaceAfter=10)
H3 = ParagraphStyle("H3", parent=H1, fontName="Serif-Italic", spaceBefore=12, spaceAfter=8)
CODE = ParagraphStyle(
    "Codigo", parent=ss["Normal"], fontName="Mono", fontSize=8, leading=10.5,
    backColor=colors.HexColor("#F4F4F4"), borderPadding=6, spaceBefore=6, spaceAfter=10,
)
CAPTION = ParagraphStyle(
    "Legenda", parent=ss["Normal"], fontName="Serif", fontSize=10, leading=14,
    alignment=TA_CENTER, spaceBefore=4, spaceAfter=14,
)
CELL = ParagraphStyle("Celula", parent=ss["Normal"], fontName="Serif", fontSize=9, leading=12)
CELL_B = ParagraphStyle("CelulaBold", parent=CELL, fontName="Serif-Bold")
REF = ParagraphStyle(
    "Referencia", parent=ss["Normal"], fontName="Serif", fontSize=12, leading=14.4,
    alignment=TA_JUSTIFY, spaceAfter=12,
)


class Doc(BaseDocTemplate):
    """Documento com numeracao de paginas no canto superior direito (ABNT)."""

    def __init__(self, path):
        super().__init__(
            path, pagesize=A4,
            leftMargin=3 * cm, topMargin=3 * cm, rightMargin=2 * cm, bottomMargin=2 * cm,
            title="Projeto Multidisciplinar - Trilha Back-End - Rede Raizes do Nordeste",
        )
        frame = Frame(self.leftMargin, self.bottomMargin, self.width, self.height, id="corpo")
        self.addPageTemplates([
            PageTemplate(id="pre", frames=[frame]),
            PageTemplate(id="corpo", frames=[frame], onPageEnd=self._numero),
        ])
        self.pagina_inicial_texto = None

    def _numero(self, canvas, doc):
        pagina = canvas.getPageNumber()
        if self.pagina_inicial_texto is None or pagina < self.pagina_inicial_texto:
            return
        canvas.saveState()
        canvas.setFont("Serif", 10)
        canvas.drawRightString(A4[0] - 2 * cm, A4[1] - 2 * cm, str(pagina))
        canvas.restoreState()

    def afterFlowable(self, flowable):
        if isinstance(flowable, Paragraph) and getattr(flowable, "_nivel", None) is not None:
            if self.pagina_inicial_texto is None:
                self.pagina_inicial_texto = self.page
            self.notify("TOCEntry", (flowable._nivel, flowable.getPlainText(), self.page))


def h(texto, nivel=0):
    estilo = [H1, H2, H3][nivel]
    p = Paragraph(texto, estilo)
    p._nivel = nivel
    return p


def p(texto, recuo=True):
    return Paragraph(texto, BODY if recuo else BODY_NOIND)


def itens(linhas):
    return [Paragraph(f"\u2022 {x}", ParagraphStyle(
        "Item", parent=BODY, firstLineIndent=0, leftIndent=0.8 * cm, spaceAfter=3)) for x in linhas]


def code(texto):
    limpo = (texto.strip().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
             .replace(" ", "&nbsp;").replace("\n", "<br/>"))
    return Paragraph(limpo, CODE)


def tabela(cabecalho, linhas, larguras):
    dados = [[Paragraph(c, CELL_B) for c in cabecalho]]
    dados += [[Paragraph(str(c), CELL) for c in linha] for linha in linhas]
    t = Table(dados, colWidths=[w * cm for w in larguras], repeatRows=1, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#999999")),
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8E8E8")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))
    return t


def figura(arquivo, legenda, largura_cm=15.0, altura_max_cm=19.0):
    from PIL import Image as PilImage

    caminho = os.path.join(IMG, arquivo)
    largura_px, altura_px = PilImage.open(caminho).size
    largura = largura_cm * cm
    altura = largura * altura_px / largura_px
    if altura > altura_max_cm * cm:
        altura = altura_max_cm * cm
        largura = altura * largura_px / altura_px
    img = Image(caminho, width=largura, height=altura)
    img.hAlign = "CENTER"
    return [img, Paragraph(legenda, CAPTION)]


def capa(aluno, ru, ano):
    return [
        Spacer(1, 1.0 * cm),
        Paragraph("CENTRO UNIVERSITÁRIO INTERNACIONAL UNINTER", CENTER),
        Paragraph("PROJETO MULTIDISCIPLINAR — TRILHA BACK-END", CENTER),
        Spacer(1, 5.0 * cm),
        Paragraph(f"<b>{aluno.upper()}</b>", CENTER),
        Paragraph(f"RU: {ru}", CENTER),
        Spacer(1, 3.0 * cm),
        Paragraph(
            "<b>REDE DE LANCHONETES “RAÍZES DO NORDESTE”:<br/>"
            "PROJETO E IMPLEMENTAÇÃO DE UMA API REST MULTICANAL</b>", CENTER),
        Spacer(1, 8.0 * cm),
        Paragraph("CURITIBA", CENTER),
        Paragraph(str(ano), CENTER),
        PageBreak(),
        Spacer(1, 1.0 * cm),
        Paragraph(f"<b>{aluno.upper()}</b>", CENTER),
        Spacer(1, 5.0 * cm),
        Paragraph(
            "<b>REDE DE LANCHONETES “RAÍZES DO NORDESTE”:<br/>"
            "PROJETO E IMPLEMENTAÇÃO DE UMA API REST MULTICANAL</b>", CENTER),
        Spacer(1, 3.0 * cm),
        Paragraph(
            "Trabalho apresentado à disciplina de Projeto Multidisciplinar — Trilha Back-End "
            "do Centro Universitário Internacional UNINTER, como requisito parcial para "
            "avaliação da atividade prática.<br/><br/>"
            "Prof. Me. Luciane Yanase Kanashiro.",
            ParagraphStyle("Natureza", parent=BODY, leftIndent=8 * cm, firstLineIndent=0,
                           fontSize=11, leading=15)),
        Spacer(1, 6.0 * cm),
        Paragraph("CURITIBA", CENTER),
        Paragraph(str(ano), CENTER),
        PageBreak(),
    ]


def sumario():
    toc = TableOfContents()
    toc.levelStyles = [
        ParagraphStyle("TOC0", fontName="Serif-Bold", fontSize=12, leading=20),
        ParagraphStyle("TOC1", fontName="Serif", fontSize=12, leading=18, leftIndent=1 * cm),
        ParagraphStyle("TOC2", fontName="Serif-Italic", fontSize=11, leading=16,
                       leftIndent=2 * cm),
    ]
    return [Paragraph("<b>SUMÁRIO</b>", CENTER), Spacer(1, 0.8 * cm), toc,
            NextPageTemplate("corpo"), PageBreak()]


def conteudo(repo):
    c = []
    a = c.append
    e = c.extend

    # 1
    a(h("1 INTRODUÇÃO E OBJETIVOS"))
    e([
        p("A rede de lanchonetes “Raízes do Nordeste” encontra-se em expansão e passou a operar "
          "por múltiplos canais de atendimento — aplicativo, totem de autoatendimento, balcão, "
          "retirada (pickup) e web — em unidades distintas, cada uma com cardápio e estoque "
          "próprios. Esse crescimento tornou inviável o controle manual e descentralizado das "
          "operações, criando a necessidade de um sistema de back-end capaz de consolidar "
          "pedidos, estoque, fidelidade e pagamentos de toda a rede."),
        p("O objetivo deste trabalho é projetar e implementar a solução de back-end dessa rede, "
          "na forma de uma API REST documentada e testável, cobrindo o fluxo crítico do negócio: "
          "<b>criação do pedido, solicitação de pagamento a um gateway externo simulado (mock) e "
          "atualização do status do pedido</b>, com persistência real em banco de dados."),
        p("São objetivos específicos: (i) levantar e priorizar os requisitos funcionais e não "
          "funcionais do cenário; (ii) modelar o domínio e a base de dados (DER, diagrama de "
          "classes e casos de uso); (iii) organizar a solução em camadas com responsabilidades "
          "separadas; (iv) definir e documentar o contrato da API em OpenAPI/Swagger; "
          "(v) aplicar controles mínimos de segurança e de conformidade com a LGPD; e "
          "(vi) evidenciar o funcionamento por meio de um plano de testes reproduzível, com "
          "cenários positivos e negativos."),
        p("A solução foi implementada em <b>Java 17</b> com <b>Spring Boot 3.3</b>, "
          "<b>Spring Data JPA</b>, <b>Spring Security</b> com autenticação por token "
          "<b>JWT</b>, banco <b>H2</b> em memória (portável para PostgreSQL por variáveis de "
          "ambiente) e documentação automática via <b>springdoc-openapi</b>. O código-fonte "
          f"completo está publicado no repositório público <b>{repo}</b>."),
    ])

    # 2
    a(h("2 ANÁLISE E REQUISITOS"))
    a(h("2.1 Requisitos funcionais", 1))
    a(p("Os requisitos funcionais foram levantados a partir do estudo de caso e priorizados "
        "segundo o fluxo crítico do negócio. Todos os requisitos listados no Quadro 1 estão "
        "implementados na API entregue."))
    e([tabela(
        ["ID", "Requisito funcional", "Implementação (endpoint principal)"],
        [
            ["RF01", "Cadastro e autenticação de usuários com perfis/roles",
             "POST /api/auth/registrar, POST /api/auth/login, GET /api/auth/me"],
            ["RF02", "Gestão das unidades da rede", "CRUD em /api/unidades"],
            ["RF03", "Consulta do cardápio por unidade, com preço praticado e saldo",
             "GET /api/unidades/{unidadeId}/produtos"],
            ["RF04", "Manutenção do catálogo de produtos e do cardápio da unidade",
             "CRUD em /api/produtos; PUT /api/unidades/{unidadeId}/produtos/{produtoId}"],
            ["RF05", "Realização de pedidos com itens, valores e status",
             "POST /api/pedidos"],
            ["RF06", "Atualização do status do pedido (cozinha, pronto, entregue, cancelado)",
             "PUT /api/pedidos/{id}/status; POST /api/pedidos/{id}/cancelar"],
            ["RF07", "Controle de estoque por unidade (entrada/saída) e restrição de venda",
             "PUT e POST em /api/unidades/{unidadeId}/estoque/produtos/{produtoId}"],
            ["RF08", "Programa de fidelidade com pontos, resgate e consentimento",
             "POST /api/clientes/{id}/pontos e /pontos/resgatar; "
             "PUT /api/clientes/{id}/consentimento"],
            ["RF09", "Promoções/campanhas aplicadas ao pedido",
             "POST /api/pedidos/{id}/desconto (regra descrita na seção 2.3)"],
            ["RF10", "Solicitação de pagamento a serviço externo simulado e registro do retorno",
             "POST /api/pedidos/{id}/pagamento/solicitar e /confirmar"],
            ["RF11", "Multicanalidade: registro e filtro do canal de origem do pedido",
             "campo canalPedido em POST /api/pedidos; GET /api/pedidos?canalPedido="],
            ["RF12", "Conformidade LGPD: consentimento e anonimização do titular",
             "PUT /api/clientes/{id}/consentimento; POST /api/clientes/{id}/anonimizar"],
            ["RF13", "Relatórios gerenciais consolidados e por unidade",
             "GET /api/relatorios/mais-vendidos e /financeiro"],
            ["RF14", "Auditoria de ações sensíveis e de acessos",
             "registro automático em audit_logs e access_logs"],
        ], [1.3, 5.5, 8.2])])
    a(Paragraph("Quadro 1 — Requisitos funcionais e endpoints correspondentes.", CAPTION))

    a(h("2.2 Requisitos não funcionais", 1))
    e([tabela(
        ["ID", "Requisito não funcional", "Como foi atendido"],
        [
            ["RNF01", "Segurança — autenticação",
             "Token JWT assinado (HMAC), stateless, com expiração configurável"],
            ["RNF02", "Segurança — senha",
             "Hash BCrypt; a senha em texto puro nunca é persistida nem retornada"],
            ["RNF03", "Segurança — autorização",
             "Perfis ADMIN, GERENTE e CLIENTE aplicados por rota no SecurityConfig"],
            ["RNF04", "LGPD",
             "Consentimento explícito, anonimização e minimização de dados nas respostas"],
            ["RNF05", "Logs e auditoria",
             "AuditService (ações sensíveis) e AccessAuditFilter (requisições HTTP)"],
            ["RNF06", "Desempenho em horários de pico",
             "Listagens paginadas (page/size), consultas por chave estrangeira e baixa "
             "de estoque dentro de transação"],
            ["RNF07", "Tolerância a falhas na integração de pagamento",
             "Pagamento desacoplado do pedido: recusa ou ausência de retorno mantêm o "
             "pedido em AGUARDANDO_PAGAMENTO, sem baixa de estoque, permitindo nova tentativa"],
            ["RNF08", "Disponibilidade e reprodutibilidade",
             "Aplicação autocontida (H2 em memória + seed), executável com um único comando"],
            ["RNF09", "Documentação",
             "OpenAPI 3 gerado pelo springdoc e publicado no Swagger UI"],
            ["RNF10", "Padronização de erros",
             "Formato JSON único para 400, 401, 403, 404, 409, 422 e 500"],
        ], [1.3, 4.5, 9.2])])
    a(Paragraph("Quadro 2 — Requisitos não funcionais.", CAPTION))

    a(h("2.3 Regras de negócio e multicanalidade", 1))
    a(p("O canal de origem é tratado como dado de domínio: o pedido possui o campo obrigatório "
        "<b>canalPedido</b>, do tipo enumerado, com os valores <b>APP, TOTEM, BALCAO, PICKUP e "
        "WEB</b>. A ausência ou o valor inválido desse campo resulta em erro 400 com a mensagem "
        "padronizada, e a listagem aceita o filtro <b>?canalPedido=</b>, permitindo consolidar "
        "e acompanhar o fluxo de pedidos por canal."))
    a(p("As demais regras do fluxo crítico são:"))
    e(itens([
        "o item só pode ser vendido se o produto estiver vinculado ao cardápio da unidade e "
        "marcado como disponível;",
        "produtos sazonais só podem ser vendidos dentro do período configurado;",
        "o preço unitário é o preço praticado pela unidade (override) ou, na ausência dele, o "
        "preço base do catálogo;",
        "não se cria pedido com estoque insuficiente (conflito 409);",
        "a baixa de estoque ocorre somente quando o pagamento é aprovado;",
        "os pontos de fidelidade são creditados automaticamente (1 ponto a cada R$ 10,00) apenas "
        "se o cliente tiver consentimento LGPD ativo;",
        "promoções são aplicadas como desconto registrado no pedido, com motivo e auditoria, "
        "não podendo exceder o subtotal;",
        "pedidos cancelados ou finalizados não aceitam novas transições de status.",
    ]))

    # 3
    a(PageBreak())
    a(h("3 DIAGRAMAS"))
    a(h("3.1 Diagrama de casos de uso", 1))
    a(p("A Figura 1 apresenta os casos de uso e os atores do sistema: o Cliente (App/Web/Totem), "
        "o Atendente (balcão), a Cozinha, o Gerente/Administrador e o sistema externo de "
        "pagamento (gateway)."))
    e(figura("casos_uso.png", "Figura 1 — Diagrama de casos de uso.", 15.5))

    a(h("3.1.1 Descrição da feature crítica: Realizar Pedido + Solicitar Pagamento", 2))
    a(p("<b>Atores:</b> Cliente (App/Totem/Web) ou Atendente; gateway de pagamento simulado.",
        False))
    a(p("<b>Pré-condições:</b> usuário autenticado com token JWT válido; unidade cadastrada; "
        "produto vinculado ao cardápio da unidade e disponível; estoque maior ou igual à "
        "quantidade pedida.", False))
    a(p("<b>Fluxo principal:</b>", False))
    e(itens([
        "o cliente consulta o cardápio da unidade (GET /api/unidades/{unidadeId}/produtos);",
        "envia POST /api/pedidos informando canalPedido, unidadeId, clienteId (opcional) e itens;",
        "a API valida cardápio, sazonalidade e estoque, calcula os totais e grava o pedido com "
        "status CRIADO;",
        "o cliente envia POST /api/pedidos/{id}/pagamento/solicitar: o pedido passa a "
        "AGUARDANDO_PAGAMENTO e a API gera a referência externa MOCK-&lt;uuid&gt;;",
        "o gateway devolve o resultado em POST /api/pedidos/{id}/pagamento/confirmar — aprovado: "
        "pedido PAGO, baixa de estoque e crédito de pontos;",
        "a cozinha evolui o status via PUT /api/pedidos/{id}/status "
        "(EM_PREPARO, PRONTO, FINALIZADO).",
    ]))
    a(p("<b>Pós-condições:</b> pedido persistido com itens, totais e histórico em pedido_eventos; "
        "estoque decrementado apenas na aprovação; ações sensíveis registradas em audit_logs e "
        "requisições em access_logs.", False))
    a(p("<b>Exceções e regras:</b>", False))
    e([tabela(
        ["Situação", "Tratamento"],
        [
            ["canalPedido ausente ou inválido", "400 Bad Request com o erro padronizado"],
            ["Unidade, cliente ou produto inexistente", "404 Not Found"],
            ["Produto fora do cardápio da unidade", "404 Not Found"],
            ["Produto indisponível ou fora da sazonalidade", "409 Conflict"],
            ["Estoque insuficiente", "409 Conflict — o pedido não é criado"],
            ["Confirmação antes da solicitação de pagamento", "409 Conflict"],
            ["Pagamento recusado",
             "Pedido permanece AGUARDANDO_PAGAMENTO e o estoque é preservado"],
            ["Pedido cancelado ou finalizado", "Novas transições de status são bloqueadas (409)"],
            ["Crédito de pontos sem consentimento LGPD", "Pontos não são creditados"],
        ], [7.0, 8.0])])
    a(Paragraph("Quadro 3 — Exceções e regras de negócio do fluxo crítico.", CAPTION))

    a(PageBreak())
    a(h("3.2 DER — modelo de dados", 1))
    a(p("A Figura 2 apresenta o Diagrama Entidade-Relacionamento, compatível com o banco "
        "utilizado pela API: as tabelas são geradas pelo mapeamento JPA das entidades do pacote "
        "<i>model</i>. Destacam-se: cada unidade possui estoque próprio (ESTOQUE_ITENS) e "
        "cardápio próprio (PRODUTO_UNIDADE); o pedido possui itens (PEDIDO_ITENS) e histórico "
        "(PEDIDO_EVENTOS); e o pagamento é desacoplado, registrado no pedido apenas pelo status "
        "e pela referência externa."))
    e(figura("der.png", "Figura 2 — Diagrama Entidade-Relacionamento (DER).", 15.5))
    a(p("As principais cardinalidades são: Cliente (1:N) Pedido; Unidade (1:N) Pedido; "
        "Pedido (1:N) PedidoItem; Produto (1:N) PedidoItem; Pedido (1:N) PedidoEvento; "
        "Unidade (1:N) EstoqueItem; Produto (1:N) EstoqueItem; Unidade (1:N) ProdutoUnidade; "
        "Produto (1:N) ProdutoUnidade; Unidade (1:N) Funcionario. O usuário possui vínculos "
        "opcionais com unidade (funcionário lotado) e com cliente."))

    a(PageBreak())
    a(h("3.3 Arquitetura — camadas e separação de responsabilidades", 1))
    a(p("A solução adota arquitetura em camadas, com dependências orientadas do exterior para o "
        "domínio, conforme a Figura 3."))
    e(figura("arquitetura.png", "Figura 3 — Camadas da solução.", 13.0))
    e([tabela(
        ["Camada", "Pacote", "Responsabilidade"],
        [
            ["API (Interface)", "controller, dto, exception",
             "Rotas REST, contratos de request/response, validação de entrada, documentação "
             "OpenAPI e padronização de erros"],
            ["Application", "service",
             "Casos de uso e orquestração: criar pedido, aplicar fidelidade, confirmar "
             "pagamento mock, atualizar status e auditar"],
            ["Domain", "model, model.enums",
             "Entidades e estados do negócio (Pedido, Cliente, Produto, EstoqueItem, "
             "StatusPedido, CanalAtendimento)"],
            ["Infrastructure", "repository, config, security, middleware",
             "Persistência JPA, carga inicial (seed), autenticação JWT, autorização por perfil "
             "e trilha de acessos"],
        ], [2.8, 4.0, 8.2])])
    a(Paragraph("Quadro 4 — Camadas, pacotes e responsabilidades.", CAPTION))

    a(PageBreak())
    a(h("3.4 Diagrama de classes", 1))
    a(p("A Figura 4 apresenta a visão de domínio, com as principais entidades, seus atributos "
        "essenciais e os relacionamentos."))
    e(figura("classes.png", "Figura 4 — Diagrama de classes (domínio).", 15.5))
    a(PageBreak())
    a(h("3.5 Diagrama de sequência do fluxo crítico", 1))
    a(p("A Figura 5 detalha a sequência Pedido → Pagamento externo → Atualização de status, "
        "incluindo a baixa de estoque e o crédito de pontos após a aprovação."))
    e(figura("classes_2.png",
             "Figura 5 — Diagrama de sequência: pedido, pagamento mock e atualização de status.",
             15.5))
    a(p("A sequência evidencia que a baixa de estoque e o crédito de pontos ocorrem apenas no "
        "retorno aprovado do gateway, mantendo o pedido consistente em caso de recusa."))

    # 4
    a(PageBreak())
    a(h("4 API E ENDPOINTS"))
    a(p("O contrato da API está publicado em OpenAPI 3 e disponível no Swagger UI da própria "
        "aplicação, em <b>http://localhost:8082/swagger-ui/index.html</b> (documento JSON em "
        "<b>/v3/api-docs</b>), refletindo exatamente os endpoints implementados. As Figuras 6 "
        "e 7 mostram a documentação gerada."))
    e(figura("swagger_geral.png", "Figura 6 — Swagger UI da API Raízes do Nordeste.", 11.5))
    e(figura("swagger_pedidos.png",
             "Figura 7 — Recursos de cardápio por unidade e de pedidos no Swagger UI.", 11.5))

    a(PageBreak())
    a(h("4.1 Convenções do contrato", 1))
    e(itens([
        "recursos no plural e identificadores no caminho (ex.: /api/produtos/{id});",
        "paginação nas listagens por page e size, com resposta contendo content, page, size, "
        "totalElements, totalPages e last;",
        "autenticação por cabeçalho Authorization: Bearer &lt;token&gt;;",
        "códigos de status coerentes: 200 (ok), 201 (criado), 400/422 (validação), "
        "401 (não autenticado), 403 (sem permissão), 404 (não encontrado) e 409 (conflito);",
        "corpo de erro sempre no mesmo formato JSON.",
    ]))
    a(p("<b>Padrão de erro:</b>", False))
    a(code('{\n'
           '  "timestamp": "2026-08-10T23:15:28.861Z",\n'
           '  "status": 409,\n'
           '  "error": "Conflict",\n'
           '  "message": "Estoque insuficiente",\n'
           '  "path": "/api/pedidos"\n'
           '}'))

    a(PageBreak())
    a(h("4.2 Endpoints por recurso", 1))
    e([tabela(
        ["Método e rota", "Perfil", "Propósito"],
        [
            ["POST /api/auth/login", "público", "Autentica e devolve o token JWT de acesso"],
            ["POST /api/auth/registrar", "público", "Registra um novo usuário com perfil CLIENTE"],
            ["GET /api/auth/me", "autenticado", "Retorna os dados do usuário do token"],
            ["GET /api/unidades", "autenticado", "Lista as unidades da rede"],
            ["GET /api/unidades/{unidadeId}/produtos", "autenticado",
             "Consulta o cardápio da unidade com preço praticado e saldo de estoque"],
            ["PUT /api/unidades/{unidadeId}/produtos/{produtoId}", "ADMIN, GERENTE",
             "Disponibiliza ou atualiza um produto no cardápio da unidade"],
            ["GET /api/produtos", "autenticado", "Lista o catálogo de forma paginada"],
            ["GET, PUT, POST /api/unidades/{unidadeId}/estoque/produtos/{produtoId}",
             "ADMIN, GERENTE", "Consulta, define e ajusta (entrada/saída) o estoque da unidade"],
            ["POST /api/pedidos", "autenticado",
             "Cria o pedido com itens, exigindo canalPedido"],
            ["GET /api/pedidos", "autenticado",
             "Lista pedidos de forma paginada, com filtros canalPedido, status e unidadeId"],
            ["GET /api/pedidos/{id}", "autenticado", "Consulta um pedido com seus itens"],
            ["PUT /api/pedidos/{id}/status", "ADMIN, GERENTE",
             "Atualiza o status do pedido (cozinha, pronto, finalizado)"],
            ["POST /api/pedidos/{id}/pagamento/solicitar", "autenticado",
             "Solicita o pagamento ao gateway simulado e gera a referência externa"],
            ["POST /api/pedidos/{id}/pagamento/confirmar", "autenticado",
             "Recebe o retorno do gateway: aprovado ou recusado"],
            ["POST /api/pedidos/{id}/desconto", "ADMIN, GERENTE",
             "Aplica desconto promocional ao pedido, com motivo"],
            ["POST /api/pedidos/{id}/cancelar", "ADMIN, GERENTE",
             "Cancela o pedido registrando o motivo"],
            ["POST /api/clientes", "ADMIN, GERENTE", "Cadastra um cliente"],
            ["PUT /api/clientes/{id}/consentimento", "ADMIN, GERENTE",
             "Registra ou revoga o consentimento LGPD"],
            ["POST /api/clientes/{id}/pontos e /pontos/resgatar", "ADMIN, GERENTE",
             "Credita e resgata pontos de fidelidade"],
            ["POST /api/clientes/{id}/anonimizar", "ADMIN",
             "Anonimiza os dados pessoais do titular"],
            ["GET /api/relatorios/mais-vendidos e /financeiro", "ADMIN, GERENTE",
             "Relatórios consolidados ou por unidade"],
        ], [6.4, 2.6, 6.0])])
    a(Paragraph("Quadro 5 — Principais endpoints da API.", CAPTION))
    a(p("A documentação completa de cada endpoint (parâmetros, exemplos de request e response e "
        "códigos de status) está no arquivo <b>docs/ENDPOINTS.md</b> do repositório e no "
        "Swagger UI."))

    a(h("4.3 Exemplo do fluxo crítico", 1))
    a(p("<b>Requisição — criação de pedido:</b>", False))
    a(code('POST /api/pedidos\n'
           'Authorization: Bearer <token>\n\n'
           '{\n'
           '  "canalPedido": "TOTEM",\n'
           '  "clienteId": 1,\n'
           '  "unidadeId": 1,\n'
           '  "itens": [ { "produtoId": 1, "quantidade": 2 } ]\n'
           '}'))
    a(p("<b>Resposta — 201 Created:</b>", False))
    a(code('{\n'
           '  "id": 1, "clienteId": 1, "unidadeId": 1,\n'
           '  "canalPedido": "TOTEM",\n'
           '  "status": "CRIADO",\n'
           '  "statusPagamentoExterno": "NAO_SOLICITADO",\n'
           '  "subtotal": 65.80, "desconto": 0, "total": 65.80\n'
           '}'))
    a(p("<b>Resposta — retorno do gateway aprovado (POST /pagamento/confirmar):</b>", False))
    a(code('{\n'
           '  "id": 1, "canalPedido": "TOTEM",\n'
           '  "status": "PAGO",\n'
           '  "statusPagamentoExterno": "CONFIRMADO",\n'
           '  "referenciaPagamentoExterno": "MOCK-0ae618c6-395e-4398-b179-41c8966a0324",\n'
           '  "total": 65.80\n'
           '}'))

    # 5
    a(PageBreak())
    a(h("5 LGPD, PRIVACIDADE E SEGURANÇA"))
    a(h("5.1 Dados pessoais, finalidade e base legal", 1))
    e([tabela(
        ["Dado", "Entidade", "Finalidade", "Base legal (Lei nº 13.709/2018)"],
        [
            ["Nome", "Cliente, Usuario, Funcionario", "Identificação no pedido e atendimento",
             "Execução de contrato (art. 7º, V)"],
            ["CPF", "Cliente", "Identificação fiscal e do programa de fidelidade",
             "Execução de contrato / obrigação legal (art. 7º, II e V)"],
            ["E-mail", "Cliente, Usuario", "Autenticação e comunicação sobre o pedido",
             "Execução de contrato (art. 7º, V)"],
            ["Telefone", "Cliente", "Contato operacional sobre o pedido",
             "Execução de contrato (art. 7º, V)"],
            ["Pontos de fidelidade", "Cliente", "Programa de benefícios",
             "Consentimento (art. 7º, I)"],
            ["IP e rota acessada", "AccessLog, AuditLog",
             "Segurança da informação e trilha de auditoria",
             "Legítimo interesse (art. 7º, IX)"],
        ], [2.6, 3.2, 4.6, 4.6])])
    a(Paragraph("Quadro 6 — Dados pessoais tratados pela API.", CAPTION))
    a(p("Não são tratados dados sensíveis (art. 5º, II) nem dados de cartão: o pagamento é "
        "delegado ao gateway externo simulado e a API armazena apenas a referência da transação."))

    a(h("5.2 Consentimento, revogação e anonimização", 1))
    a(p("O cadastro do cliente inicia sempre com o consentimento em <i>false</i>. O consentimento "
        "é registrado explicitamente por <b>PUT /api/clientes/{id}/consentimento</b>, que grava a "
        "flag e o carimbo de data/hora, e é revogado pelo mesmo endpoint. O programa de "
        "fidelidade só opera com consentimento ativo: créditos e resgates retornam 409 quando o "
        "titular não consentiu, e o crédito automático após o pagamento é ignorado nesse caso. "
        "O direito de eliminação (art. 18, VI) é atendido por "
        "<b>POST /api/clientes/{id}/anonimizar</b>, que remove nome, CPF, e-mail e telefone, "
        "preservando os pedidos históricos sem dados identificáveis."))

    a(h("5.3 Controles técnicos", 1))
    e([tabela(
        ["Controle", "Implementação"],
        [
            ["Hash de senha", "BCrypt; apenas o hash é persistido em usuarios.senha_hash"],
            ["Autenticação", "Token JWT assinado (HMAC), stateless, com expiração configurável"],
            ["Autorização", "Perfis ADMIN, GERENTE e CLIENTE aplicados por rota"],
            ["Exposição de dados", "Respostas por DTO; a senha nunca é retornada pela API"],
            ["Logs de acesso",
             "AccessAuditFilter registra usuário, método, rota, status, IP e duração"],
            ["Auditoria de ações sensíveis",
             "AuditService registra login, criação de pedido, pagamento, alteração de status, "
             "consentimento e anonimização"],
            ["Erros", "JSON padronizado, sem stack trace nem detalhes internos"],
            ["Segredos", "JWT_SECRET e credenciais do banco por variáveis de ambiente "
                         "(.env.example), fora do código-fonte"],
        ], [4.5, 10.5])])
    a(Paragraph("Quadro 7 — Controles técnicos de segurança e privacidade.", CAPTION))
    a(p("A Figura 8 evidencia a trilha de auditoria gravada em banco durante a execução do fluxo "
        "crítico, com o registro de login, criação de pedido, pagamento, alteração de status, "
        "consentimento e anonimização."))
    e(figura("auditoria_h2.png",
             "Figura 8 — Registros da tabela audit_logs após a execução do fluxo crítico.", 15.0))

    # 6
    a(PageBreak())
    a(h("6 ENTREGA TÉCNICA"))
    a(h("6.1 Repositório e evidências", 1))
    e(itens([
        f"Repositório público: <b>{repo}</b>",
        "Swagger UI (local): <b>http://localhost:8082/swagger-ui/index.html</b> — "
        "documento OpenAPI em <b>/v3/api-docs</b>",
        "Coleção Postman: <b>docs/postman/RaizesDoNordeste.postman_collection.json</b> e "
        "environment <b>docs/postman/RaizesDoNordeste.postman_environment.json</b>",
        "Plano de testes: <b>TEST_PLAN.md</b>",
        "DER, casos de uso e classes: <b>docs/DIAGRAMAS/</b> (fontes) e "
        "<b>docs/imagens/</b> (imagens)",
        "Saída completa da execução dos cenários: <b>docs/evidencias-execucao.txt</b>",
    ]))

    a(h("6.2 Como executar", 1))
    a(p("Requisitos: Java 17 (JDK) e Maven 3.9. O banco é H2 em memória, portanto não é "
        "necessário instalar SGBD; as tabelas são criadas na inicialização e a carga inicial "
        "(seed) cria 2 unidades, 3 produtos com estoque, 1 cliente e 3 usuários."))
    a(code('git clone https://github.com/CindyPerico/trabalhoback-end.git\n'
           'cd trabalhoback-end\n'
           'cp .env.example .env      # opcional: ajusta porta, banco e JWT_SECRET\n'
           'mvn clean package         # instala dependencias e compila\n'
           'mvn spring-boot:run       # inicia a API em http://localhost:8082\n'
           'mvn test                  # executa os 17 testes automatizados'))
    a(p("Usuários criados pelo seed para teste: <b>admin@raizes.com / admin12345</b> (ADMIN), "
        "<b>gerente@raizes.com / gerente12345</b> (GERENTE) e "
        "<b>maria.souza@example.com / cliente12345</b> (CLIENTE)."))

    a(h("6.3 MVP entregue", 1))
    a(p("O fluxo obrigatório escolhido foi o <b>Fluxo A — Pedido → Pagamento mock → Atualização "
        "de status</b>, entregue completo e testável, com persistência real (CRUD em banco), "
        "autenticação JWT com perfis e padrão de erro consistente. O Fluxo B (estoque por "
        "unidade) também está implementado, já que a baixa e o controle de saldo por unidade "
        "fazem parte das regras do fluxo principal."))

    # 7
    a(PageBreak())
    a(h("7 PLANO DE TESTES"))
    a(h("7.1 Como reproduzir", 1))
    a(p("A validação é reproduzível por dois caminhos complementares. O primeiro são os "
        "<b>17 testes automatizados</b> (JUnit 5 + MockMvc), executados por <b>mvn test</b>, que "
        "sobem o contexto completo da aplicação com banco e seed próprios. O segundo é a "
        "<b>coleção Postman</b>, com 22 requisições organizadas nas pastas Auth, Cardapio, "
        "Pedidos, Pagamento, Fidelidade e LGPD, Relatorios e Erros, cada uma com asserções "
        "automáticas (pm.test). A ordem sugerida de execução é a das pastas; o login salva "
        "automaticamente as variáveis de ambiente token, tokenCliente e pedidoId. Pela linha de "
        "comando:"))
    a(code('newman run docs/postman/RaizesDoNordeste.postman_collection.json \\\n'
           '  -e docs/postman/RaizesDoNordeste.postman_environment.json'))
    a(p("Pré-condição comum a todos os cenários: aplicação em execução com o seed aplicado."))

    a(h("7.2 Cenários positivos", 1))
    e([tabela(
        ["ID", "Cenário / Endpoint", "Entrada", "Esperado", "Evidência"],
        [
            ["T01", "Login válido — POST /api/auth/login",
             '{"email":"admin@raizes.com","senha":"admin12345"}',
             "200 + accessToken e role ADMIN",
             "Postman Auth/T01; AutenticacaoTest.loginValido"],
            ["T02", "Usuário autenticado — GET /api/auth/me", "token válido",
             "200 + e-mail do token", "Postman Auth/T03"],
            ["T03", "Cardápio por unidade — GET /api/unidades/1/produtos", "unidadeId=1",
             "200 + itens com preço e quantidadeEmEstoque",
             "Postman Cardapio/T04; PedidoFluxoTest.cardapioPorUnidade"],
            ["T04", "Criar pedido — POST /api/pedidos",
             '{"canalPedido":"TOTEM","clienteId":1,"unidadeId":1,'
             '"itens":[{"produtoId":1,"quantidade":2}]}',
             "201 + canalPedido TOTEM, status CRIADO e total calculado",
             "Postman Pedidos/T06; PedidoFluxoTest.fluxoPedidoPagamentoStatus"],
            ["T05", "Filtrar por canal — GET /api/pedidos?canalPedido=TOTEM",
             "canalPedido=TOTEM&amp;page=0&amp;size=10",
             "200 + página só com pedidos TOTEM",
             "Postman Pedidos/T07; PedidoFluxoTest.filtrarPorCanal"],
            ["T06", "Pagamento aprovado — POST /pagamento/solicitar e /confirmar",
             '{"confirmado":true}',
             "200 + status PAGO, pagamento CONFIRMADO e estoque decrementado de 50 para 48",
             "Postman Pagamento/T08 e T09; PedidoFluxoTest.fluxoPedidoPagamentoStatus"],
            ["T07", "Atualizar status — PUT /api/pedidos/{id}/status", '{"status":"EM_PREPARO"}',
             "200 + status EM_PREPARO", "Postman Pagamento/T10"],
            ["T08", "Fidelidade com consentimento — PUT /consentimento e POST /pontos",
             '{"consentido":true}', "200 + lgpdConsentido true e saldo de pontos atualizado",
             "Postman Fidelidade e LGPD/T12 e T13; FidelidadeLgpdTest"],
            ["T09", "Relatório financeiro — GET /api/relatorios/financeiro?unidadeId=1",
             "unidadeId=1", "200 + totalPedidos e totalVendido", "Postman Relatorios/T16"],
        ], [1.1, 4.2, 3.3, 3.4, 3.0])])
    a(Paragraph("Quadro 8 — Cenários de teste positivos.", CAPTION))

    a(h("7.3 Cenários negativos", 1))
    e([tabela(
        ["ID", "Cenário / Endpoint", "Entrada", "Esperado", "Evidência"],
        [
            ["T10", "Acesso sem token — GET /api/pedidos", "sem Authorization",
             "401 + erro padronizado", "Postman Erros/T17; AutenticacaoTest.semToken"],
            ["T11", "Perfil sem permissão — GET /api/relatorios/financeiro", "token de CLIENTE",
             "403 + “Acesso negado”",
             "Postman Erros/T18; AutenticacaoTest.clienteSemPermissao"],
            ["T12", "Credenciais inválidas — POST /api/auth/login", "senha incorreta",
             "401", "AutenticacaoTest.loginInvalido"],
            ["T13", "Campo obrigatório ausente — POST /api/pedidos", "body sem canalPedido",
             "400 + mensagem citando canalPedido",
             "Postman Erros/T19; PedidoFluxoTest.pedidoSemCanal"],
            ["T14", "Formato/valor inválido — POST /api/pedidos e /api/clientes",
             "quantidade -3; e-mail inválido", "400 + erro de validação",
             "Postman Erros/T22; FidelidadeLgpdTest.emailInvalido"],
            ["T15", "Produto inexistente — POST /api/pedidos", "produtoId 9999", "404",
             "Postman Erros/T20; PedidoFluxoTest.pedidoProdutoInexistente"],
            ["T16", "Estoque insuficiente — POST /api/pedidos", "quantidade 9999",
             "409 + “Estoque insuficiente”",
             "Postman Erros/T21; PedidoFluxoTest.pedidoEstoqueInsuficiente"],
            ["T17", "Pagamento recusado — POST /pagamento/confirmar", '{"confirmado":false}',
             "200 + pagamento RECUSADO, pedido segue AGUARDANDO_PAGAMENTO e estoque intacto",
             "Postman Pagamento/T11; PedidoFluxoTest.pagamentoRecusado"],
            ["T18", "Fidelidade sem consentimento e resgate sem saldo — POST /pontos "
                    "e /pontos/resgatar",
             '{"pontos":9999}', "409 nos dois casos",
             "FidelidadeLgpdTest.fidelidadeComConsentimento"],
        ], [1.1, 4.2, 3.3, 3.4, 3.0])])
    a(Paragraph("Quadro 9 — Cenários de teste negativos.", CAPTION))

    a(h("7.4 Logs e auditoria", 1))
    e([tabela(
        ["ID", "Cenário", "Verificação", "Evidência"],
        [
            ["T19", "Ação sensível gera registro de auditoria",
             "A criação de pedido grava PEDIDO_CRIADO em audit_logs e toda requisição gera "
             "registro em access_logs",
             "PedidoFluxoTest.auditoriaDoPedido; Figura 8 (consulta no H2 Console)"],
        ], [1.1, 4.0, 5.4, 4.5])])
    a(Paragraph("Quadro 10 — Cenário de auditoria.", CAPTION))

    a(h("7.5 Resultados obtidos", 1))
    a(p("São 19 cenários documentados — 9 positivos, 9 negativos e 1 de auditoria —, acima do "
        "mínimo exigido (10 cenários, com 6 positivos e 4 negativos). Todos foram executados "
        "contra a API em funcionamento; a saída completa está em "
        "<b>docs/evidencias-execucao.txt</b>. A suíte automatizada foi executada com sucesso:"))
    a(code('[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- PedidoFluxoTest\n'
           '[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- FidelidadeLgpdTest\n'
           '[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- AutenticacaoTest\n'
           '[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- ApplicationTests\n'
           '[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0\n'
           '[INFO] BUILD SUCCESS'))
    a(p("Exemplos de respostas obtidas na execução dos cenários negativos, todas no formato de "
        "erro padronizado:"))
    a(code('T13  400  {"status":400,"error":"Bad Request",\n'
           '            "message":"canalPedido: must not be null","path":"/api/pedidos"}\n'
           'T15  404  {"status":404,"error":"Not Found",\n'
           '            "message":"Produto nao encontrado","path":"/api/pedidos"}\n'
           'T16  409  {"status":409,"error":"Conflict",\n'
           '            "message":"Estoque insuficiente","path":"/api/pedidos"}\n'
           'T10  401  {"status":401,"error":"Unauthorized",\n'
           '            "message":"Autenticacao necessaria: informe um token JWT valido"}\n'
           'T11  403  {"status":403,"error":"Forbidden",\n'
           '            "message":"Acesso negado: perfil sem permissao para este recurso"}'))

    # 8
    a(PageBreak())
    a(h("8 CONCLUSÃO"))
    e([
        p("O trabalho entregou uma API REST funcional, documentada e reproduzível para a rede "
          "“Raízes do Nordeste”, com o fluxo crítico fechado de ponta a ponta: criação do pedido "
          "com validação de cardápio, sazonalidade e estoque; solicitação de pagamento ao "
          "gateway externo simulado; recebimento do retorno; e atualização do status do pedido, "
          "com persistência real em banco."),
        p("A priorização seguiu o MVP obrigatório. Primeiro foi implementado o fluxo "
          "Pedido → Pagamento mock → Status, por ser o que sustenta a operação da rede e o que "
          "atravessa todas as entidades do domínio. Em seguida vieram os controles transversais "
          "— autenticação JWT, autorização por perfil, padronização de erros e auditoria — que "
          "são requisitos não funcionais sem os quais a solução não se sustentaria em produção. "
          "Por último, os cadastros de apoio e os relatórios gerenciais."),
        p("Os artefatos de modelagem conectam-se diretamente ao código entregue. As entidades do "
          "DER correspondem às tabelas geradas pelo mapeamento JPA das classes do pacote "
          "<i>model</i>; o diagrama de classes reflete esse mesmo domínio e seus enumerados "
          "(CanalAtendimento, StatusPedido, StatusPagamentoExterno); e cada caso de uso possui "
          "endpoint correspondente — UC02 em GET /api/unidades/{id}/produtos, UC03 em "
          "POST /api/pedidos, UC04 e UC05 no par pagamento/solicitar e pagamento/confirmar, "
          "UC06 em PUT /api/pedidos/{id}/status e UC10 nos endpoints de fidelidade e "
          "consentimento."),
        p("O pagamento foi tratado como integração simulada e desacoplada: a solicitação gera a "
          "referência MOCK-&lt;uuid&gt; e coloca o pedido em AGUARDANDO_PAGAMENTO; o retorno do "
          "gateway é recebido pelo endpoint de confirmação e, quando aprovado, o pedido passa a "
          "PAGO, o estoque é baixado e os pontos de fidelidade são creditados; quando recusado, "
          "o estoque permanece intacto e uma nova tentativa é possível. Esse desacoplamento é "
          "também a estratégia de tolerância a falhas na integração. As validações e erros foram "
          "padronizados em um único formato JSON, com 400 e 422 para validação, 401 para não "
          "autenticado, 403 para perfil sem permissão, 404 para recurso inexistente e 409 para "
          "conflito de regra de negócio."),
        p("Em segurança e privacidade, os principais cuidados foram o armazenamento de senhas "
          "apenas como hash BCrypt, a autenticação stateless por token JWT com expiração, a "
          "autorização por perfil em cada rota, a exposição de dados por DTO, o consentimento "
          "explícito como condição para o programa de fidelidade, a anonimização do titular e a "
          "trilha de auditoria de ações sensíveis e de acessos. Os testes evidenciam esse "
          "funcionamento: 17 testes automatizados e 19 cenários documentados cobrem autenticação "
          "e autorização, validação de dados, regras do fluxo principal, pagamento aprovado e "
          "recusado e o registro em auditoria."),
        p("Ficaram como proposta de evolução: campanhas promocionais automáticas por regra "
          "(hoje o desconto é aplicado manualmente ao pedido, com motivo e auditoria), "
          "<i>refresh token</i> e revogação de credenciais, migrations versionadas para "
          "ambientes de produção com PostgreSQL, idempotência por chave na criação de pedido e "
          "notificação assíncrona do status ao cliente."),
        p("Respondendo à reflexão final proposta pelo roteiro, a solução se sustentaria em uma "
          "entrevista técnica: entrega um fluxo de negócio completo em vez de um conjunto "
          "disperso de endpoints, apresenta separação clara de responsabilidades, contrato "
          "documentado, tratamento consistente de erros, controles de segurança e privacidade e "
          "evidências de teste reproduzíveis por qualquer avaliador."),
    ])

    # Referencias
    a(PageBreak())
    a(h("REFERÊNCIAS"))
    for r in [
        "BRASIL. <b>Lei nº 13.709, de 14 de agosto de 2018</b>. Lei Geral de Proteção de Dados "
        "Pessoais (LGPD). Brasília, DF: Presidência da República, 2018.",
        "FIELDING, Roy Thomas. <b>Architectural styles and the design of network-based software "
        "architectures</b>. 2000. Tese (Doutorado em Ciência da Computação) — University of "
        "California, Irvine, 2000.",
        "FOWLER, Martin. <b>Patterns of enterprise application architecture</b>. Boston: "
        "Addison-Wesley, 2003.",
        "H2 DATABASE ENGINE. <b>H2 database documentation</b>. Disponível em: "
        "https://www.h2database.com/html/main.html. Acesso em: 10 ago. 2026.",
        "JONES, Michael; BRADLEY, John; SAKIMURA, Nat. <b>RFC 7519: JSON Web Token (JWT)</b>. "
        "Internet Engineering Task Force, 2015. Disponível em: "
        "https://datatracker.ietf.org/doc/html/rfc7519. Acesso em: 10 ago. 2026.",
        "OPENAPI INITIATIVE. <b>OpenAPI specification</b>. Disponível em: "
        "https://spec.openapis.org/oas/latest.html. Acesso em: 10 ago. 2026.",
        "RICHARDSON, Leonard; AMUNDSEN, Mike. <b>RESTful web APIs</b>. Sebastopol: O’Reilly "
        "Media, 2013.",
        "SPRING. <b>Spring Boot reference documentation</b>. Disponível em: "
        "https://docs.spring.io/spring-boot/documentation.html. Acesso em: 10 ago. 2026.",
        "SPRING. <b>Spring Security reference documentation</b>. Disponível em: "
        "https://docs.spring.io/spring-security/reference/. Acesso em: 10 ago. 2026.",
    ]:
        a(Paragraph(r, REF))
    return c


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--aluno", default="Cindy Perico")
    parser.add_argument("--ru", default="0000000")
    parser.add_argument("--ano", default="2026")
    parser.add_argument("--repo", default="https://github.com/CindyPerico/trabalhoback-end")
    parser.add_argument("--saida", default=os.path.join(BASE, "docs", "pdf",
                                                        "Projeto_Back_End.pdf"))
    args = parser.parse_args()

    doc = Doc(args.saida)
    historia = capa(args.aluno, args.ru, args.ano) + sumario() + conteudo(args.repo)
    doc.multiBuild(historia)
    print(f"PDF gerado em {args.saida}")


if __name__ == "__main__":
    main()
