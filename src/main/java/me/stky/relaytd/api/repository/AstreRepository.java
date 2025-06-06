package me.stky.relaytd.api.repository;

import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AstreRepository extends JpaRepository<Astre, AstreID> {

}
