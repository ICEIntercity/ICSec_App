package com.czintercity.icsec_app.controls.repository;

import com.czintercity.icsec_app.controls.entity.Control;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface ControlRepository extends CrudRepository<Control, UUID> {
}
