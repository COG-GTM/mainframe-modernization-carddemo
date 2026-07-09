package com.carddemo.batch.orchestration;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST API to launch the CS-14 batch orchestration pipelines — the online-triggered
 * equivalent of submitting the legacy JCL to the internal reader.
 *
 * <ul>
 *   <li>{@code GET  /api/batch/jobs}          — list the launchable pipelines and the JCL they replace.</li>
 *   <li>{@code POST /api/batch/jobs/{name}}   — launch a pipeline synchronously; the optional JSON
 *       body supplies job parameters ({@code startDate}/{@code endDate}/{@code outputFile}/
 *       {@code parmDate}).</li>
 * </ul>
 *
 * <p>Every endpoint is admin-only ({@code ROLE_ADMIN}); a signed-on {@code ROLE_USER} gets
 * {@code 403} and an unauthenticated caller {@code 401}. Method-level security is already enabled
 * app-wide (CS-9 {@code @EnableMethodSecurity}).</p>
 */
@RestController
@RequestMapping("/api/batch/jobs")
@PreAuthorize("hasRole('ADMIN')")
public class BatchJobController {

    private final BatchJobLauncherService launcher;

    public BatchJobController(BatchJobLauncherService launcher) {
        this.launcher = launcher;
    }

    @GetMapping
    public List<PipelineInfo> list() {
        return BatchPipeline.all().stream()
                .map(p -> new PipelineInfo(p.getName(), p.getJobBeanName(), p.getLegacyJcl()))
                .collect(Collectors.toList());
    }

    @PostMapping("/{name}")
    public ResponseEntity<BatchJobLaunchResult> launch(@PathVariable String name,
                                                       @RequestBody(required = false) Map<String, String> params) {
        BatchJobLaunchResult result = launcher.launch(name, params == null ? Map.of() : params);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleUnknownPipeline(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BatchJobLauncherService.BatchLaunchException.class)
    public ResponseEntity<Map<String, String>> handleLaunchFailure(
            BatchJobLauncherService.BatchLaunchException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
    }

    /** Lightweight view of a launchable pipeline for the {@code GET} listing. */
    public record PipelineInfo(String name, String jobName, String legacyJcl) {
    }
}
