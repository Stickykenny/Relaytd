package me.stky.relaytd.api.service;

import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.repository.AstreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AstreServiceImpl implements AstreService {

    private final AstreRepository astreRepository;


    @Autowired
    public AstreServiceImpl(AstreRepository astreRepository) {
        this.astreRepository = astreRepository;
    }

    @Override
    public List<Astre> getAllAstre() {
        return astreRepository.findAll().stream().toList();
    }

    @Override
    public Optional<Astre> getAstreById(String type, String name) {
        return astreRepository.findById(new AstreID(type, name));
    }

    @Override
    public Optional<Astre> saveAstre(Astre astre) {
        AstreID astreID = astre.getAstreID();
        if (astreRepository.findById(astreID).isEmpty()) {
            astre.setDate_added(LocalDate.now());
            astre.setLast_modified(LocalDate.now());
            return Optional.of(astreRepository.save(astre));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Astre> updateAstre(AstreDTO astreDTO) {
        Optional<Astre> optAstre = astreRepository.findById(astreDTO.astreID());
        if (optAstre.isEmpty()) {
            return saveAstre(createOrUpdateTimestamps(convertToEntity(astreDTO)));
        }
        Astre astre = createOrUpdateTimestamps(convertToEntity(astreDTO));
        return Optional.of(astreRepository.save(astre));

    }

    @Override
    public boolean deleteAstre(String type, String name) {
        astreRepository.deleteById(new AstreID(type, name));
        return astreRepository.findById(new AstreID(type, name)).isEmpty();
    }

    @Override
    public List<Astre> upsertAstres(List<AstreDTO> astresDTO) {
        List<Astre> updatedAstres = new ArrayList<>();
        for (AstreDTO astreDTO : astresDTO) {
            updateAstre(astreDTO).map(updatedAstres::add);
        }
        return updatedAstres;
    }

    private Astre convertToEntity(AstreDTO astreDTO) {
        Astre astre = new Astre();
        astre.setAstreID(astreDTO.astreID());
        astre.setTags(astreDTO.tags());
        astre.setDescription(astreDTO.description());
        astre.setParent(astreDTO.parent());
        astre.setDescription(astreDTO.description());
        return astre;
    }

    private Astre createOrUpdateTimestamps(Astre astre) {
        Optional<Astre> astreDB = astreRepository.findById(astre.getAstreID());
        if (astreDB.isPresent()) {
            astre.setDate_added(astreDB.get().getDate_added());
        } else {
            astre.setDate_added(LocalDate.now());
        }
        astre.setLast_modified(LocalDate.now());
        return astre;
    }
}
