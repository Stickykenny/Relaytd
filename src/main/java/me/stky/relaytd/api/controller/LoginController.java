package me.stky.relaytd.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {


    @GetMapping("/user")
public String getUser(){
return "User page";
}

@GetMapping("/admin")
    public String getAdmin(){
        return "Admin page";
    }
}
