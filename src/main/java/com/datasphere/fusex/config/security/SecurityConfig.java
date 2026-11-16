package com.datasphere.fusex.config.security;

import com.datasphere.fusex.auth.service.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenService tokenService;
    private final RespostaErroSeguranca respostaErroSeguranca;

    public SecurityConfig(TokenService tokenService, RespostaErroSeguranca respostaErroSeguranca) {
        this.tokenService = tokenService;
        this.respostaErroSeguranca = respostaErroSeguranca;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostaErroSeguranca::naoAutenticado)
                        .accessDeniedHandler(respostaErroSeguranca::acessoNegado))
                .addFilterBefore(new TokenAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(rotas -> rotas
                        // Públicas (a ordem importa: vale a PRIMEIRA regra que casar)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/cadastro").permitAll()
                        .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Pré-guias
                        .requestMatchers(HttpMethod.PATCH, "/pre-guias/*/aprovar", "/pre-guias/*/reprovar")
                        .hasAnyRole("MEDICO", "CHEFE")
                        .requestMatchers(HttpMethod.POST, "/pre-guias/*/validar", "/pre-guias/*/emitir").hasRole("EMISSOR")
                        .requestMatchers(HttpMethod.POST, "/pre-guias").hasRole("EMISSOR")
                        .requestMatchers(HttpMethod.PUT, "/pre-guias/*").hasRole("EMISSOR")
                        .requestMatchers(HttpMethod.GET, "/pre-guias", "/pre-guias/**")
                        .hasAnyRole("MEDICO", "CHEFE", "EMISSOR")

                        // Guias e beneficiário
                        .requestMatchers(HttpMethod.GET, "/beneficiarios/*/pre-guias").hasRole("BENEFICIARIO")
                        .requestMatchers(HttpMethod.GET, "/guias/*/documento").hasAnyRole("EMISSOR", "CHEFE", "BENEFICIARIO")
                        .requestMatchers(HttpMethod.GET, "/guias/*/historico").hasAnyRole("AUDITOR", "EMISSOR", "CHEFE")
                        .requestMatchers(HttpMethod.GET, "/beneficiarios/*/guias").hasAnyRole("BENEFICIARIO", "EMISSOR", "CHEFE")
                        .requestMatchers(HttpMethod.GET, "/beneficiarios").hasAnyRole("CHEFE", "EMISSOR", "MEDICO")

                        // OCS e atendimentos (o QR do beneficiário vem antes da regra geral da OCS)
                        .requestMatchers(HttpMethod.GET, "/atendimentos/*/qrcode").hasRole("BENEFICIARIO")
                        .requestMatchers("/atendimentos/**", "/ocs/*/atendimentos").hasRole("OCS")

                        // Espelhos
                        .requestMatchers(HttpMethod.PATCH, "/espelhos/*/aprovar", "/espelhos/*/reprovar").hasRole("AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/espelhos", "/espelhos/**").hasAnyRole("OCS", "AUDITOR")
                        .requestMatchers("/espelhos", "/espelhos/**").hasRole("OCS")

                        // Faturas
                        .requestMatchers("/faturas", "/faturas/**").hasRole("AUDITOR")

                        // Cadastros: leitura para quem monta pré-guia, escrita só para o chefe
                        .requestMatchers(HttpMethod.GET, "/ocs", "/procedimentos", "/contratos", "/medicos")
                        .hasAnyRole("CHEFE", "EMISSOR", "MEDICO")
                        .requestMatchers("/ocs", "/procedimentos", "/contratos", "/usuarios", "/usuarios/**")
                        .hasRole("CHEFE")

                        // Administração
                        .requestMatchers("/admin/**").hasRole("CHEFE")

                        // Qualquer outra rota: basta estar logado
                        .anyRequest().authenticated());

        return http.build();
    }
}
