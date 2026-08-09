package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.auth.LoginRequest;
import br.com.cindyperico.raizesdonordeste.dto.auth.LoginResponse;
import br.com.cindyperico.raizesdonordeste.dto.auth.RegistroRequest;
import br.com.cindyperico.raizesdonordeste.dto.auth.UsuarioResponse;
import br.com.cindyperico.raizesdonordeste.exception.BusinessRuleException;
import br.com.cindyperico.raizesdonordeste.exception.NotFoundException;
import br.com.cindyperico.raizesdonordeste.model.Usuario;
import br.com.cindyperico.raizesdonordeste.model.enums.Role;
import br.com.cindyperico.raizesdonordeste.repository.UsuarioRepository;
import br.com.cindyperico.raizesdonordeste.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public LoginResponse login(HttpServletRequest request, LoginRequest dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BadCredentialsException("Usuário inativo");
        }

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenhaHash())) {
            auditService.log(request, "LOGIN_FALHOU", "Usuario", usuario.getId(), "Senha inválida");
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getRole().name());
        auditService.log(request, "LOGIN_SUCESSO", "Usuario", usuario.getId(), "Role=" + usuario.getRole());

        return new LoginResponse(token, "Bearer", jwtService.getExpiracaoMs(), usuario.getEmail(), usuario.getRole());
    }

    public UsuarioResponse registrarCliente(HttpServletRequest request, RegistroRequest dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessRuleException("Já existe usuário cadastrado com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(true);

        Usuario saved = usuarioRepository.save(usuario);
        auditService.log(request, "USUARIO_REGISTRADO", "Usuario", saved.getId(), "Role=CLIENTE");

        return toResponse(saved);
    }

    public UsuarioResponse buscarPorEmail(String email) {
        return toResponse(usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado")));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getRole(),
                u.getUnidade() != null ? u.getUnidade().getId() : null,
                u.getCliente() != null ? u.getCliente().getId() : null);
    }
}
