package org.sgd.worldcup.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sgd.worldcup.dataimport.WorldCupDataImporter;
import org.sgd.worldcup.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for World Cup data import operations
 * Provides endpoints to import data from OpenFootball JSON repository
 */
@Slf4j
@RestController
@RequestMapping("/admin/import")
@RequiredArgsConstructor
public class DataImportController {

    private final WorldCupDataImporter dataImporter;

    /**
     * Import all World Cup 2026 data from OpenFootball repository
     *
     * This endpoint will:
     * 1. Import all 32 teams
     * 2. Create 8 groups and assign teams
     * 3. Import all group stage and knockout matches
     * 4. Create player squads for all teams
     * 5. Import goals from completed matches
     *
     * @return ApiResponse with import status
     */
    @PostMapping("/worldcup-2026")
    public ResponseEntity<ApiResponse<String>> importWorldCup2026Data() {
        try {
            log.info("Starting World Cup 2026 data import via API...");
            dataImporter.importAllData();

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .success(true)
                            .message("✅ World Cup 2026 data imported successfully!")
                            .data("Data import completed. Check logs for details.")
                            .build()
            );
        } catch (Exception e) {
            log.error("Data import failed", e);
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .success(false)
                            .message("❌ Data import failed: " + e.getMessage())
                            .code(400)
                            .build()
            );
        }
    }

    /**
     * Health check endpoint - returns import service status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getImportStatus() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Import service is available")
                        .data("Ready to import World Cup 2026 data from OpenFootball repository")
                        .build()
        );
    }

    /**
     * Get data source information
     */
    @GetMapping("/source")
    public ResponseEntity<ApiResponse<String>> getDataSourceInfo() {
        String sourceInfo = "Data Source: OpenFootball World Cup JSON Repository\n" +
                "Repository URL: https://github.com/openfootball/worldcup.json\n" +
                "Data Location: /2026 directory\n" +
                "Files Used:\n" +
                "  - worldcup.teams.json (Teams data)\n" +
                "  - worldcup.groups.json (Groups)\n" +
                "  - worldcup.json (Matches & Goals)\n" +
                "  - worldcup.squads.json (Player rosters)\n" +
                "  - worldcup.stadiums.json (Venue information)\n";

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Data source information")
                        .data(sourceInfo)
                        .build()
        );
    }
}

