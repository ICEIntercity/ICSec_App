package com.czintercity.icsec_app.assessment.controller;

import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Handles paginated listing of {@link Assessment} records.
 * <p>
 * Two endpoints share the same query and model population:
 * {@code /assessment/all} returns the full page shell on first load, while
 * {@code /assessment/all/page} returns only the {@code assessmentList} fragment
 * for HTMX-driven page turns.
 */
@Controller
public class AssessmentListController {

    private static final int PAGE_SIZE = 10;

    private final AssessmentRepository assessmentRepository;

    public AssessmentListController(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    @GetMapping("/assessment/all")
    public String listAssessments(Model model) {
        Page<Assessment> page = assessmentRepository.findAllByOrderByUpdatedDesc(PageRequest.of(0, PAGE_SIZE));
        model.addAttribute("assessmentPage", page);
        return "assessment/allAssessments";
    }

    /**
     * Returns only the {@code assessmentList} fragment; called by HTMX when the user
     * navigates between pages without a full reload.
     */
    @GetMapping("/assessment/all/page")
    public String listAssessmentsPage(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Assessment> assessmentPage = assessmentRepository.findAllByOrderByUpdatedDesc(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("assessmentPage", assessmentPage);
        return "assessment/allAssessments :: assessmentList";
    }

    /** Deletes the given assessment and redirects back to the list via {@code HX-Redirect}. */
    @DeleteMapping("/assessment/{id}/delete")
    public ResponseEntity<Void> deleteAssessment(@PathVariable UUID id) {
        if (!assessmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        assessmentRepository.deleteById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("HX-Redirect", "/assessment/all");
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
}