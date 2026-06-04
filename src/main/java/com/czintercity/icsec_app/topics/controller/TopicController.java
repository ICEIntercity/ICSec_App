package com.czintercity.icsec_app.topics.controller;

import com.czintercity.icsec_app.topics.repository.TopicRepository;
import com.czintercity.icsec_app.topics.entity.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for listing, viewing, creating, editing, and deleting {@link Topic} entities.
 * Topics cannot be deleted while they have assigned controls; such attempts are rejected with an
 * inline HTMX error message rather than an HTTP error response.
 */
@Controller
public class TopicController {
    private static final Logger log = LoggerFactory.getLogger(TopicController.class);

    @Autowired
    private TopicRepository topicRepository;

    /** Renders the full list of topics. */
    @GetMapping("/topic/all")
    public String listTopics(Model model) {
        log.trace("listTopics called.");
        Iterable<Topic> topics = topicRepository.findAll();
        model.addAttribute("topics", topics);
        return "topic/allTopics";
    }


    /** Renders the topic creation form with an empty {@link Topic}. */
    @GetMapping("/topic/new")
    public String newTopic(Model model) {
        log.trace("New topic invoked.");
        model.addAttribute("topic", new Topic());
        return "topic/topicForm";
    }

    /** Renders the read-only topic detail view. */
    @GetMapping("/topic/{id}")
    public String showTopic(@PathVariable UUID id, Model model) {
        log.trace("Received request to show topic with id {}", id);
        Optional<Topic> topic = topicRepository.findById(id);
        if(topic.isPresent()){
            model.addAttribute("topic", topic.get());
            return "topic/topicView";
        }
        else {
            model.addAttribute("error", "http.cat/404");
            return "error";
        }
    }

    /** Renders the topic edit form pre-populated with the existing topic data. */
    @GetMapping("/topic/edit/{id}")
    public String editTopic(@PathVariable UUID id, Model model) {
        log.trace("Received request to edit topic with id {}", id);
        Optional<Topic> topic = topicRepository.findById(id);
        if(topic.isPresent()){
            model.addAttribute("topic", topic.get());
            return "topic/topicForm";
        }
        else {
            model.addAttribute("error", "http.cat/404");
            return "error";
        }
    }

    /** Validates and persists the topic form, returning the form view with success or error feedback. */
    @PostMapping("/topic/edit")
    public String saveTopic(@Valid @ModelAttribute("topic") Topic received, BindingResult result, Model model) {
        log.trace("Saving topic {}", received);

        if (!result.hasErrors()) {
            try {
                if (received.getColor() != null && received.getColor().isBlank()) {
                    received.setColor(null);
                }
                topicRepository.save(received);
                model.addAttribute("formSuccess", "Topic updated successfully.");
            }
            catch (Exception e) {
                // TODO: Add proper validation
                model.addAttribute("formError", "Failed to update topic.");
            }
        } else {
            model.addAttribute("formError", result.getAllErrors());
        }
        return "topic/topicForm";
    }

    /**
     * Deletes the topic with the given ID, or returns an inline HTMX error message if the topic
     * still has assigned controls. On success, triggers a client-side redirect to the topic list.
     */
    @DeleteMapping("/topic/delete/{id}")
    public ResponseEntity<String> deleteTopic(@PathVariable UUID id) {
        Optional<Topic> target = topicRepository.findById(id);

        if (target.isPresent()) {
            HttpHeaders headers = new HttpHeaders();

            Topic toDelete = target.get();
            if (toDelete.getControls() != null && toDelete.getControls().iterator().hasNext()) {

                // Handle the constraint violation
                log.warn("Attempted to delete Topic ID {} which has assigned controls.", id);
                headers.add("HX-Retarget", "#message-container");
                String errorHtml = "<div class='alert alert-warning'>Cannot delete topic '" +
                        toDelete.getName() + "' because it has assigned controls.</div>";

                return new ResponseEntity<>(errorHtml, headers, HttpStatus.OK);
            }

            topicRepository.delete(toDelete);
            log.info("Deleted topic ID: {}", toDelete.getId());


            // Pass a 'status' parameter in the URL
            headers.add("HX-Redirect", "/topic/all?deleted=true");
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic with id " + id + " not found");
        }
    }
}
