package org.sgd.worldcup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sgd.worldcup.dto.ApiResponse;
import org.sgd.worldcup.dto.MatchDTO;
import org.sgd.worldcup.service.MatchService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/matches")
@Tag(name = "Matches", description = "Match management endpoints")
public class MatchController {
    @Autowired
    private MatchService matchService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @PostMapping
    @Operation(summary = "Create a new match", description = "Creates a new match between two teams")
    public ResponseEntity<ApiResponse<MatchDTO>> createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        log.info("Received request to create match between teams {} and {}", matchDTO.getHomeTeamId(), matchDTO.getAwayTeamId());
        MatchDTO createdMatch = matchService.createMatch(matchDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdMatch, "Match created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all matches", description = "Retrieves all matches")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> getAllMatches() {
        log.info("Received request to get all matches");
        List<MatchDTO> matches = matchService.getAllMatches();
        return ResponseEntity.ok(ApiResponse.success(matches, "Matches retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match by ID", description = "Retrieves a match by its unique identifier")
    public ResponseEntity<ApiResponse<MatchDTO>> getMatchById(@PathVariable Long id) {
        log.info("Received request to get match with ID: {}", id);
        MatchDTO match = matchService.getMatchById(id);
        return ResponseEntity.ok(ApiResponse.success(match, "Match retrieved successfully"));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming matches", description = "Retrieves all scheduled upcoming matches")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> getUpcomingMatches() {
        log.info("Received request to get upcoming matches");
        List<MatchDTO> matches = matchService.getUpcomingMatches();
        return ResponseEntity.ok(ApiResponse.success(matches, "Upcoming matches retrieved successfully"));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed matches", description = "Retrieves all completed matches")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> getCompletedMatches() {
        log.info("Received request to get completed matches");
        List<MatchDTO> matches = matchService.getCompletedMatches();
        return ResponseEntity.ok(ApiResponse.success(matches, "Completed matches retrieved successfully"));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get matches by team", description = "Retrieves all matches for a specific team")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> getMatchesByTeam(@PathVariable Long teamId) {
        log.info("Received request to get matches for team: {}", teamId);
        List<MatchDTO> matches = matchService.getMatchesByTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(matches, "Team matches retrieved successfully"));
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get matches by group", description = "Retrieves all matches in a specific group")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> getMatchesByGroup(@PathVariable Long groupId) {
        log.info("Received request to get matches for group: {}", groupId);
        List<MatchDTO> matches = matchService.getMatchesByGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success(matches, "Group matches retrieved successfully"));
    }

    @GetMapping("/between-dates")
    @Operation(summary = "Get matches between dates", description = "Retrieves all matches between two dates")
    public ResponseEntity<ApiResponse<List<MatchDTO>>> getMatchesBetweenDates(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Received request to get matches between {} and {}", startDate, endDate);
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        List<MatchDTO> matches = matchService.getMatchesBetweenDates(start, end);
        return ResponseEntity.ok(ApiResponse.success(matches, "Matches retrieved successfully"));
    }

    @PutMapping("/{id}/result")
    @Operation(summary = "Update match result", description = "Updates the result and statistics of a completed match")
    public ResponseEntity<ApiResponse<MatchDTO>> updateMatchResult(
            @PathVariable Long id,
            @Valid @RequestBody MatchDTO matchDTO) {
        log.info("Received request to update match result for match ID: {}", id);
        MatchDTO updatedMatch = matchService.updateMatchResult(id, matchDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedMatch, "Match result updated successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update match", description = "Updates match details")
    public ResponseEntity<ApiResponse<MatchDTO>> updateMatch(
            @PathVariable Long id,
            @Valid @RequestBody MatchDTO matchDTO) {
        log.info("Received request to update match with ID: {}", id);
        MatchDTO updatedMatch = matchService.updateMatchResult(id, matchDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedMatch, "Match updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete match", description = "Deletes a match by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteMatch(@PathVariable Long id) {
        log.info("Received request to delete match with ID: {}", id);
        matchService.deleteMatch(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Match deleted successfully"));
    }
}

