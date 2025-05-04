package me.stky.relaytd.api.model;


import lombok.Builder;

import java.util.Objects;


@Builder
public record AstreDTO(
        AstreID astreID,
        String tags,
        String description,
        String parent
) {
    public AstreDTO {
        Objects.requireNonNull(astreID, "Please provide a non-null ID for the Astre");
    }
}
