package me.stky.relaytd.api.service;

import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.model.CollectionEntry;

import java.util.List;
import java.util.Optional;

public interface CollectionService {
    List<CollectionEntry> getCollection(String collection);

    Optional<CollectionEntry> saveCollectionEntry(CollectionEntry entry, AstreID foreignKeyEntity);

}
