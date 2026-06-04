package com.czintercity.icsec_app.attack.repository;

import com.czintercity.icsec_app.attack.entity.Tactic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
/**
 * Spring Data repository for {@link Tactic} entities, keyed by {@link UUID}.
 */
public interface TacticRepository extends JpaRepository<Tactic, UUID> {
    /** Returns all tactics whose name exactly matches {@code name}. */
    List<Tactic> findByName(String name);
}
