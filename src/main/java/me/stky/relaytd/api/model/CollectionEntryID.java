package me.stky.relaytd.api.model;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class CollectionEntryID {
    private String id;
    private String collection;
    private String variant;
}
