package br.com.cindyperico.raizesdonordeste.config;

import br.com.cindyperico.raizesdonordeste.model.Cliente;
import br.com.cindyperico.raizesdonordeste.model.EstoqueItem;
import br.com.cindyperico.raizesdonordeste.model.Produto;
import br.com.cindyperico.raizesdonordeste.model.ProdutoUnidade;
import br.com.cindyperico.raizesdonordeste.model.Unidade;
import br.com.cindyperico.raizesdonordeste.model.Usuario;
import br.com.cindyperico.raizesdonordeste.model.enums.Role;
import br.com.cindyperico.raizesdonordeste.repository.ClienteRepository;
import br.com.cindyperico.raizesdonordeste.repository.EstoqueItemRepository;
import br.com.cindyperico.raizesdonordeste.repository.ProdutoRepository;
import br.com.cindyperico.raizesdonordeste.repository.ProdutoUnidadeRepository;
import br.com.cindyperico.raizesdonordeste.repository.UnidadeRepository;
import br.com.cindyperico.raizesdonordeste.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final EstoqueItemRepository estoqueItemRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      UnidadeRepository unidadeRepository,
                      ProdutoRepository produtoRepository,
                      ProdutoUnidadeRepository produtoUnidadeRepository,
                      EstoqueItemRepository estoqueItemRepository,
                      ClienteRepository clienteRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
        this.produtoUnidadeRepository = produtoUnidadeRepository;
        this.estoqueItemRepository = estoqueItemRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Unidade recife = criarUnidade("Raízes do Nordeste - Boa Viagem", "PE", "Recife", "Av. Boa Viagem, 1000");
        Unidade fortaleza = criarUnidade("Raízes do Nordeste - Meireles", "CE", "Fortaleza", "Av. Beira Mar, 500");

        Produto baiao = criarProduto("Baião de Dois", "Arroz, feijão de corda, queijo coalho e carne seca", "32.90");
        Produto tapioca = criarProduto("Tapioca de Carne de Sol", "Tapioca recheada com carne de sol e queijo", "24.50");
        Produto cajuina = criarProduto("Cajuína gelada", "Bebida tradicional do Piauí, 300ml", "9.90");

        disponibilizar(recife, baiao, "32.90", 50);
        disponibilizar(recife, tapioca, "24.50", 40);
        disponibilizar(recife, cajuina, "9.90", 100);
        disponibilizar(fortaleza, baiao, "34.90", 30);
        disponibilizar(fortaleza, cajuina, "8.90", 60);

        Cliente cliente = new Cliente();
        cliente.setNome("Maria Souza");
        cliente.setCpf("123.456.789-00");
        cliente.setEmail("maria.souza@example.com");
        cliente.setTelefone("(81) 99999-0000");
        cliente = clienteRepository.save(cliente);

        criarUsuario("Administrador da Rede", "admin@raizes.com", "admin12345", Role.ADMIN, null, null);
        criarUsuario("Gerente Boa Viagem", "gerente@raizes.com", "gerente12345", Role.GERENTE, recife, null);
        criarUsuario("Maria Souza", "maria.souza@example.com", "cliente12345", Role.CLIENTE, null, cliente);
    }

    private Unidade criarUnidade(String nome, String uf, String cidade, String endereco) {
        Unidade u = new Unidade();
        u.setNome(nome);
        u.setUf(uf);
        u.setCidade(cidade);
        u.setEndereco(endereco);
        return unidadeRepository.save(u);
    }

    private Produto criarProduto(String nome, String descricao, String preco) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setDescricao(descricao);
        p.setPrecoBase(new BigDecimal(preco));
        p.setAtivo(true);
        return produtoRepository.save(p);
    }

    private void disponibilizar(Unidade unidade, Produto produto, String preco, int quantidade) {
        ProdutoUnidade pu = new ProdutoUnidade();
        pu.setUnidade(unidade);
        pu.setProduto(produto);
        pu.setDisponivel(true);
        pu.setPrecoOverride(new BigDecimal(preco));
        produtoUnidadeRepository.save(pu);

        EstoqueItem estoque = new EstoqueItem();
        estoque.setUnidade(unidade);
        estoque.setProduto(produto);
        estoque.setQuantidade(quantidade);
        estoqueItemRepository.save(estoque);
    }

    private void criarUsuario(String nome, String email, String senha, Role role, Unidade unidade, Cliente cliente) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setUnidade(unidade);
        usuario.setCliente(cliente);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }
}
