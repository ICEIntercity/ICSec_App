package com.czintercity.icsec_app.topics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Optional;

@Controller
public class TopicController {

    @Autowired
    private TopicRepository topicRepository;

    @GetMapping("/topic/all")
    public String listTopics(Model model) {
        Iterable<Topic> topics = topicRepository.findAll();
        model.addAttribute("topics", topics);
        return "topic/topicList";
    }


    @GetMapping("/topic/new")
    public String newTopic(Model model) {
        model.addAttribute("topic", new Topic());
        return "topic/topicForm";
    }

    @GetMapping("/topic/{id}")
    public String showTopic(@PathVariable Long id, Model model) {
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

    @GetMapping("/topic/edit/{id}")
    public String editTopic(@PathVariable Long id, Model model) {
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

    @PostMapping("/topic/edit")
    public String saveTopic(@Valid @ModelAttribute("topic") Topic topic, BindingResult result, Model model) {
        if(!result.hasErrors()) {
            topicRepository.save(topic);
            model.addAttribute("formSuccess", "Topic updated successfully.");
        }
        else{
            model.addAttribute("formError", result.getAllErrors());
        }
        return "topic/topicForm";
    }
}
