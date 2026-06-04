package com.czintercity.icsec_app.attack.repository;

import com.czintercity.icsec_app.attack.entity.Technique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
/**
 * Spring Data repository for {@link Technique} entities, keyed by {@link UUID}.
 */
public interface TechniqueRepository extends JpaRepository<Technique, UUID> {

    /** Returns the technique with the given MITRE ATT&amp;CK identifier, or empty if not found. */
    Optional<Technique> findByMitreId(String mitreId);
}
