package com.czintercity.icsec_app.controls.repository;

import com.czintercity.icsec_app.controls.entity.Control;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

/**
 * Spring Data repository for {@link Control} entities, keyed by {@link UUID}.
 */
public interface ControlRepository extends CrudRepository<Control, UUID> {
    /**
     * Returns a proxy reference to the control with the given ID without loading it from the database.
     * Throws {@link jakarta.persistence.EntityNotFoundException} at access time if no such control exists.
     */
    Control getReferenceById(UUID id);
}
