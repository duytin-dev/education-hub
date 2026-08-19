package com.iTech.education;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        String requestJson = """
                {
                  "email": "student@test.com",
                  "password": "pass123",
                  "fullName": "Test Student",
                  "phone": "0123456789"
                }
                """;

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("student@test.com"));
    }

    @Test
    void register_shouldReturn400_whenEmailAlreadyExists() throws Exception {
        String requestJson = """
                {
                  "email": "duplicate@test.com",
                  "password": "pass123",
                  "fullName": "Duplicate User",
                  "phone": "0123456789"
                }
                """;

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_shouldReturn200WithToken_whenCredentialsAreValid() throws Exception {
        String registerJson = """
                {
                  "email": "login@test.com",
                  "password": "pass123",
                  "fullName": "Login User",
                  "phone": "0123456789"
                }
                """;

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
                {
                  "email": "login@test.com",
                  "password": "pass123"
                }
                """;

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void login_shouldReturn400_whenPasswordIsWrong() throws Exception {
        String registerJson = """
                {
                  "email": "wrongpass@test.com",
                  "password": "pass123",
                  "fullName": "Wrong Pass User",
                  "phone": "0123456789"
                }
                """;

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
                {
                  "email": "wrongpass@test.com",
                  "password": "wrong-password"
                }
                """;

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
