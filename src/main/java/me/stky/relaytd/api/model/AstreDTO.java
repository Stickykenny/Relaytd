package me.stky.relaytd.api.model;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record AstreDTO(
        @NotNull
        AstreID astreID,

        String subname,
        String tags,
        String link,
        String description,
        String parent,
        String id,

        Boolean fromBefore
) {
}
