package me.stky.relaytd.api.controller;


import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/csrf/token")
public class CsrfController {

    @GetMapping
    public CsrfToken CsrfToken(CsrfToken token) {
        return token;

    }
}