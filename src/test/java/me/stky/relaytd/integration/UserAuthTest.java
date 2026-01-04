package me.stky.relaytd.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.Cookie;
import me.stky.relaytd.api.service.JWTService;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserAuthTest {


    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.security.jwt.key}")
    private String jwtKey;

    @Value("${spring.security.jwt.refresh.name}")
    private String jwtRefreshName;
    @Value("${spring.security.jwt.access.name}")
    private String jwtAccessName;

    @Autowired
    private JWTService jwtService;
    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    private AuthenticationManager authenticationManager;

    private final String testOpenEnpoint = "/test/open";
    private final String testVisitorEnpoint = "/test/visitor";
    private final String testUserEnpoint = "/test/user";
    private final String testAdminEnpoint = "/test/admin";

    @BeforeEach
    void init() {

    }

    @Test
    void testInit() {
        System.out.println("Test init");
    }

    @Test
    void testGetJwtAfterLogin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode loginRequest = objectMapper.createObjectNode();
        loginRequest.put("username", "visitor");
        loginRequest.put("password", "password");

        ResultActions mvcResult = mockMvc.perform(post("/auth/login2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest.toString())
        );

        mvcResult.andExpect(status().isOk());
        mvcResult.andExpect(cookie().exists(jwtRefreshName));
        mvcResult.andExpect(cookie().secure(jwtRefreshName, true));
        mvcResult.andExpect(cookie().httpOnly(jwtRefreshName, true));
        var response = mvcResult.andReturn().getResponse();

        assertNotNull(response.getCookie(jwtRefreshName));
    }

    @Test
    void testLoginVisitorRoles() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode loginRequest = objectMapper.createObjectNode();
        loginRequest.put("username", "visitor");
        loginRequest.put("password", "password");

        ResultActions mvcResult = mockMvc.perform(post("/auth/login2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest.toString())
        );

        mvcResult.andExpect(status().isOk());
        mvcResult.andExpect(cookie().exists(jwtRefreshName));
        MockHttpServletResponse response = mvcResult.andReturn().getResponse();

        Jwt jwt = getJwt(response);
        List<SimpleGrantedAuthority> authorities = jwtService.getRoles(jwt);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_INMEMORY")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_VISITOR")));
        assertFalse(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testLoginVisitor2Roles() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode loginRequest = objectMapper.createObjectNode();
        loginRequest.put("username", "visitor2");
        loginRequest.put("password", "password");

        ResultActions mvcResult = mockMvc.perform(post("/auth/login2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest.toString())
        );

        mvcResult.andExpect(status().isOk());
        mvcResult.andExpect(cookie().exists(jwtRefreshName));
        MockHttpServletResponse response = mvcResult.andReturn().getResponse();

        Jwt jwt = getJwt(response);
        List<SimpleGrantedAuthority> authorities = jwtService.getRoles(jwt);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_INMEMORY")));
        assertFalse(authorities.contains(new SimpleGrantedAuthority("ROLE_VISITOR")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testNoRoleEndpoint() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testOpenEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isOk());
    }

    @Test
    void testVisitorEndpoint_NoRole() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testVisitorEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isUnauthorized());
    }

    @Test
    void testVisitorEndpoint_withVisitor() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testVisitorEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isUnauthorized());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("visitor", "password"));
        String jwt = jwtService.generateToken(authentication);
        mockMvc.perform(get(testVisitorEnpoint).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)).andExpect(status().isOk());
    }


    @Test
    void testUserEndpoint_NoRole() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testUserEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isUnauthorized());
    }

    @Test
    void testUserEndpoint_withVisitor() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testUserEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isUnauthorized());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("visitor", "password"));
        String jwt = jwtService.generateToken(authentication);
        mockMvc.perform(get(testUserEnpoint).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)).andExpect(status().isForbidden());
    }

    @Test
    void testUserEndpoint_withVisitorUser() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testUserEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isUnauthorized());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("visitor2", "password"));
        String jwt = jwtService.generateToken(authentication);
        System.out.println(jwt.length());
        mockMvc.perform(get(testUserEnpoint).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)).andExpect(status().isOk());
    }

    @Test
    void testAdminEndpoint_NoRole() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testAdminEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminEndpoint_withVisitor() throws Exception {
        ResultActions mvcResult = mockMvc.perform(get(testAdminEnpoint).contentType(MediaType.APPLICATION_JSON));
        mvcResult.andExpect(status().isUnauthorized());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("visitor", "password"));
        String jwt = jwtService.generateToken(authentication);
        mockMvc.perform(get(testAdminEnpoint).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)).andExpect(status().isForbidden());

        Authentication authentication2 = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("visitor2", "password"));
        String jwt2 = jwtService.generateToken(authentication);
        mockMvc.perform(get(testAdminEnpoint).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt2)).andExpect(status().isForbidden());
    }


    private Jwt getJwt(MockHttpServletResponse response) {
        Cookie cookie = response.getCookie(jwtRefreshName);
        assertNotNull(cookie);
        assertTrue(jwtService.validateCookie(cookie));

        return jwtDecoder.decode(cookie.getValue());
    }

}
