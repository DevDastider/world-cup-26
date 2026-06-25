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
import org.sgd.worldcup.dto.GoalDTO;
import org.sgd.worldcup.service.GoalService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/goals")
@Tag(name = "Goals", description = "Goal tracking endpoints")
public class GoalController {
    @Autowired
    private GoalService goalService;

    @PostMapping
    @Operation(summary = "Record a goal", description = "Records a goal scored in a match")
    public ResponseEntity<ApiResponse<GoalDTO>> recordGoal(@Valid @RequestBody GoalDTO goalDTO) {
        log.info("Received request to record goal by player {} in match {}", goalDTO.getPlayerId(), goalDTO.getMatchId());
        GoalDTO recordedGoal = goalService.recordGoal(goalDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(recordedGoal, "Goal recorded successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all goals", description = "Retrieves all recorded goals")
    public ResponseEntity<ApiResponse<List<GoalDTO>>> getAllGoals() {
        log.info("Received request to get all goals");
        List<GoalDTO> goals = goalService.getAllGoals();
        return ResponseEntity.ok(ApiResponse.success(goals, "Goals retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID", description = "Retrieves a goal by its unique identifier")
    public ResponseEntity<ApiResponse<GoalDTO>> getGoalById(@PathVariable Long id) {
        log.info("Received request to get goal with ID: {}", id);
        GoalDTO goal = goalService.getGoalById(id);
        return ResponseEntity.ok(ApiResponse.success(goal, "Goal retrieved successfully"));
    }

    @GetMapping("/match/{matchId}")
    @Operation(summary = "Get goals by match", description = "Retrieves all goals scored in a specific match")
    public ResponseEntity<ApiResponse<List<GoalDTO>>> getGoalsByMatch(@PathVariable Long matchId) {
        log.info("Received request to get goals for match: {}", matchId);
        List<GoalDTO> goals = goalService.getGoalsByMatch(matchId);
        return ResponseEntity.ok(ApiResponse.success(goals, "Match goals retrieved successfully"));
    }

    @GetMapping("/player/{playerId}")
    @Operation(summary = "Get goals by player", description = "Retrieves all goals scored by a specific player")
    public ResponseEntity<ApiResponse<List<GoalDTO>>> getGoalsByPlayer(@PathVariable Long playerId) {
        log.info("Received request to get goals for player: {}", playerId);
        List<GoalDTO> goals = goalService.getGoalsByPlayer(playerId);
        return ResponseEntity.ok(ApiResponse.success(goals, "Player goals retrieved successfully"));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get goals by team", description = "Retrieves all goals scored by a specific team")
    public ResponseEntity<ApiResponse<List<GoalDTO>>> getGoalsByTeam(@PathVariable Long teamId) {
        log.info("Received request to get goals for team: {}", teamId);
        List<GoalDTO> goals = goalService.getGoalsByTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(goals, "Team goals retrieved successfully"));
    }

    @GetMapping("/player/{playerId}/count")
    @Operation(summary = "Get goal count for player", description = "Retrieves the total number of goals scored by a player")
    public ResponseEntity<ApiResponse<Integer>> getGoalCountByPlayer(@PathVariable Long playerId) {
        log.info("Received request to get goal count for player: {}", playerId);
        int count = goalService.getGoalCountByPlayer(playerId);
        return ResponseEntity.ok(ApiResponse.success(count, "Goal count retrieved successfully"));
    }

    @GetMapping("/match/{matchId}/team/{teamId}/count")
    @Operation(summary = "Get goal count by team in match", description = "Retrieves the number of goals scored by a team in a specific match")
    public ResponseEntity<ApiResponse<Integer>> getGoalCountByTeamInMatch(
            @PathVariable Long matchId,
            @PathVariable Long teamId) {
        log.info("Received request to get goal count for team {} in match {}", teamId, matchId);
        int count = goalService.getGoalCountByTeamInMatch(teamId, matchId);
        return ResponseEntity.ok(ApiResponse.success(count, "Goal count retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal", description = "Deletes a recorded goal")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        log.info("Received request to delete goal with ID: {}", id);
        goalService.deleteGoal(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully"));
    }
}

