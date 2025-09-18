package me.stky.relaytd.api.model;


import lombok.Builder;

import java.util.Objects;


@Builder
public record AstreDTO(
        AstreID astreID,


        String subname,
        String tags,
        String link,
        String description,
        String parent,
        String id,

        Boolean fromBefore
) {
    public AstreDTO {
        Objects.requireNonNull(astreID, "Please provide a non-null ID for the Astre");
    }
}
