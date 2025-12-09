package me.stky.relaytd.api.model;

import jakarta.persistence.*;
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
    private String id;

    @Id
    private String collection;

    @Id
    private String variant;

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
