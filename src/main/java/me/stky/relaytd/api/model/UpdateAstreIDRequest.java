package me.stky.relaytd.api.model;

import lombok.Getter;

@Getter
public class UpdateAstreIDRequest {

    private AstreID oldID;
    private AstreID newID;
}
