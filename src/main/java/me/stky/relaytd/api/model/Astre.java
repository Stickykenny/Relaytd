package me.stky.relaytd.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "connections")
public class Astre {

    @EmbeddedId
    private AstreID astreID;

    private String tags;
    private String description;
    private String parent;
    @Schema(hidden = true)
    private LocalDate date_added;
    @Schema(hidden = true)
    private LocalDate last_modified;
    private Boolean from_before; // this field indicate the date_added isn't representative of the date this astre was discovered

    /**
     * Deep copy
     *
     * @return a deep-copy of the current Astre
     */
    public Astre clone() {
        return new Astre(
                new AstreID(astreID.getType(), astreID.getName()),
                tags, description, parent, date_added, last_modified, from_before);
    }
}
