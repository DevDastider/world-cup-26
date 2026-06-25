package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sgd.worldcup.dto.MatchStatisticDTO;
import org.sgd.worldcup.entity.Match;
import org.sgd.worldcup.entity.MatchStatistic;
import org.sgd.worldcup.entity.Team;
import org.sgd.worldcup.exception.DuplicateResourceException;
import org.sgd.worldcup.exception.ResourceNotFoundException;
import org.sgd.worldcup.mapper.MatchStatisticMapper;
import org.sgd.worldcup.repository.MatchRepository;
import org.sgd.worldcup.repository.MatchStatisticRepository;
import org.sgd.worldcup.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MatchStatisticService {
    @Autowired
    private MatchStatisticRepository matchStatisticRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchStatisticMapper matchStatisticMapper;

    public MatchStatisticDTO createMatchStatistic(MatchStatisticDTO statisticDTO) {
        log.info("Creating match statistics for match ID: {}", statisticDTO.getMatchId());

        Match match = matchRepository.findById(statisticDTO.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + statisticDTO.getMatchId()));

        if (matchStatisticRepository.findByMatchId(statisticDTO.getMatchId()).isPresent()) {
            throw new DuplicateResourceException("Statistics already exist for match ID: " + statisticDTO.getMatchId());
        }

        Team homeTeam = teamRepository.findById(statisticDTO.getHomeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Home team not found with ID: " + statisticDTO.getHomeTeamId()));

        Team awayTeam = teamRepository.findById(statisticDTO.getAwayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Away team not found with ID: " + statisticDTO.getAwayTeamId()));

        MatchStatistic statistic = matchStatisticMapper.toEntity(statisticDTO);
        statistic.setMatch(match);
        statistic.setHomeTeam(homeTeam);
        statistic.setAwayTeam(awayTeam);

        MatchStatistic savedStatistic = matchStatisticRepository.save(statistic);
        log.info("Match statistics created successfully with ID: {}", savedStatistic.getId());
        return matchStatisticMapper.toDTO(savedStatistic);
    }

    public MatchStatisticDTO getStatisticById(Long id) {
        log.info("Fetching match statistics with ID: {}", id);
        MatchStatistic statistic = matchStatisticRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match statistics not found with ID: " + id));
        return matchStatisticMapper.toDTO(statistic);
    }

    public MatchStatisticDTO getStatisticByMatchId(Long matchId) {
        log.info("Fetching match statistics for match: {}", matchId);
        MatchStatistic statistic = matchStatisticRepository.findByMatchId(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match statistics not found for match ID: " + matchId));
        return matchStatisticMapper.toDTO(statistic);
    }

    public List<MatchStatisticDTO> getAllStatistics() {
        log.info("Fetching all match statistics");
        return matchStatisticRepository.findAll().stream()
                .map(matchStatisticMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MatchStatisticDTO updateStatistic(Long id, MatchStatisticDTO statisticDTO) {
        log.info("Updating match statistics with ID: {}", id);
        MatchStatistic statistic = matchStatisticRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match statistics not found with ID: " + id));

        statistic.setHomeTeamPossession(statisticDTO.getHomeTeamPossession());
        statistic.setAwayTeamPossession(statisticDTO.getAwayTeamPossession());
        statistic.setHomeTeamPasses(statisticDTO.getHomeTeamPasses());
        statistic.setAwayTeamPasses(statisticDTO.getAwayTeamPasses());
        statistic.setHomeTeamPassAccuracy(statisticDTO.getHomeTeamPassAccuracy());
        statistic.setAwayTeamPassAccuracy(statisticDTO.getAwayTeamPassAccuracy());

        MatchStatistic updatedStatistic = matchStatisticRepository.save(statistic);
        log.info("Match statistics updated successfully with ID: {}", id);
        return matchStatisticMapper.toDTO(updatedStatistic);
    }

    public void deleteStatistic(Long id) {
        log.info("Deleting match statistics with ID: {}", id);
        MatchStatistic statistic = matchStatisticRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match statistics not found with ID: " + id));
        matchStatisticRepository.delete(statistic);
        log.info("Match statistics deleted successfully with ID: {}", id);
    }

    public void deleteByMatchId(Long matchId) {
        log.info("Deleting statistics for match ID: {}", matchId);
        matchStatisticRepository.deleteByMatchId(matchId);
        log.info("Match statistics deleted successfully for match ID: {}", matchId);
    }

    public boolean existsByMatchId(Long matchId) {
        return matchStatisticRepository.findByMatchId(matchId).isPresent();
    }
}

