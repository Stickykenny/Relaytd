package me.stky.relaytd.api.service;

import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;

import java.util.List;
import java.util.Optional;

public interface AstreService {
    List<Astre> getAllAstre();

    Optional<Astre> getAstreById(String type, String name);

    Optional<Astre> saveAstre(Astre astre);

    Optional<Astre> updateAstre(AstreDTO astreDTO);

    boolean deleteAstre(String type, String name);

    List<Astre> upsertAstres(List<AstreDTO> astresDTO);
}
