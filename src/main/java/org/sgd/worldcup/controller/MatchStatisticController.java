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
import org.sgd.worldcup.dto.MatchStatisticDTO;
import org.sgd.worldcup.service.MatchStatisticService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statistics")
@Tag(name = "Match Statistics", description = "Match statistics endpoints")
public class MatchStatisticController {
    @Autowired
    private MatchStatisticService matchStatisticService;

    @PostMapping
    @Operation(summary = "Create match statistics", description = "Creates detailed statistics for a match")
    public ResponseEntity<ApiResponse<MatchStatisticDTO>> createStatistic(@Valid @RequestBody MatchStatisticDTO statisticDTO) {
        log.info("Received request to create statistics for match: {}", statisticDTO.getMatchId());
        MatchStatisticDTO createdStatistic = matchStatisticService.createMatchStatistic(statisticDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdStatistic, "Match statistics created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all statistics", description = "Retrieves all match statistics")
    public ResponseEntity<ApiResponse<List<MatchStatisticDTO>>> getAllStatistics() {
        log.info("Received request to get all match statistics");
        List<MatchStatisticDTO> statistics = matchStatisticService.getAllStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get statistic by ID", description = "Retrieves statistics by their unique identifier")
    public ResponseEntity<ApiResponse<MatchStatisticDTO>> getStatisticById(@PathVariable Long id) {
        log.info("Received request to get statistic with ID: {}", id);
        MatchStatisticDTO statistic = matchStatisticService.getStatisticById(id);
        return ResponseEntity.ok(ApiResponse.success(statistic, "Statistic retrieved successfully"));
    }

    @GetMapping("/match/{matchId}")
    @Operation(summary = "Get statistics by match", description = "Retrieves statistics for a specific match")
    public ResponseEntity<ApiResponse<MatchStatisticDTO>> getStatisticByMatchId(@PathVariable Long matchId) {
        log.info("Received request to get statistics for match: {}", matchId);
        MatchStatisticDTO statistic = matchStatisticService.getStatisticByMatchId(matchId);
        return ResponseEntity.ok(ApiResponse.success(statistic, "Match statistics retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update statistics", description = "Updates match statistics")
    public ResponseEntity<ApiResponse<MatchStatisticDTO>> updateStatistic(
            @PathVariable Long id,
            @Valid @RequestBody MatchStatisticDTO statisticDTO) {
        log.info("Received request to update statistics with ID: {}", id);
        MatchStatisticDTO updatedStatistic = matchStatisticService.updateStatistic(id, statisticDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedStatistic, "Statistics updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete statistics", description = "Deletes match statistics by ID")
    public ResponseEntity<ApiResponse<Void>> deleteStatistic(@PathVariable Long id) {
        log.info("Received request to delete statistics with ID: {}", id);
        matchStatisticService.deleteStatistic(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Statistics deleted successfully"));
    }

    @DeleteMapping("/match/{matchId}")
    @Operation(summary = "Delete statistics by match", description = "Deletes statistics for a specific match")
    public ResponseEntity<ApiResponse<Void>> deleteStatisticByMatchId(@PathVariable Long matchId) {
        log.info("Received request to delete statistics for match: {}", matchId);
        matchStatisticService.deleteByMatchId(matchId);
        return ResponseEntity.ok(ApiResponse.success(null, "Match statistics deleted successfully"));
    }
}

