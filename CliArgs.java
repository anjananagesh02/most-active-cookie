package com.quantcast.cookie;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal CLI parsing for -f <filename> -d <yyyy-MM-dd>.
 
 */
public record CliArgs(Path filename, LocalDate date) {

    public static CliArgs parse(String[] args) {
        Map<String, String> flags = parseFlags(args);

        String fileStr = flags.get("-f");
        String dateStr = flags.get("-d");

        if (fileStr == null || fileStr.isBlank()) {
            throw new IllegalArgumentException("Missing required -f <filename> argument.");
        }
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException("Missing required -d <yyyy-MM-dd> argument.");
        }

        Path file = Path.of(fileStr);
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Not a regular file: " + file);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for -d. Expected yyyy-MM-dd but got: " + dateStr);
        }

        return new CliArgs(file, date);
    }

    private static Map<String, String> parseFlags(String[] args) {
        Map<String, String> flags = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("-")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for argument: " + a);
                }
                String value = args[++i];
                flags.put(a, value);
            } else {
                throw new IllegalArgumentException("Unexpected argument: " + a);
            }
        }
        return flags;
    }

    public static String usage() {
        return "Usage: java -jar most-active-cookie.jar -f <cookie_log.csv> -d <yyyy-MM-dd>";
    }
}
