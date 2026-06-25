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
import org.sgd.worldcup.dto.PlayerDTO;
import org.sgd.worldcup.enums.PlayerPosition;
import org.sgd.worldcup.service.PlayerService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/players")
@Tag(name = "Players", description = "Player management endpoints")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @PostMapping
    @Operation(summary = "Create a new player", description = "Creates a new player and adds them to a team")
    public ResponseEntity<ApiResponse<PlayerDTO>> createPlayer(@Valid @RequestBody PlayerDTO playerDTO) {
        log.info("Received request to create player: {}", playerDTO.getName());
        PlayerDTO createdPlayer = playerService.createPlayer(playerDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdPlayer, "Player created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all players", description = "Retrieves a list of all players")
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> getAllPlayers() {
        log.info("Received request to get all players");
        List<PlayerDTO> players = playerService.getAllPlayers();
        return ResponseEntity.ok(ApiResponse.success(players, "Players retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID", description = "Retrieves a player by their unique identifier")
    public ResponseEntity<ApiResponse<PlayerDTO>> getPlayerById(@PathVariable Long id) {
        log.info("Received request to get player with ID: {}", id);
        PlayerDTO player = playerService.getPlayerById(id);
        return ResponseEntity.ok(ApiResponse.success(player, "Player retrieved successfully"));
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get players by team", description = "Retrieves all players from a specific team")
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> getPlayersByTeam(@PathVariable Long teamId) {
        log.info("Received request to get players for team: {}", teamId);
        List<PlayerDTO> players = playerService.getPlayersByTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(players, "Team players retrieved successfully"));
    }

    @GetMapping("/position/{position}")
    @Operation(summary = "Get players by position", description = "Retrieves all players with a specific position")
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> getPlayersByPosition(@PathVariable PlayerPosition position) {
        log.info("Received request to get players with position: {}", position);
        List<PlayerDTO> players = playerService.getPlayersByPosition(position);
        return ResponseEntity.ok(ApiResponse.success(players, "Players retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search players", description = "Searches players by name")
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> searchPlayers(@RequestParam String name) {
        log.info("Received request to search players with name: {}", name);
        List<PlayerDTO> players = playerService.searchPlayers(name);
        return ResponseEntity.ok(ApiResponse.success(players, "Players found"));
    }

    @GetMapping("/top-scorers")
    @Operation(summary = "Get top scorers", description = "Retrieves the top scorers across all teams")
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> getTopScorers() {
        log.info("Received request to get top scorers");
        List<PlayerDTO> players = playerService.getTopScorers();
        return ResponseEntity.ok(ApiResponse.success(players, "Top scorers retrieved successfully"));
    }

    @GetMapping("/team/{teamId}/top-scorers")
    @Operation(summary = "Get top scorers by team", description = "Retrieves the top scorers for a specific team")
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> getTopScorersByTeam(@PathVariable Long teamId) {
        log.info("Received request to get top scorers for team: {}", teamId);
        List<PlayerDTO> players = playerService.getTopScorersByTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(players, "Top scorers retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update player", description = "Updates an existing player")
    public ResponseEntity<ApiResponse<PlayerDTO>> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerDTO playerDTO) {
        log.info("Received request to update player with ID: {}", id);
        PlayerDTO updatedPlayer = playerService.updatePlayer(id, playerDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedPlayer, "Player updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete player", description = "Deletes a player by their ID")
    public ResponseEntity<ApiResponse<Void>> deletePlayer(@PathVariable Long id) {
        log.info("Received request to delete player with ID: {}", id);
        playerService.deletePlayer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Player deleted successfully"));
    }
}

