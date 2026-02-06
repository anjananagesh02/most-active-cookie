package com.quantcast.cookie;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes a cookie log CSV and returns the most active cookie(s) for a given day.
 
 */
public final class CookieLogProcessor {

    /**
     * Finds the most active cookie(s) for the provided UTC date.
     *
     * @param csvFile path to the cookie log
     * @param utcDate date in UTC
     * @return list of cookie ids with the maximum occurrence on that date (may be empty)
     */
    public List<String> findMostActiveCookies(Path csvFile, LocalDate utcDate) throws IOException {
        Map<String, Integer> counts = new HashMap<>();

        boolean seenTargetDate = false;

        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // header
            if (line == null) {
                return List.of();
            }

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                CsvRow row = CsvRow.parse(line);
                LocalDate rowUtcDate = row.timestampUtcDate();

                if (rowUtcDate.isAfter(utcDate)) {
                    // File is sorted newest-first; dates after target appear first.
                    continue;
                }

                if (rowUtcDate.isEqual(utcDate)) {
                    seenTargetDate = true;
                    counts.merge(row.cookie(), 1, Integer::sum);
                    continue;
                }

                // rowUtcDate is before utcDate
                if (seenTargetDate) {
                    // Already passed through the target date section, can stop early.
                    break;
                }
            }
        }

        return extractMaxKeys(counts);
    }

    private static List<String> extractMaxKeys(Map<String, Integer> counts) {
        int max = 0;
        for (int c : counts.values()) {
            if (c > max) {
                max = c;
            }
        }

        if (max == 0) {
            return List.of();
        }

        List<String> winners = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() == max) {
                winners.add(e.getKey());
            }
        }

        
        winners.sort(String::compareTo);
        return winners;
    }

    
    private record CsvRow(String cookie, OffsetDateTime timestamp) {
        static CsvRow parse(String line) {
            String[] parts = line.split(",", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid CSV row (expected 2 columns): " + line);
            }
            String cookie = parts[0].trim();
            String ts = parts[1].trim();
            if (cookie.isEmpty() || ts.isEmpty()) {
                throw new IllegalArgumentException("Invalid CSV row (blank cookie or timestamp): " + line);
            }
            OffsetDateTime timestamp;
            try {
                timestamp = OffsetDateTime.parse(ts);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid timestamp: " + ts);
            }
            return new CsvRow(cookie, timestamp);
        }

        LocalDate timestampUtcDate() {
            return timestamp.atZoneSameInstant(ZoneOffset.UTC).toLocalDate();
        }
    }
}
