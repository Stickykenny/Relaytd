package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.model.UpdateAstreIDRequest;
import me.stky.relaytd.api.service.AstreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "BearerAuthentication")
@RequestMapping("/api/astres")
public class AstreController {

    @Autowired
    AstreService astreService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/welcome")
    public ResponseEntity<String> getWelcome(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println(" cookie: " + cookie.getName() + " = " + cookie.getValue());
            }
        }
        System.out.println();
        System.out.println();
        return new ResponseEntity<>("Welcome to the controller", HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')") // TODO : Change this in prod
    @Operation(summary = "Save an astre", description = "Save an astre, doesn't save if the ID is already used")
    @PostMapping("/astre")
    public ResponseEntity<Astre> saveAstre(@RequestBody Astre astre) {
        return astreService.saveAstre(astre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Get all astres", description = "Get all astres")
    @GetMapping("/getall")
    public ResponseEntity<List<Astre>> getAstres() {
        return ResponseEntity.ok(astreService.getAllAstre());
    }

    @Operation(summary = "Get an astre", description = "Get an astre using a type and name")
    @GetMapping("/astre")
    public ResponseEntity<Astre> getAstre(String type, String subtype, String name) {
        // Don't use Request Body on Get Mapping, it is allowed but most of the tome not supported
        AstreID astreID = new AstreID(type, subtype, name);
        return astreService.getAstreById(astreID)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /*
    // This method is kept for archiving purpose : Using Path Variable

    @Operation(summary = "Get an astre", description = "Get an astre using a type and name")
    @GetMapping("/{type}/{name}")
    public ResponseEntity<Astre> getAstre(@PathVariable("type") String type, @PathVariable("name") String name) {
        return astreService.getAstreById(type, name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }*/

    @PreAuthorize("hasAuthority('ROLE_USER')") // TODO : Change this in prod
    @Operation(summary = "Update an astre", description = "Update (or create) an astre using a type and name")
    @PutMapping("/astre")
    public ResponseEntity<Astre> update(@RequestBody AstreDTO astreDTO) {
        return astreService.updateAstre(astreDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ROLE_USER')") // TODO : Change this in prod
    @Operation(summary = "Delete an astre", description = "Delete using the type and name")
    @DeleteMapping("/astre")
    public ResponseEntity<Object> deleteAstre(@RequestBody AstreID astreID) {
        if (astreService.deleteAstre(astreID)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PreAuthorize("hasAuthority('ROLE_USER')") // TODO : Change this in prod
    @Operation(summary = "Save/Update multiples astre", description = "Mass update")
    @PostMapping("/astres")
    public ResponseEntity<List<Astre>> upsertAstres(@RequestBody List<AstreDTO> astresDTO) {
        System.out.println("save multiples");
        List<Astre> upsertedAstres = astreService.upsertAstres(astresDTO);
        if (upsertedAstres.isEmpty()) {
            return ResponseEntity.noContent().build(); // returns 204 No Content with no body
        }
        return ResponseEntity.ok(upsertedAstres); // returns 200 regardless of list content
    }


    @PreAuthorize("hasAuthority('ROLE_USER')") // TODO : Change this in prod
    @Operation(summary = "Update an Astre's ID", description = "Update an Astre's ID, remove the old one")
    @PutMapping("astreid")
    public ResponseEntity<Astre> updateAstreID(@RequestBody UpdateAstreIDRequest updateRequest) {
        return astreService.updateAstreID(updateRequest.getOldID(), updateRequest.getNewID())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
