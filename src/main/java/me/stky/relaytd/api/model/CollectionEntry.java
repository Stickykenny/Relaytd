package me.stky.relaytd.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode
@IdClass(CollectionEntryID.class)
@Table(name = "collections")
public class CollectionEntry {

    @Id
    @NotNull
    private String id;

    @Id
    @NotNull
    private String collection;

    @Id
    @NotNull
    private String variant;

    @NotNull
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "type", referencedColumnName = "type"),
            @JoinColumn(name = "subtype", referencedColumnName = "subtype"),
            @JoinColumn(name = "name", referencedColumnName = "name")
    })
    private Astre astre;

    private String qty;
    private String description;
    private String parent;
    private LocalDate acquisition_date;

}
