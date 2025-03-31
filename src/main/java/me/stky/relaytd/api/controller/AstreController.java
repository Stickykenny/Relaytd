package me.stky.relaytd.api.controller;

import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;
import me.stky.relaytd.api.repository.AstreRepository;
import me.stky.relaytd.api.service.AstreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/astres")
public class AstreController {

    @Autowired
    AstreRepository connectionRepository;

    @Autowired
    AstreService astreService;

    @GetMapping("/welcome")
    public String getWelcome(){
        return "Welcome to the controller";
    }

    @GetMapping("/getall")
    public List<AstreDTO> getAllAstres() {
        return astreService.getAllAstre();
    }

    @PostMapping("/astre")
    public Astre saveAstre(@RequestBody Astre astre) {
        Astre a = astreService.saveAstre(astre);
        return a;
    }
}
