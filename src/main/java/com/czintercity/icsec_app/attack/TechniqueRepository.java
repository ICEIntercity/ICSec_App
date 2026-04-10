package com.czintercity.icsec_app.attack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TechniqueRepository extends JpaRepository<Technique, UUID> {

}
