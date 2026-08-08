package eventHub.deepak.security;

import eventHub.deepak.service.interfaces.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JwtAuthenticationFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
}
