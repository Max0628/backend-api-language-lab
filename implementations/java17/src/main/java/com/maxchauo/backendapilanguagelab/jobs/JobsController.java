package com.maxchauo.backendapilanguagelab.jobs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** Annotation-based Spring MVC controller for the jobs API. */
@RestController
@RequestMapping("/jobs")
public class JobsController {
    private final JobsService service;

    /** Creates the controller with constructor-injected service. */
    public JobsController(JobsService service) {
        this.service = service;
    }

    /** Returns jobs matching the query parameters as a JSON list. */
    @GetMapping
    public ResponseEntity<List<JobResponse>> list(
            @ModelAttribute JobQueryParameters parameters) {
        List<JobResponse> responses = service.list(parameters).stream()
                .map(JobResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
