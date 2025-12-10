package me.stky.relaytd.api.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.model.CollectionEntry;
import me.stky.relaytd.api.model.CollectionEntryID;
import me.stky.relaytd.api.repository.CollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    public CollectionServiceImpl(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Override
    public List<CollectionEntry> getCollection(String collection) {
        return collectionRepository.findAll();//collectionRepository.getAllByCollection(collection);
    }

    @Override
    public Optional<CollectionEntry> saveCollectionEntry(CollectionEntry entry, AstreID foreignKeyEntity) {

        CollectionEntryID id = new CollectionEntryID(entry.getId(), entry.getCollection(), entry.getVariant());
        if (collectionRepository.findById(id).isEmpty()) {
            Astre astreRef = entityManager.getReference(Astre.class, foreignKeyEntity);

            entry.setAstre(astreRef);

            return Optional.of(collectionRepository.save(entry));
        }
        return Optional.empty();
    }

}
