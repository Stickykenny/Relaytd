package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.model.CollectionEntry;
import me.stky.relaytd.api.model.UpdateAstreIDRequest;
import me.stky.relaytd.api.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "BearerAuthentication")
@RequestMapping("/api/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "", description = "")
    @PostMapping("/getCollectionFromName")
    public ResponseEntity<List<CollectionEntry>> getCollectionFromName(@RequestBody String collection) {
        return ResponseEntity.ok(collectionService.getCollectionFromName(collection));
    }


    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "", description = "")
    @PostMapping("/getCollection")
    public ResponseEntity<List<CollectionEntry>> getCollection(@RequestBody AstreID astreID) {
        return ResponseEntity.ok(collectionService.getFromSource(astreID));
    }


    @PreAuthorize("hasAuthority('ROLE_USER')") // TODO : Change this in prod
    @Operation(summary = "", description = "")
    @PostMapping("/astre")
    public ResponseEntity<CollectionEntry> saveAstre(@RequestBody CollectionEntry entry) {
        return collectionService.saveCollectionEntry(entry, entry.getAstre().getAstreID())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "", description = "")
    @PutMapping("/changeAstreSource")
    public ResponseEntity<List<CollectionEntry>> changeAstreSource(@RequestBody UpdateAstreIDRequest updateAstreIDRequest) {
        return ResponseEntity.ok(collectionService.getFromSource(updateAstreIDRequest.getNewID()));
    }

}
