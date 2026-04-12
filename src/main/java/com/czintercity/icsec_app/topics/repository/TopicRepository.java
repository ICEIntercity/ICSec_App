package com.czintercity.icsec_app.topics.repository;

import com.czintercity.icsec_app.topics.entity.Topic;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface TopicRepository extends CrudRepository<Topic, UUID> {
}
