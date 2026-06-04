package com.czintercity.icsec_app.relationships.techniqueCoverage.repository;

import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Spring Data repository for {@link TechniqueCoverage} records, keyed by surrogate {@code Long} ID.
 */
public interface TechniqueCoverageRepository extends JpaRepository<TechniqueCoverage, Long> {
    /** Returns all coverage records associated with the given control. */
    List<TechniqueCoverage> findByControl(Control control);
    /** Returns all coverage records associated with the given technique. */
    List<TechniqueCoverage> findByTechnique(Technique technique);
    /** Returns coverage records that link the specific control to the specific technique. */
    List<TechniqueCoverage> findByControlAndTechnique(Control control, Technique technique);
}