package org.sgd.worldcup.dataimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utility class for World Cup data import
 * Provides methods for downloading, caching, and validating data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataImportUtility {

    private static final String CACHE_DIR = "worldcup-data-cache";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final long CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000; // 24 hours

    /**
     * Download JSON from URL with caching
     */
    public String downloadJson(String url, String fileName, boolean useCache) {
        try {
            File cacheFile = new File(CACHE_DIR, fileName);

            // Check if we can use cached file
            if (useCache && cacheFile.exists() && isCacheValid(cacheFile)) {
                log.info("📦 Using cached data: {}", fileName);
                return new String(Files.readAllBytes(cacheFile.toPath()));
            }

            // Download fresh data
            log.info("⬇️  Downloading: {}", fileName);
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            String content = new String(connection.getInputStream().readAllBytes());

            // Cache the data
            if (useCache) {
                new File(CACHE_DIR).mkdirs();
                Files.write(cacheFile.toPath(), content.getBytes());
                log.info("💾 Cached data: {}", fileName);
            }

            return content;

        } catch (Exception e) {
            log.error("❌ Failed to download {}: {}", fileName, e.getMessage());
            throw new RuntimeException("Failed to download data: " + e.getMessage(), e);
        }
    }

    /**
     * Check if cache file is still valid
     */
    private boolean isCacheValid(File cacheFile) {
        long fileAge = System.currentTimeMillis() - cacheFile.lastModified();
        return fileAge < CACHE_VALIDITY_MS;
    }

    /**
     * Get list of all available data files
     */
    public List<String> getAvailableDataFiles() {
        return Arrays.asList(
                "worldcup.teams.json",
                "worldcup.groups.json",
                "worldcup.json",
                "worldcup.squads.json",
                "worldcup.stadiums.json",
                "worldcup.quali_playoffs.json"
        );
    }

    /**
     * Get detailed information about data sources
     */
    public Map<String, String> getDataSourceInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Repository", "OpenFootball/worldcup.json");
        info.put("URL", "https://github.com/openfootball/worldcup.json");
        info.put("Year", "2026");
        info.put("Base URL", "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/");
        info.put("License", "Creative Commons");
        return info;
    }

    /**
     * Validate downloaded JSON
     */
    public boolean validateJsonStructure(String json, String expectedRootKey) {
        try {
            var root = objectMapper.readTree(json);
            return root.has(expectedRootKey) || root.isArray();
        } catch (Exception e) {
            log.warn("Invalid JSON structure: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get summary statistics from data files
     */
    public ImportStatistics analyzeData(String teamsJson, String groupsJson, String matchesJson, String squadsJson) {
        return ImportStatistics.builder()
                .teamCount(countJsonArrayElements(teamsJson))
                .groupCount(countJsonObjectProperty(groupsJson, "groups"))
                .matchCount(countJsonObjectProperty(matchesJson, "matches"))
                .squadCount(countJsonArrayElements(squadsJson))
                .build();
    }

    private int countJsonArrayElements(String json) {
        try {
            var array = objectMapper.readTree(json);
            return array.isArray() ? array.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int countJsonObjectProperty(String json, String key) {
        try {
            var root = objectMapper.readTree(json);
            var property = root.get(key);
            return property != null && property.isArray() ? property.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Clear cached data
     */
    public void clearCache() {
        try {
            File cacheDir = new File(CACHE_DIR);
            if (cacheDir.exists()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.delete()) {
                            log.info("✓ Deleted cache: {}", file.getName());
                        }
                    }
                }
            }
            log.info("✓ Cache cleared successfully");
        } catch (Exception e) {
            log.error("Failed to clear cache: {}", e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        File cacheDir = new File(CACHE_DIR);

        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                stats.put("cached_files", files.length);
                long totalSize = 0;
                for (File file : files) {
                    totalSize += file.length();
                }
                stats.put("total_size_mb", String.format("%.2f", totalSize / (1024.0 * 1024.0)));
                stats.put("cache_directory", cacheDir.getAbsolutePath());
            }
        } else {
            stats.put("cached_files", 0);
            stats.put("total_size_mb", "0");
        }

        return stats;
    }

    /**
     * Data statistics holder
     */
    public static class ImportStatistics {
        private int teamCount;
        private int groupCount;
        private int matchCount;
        private int squadCount;

        public ImportStatistics(int teamCount, int groupCount, int matchCount, int squadCount) {
            this.teamCount = teamCount;
            this.groupCount = groupCount;
            this.matchCount = matchCount;
            this.squadCount = squadCount;
        }

        public static ImportStatisticsBuilder builder() {
            return new ImportStatisticsBuilder();
        }

        @Override
        public String toString() {
            return String.format(
                    "Teams: %d, Groups: %d, Matches: %d, Squads: %d",
                    teamCount, groupCount, matchCount, squadCount
            );
        }

        public int getTeamCount() { return teamCount; }
        public int getGroupCount() { return groupCount; }
        public int getMatchCount() { return matchCount; }
        public int getSquadCount() { return squadCount; }

        public static class ImportStatisticsBuilder {
            private int teamCount;
            private int groupCount;
            private int matchCount;
            private int squadCount;

            public ImportStatisticsBuilder teamCount(int count) { this.teamCount = count; return this; }
            public ImportStatisticsBuilder groupCount(int count) { this.groupCount = count; return this; }
            public ImportStatisticsBuilder matchCount(int count) { this.matchCount = count; return this; }
            public ImportStatisticsBuilder squadCount(int count) { this.squadCount = count; return this; }

            public ImportStatistics build() {
                return new ImportStatistics(teamCount, groupCount, matchCount, squadCount);
            }
        }
    }
}

