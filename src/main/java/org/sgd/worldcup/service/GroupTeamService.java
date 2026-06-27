package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.sgd.worldcup.entity.Match;
import org.sgd.worldcup.enums.MatchStatus;
import org.sgd.worldcup.repository.MatchRepository;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private MatchRepository matchRepository;

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

    public void recalculateStandings(Long groupId) {
        log.debug("Recalculating standings for group: {}", groupId);
        List<GroupTeam> standings = groupTeamRepository.findByGroupId(groupId);
        if (standings.isEmpty()) {
            return;
        }

        //Index by team id and reset every tally
        Map<Long, GroupTeam> byTeamId = standings.stream()
                .collect(Collectors.toMap(gt-> gt.getTeam().getId(), gt->gt));
        for (GroupTeam groupTeam : standings) {
            groupTeam.setWins(0);
            groupTeam.setLosses(0);
            groupTeam.setDraws(0);
            groupTeam.setGoalsFor(0);
            groupTeam.setGoalsAgainst(0);
            groupTeam.setPoints(0);
            groupTeam.setGoalDifference(0);
        }

        //Replay completed matches
        for (Match match: matchRepository.findByStatusAndGroupId(MatchStatus.COMPLETED, groupId)){
            Integer homeGoals = match.getHomeTeamGoals();
            Integer awayGoals = match.getAwayTeamGoals();
            if (homeGoals == null || awayGoals == null || match.getHomeTeam() == null || match.getAwayTeam() == null) {
                continue;
            }

            GroupTeam home = byTeamId.get(match.getHomeTeam().getId());
            GroupTeam away = byTeamId.get(match.getAwayTeam().getId());
            if (home == null || away == null) {
                continue;
            }

            home.setGoalsFor(home.getGoalsFor() + homeGoals);
            home.setGoalsAgainst(home.getGoalsAgainst() + awayGoals);
            away.setGoalsFor(away.getGoalsFor() + awayGoals);
            away.setGoalsAgainst(away.getGoalsAgainst() + homeGoals);

            if (homeGoals > awayGoals) {
                home.setWins(home.getWins() + 1);
                away.setLosses(away.getLosses() + 1);
            } else if (homeGoals < awayGoals) {
                away.setWins(away.getWins() + 1);
                home.setLosses(home.getLosses() + 1);
            } else {
                home.setDraws(home.getDraws() + 1);
                away.setDraws(away.getDraws() + 1);
            }
        }

        //Derive points and goal difference
        for (GroupTeam groupTeam : standings) {
            groupTeam.setPoints(groupTeam.getPoints() * 3 + groupTeam.getDraws());
            groupTeam.setGoalDifference(groupTeam.getGoalsFor() - groupTeam.getGoalsAgainst());
        }

        //Rank: points > goal difference > goals for > less goals against
        standings.sort(Comparator
                .comparingInt(GroupTeam::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(GroupTeam::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(GroupTeam::getGoalsFor).reversed())
                .thenComparing(Comparator.comparingInt(GroupTeam::getGoalsAgainst)));

        int position=1;
        for (GroupTeam groupTeam : standings) {
            groupTeam.setGroupPosition(position++);
        }
        groupTeamRepository.saveAll(standings);
        log.info("Standings updated successfully for group {} ({} teams)", groupId, standings.size());
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


