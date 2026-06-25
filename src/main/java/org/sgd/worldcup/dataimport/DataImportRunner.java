package org.sgd.worldcup.dataimport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Optional command-line runner for automatic data import on startup
 * Enable by setting: app.import.enabled=true in application.properties
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataImportRunner implements CommandLineRunner {

    private final WorldCupDataImporter dataImporter;

    @Value("${app.import.enabled:false}")
    private boolean importEnabled;

    @Value("${app.import.on-startup:false}")
    private boolean importOnStartup;

    @Override
    public void run(String... args) throws Exception {
        // Check if import is enabled via command line argument
        boolean importViaArg = java.util.Arrays.asList(args).contains("--import-worldcup-2026");

        if (importViaArg || (importEnabled && importOnStartup)) {
            log.info("╔════════════════════════════════════════════════╗");
            log.info("║  World Cup 2026 Data Import Starting...         ║");
            log.info("╚════════════════════════════════════════════════╝");

            try {
                long startTime = System.currentTimeMillis();
                dataImporter.importAllData();
                long duration = System.currentTimeMillis() - startTime;

                log.info("╔════════════════════════════════════════════════╗");
                log.info("║  ✅ Data import completed successfully!         ║");
                log.info("║  Duration: {} ms                              ║", duration);
                log.info("╚════════════════════════════════════════════════╝");

            } catch (Exception e) {
                log.error("╔════════════════════════════════════════════════╗");
                log.error("║  ❌ Data import failed!                        ║");
                log.error("╚════════════════════════════════════════════════╝");
                log.error("Error details:", e);
            }
        }
    }
}

