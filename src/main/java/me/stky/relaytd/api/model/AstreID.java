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
public class AstreID {
    @NotNull
    private String type;
    @NotNull
    private String subtype;
    @NotNull
    private String name;
}
