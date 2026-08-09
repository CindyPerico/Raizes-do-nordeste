package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.auth.LoginRequest;
import br.com.cindyperico.raizesdonordeste.dto.auth.LoginResponse;
import br.com.cindyperico.raizesdonordeste.dto.auth.RegistroRequest;
import br.com.cindyperico.raizesdonordeste.dto.auth.UsuarioResponse;
import br.com.cindyperico.raizesdonordeste.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticação e emissão de token JWT")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário e devolve o token JWT de acesso")
    public ResponseEntity<LoginResponse> login(HttpServletRequest request, @Valid @RequestBody LoginRequest dto) {
        return ResponseEntity.ok(usuarioService.login(request, dto));
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registra um novo usuário com perfil CLIENTE")
    public ResponseEntity<UsuarioResponse> registrar(HttpServletRequest request, @Valid @RequestBody RegistroRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrarCliente(request, dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna os dados do usuário autenticado")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(authentication.getName()));
    }
}
