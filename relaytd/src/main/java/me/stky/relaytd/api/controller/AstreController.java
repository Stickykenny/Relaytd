package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.service.AstreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/astres")
public final class AstreController {

    @Autowired
    AstreService astreService;

    @GetMapping("/welcome")
    public ResponseEntity<String> getWelcome() {
        return new ResponseEntity<>("Welcome to the controller", HttpStatus.OK);
    }

    @Operation(summary = "Save an astre", description = "Save an astre, doesn't save if the ID is already used")
    @PostMapping("/astre")
    public ResponseEntity<Astre> saveAstre(@RequestBody Astre astre) {
        return astreService.saveAstre(astre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Get all astres", description = "Get all astres")
    @GetMapping("/getall")
    public ResponseEntity<List<Astre>> getAstres() {
        return ResponseEntity.ok(astreService.getAllAstre());
    }

    @Operation(summary = "Get an astre", description = "Get an astre using a type and name")
    @GetMapping("/{type}/{name}")
    public ResponseEntity<Astre> getAstre(@PathVariable("type") String type, @PathVariable("name") String name) {
        return astreService.getAstreById(type, name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @Operation(summary = "Update an astre", description = "Update (or create) an astre using a type and name")
    @PutMapping("/astre")
    public ResponseEntity<Astre> update(@RequestBody AstreDTO astreDTO) {
        return astreService.updateAstre(astreDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an astre", description = "Delete using the type and name")
    @DeleteMapping("/{type}/{name}")
    public ResponseEntity<Object> deleteAstre(@PathVariable("type") String type, @PathVariable("name") String name) {
        if (astreService.deleteAstre(type, name)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Operation(summary = "Save/Update multiples astre", description = "Mass update")
    @PostMapping("/astres")
    public ResponseEntity<List<Astre>> upsertAstres(@RequestBody List<AstreDTO> astresDTO) {
        List<Astre> upsertedAstres = astreService.upsertAstres(astresDTO);
        if (upsertedAstres.isEmpty()) {
            return ResponseEntity.noContent().build(); // returns 204 No Content with no body
        }
        return ResponseEntity.ok(upsertedAstres); // returns 200 regardless of list content
    }


    @Operation(summary = "Update an Astre's ID", description = "Update an Astre's ID, remove the old one")
    @PutMapping("/{type}/{name}")
    public ResponseEntity<Astre> updateAstreID(@PathVariable("type") String type, @PathVariable("name") String name, @RequestBody AstreID newAstreID) {
        return astreService.updateAstreID(new AstreID(type, name), newAstreID)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
