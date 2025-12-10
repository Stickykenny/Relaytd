package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import me.stky.relaytd.api.model.CollectionEntry;
import me.stky.relaytd.api.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name = "BearerAuthentication")
@RequestMapping("/api/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    //@PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Get all astres", description = "Get all astres")
    @PostMapping("/getCollection")
    public ResponseEntity<List<CollectionEntry>> getCollection(@RequestBody String collection) {

        return ResponseEntity.ok(collectionService.getCollection(collection));
    }


    @PreAuthorize("hasAuthority('ROLE_USER')") // TODO : Change this in prod
    @Operation(summary = "Save a ", description = "Save an astre, doesn't save if the ID is already used")
    @PostMapping("/astre")
    public ResponseEntity<CollectionEntry> saveAstre(@RequestBody CollectionEntry entry) {
        return collectionService.saveCollectionEntry(entry, entry.getAstre().getAstreID())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

}
