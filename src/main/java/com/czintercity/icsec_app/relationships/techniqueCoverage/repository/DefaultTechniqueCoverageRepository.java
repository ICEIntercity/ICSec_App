package com.czintercity.icsec_app.relationships.techniqueCoverage.repository;

import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.DefaultTechniqueCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultTechniqueCoverageRepository extends JpaRepository<DefaultTechniqueCoverage, Long> {
    List<DefaultTechniqueCoverage> findByControl(Control control);
    List<DefaultTechniqueCoverage> findByTechnique(Technique technique);
    List<DefaultTechniqueCoverage> findByControlAndTechnique(Control control, Technique technique);
}
