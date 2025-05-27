package com.accesscontroll.main.services;

import com.accesscontroll.main.DTO.RoleDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KeycloakAdminService {

    private final String serverUrl = "http://localhost:8180";
    private final String realm = "company-access";
    private final String clientId = "admin-client";
    private final String clientSecret = "z26KlTs7yJPyoS4bc7s1X5M0wOUGM7WF";

    private String getAccessToken() {
        WebClient webClient = WebClient.create();
        return webClient.post()
                .uri(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(Map.class)
                .map(tokenMap -> tokenMap.get("access_token").toString())
                .block();
    }

    public Optional<String> getUserIdByUsername(String username) {
        String token = getAccessToken();
        WebClient webClient = WebClient.create();
        List<?> users = webClient.get()
                .uri(serverUrl + "admin/master/console/#/" + realm + "/users?username=" + username)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (users != null && !users.isEmpty()) {
            Map<?, ?> user = (Map<?, ?>) users.get(0);
            return Optional.of(user.get("id").toString());
        }
        return Optional.empty();
    }

    public List<?> getAllUsers() {
        String token = getAccessToken();
        WebClient webClient = WebClient.create();
        return webClient.get()
                .uri(serverUrl + "/admin/realms/" + realm + "/users")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }

    public void updateUser(String userId, Map<String, Object> updateData) {
        String token = getAccessToken();
        WebClient webClient = WebClient.create();

        webClient.put()
                .uri(serverUrl + "/admin/realms/" + realm + "/users/" + userId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateData)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public List<Map<String, Object>> getRealmRoles() {
        String token = getAccessToken();
        WebClient webClient = WebClient.create();

        return webClient.get()
                .uri(serverUrl + "/admin/realms/" + realm + "/roles")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }

    public void assignRealmRole(String userId, RoleDTO roleDTO) {
        String token = getAccessToken();

        WebClient webClient = WebClient.create();

        // Получаем все доступные роли
        List<Map<String, Object>> availableRoles = webClient.get()
                .uri(serverUrl + "/admin/realms/" + realm + "/roles")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        // Ищем нужную по имени
        Map<String, Object> roleToAssign = availableRoles.stream()
                .filter(role -> roleDTO.getRoleName().equals(role.get("name")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));

        // Назначаем
        webClient.post()
                .uri(serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(roleToAssign))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
