package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sgd.worldcup.dto.GroupTeamDTO;
import org.sgd.worldcup.entity.Group;
import org.sgd.worldcup.entity.GroupTeam;
import org.sgd.worldcup.entity.Team;
import org.sgd.worldcup.exception.DuplicateResourceException;
import org.sgd.worldcup.exception.InvalidOperationException;
import org.sgd.worldcup.exception.ResourceNotFoundException;
import org.sgd.worldcup.mapper.TeamMapper;
import org.sgd.worldcup.repository.GroupRepository;
import org.sgd.worldcup.repository.GroupTeamRepository;
import org.sgd.worldcup.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class GroupTeamService {
    @Autowired
    private GroupTeamRepository groupTeamRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMapper teamMapper;

    public GroupTeamDTO addTeamToGroup(Long groupId, Long teamId, Integer position) {
        log.info("Adding team {} to group {}", teamId, groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        if (groupTeamRepository.existsByGroupIdAndTeamId(groupId, teamId)) {
            throw new DuplicateResourceException("Team is already added to this group");
        }

        GroupTeam groupTeam = GroupTeam.builder()
                .group(group)
                .team(team)
                .groupPosition(position)
                .wins(0)
                .losses(0)
                .draws(0)
                .goalsFor(0)
                .goalsAgainst(0)
                .points(0)
                .build();

        GroupTeam savedGroupTeam = groupTeamRepository.save(groupTeam);
        log.info("Team added to group successfully. GroupTeam ID: {}", savedGroupTeam.getId());
        return convertToDTO(savedGroupTeam);
    }

    public void removeTeamFromGroup(Long groupId, Long teamId) {
        log.info("Removing team {} from group {}", teamId, groupId);

        if (!groupTeamRepository.existsByGroupIdAndTeamId(groupId, teamId)) {
            throw new ResourceNotFoundException("Team not found in this group");
        }

        groupTeamRepository.deleteByGroupIdAndTeamId(groupId, teamId);
        log.info("Team removed from group successfully");
    }

    public List<GroupTeamDTO> getGroupStandings(Long groupId) {
        log.info("Fetching standings for group: {}", groupId);

        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with ID: " + groupId);
        }

        return groupTeamRepository.findGroupStandingsByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<GroupTeamDTO> getTeamsByGroup(Long groupId) {
        log.info("Fetching teams in group: {}", groupId);

        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with ID: " + groupId);
        }

        return groupTeamRepository.findByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public GroupTeamDTO getGroupTeamById(Long groupTeamId) {
        log.info("Fetching group team with ID: {}", groupTeamId);
        GroupTeam groupTeam = groupTeamRepository.findById(groupTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Group team not found with ID: " + groupTeamId));
        return convertToDTO(groupTeam);
    }

    public void updateStandings(Long groupTeamId, Integer wins, Integer losses, Integer draws,
                                 Integer goalsFor, Integer goalsAgainst) {
        log.info("Updating standings for group team: {}", groupTeamId);

        GroupTeam groupTeam = groupTeamRepository.findById(groupTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Group team not found with ID: " + groupTeamId));

        groupTeam.setWins(wins);
        groupTeam.setLosses(losses);
        groupTeam.setDraws(draws);
        groupTeam.setGoalsFor(goalsFor);
        groupTeam.setGoalsAgainst(goalsAgainst);

        // Calculate points and goal difference
        int points = (wins * 3) + (draws * 1);
        groupTeam.setPoints(points);
        groupTeam.setGoalDifference(goalsFor - goalsAgainst);

        groupTeamRepository.save(groupTeam);
        log.info("Standings updated successfully");
    }

    public int getTeamsCountInGroup(Long groupId) {
        return groupTeamRepository.countByGroupId(groupId);
    }

    private GroupTeamDTO convertToDTO(GroupTeam groupTeam) {
        return GroupTeamDTO.builder()
                .id(groupTeam.getId())
                .groupId(groupTeam.getGroup().getId())
                .team(teamMapper.toDTO(groupTeam.getTeam()))
                .groupPosition(groupTeam.getGroupPosition())
                .wins(groupTeam.getWins())
                .losses(groupTeam.getLosses())
                .draws(groupTeam.getDraws())
                .goalsFor(groupTeam.getGoalsFor())
                .goalsAgainst(groupTeam.getGoalsAgainst())
                .points(groupTeam.getPoints())
                .goalDifference(groupTeam.getGoalDifference())
                .createdAt(groupTeam.getCreatedAt())
                .updatedAt(groupTeam.getUpdatedAt())
                .build();
    }
}


