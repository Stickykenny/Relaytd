package me.stky.relaytd.api.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class LoginRequest {
    @Schema(description = "Username for login", example = "visitor")
    private String username;
    @Schema(description = "Password for login", example = "password")
    private String password;
}