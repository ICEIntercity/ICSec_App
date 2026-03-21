package com.czintercity.icsec_app.relationships.techniqueCoverage;

import com.czintercity.icsec_app.attack.Technique;
import com.czintercity.icsec_app.controls.Control;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultTechniqueCoverageRepository extends JpaRepository<DefaultTechniqueCoverage, Long> {
    List<DefaultTechniqueCoverage> findByControl(Control control);
    List<DefaultTechniqueCoverage> findByTechnique(Technique technique);
    List<DefaultTechniqueCoverage> findByControlAndTechnique(Control control, Technique technique);
}
