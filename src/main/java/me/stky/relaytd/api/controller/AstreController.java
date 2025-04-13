package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;
import me.stky.relaytd.api.repository.AstreRepository;
import me.stky.relaytd.api.service.AstreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/astres")
public class AstreController {

    @Autowired
    AstreRepository connectionRepository;

    @Autowired
    AstreService astreService;

    @GetMapping("/welcome")
    public String getWelcome() {
        return "Welcome to the controller";
    }

    @Operation(summary = "Save an astre", description = "Save an astre, doesn't save if the ID is already used")
    @PostMapping("/astre")
    public Optional<Astre> saveAstre(@RequestBody Astre astre) {
        return astreService.saveAstre(astre);
    }

    @Operation(summary = "Get all astres", description = "Get all astres")
    @GetMapping("/getall")
    public List<Astre> getAstres() {
        return astreService.getAllAstre();
    }

    @Operation(summary = "Get an astre", description = "Get an astre using a type and name")
    @GetMapping("/{type}/{name}")
    private Optional<Astre> getAstre(@PathVariable("type") String type, @PathVariable("name") String name) {
        return astreService.getAstreById(type, name);
    }

    @Operation(summary = "Update an astre", description = "Update (or create) an astre using a type and name")
    @PutMapping("/astre")
    private Astre update(@RequestBody Astre astre) {
        astreService.updateAstre(astre);
        return astre;
    }

    @Operation(summary = "Delete an astre", description = "Delete using the type and name")
    @DeleteMapping("/{type}/{name}")
    private void deleteAstre(@PathVariable("type") String type, @PathVariable("name") String name) {
        astreService.deleteAstre(type, name);
    }

}
