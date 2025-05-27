package com.accesscontroll.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        // Обычно роли в realm_access.roles с префиксом "ROLE_"

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
            converter.setAuthorityPrefix("ROLE_");
            converter.setAuthoritiesClaimName("realm_access.roles");
            Collection<String> realmRoles = converter.convert(jwt)
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            List<String> groups = jwt.getClaimAsStringList("groups");
            if (groups != null) {
                realmRoles.addAll(groups.stream().map(g -> "GROUP_" + g).toList());
            }
            return realmRoles.stream().map(r -> (org.springframework.security.core.GrantedAuthority) () -> r).toList();
        });

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/resources/**").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakRoleConverter()))
                );;

        return http.build();
    }
}