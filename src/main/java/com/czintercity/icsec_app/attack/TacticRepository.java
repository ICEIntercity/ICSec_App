package com.czintercity.icsec_app.attack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacticRepository extends JpaRepository<Tactic, Long> {
    List<Tactic> findByName(String name);
}
