package me.stky.relaytd.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "connections")
public class Astre {

    @EmbeddedId
    private AstreID astreID;

    private String tags;
    private String description;
    @Schema(hidden = true)
    private LocalDate date_added;
    @Schema(hidden = true)
    private LocalDate last_modified;
}
