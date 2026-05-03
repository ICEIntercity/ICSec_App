package com.czintercity.icsec_app.relationships.techniqueCoverage.repository;

import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechniqueCoverageRepository extends JpaRepository<TechniqueCoverage, Long> {
    List<TechniqueCoverage> findByControl(Control control);
    List<TechniqueCoverage> findByTechnique(Technique technique);
    List<TechniqueCoverage> findByControlAndTechnique(Control control, Technique technique);
}