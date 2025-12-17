package me.stky.relaytd.api.model;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class CollectionEntryID {
    @NotNull
    private String id;
    @NotNull
    private String collection;
    @NotNull
    private String variant;
}
