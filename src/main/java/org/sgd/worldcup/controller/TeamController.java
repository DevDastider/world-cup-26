package org.sgd.worldcup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.sgd.worldcup.dto.ApiResponse;
import org.sgd.worldcup.dto.TeamDTO;
import org.sgd.worldcup.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/teams")
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @PostMapping
    @Operation(summary = "Create a new team", description = "Creates a new team with the provided information")
    public ResponseEntity<ApiResponse<TeamDTO>> createTeam(@Valid @RequestBody TeamDTO teamDTO) {
        log.info("Received request to create team: {}", teamDTO.getName());
        TeamDTO createdTeam = teamService.createTeam(teamDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdTeam, "Team created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Retrieves list of all teams")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getAllTeams(
            @RequestParam(name = "includePlaceholders", defaultValue = "false") boolean includePlaceholders) {
        log.info("Received request to get all teams");
        List<TeamDTO> teams = teamService.getAllTeams(includePlaceholders);
        return ResponseEntity.ok(ApiResponse.success(teams, "Teams retrieved successfully"));
    }

    @GetMapping("/placeholders")
    @Operation(summary = "Get placeholder teams", description = "Retrieves only the auto-generated placeholder teams")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getPlaceholderTeams(){
        log.info("Received request to get placeholder teams");
        List<TeamDTO> teams = teamService.getPlaceholderTeams();
        return ResponseEntity.ok(ApiResponse.success(teams, "Placeholder teams retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID", description = "Retrieves a team by its unique identifier")
    public ResponseEntity<ApiResponse<TeamDTO>> getTeamById(@PathVariable Long id) {
        log.info("Received request to get team with ID: {}", id);
        TeamDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success(team, "Team retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search teams", description = "Searches teams by name or country code")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> searchTeams(@RequestParam String term) {
        log.info("Received request to search teams with term: {}", term);
        List<TeamDTO> teams = teamService.searchTeams(term);
        return ResponseEntity.ok(ApiResponse.success(teams, "Teams found"));
    }

    @GetMapping("/confederation/{confederation}")
    @Operation(summary = "Get teams by confederation", description = "Retrieves all teams from a specific confederation")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getTeamsByConfederation(@PathVariable String confederation) {
        log.info("Received request to get teams from confederation: {}", confederation);
        List<TeamDTO> teams = teamService.getTeamsByConfederation(confederation);
        return ResponseEntity.ok(ApiResponse.success(teams, "Teams retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update team", description = "Updates an existing team")
    public ResponseEntity<ApiResponse<TeamDTO>> updateTeam(@PathVariable Long id, @Valid @RequestBody TeamDTO teamDTO) {
        log.info("Received request to update team with ID: {}", id);
        TeamDTO updatedTeam = teamService.updateTeam(id, teamDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedTeam, "Team updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete team", description = "Deletes a team by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        log.info("Received request to delete team with ID: {}", id);
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Team deleted successfully"));
    }
}

