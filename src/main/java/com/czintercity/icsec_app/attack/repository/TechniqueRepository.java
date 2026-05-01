package com.czintercity.icsec_app.attack.repository;

import com.czintercity.icsec_app.attack.entity.Technique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechniqueRepository extends JpaRepository<Technique, UUID> {

    Optional<Technique> findByMitreId(String mitreId);
}
