package com.czintercity.icsec_app.controls;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface ControlRepository extends CrudRepository<Control, UUID> {
}
