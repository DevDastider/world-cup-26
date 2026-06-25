package org.sgd.worldcup.dataimport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sgd.worldcup.dto.*;
import org.sgd.worldcup.enums.GoalType;
import org.sgd.worldcup.enums.MatchStatus;
import org.sgd.worldcup.enums.MatchType;
import org.sgd.worldcup.enums.PlayerPosition;
import org.sgd.worldcup.enums.StageType;
import org.sgd.worldcup.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Imports FIFA World Cup 2026 data from OpenFootball JSON repository
 * Data source: <a href="https://github.com/openfootball/worldcup.json/tree/master/2026">OpenFootball World Cup</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorldCupDataImporter {

    private final TeamService teamService;
    private final GroupService groupService;
    private final GroupTeamService groupTeamService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final GoalService goalService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // JSON URLs from OpenFootball repository
    private static final String BASE_URL = "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/";
    private static final String TEAMS_URL = BASE_URL + "worldcup.teams.json";
    private static final String GROUPS_URL = BASE_URL + "worldcup.groups.json";
    private static final String MATCHES_URL = BASE_URL + "worldcup.json";
    private static final String SQUADS_URL = BASE_URL + "worldcup.squads.json";

    private Map<String, TeamDTO> teamCache;
    private Map<String, GroupDTO> groupCache;
    private Map<String, Long> teamIdCache;
    private Map<String, Long> groupIdCache;

    /**
     * Main import method - imports all data in the correct order
     */
    @Transactional
    public void importAllData() {
        try {
            log.info("Starting World Cup 2026 data import from OpenFootball repository...");

            // Initialize caches
            teamCache = new HashMap<>();
            groupCache = new HashMap<>();
            teamIdCache = new HashMap<>();
            groupIdCache = new HashMap<>();

            // Import in order
            log.info("Step 1/5: Importing teams...");
            importTeams();

            log.info("Step 2/5: Importing groups...");
            importGroups();

            log.info("Step 3/5: Adding teams to groups...");
            addTeamsToGroups();

            log.info("Step 4/5: Importing matches...");
            importMatches();

            log.info("Step 5/5: Importing player squads...");
            importPlayerSquads();

            log.info("✅ World Cup 2026 data import completed successfully!");

        } catch (Exception e) {
            log.error("❌ Error during data import", e);
            throw new RuntimeException("Data import failed: " + e.getMessage(), e);
        }
    }

    /**
     * Import teams from worldcup.teams.json
     */
    private void importTeams() throws IOException {
        try (var inputStream = new URL(TEAMS_URL).openStream()) {
            JsonNode teamsJson = objectMapper.readTree(inputStream);

            for (JsonNode teamNode : teamsJson) {
                try {
                    String teamName = teamNode.get("name").asText();
                    String countryCode = teamNode.get("fifa_code").asText();
                    String confederation = teamNode.get("confed").asText();

                    TeamDTO teamDTO = TeamDTO.builder()
                            .name(teamName)
                            .countryCode(countryCode)
                            .confederation(confederation)
                            .build();

                    TeamDTO created = teamService.createTeam(teamDTO);
                    teamCache.put(teamName, created);
                    teamIdCache.put(teamName, created.getId());

                    log.debug("✓ Created team: {}", teamName);

                } catch (Exception e) {
                    log.warn("⚠ Failed to import team: " + teamNode.get("name").asText(), e);
                }
            }

            log.info("✓ Imported {} teams", teamCache.size());
        }
    }

    /**
     * Import groups from worldcup.groups.json
     */
    private void importGroups() throws IOException {
        try (var inputStream = new URL(GROUPS_URL).openStream()) {
            JsonNode groupsRoot = objectMapper.readTree(inputStream);
            JsonNode groupsArray = groupsRoot.get("groups");

            for (JsonNode groupNode : groupsArray) {
                try {
                    String groupName = groupNode.get("name").asText(); // "Group A", "Group B", etc.
//                    String stageType = groupName.contains("Group") ? "GROUP_STAGE" : "KNOCKOUT";

                    GroupDTO groupDTO = GroupDTO.builder()
                            .name(groupName)
//                            .stage(StageType.valueOf(stageType))
                            .build();

                    GroupDTO created = groupService.createGroup(groupDTO);
                    groupCache.put(groupName, created);
                    groupIdCache.put(groupName, created.getId());

                    log.debug("✓ Created group: {}", groupName);

                } catch (Exception e) {
                    log.warn("⚠ Failed to import group: " + groupNode.get("name").asText(), e);
                }
            }

            log.info("✓ Imported {} groups", groupCache.size());
        }
    }

    /**
     * Add teams to groups based on group assignments
     */
    private void addTeamsToGroups() throws IOException {
        try (var inputStream = new URL(TEAMS_URL).openStream()) {
            JsonNode teamsJson = objectMapper.readTree(inputStream);
            int position = 1;

            for (JsonNode teamNode : teamsJson) {
                try {
                    String teamName = teamNode.get("name").asText();
                    String groupName = "Group " + teamNode.get("group").asText(); // "Group A", etc.

                    Long teamId = teamIdCache.get(teamName);
                    Long groupId = groupIdCache.get(groupName);

                    if (teamId != null && groupId != null) {
                        groupTeamService.addTeamToGroup(groupId, teamId, position++);
                        log.debug("✓ Added {} to {}", teamName, groupName);
                    }

                } catch (Exception e) {
                    log.warn("⚠ Failed to add team to group: {}", e.getMessage());
                }
            }

            log.info("✓ Added teams to groups");
        }
    }

    /**
     * Import matches from worldcup.json
     */
    private void importMatches() throws IOException {
        try (var inputStream = new URL(MATCHES_URL).openStream()) {
            JsonNode matchesRoot = objectMapper.readTree(inputStream);
            JsonNode matchesArray = matchesRoot.get("matches");

            if (matchesArray == null) {
                log.warn("No matches found in data");
                return;
            }

            for (JsonNode matchNode : matchesArray) {
                try {
                    String team1Name = matchNode.get("team1").asText();
                    String team2Name = matchNode.get("team2").asText();
                    String dateStr = matchNode.get("date").asText(); // "2026-06-11"
                    String timeStr = matchNode.get("time").asText();
                    String groupStr = matchNode.get("group").asText();
                    String ground = matchNode.get("ground").asText();

                    Long homeTeamId = teamIdCache.get(team1Name);
                    Long awayTeamId = teamIdCache.get(team2Name);

                    if (homeTeamId == null || awayTeamId == null) {
                        log.warn("⚠ Team not found: {} vs {}", team1Name, team2Name);
                        continue;
                    }

                    // Parse match date and time
                    LocalDateTime matchDateTime = parseMatchDateTime(dateStr, timeStr);

                    // Determine match type
                    MatchType matchType = MatchType.GROUP_MATCH;
                    Long groupId = null;

                    if (groupStr != null && !groupStr.isEmpty()) {
                        groupId = groupIdCache.get("Group " + groupStr);
                    } else {
                        // Knockout stage
                        String round = matchNode.get("round").asText();
                        matchType = determineMatchType(round);
                    }

                    // Check if match has score (completed or ongoing)
                    MatchStatus status = MatchStatus.SCHEDULED;
                    Integer homeGoals = null;
                    Integer awayGoals = null;

                    JsonNode scoreNode = matchNode.get("score");
                    if (scoreNode != null && scoreNode.has("ft")) {
                        homeGoals = scoreNode.get("ft").get(0).asInt();
                        awayGoals = scoreNode.get("ft").get(1).asInt();
                        status = MatchStatus.COMPLETED;
                    }

                    // Create match DTO
                    MatchDTO matchDTO = MatchDTO.builder()
                            .homeTeamId(homeTeamId)
                            .awayTeamId(awayTeamId)
                            .groupId(groupId)
                            .matchType(matchType)
                            .matchDate(matchDateTime)
                            .venue(ground)
                            .status(status)
                            .homeTeamGoals(homeGoals)
                            .awayTeamGoals(awayGoals)
                            .build();

                    MatchDTO created = matchService.createMatch(matchDTO);

                    // If match has goals, import them
                    if (homeGoals != null) {
                        importGoalsForMatch(matchNode, created.getId(), homeTeamId, awayTeamId);
                    }

                    log.debug("✓ Created match: {} vs {}", team1Name, team2Name);

                } catch (Exception e) {
                    log.warn("⚠ Failed to import match", e);
                }
            }

            log.info("✓ Imported matches");
        }
    }

    /**
     * Import goals for a specific match
     */
    private void importGoalsForMatch(JsonNode matchNode, Long matchId, Long homeTeamId, Long awayTeamId) {
        try {
            // Get goals for team 1
            JsonNode goals1Array = matchNode.get("goals1");
            if (goals1Array != null) {
                for (JsonNode goalNode : goals1Array) {
                    try {
                        String playerName = goalNode.get("name").asText();
                        int minute = Integer.parseInt(goalNode.get("minute").asText());

                        // Find player by name and team
                        List<PlayerDTO> players = playerService.searchPlayers(playerName);
                        for (PlayerDTO player : players) {
                            if (player.getTeamId().equals(homeTeamId)) {
                                GoalDTO goalDTO = GoalDTO.builder()
                                        .matchId(matchId)
                                        .playerId(player.getId())
                                        .scoringTeamId(homeTeamId)
                                        .minute(minute)
                                        .goalType(GoalType.valueOf("REGULAR"))
                                        .isPenaltyGoal(false)
                                        .build();

                                goalService.recordGoal(goalDTO);
                                log.debug("✓ Recorded goal by {} in minute {}", playerName, minute);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("⚠ Failed to record goal: {}", e.getMessage());
                    }
                }
            }

            // Get goals for team 2
            JsonNode goals2Array = matchNode.get("goals2");
            if (goals2Array != null) {
                for (JsonNode goalNode : goals2Array) {
                    try {
                        String playerName = goalNode.get("name").asText();
                        int minute = Integer.parseInt(goalNode.get("minute").asText());

                        // Find player by name and team
                        List<PlayerDTO> players = playerService.searchPlayers(playerName);
                        for (PlayerDTO player : players) {
                            if (player.getTeamId().equals(awayTeamId)) {
                                GoalDTO goalDTO = GoalDTO.builder()
                                        .matchId(matchId)
                                        .playerId(player.getId())
                                        .scoringTeamId(awayTeamId)
                                        .minute(minute)
                                        .goalType(GoalType.REGULAR)
                                        .isPenaltyGoal(false)
                                        .build();

                                goalService.recordGoal(goalDTO);
                                log.debug("✓ Recorded goal by {} in minute {}", playerName, minute);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("⚠ Failed to record goal: {}", e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.warn("⚠ Failed to import goals for match", e);
        }
    }

    /**
     * Import player squads from worldcup.squads.json
     */
    private void importPlayerSquads() throws IOException {
        String raw;
        try (var inputStream = new URL(SQUADS_URL).openStream()) {
            raw = new String(inputStream.readAllBytes());
        }

        // Parse as array of squads
        JsonNode squadsArray = objectMapper.readTree(raw);

        for (JsonNode squadNode : squadsArray) {
            try {
                String teamName = squadNode.get("name").asText();
                Long teamId = teamIdCache.get(teamName);

                if (teamId == null) {
                    log.warn("⚠ Team not found for squad: {}", teamName);
                    continue;
                }

                JsonNode playersArray = squadNode.get("players");
                if (playersArray != null) {
                    for (JsonNode playerNode : playersArray) {
                        try {
                            String playerName = playerNode.get("name").asText();
                            int jerseyNumber = playerNode.get("num").asInt();
                            String position = playerNode.has("pos") ? playerNode.get("pos").asText() : "MID";

                            PlayerDTO playerDTO = PlayerDTO.builder()
                                    .teamId(teamId)
                                    .name(playerName)
                                    .jerseyNumber(jerseyNumber)
                                    .position(mapPositionCode(position))
                                    .build();

                            playerService.createPlayer(playerDTO);
                            log.debug("✓ Created player: {} ({})", playerName, teamName);

                        } catch (Exception e) {
                            log.debug("⚠ Failed to import player: {}", e.getMessage());
                        }
                    }
                }

                log.debug("✓ Imported squad for team: {}", teamName);

            } catch (Exception e) {
                log.warn("⚠ Failed to import squad", e);
            }
        }

        log.info("✓ Imported player squads");
    }

    /**
     * Parse match date and time
     */
    private LocalDateTime parseMatchDateTime(String dateStr, String timeStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);

            // Extract hour from time string like "13:00 UTC-6"
            String timeOnly = timeStr.split(" ")[0]; // "13:00"
            String[] timeParts = timeOnly.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, minute);
        } catch (Exception e) {
            log.warn("⚠ Failed to parse date/time: {} {}", dateStr, timeStr);
            return LocalDateTime.now();
        }
    }

    /**
     * Determine match type based on round name
     */
    private MatchType determineMatchType(String round) {
        if (round == null) return MatchType.GROUP_MATCH;

        if (round.contains("Final") && !round.contains("Semi")) {
            return MatchType.FINAL;
        } else if (round.contains("Semi")) {
            return MatchType.SEMI_FINAL;
        } else if (round.contains("Quarter")) {
            return MatchType.KNOCKOUT;
        } else if (round.contains("Round of 16")) {
            return MatchType.KNOCKOUT;
        }

        return MatchType.GROUP_MATCH;
    }

    /**
     * Map position codes to PlayerPosition enum
     */
    private PlayerPosition mapPositionCode(String posCode) {
        if (posCode == null) return PlayerPosition.MID;

        return switch (posCode.toUpperCase()) {
            case "GK" -> PlayerPosition.GK;
            case "DEF", "D" -> PlayerPosition.DEF;
            case "MID", "M" -> PlayerPosition.MID;
            case "FWD", "F" -> PlayerPosition.FWD;
            default -> PlayerPosition.MID;
        };
    }
}

