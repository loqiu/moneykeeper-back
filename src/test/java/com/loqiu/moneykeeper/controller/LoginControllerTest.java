package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
import com.loqiu.moneykeeper.entity.User;
import com.loqiu.moneykeeper.service.LoginService;
import com.loqiu.moneykeeper.service.PasswordService;
import com.loqiu.moneykeeper.service.UserService;
import com.loqiu.moneykeeper.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private LoginService loginService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LoginController controller = new LoginController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "passwordService", passwordService);
        ReflectionTestUtils.setField(controller, "loginService", loginService);
        ReflectionTestUtils.setField(controller, "jwtUtil", new JwtUtil());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginShouldReturnBusinessBadRequestWhenPasswordMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errorKey").value(ErrorKeyConstants.COMMON_BAD_REQUEST))
                .andExpect(jsonPath("$.message").value("Username and password are required"));
    }

    @Test
    void registerShouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("taken@example.com");

        when(userService.findByUsername("alice")).thenReturn(null);
        when(userService.findByEmail("taken@example.com")).thenReturn(existingUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "secret123",
                                  "email": "taken@example.com",
                                  "firstName": "Alice",
                                  "lastName": "Smith"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.errorKey").value(ErrorKeyConstants.AUTH_EMAIL_EXISTS))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void googleAuthShouldReturnBusinessBadRequestWhenIdTokenMissing() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errorKey").value(ErrorKeyConstants.AUTH_GOOGLE_ID_TOKEN_REQUIRED))
                .andExpect(jsonPath("$.message").value("Google idToken is required"));
    }
}
