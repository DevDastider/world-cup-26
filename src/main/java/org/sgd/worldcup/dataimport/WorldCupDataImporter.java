package org.sgd.worldcup.dataimport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sgd.worldcup.dto.GoalDTO;
import org.sgd.worldcup.dto.GroupDTO;
import org.sgd.worldcup.dto.MatchDTO;
import org.sgd.worldcup.dto.PlayerDTO;
import org.sgd.worldcup.dto.TeamDTO;
import org.sgd.worldcup.enums.GoalType;
import org.sgd.worldcup.enums.MatchStatus;
import org.sgd.worldcup.enums.MatchType;
import org.sgd.worldcup.enums.PlayerPosition;
import org.sgd.worldcup.service.GoalService;
import org.sgd.worldcup.service.GroupService;
import org.sgd.worldcup.service.GroupTeamService;
import org.sgd.worldcup.service.MatchService;
import org.sgd.worldcup.service.PlayerService;
import org.sgd.worldcup.service.TeamService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, Long> teamIdByCodeCache;
    private Map<String, Long> groupIdCache;

    /**
     * Confederation marker used to flag auto-generated placeholder teams that
     * represent undecided knockout slots (e.g. "Winner Group A", "W73").
     * Query {@code confederation <> 'TBD'} to exclude them.
     */
    private static final String PLACEHOLDER_CONFED = "TBD";

    /** Counter used to mint unique country codes for placeholder teams. */
    private int placeholderCounter;

    /**
     * Main import method - imports all data in the correct order.
     *
     * <p>Note: this method is intentionally NOT wrapped in a single
     * {@code @Transactional} boundary. Each individual entity is persisted by its
     * own service call (which runs in its own transaction). If one record fails
     * validation it is skipped without marking a shared transaction as
     * rollback-only, which previously caused an
     * {@code UnexpectedRollbackException} at the end of the import.
     */
    public void importAllData() {
        try {
            log.info("Starting World Cup 2026 data import from OpenFootball repository...");

            // Initialize caches
            teamCache = new HashMap<>();
            groupCache = new HashMap<>();
            teamIdCache = new HashMap<>();
            teamIdByCodeCache = new HashMap<>();
            groupIdCache = new HashMap<>();
            placeholderCounter = 0;

            // Import in order
            log.info("Step 1/5: Importing teams...");
            importTeams();

            log.info("Step 2/5: Importing groups...");
            importGroups();

            log.info("Step 3/5: Adding teams to groups...");
            addTeamsToGroups();

            // Squads must be imported BEFORE matches so that players exist in the
            // database when goals are attributed to them during match import.
            log.info("Step 4/5: Importing player squads...");
            importPlayerSquads();

            log.info("Step 5/5: Importing matches and goals...");
            importMatches();

            log.info("Calculating group standings...");
            calculateGroupStandings();

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
                    String confederation = textOrNull(teamNode, "confed");

                    TeamDTO teamDTO = TeamDTO.builder()
                            .name(teamName)
                            .countryCode(countryCode)
                            .confederation(confederation)
                            .nameNormalised(textOrNull(teamNode, "name_normalised"))
                            .continent(textOrNull(teamNode, "continent"))
                            .flagIcon(textOrNull(teamNode, "flag_icon"))
                            .build();

                    TeamDTO created = teamService.createTeam(teamDTO);
                    teamCache.put(teamName, created);
                    teamIdCache.put(teamName, created.getId());
                    if (countryCode != null) {
                        teamIdByCodeCache.put(countryCode, created.getId());
                    }

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
     * Recalculate standings (wins/draws/losses, goals, points, goal difference
     * and position) for every imported group from the completed group matches.
     */
    private void calculateGroupStandings() {
        for (Map.Entry<String, Long> entry : groupIdCache.entrySet()) {
            try {
                groupTeamService.recalculateStandings(entry.getValue());
            } catch (Exception e) {
                log.warn("⚠ Failed to calculate standings for {}: {}", entry.getKey(), e.getMessage());
            }
        }
        log.info("✓ Calculated standings for {} groups", groupIdCache.size());
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
                    String team1Name = textOrNull(matchNode, "team1");
                    String team2Name = textOrNull(matchNode, "team2");
                    String dateStr = textOrNull(matchNode, "date");
                    String timeStr = textOrNull(matchNode, "time");
                    String groupStr = textOrNull(matchNode, "group");
                    String ground = textOrNull(matchNode, "ground");

                    if (team1Name == null || team2Name == null || dateStr == null) {
                        log.warn("⚠ Skipping match with missing team or date information");
                        continue;
                    }

                    Long homeTeamId = resolveOrCreatePlaceholderTeam(team1Name);
                    Long awayTeamId = resolveOrCreatePlaceholderTeam(team2Name);

                    if (homeTeamId == null || awayTeamId == null) {
                        log.warn("⚠ Could not resolve teams for match: {} vs {}", team1Name, team2Name);
                        continue;
                    }
                    if (homeTeamId.equals(awayTeamId)) {
                        log.warn("⚠ Skipping match with identical home/away slot: {} vs {}", team1Name, team2Name);
                        continue;
                    }

                    // Parse match date and time
                    LocalDateTime matchDateTime = parseMatchDateTime(dateStr, timeStr);

                    // Determine match type
                    MatchType matchType = MatchType.GROUP_MATCH;
                    Long groupId = null;

                    if (groupStr != null && !groupStr.isEmpty()) {
                        // The matches feed already uses the full group name
                        // (e.g. "Group A"); the groups feed is keyed the same
                        // way. Normalise so a bare letter ("A") also resolves.
                        groupId = resolveGroupId(groupStr);
                    } else {
                        // Knockout stage
                        String round = textOrNull(matchNode, "round");
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
            // goals1 are credited to the home team; an own goal here is scored by
            // an away player. goals2 are credited to the away team; an own goal
            // here is scored by a home player. Pass both ids so own goals can be
            // attributed to the correct (opposing) scorer.
            importGoalsForSide(matchNode.get("goals1"), matchId, homeTeamId, awayTeamId);
            importGoalsForSide(matchNode.get("goals2"), matchId, awayTeamId, homeTeamId);
        } catch (Exception e) {
            log.warn("⚠ Failed to import goals for match", e);
        }
    }

    /**
     * Import the list of goals scored for a single (beneficiary) team in a match.
     *
     * @param beneficiaryTeamId the team credited with the goal
     * @param opposingTeamId    the other team; an own goal in this list is
     *                          actually scored by one of their players
     */
    private void importGoalsForSide(JsonNode goalsArray, Long matchId, Long beneficiaryTeamId, Long opposingTeamId) {
        if (goalsArray == null) {
            return;
        }

        // Build diacritic-insensitive lookups for both squads so goal scorer
        // names from worldcup.json (which may spell accents differently than
        // worldcup.squads.json) still resolve. Normal goals resolve against the
        // beneficiary squad; own goals resolve against the opposing squad.
        Map<String, PlayerDTO> beneficiaryPlayers = loadSquadByNormalisedName(beneficiaryTeamId);
        Map<String, PlayerDTO> opposingPlayers = loadSquadByNormalisedName(opposingTeamId);

        for (JsonNode goalNode : goalsArray) {
            try {
                String playerName = textOrNull(goalNode, "name");
                if (playerName == null) {
                    continue;
                }
                Integer minute = parseGoalMinute(textOrNull(goalNode, "minute"));
                if (minute == null) {
                    continue;
                }

                boolean isPenalty = goalNode.has("penalty") && goalNode.get("penalty").asBoolean(false);
                boolean isOwnGoal = goalNode.has("owngoal") && goalNode.get("owngoal").asBoolean(false);
                GoalType goalType = isOwnGoal
                        ? (isPenalty ? GoalType.PENALTY_OWN_GOAL : GoalType.OWN_GOAL)
                        : (isPenalty ? GoalType.PENALTY : GoalType.REGULAR);

                // For an own goal the scorer is a player of the OPPOSING team,
                // but the goal is credited to the beneficiary team.
                Map<String, PlayerDTO> squad = isOwnGoal ? opposingPlayers : beneficiaryPlayers;
                PlayerDTO player = squad.get(normaliseName(playerName));
                if (player == null) {
                    Long squadTeamId = isOwnGoal ? opposingTeamId : beneficiaryTeamId;
                    log.debug("⚠ Goal scorer '{}' not found in team {} squad", playerName, squadTeamId);
                    continue;
                }

                GoalDTO goalDTO = GoalDTO.builder()
                        .matchId(matchId)
                        .playerId(player.getId())
                        .scoringTeamId(beneficiaryTeamId)
                        .minute(minute)
                        .goalType(goalType)
                        .isPenaltyGoal(isPenalty)
                        .build();

                goalService.recordGoal(goalDTO);
                log.debug("✓ Recorded {}goal by {} in minute {}", isOwnGoal ? "own " : "", playerName, minute);
            } catch (Exception e) {
                log.debug("⚠ Failed to record goal: {}", e.getMessage());
            }
        }
    }

    /**
     * Load a team's squad as a diacritic-insensitive name → player lookup.
     */
    private Map<String, PlayerDTO> loadSquadByNormalisedName(Long teamId) {
        Map<String, PlayerDTO> byName = new HashMap<>();
        if (teamId == null) {
            return byName;
        }
        try {
            for (PlayerDTO p : playerService.getPlayersByTeam(teamId)) {
                byName.putIfAbsent(normaliseName(p.getName()), p);
            }
        } catch (Exception e) {
            log.debug("⚠ Could not load players for team {}: {}", teamId, e.getMessage());
        }
        return byName;
    }

    /**
     * Normalise a name for matching: strip diacritics, collapse whitespace and
     * lowercase. e.g. "Ladislav Krejčí" and "Ladislav Krejcí" both become
     * "ladislav krejci".
     */
    private String normaliseName(String name) {
        if (name == null) {
            return "";
        }
        String stripped = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return stripped.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Parse a goal minute string. Handles plain minutes ("67") and stoppage
     * time ("45+2" -> 47, "90+3" -> 93). Returns {@code null} when unparseable.
     */
    private Integer parseGoalMinute(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            String cleaned = raw.trim();
            if (cleaned.contains("+")) {
                String[] parts = cleaned.split("\\+");
                int base = Integer.parseInt(parts[0].trim());
                int extra = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
                return Math.min(base + extra, 150);
            }
            return Math.min(Integer.parseInt(cleaned), 150);
        } catch (NumberFormatException e) {
            log.debug("⚠ Unparseable goal minute: {}", raw);
            return null;
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
                String fifaCode = textOrNull(squadNode, "fifa_code");

                // The squad feed sometimes uses a different display name than the
                // teams feed (e.g. "United States" vs "USA", "Bosnia and
                // Herzegovina" vs "Bosnia & Herzegovina"). Match on the stable
                // FIFA code first, then fall back to the name.
                Long teamId = null;
                if (fifaCode != null) {
                    teamId = teamIdByCodeCache.get(fifaCode);
                }
                if (teamId == null) {
                    teamId = teamIdCache.get(teamName);
                }

                if (teamId == null) {
                    log.warn("⚠ Team not found for squad: {} ({})", teamName, fifaCode);
                    continue;
                }

                JsonNode playersArray = squadNode.get("players");
                if (playersArray != null) {
                    int autoJerseyNumber = 0;
                    for (JsonNode playerNode : playersArray) {
                        autoJerseyNumber++;
                        try {
                            String playerName = textOrNull(playerNode, "name");
                            if (playerName == null || playerName.isBlank()) {
                                log.debug("⚠ Skipping player with no name for team {}", teamName);
                                continue;
                            }

                            // The squad feed does not always provide a shirt number ("number").
                            // Fall back to a sequential number so the player can still be imported.
                            JsonNode numNode = playerNode.get("number");
                            int jerseyNumber = (numNode != null && !numNode.isNull() && numNode.asInt() > 0)
                                    ? numNode.asInt()
                                    : autoJerseyNumber;

                            String position = textOrNull(playerNode, "pos");

                            // Club is a nested object: { "name": ..., "country": ... }
                            String clubName = null;
                            String clubCountry = null;
                            JsonNode clubNode = playerNode.get("club");
                            if (clubNode != null && !clubNode.isNull()) {
                                clubName = textOrNull(clubNode, "name");
                                clubCountry = textOrNull(clubNode, "country");
                            }

                            PlayerDTO playerDTO = PlayerDTO.builder()
                                    .teamId(teamId)
                                    .name(playerName)
                                    .jerseyNumber(jerseyNumber)
                                    .position(mapPositionCode(position))
                                    .dateOfBirth(parseDate(textOrNull(playerNode, "date_of_birth")))
                                    .clubName(clubName)
                                    .clubCountry(clubCountry)
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

            int hour = 0;
            int minute = 0;
            if (timeStr != null && !timeStr.isBlank()) {
                // Extract hour from time string like "13:00 UTC-6"
                String timeOnly = timeStr.split(" ")[0]; // "13:00"
                String[] timeParts = timeOnly.split(":");
                hour = Integer.parseInt(timeParts[0]);
                minute = Integer.parseInt(timeParts[1]);
            }

            return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, minute);
        } catch (Exception e) {
            log.warn("⚠ Failed to parse date/time: {} {}", dateStr, timeStr);
            return LocalDateTime.now();
        }
    }

    /**
     * Safely read a text field from a JSON node, returning {@code null} when the
     * field is absent or explicitly null.
     */
    private String textOrNull(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    /**
     * Determine match type based on the source "round" name.
     *
     * Rule (per the OpenFootball feed): a round that starts with "Matchday"
     * (e.g. "Matchday 15") is a group-stage fixture. Anything else is a
     * knockout fixture, refined into its specific stage.
     */
    private MatchType determineMatchType(String round) {
        if (round == null) return MatchType.GROUP_MATCH;          // no info → treat as group

        String normalised = round.trim();                          // tolerate stray whitespace

        // Group stage: any "Matchday N" round (case-insensitive for safety)
        if (normalised.toLowerCase().startsWith("matchday")) {
            return MatchType.GROUP_MATCH;
        }

        // Otherwise it is a knockout round — refine the specific stage
        if (normalised.toLowerCase().contains("third")) {          // "Match for third place"
            return MatchType.THIRD_PLACE;
        } else if (normalised.contains("Final") && !normalised.contains("Semi")) {
            return MatchType.FINAL;                                 // "Final" (not "Semi-Final")
        } else if (normalised.contains("Semi")) {
            return MatchType.SEMI_FINAL;                            // "Semi-finals"
        } else if (normalised.contains("Quarter")) {
            return MatchType.QUARTER_FINAL;                         // "Quarter-finals"
        } else if (normalised.contains("Round of 16")) {
            return MatchType.ROUND_OF_16;                           // "Round of 16"
        } else if (normalised.contains("Round of 32")) {
            return MatchType.ROUND_OF_32;                           // "Round of 32"
        }

        // Any other unrecognised knockout label → default to the earliest knockout round
        return MatchType.GROUP_MATCH;
    }

    /**
     * Map position codes to PlayerPosition enum.
     * The OpenFootball squad feed uses two-letter codes: GK, DF, MF, FW.
     */
    private PlayerPosition mapPositionCode(String posCode) {
        if (posCode == null) return PlayerPosition.MID;

        return switch (posCode.toUpperCase()) {
            case "GK" -> PlayerPosition.GK;
            case "DF", "DEF", "D" -> PlayerPosition.DEF;
            case "MF", "MID", "M" -> PlayerPosition.MID;
            case "FW", "FWD", "F" -> PlayerPosition.FWD;
            default -> PlayerPosition.MID;
        };
    }

    /**
     * Resolve a match team name to a team id. Real teams (group stage) resolve
     * from the cache. For undecided knockout slots (e.g. "1A", "2B", "W73",
     * "3A/B/C/D/F") no real team exists yet, so a lightweight placeholder team
     * is created on demand and reused for the rest of the import. This lets the
     * full 104-match schedule import without failing, while the placeholder is
     * tagged with confederation "TBD" so it can be excluded from queries.
     */
    private Long resolveOrCreatePlaceholderTeam(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            return null;
        }
        Long existing = teamIdCache.get(teamName);
        if (existing != null) {
            return existing;
        }
        try {
            String label = placeholderLabel(teamName);
            TeamDTO placeholder = TeamDTO.builder()
                    .name(label)
                    .countryCode(nextPlaceholderCode())
                    .confederation(PLACEHOLDER_CONFED)
                    .placeholder(true)
                    .build();

            TeamDTO created = teamService.createTeam(placeholder);
            // Cache under the raw slot code so repeated references reuse it.
            teamIdCache.put(teamName, created.getId());
            log.debug("✓ Created placeholder team '{}' for slot '{}'", label, teamName);
            return created.getId();
        } catch (Exception e) {
            log.warn("⚠ Failed to create placeholder team for slot '{}': {}", teamName, e.getMessage());
            return null;
        }
    }

    /**
     * Build a human-readable name for a knockout placeholder slot.
     * Examples: "1A" -> "Winner Group A", "2B" -> "Runner-up Group B",
     * "3A/B/C/D/F" -> "3rd Place A/B/C/D/F", "W73" -> "Winner Match 73",
     * "L101" -> "Loser Match 101".
     */
    private String placeholderLabel(String slot) {
        try {
            if (slot.matches("1[A-L]")) {
                return "Winner Group " + slot.substring(1);
            }
            if (slot.matches("2[A-L]")) {
                return "Runner-up Group " + slot.substring(1);
            }
            if (slot.startsWith("3")) {
                return "3rd Place " + slot.substring(1);
            }
            if (slot.matches("W\\d+")) {
                return "Winner Match " + slot.substring(1);
            }
            if (slot.matches("L\\d+")) {
                return "Loser Match " + slot.substring(1);
            }
        } catch (Exception ignored) {
            // fall through to raw slot
        }
        return "TBD " + slot;
    }

    /**
     * Generate a unique, schema-valid (2-3 char) country code for a placeholder
     * team. Uses an 'X' prefix plus a base-36 counter so it never collides with
     * real 3-letter FIFA codes (which contain only letters).
     */
    private String nextPlaceholderCode() {
        String suffix = Integer.toString(placeholderCounter++, 36).toUpperCase();
        if (suffix.length() < 2) {
            suffix = "0" + suffix;
        }
        return "X" + suffix;
    }

    /**
     * Resolve a group id from either the full group name ("Group A") or a bare
     * group letter ("A").
     */
    private Long resolveGroupId(String groupValue) {
        if (groupValue == null || groupValue.isBlank()) {
            return null;
        }
        String key = groupValue.startsWith("Group ") ? groupValue : "Group " + groupValue;
        return groupIdCache.get(key);
    }

    /**
     * Parse an ISO date string (yyyy-MM-dd), returning {@code null} when absent
     * or unparseable.
     */
    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            log.debug("⚠ Unparseable date: {}", raw);
            return null;
        }
    }
}

