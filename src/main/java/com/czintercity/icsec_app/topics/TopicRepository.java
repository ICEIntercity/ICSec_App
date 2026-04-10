package com.czintercity.icsec_app.topics;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface TopicRepository extends CrudRepository<Topic, UUID> {
}
