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
import org.sgd.worldcup.dto.GroupDTO;
import org.sgd.worldcup.dto.GroupTeamDTO;
import org.sgd.worldcup.service.GroupService;
import org.sgd.worldcup.service.GroupTeamService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/groups")
@Tag(name = "Groups", description = "Group management endpoints")
public class GroupController {
    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupTeamService groupTeamService;

    @PostMapping
    @Operation(summary = "Create a new group", description = "Creates a new tournament group")
    public ResponseEntity<ApiResponse<GroupDTO>> createGroup(@Valid @RequestBody GroupDTO groupDTO) {
        log.info("Received request to create group: {}", groupDTO.getName());
        GroupDTO createdGroup = groupService.createGroup(groupDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdGroup, "Group created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all groups", description = "Retrieves all tournament groups")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getAllGroups() {
        log.info("Received request to get all groups");
        List<GroupDTO> groups = groupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success(groups, "Groups retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID", description = "Retrieves a group by its unique identifier")
    public ResponseEntity<ApiResponse<GroupDTO>> getGroupById(@PathVariable Long id) {
        log.info("Received request to get group with ID: {}", id);
        GroupDTO group = groupService.getGroupById(id);
        return ResponseEntity.ok(ApiResponse.success(group, "Group retrieved successfully"));
    }

    @GetMapping("/{groupId}/standings")
    @Operation(summary = "Get group standings", description = "Retrieves the standings for a group")
    public ResponseEntity<ApiResponse<List<GroupTeamDTO>>> getGroupStandings(@PathVariable Long groupId) {
        log.info("Received request to get standings for group: {}", groupId);
        List<GroupTeamDTO> standings = groupTeamService.getGroupStandings(groupId);
        return ResponseEntity.ok(ApiResponse.success(standings, "Group standings retrieved successfully"));
    }

    @GetMapping("/{groupId}/teams")
    @Operation(summary = "Get teams in group", description = "Retrieves all teams in a specific group")
    public ResponseEntity<ApiResponse<List<GroupTeamDTO>>> getTeamsByGroup(@PathVariable Long groupId) {
        log.info("Received request to get teams for group: {}", groupId);
        List<GroupTeamDTO> teams = groupTeamService.getTeamsByGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success(teams, "Group teams retrieved successfully"));
    }

    @PostMapping("/{groupId}/teams/{teamId}")
    @Operation(summary = "Add team to group", description = "Adds a team to a specific group")
    public ResponseEntity<ApiResponse<GroupTeamDTO>> addTeamToGroup(
            @PathVariable Long groupId,
            @PathVariable Long teamId,
            @RequestParam(required = false) Integer position) {
        log.info("Received request to add team {} to group {}", teamId, groupId);
        GroupTeamDTO groupTeam = groupTeamService.addTeamToGroup(groupId, teamId, position);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(groupTeam, "Team added to group successfully"));
    }

    @DeleteMapping("/{groupId}/teams/{teamId}")
    @Operation(summary = "Remove team from group", description = "Removes a team from a specific group")
    public ResponseEntity<ApiResponse<Void>> removeTeamFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long teamId) {
        log.info("Received request to remove team {} from group {}", teamId, groupId);
        groupTeamService.removeTeamFromGroup(groupId, teamId);
        return ResponseEntity.ok(ApiResponse.success(null, "Team removed from group successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update group", description = "Updates an existing group")
    public ResponseEntity<ApiResponse<GroupDTO>> updateGroup(@PathVariable Long id, @Valid @RequestBody GroupDTO groupDTO) {
        log.info("Received request to update group with ID: {}", id);
        GroupDTO updatedGroup = groupService.updateGroup(id, groupDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedGroup, "Group updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete group", description = "Deletes a group by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        log.info("Received request to delete group with ID: {}", id);
        groupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Group deleted successfully"));
    }
}

