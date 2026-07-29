package br.com.cindyperico.raizesdonordeste.middleware;

import br.com.cindyperico.raizesdonordeste.model.AccessLog;
import br.com.cindyperico.raizesdonordeste.repository.AccessLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccessAuditFilter extends OncePerRequestFilter {

    private final AccessLogRepository accessLogRepository;

    public AccessAuditFilter(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;

        request.setAttribute("audit.username", username);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            try {
                AccessLog log = new AccessLog();
                log.setUsuario(username);
                log.setMetodo(request.getMethod());
                log.setPath(request.getRequestURI());
                log.setStatus(response.getStatus());
                log.setIp(request.getRemoteAddr());
                log.setDuracaoMs(duration);
                accessLogRepository.save(log);
            } catch (Exception ignored) {
            }
        }
    }
}
