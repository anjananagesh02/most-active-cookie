package com.quantcast.cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliArgsTest {

    @TempDir
    Path tempDir;

    @Test
    void parsesValidArguments() throws Exception {
        Path file = tempDir.resolve("cookie_log.csv");
        Files.writeString(file, "cookie,timestamp\n");

        CliArgs args = CliArgs.parse(new String[]{"-f", file.toString(), "-d", "2018-12-09"});
        assertEquals(file, args.filename());
        assertEquals("2018-12-09", args.date().toString());
    }

    @Test
    void rejectsMissingFile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CliArgs.parse(new String[]{"-d", "2018-12-09"}));
        assertTrue(ex.getMessage().contains("-f"));
    }

    @Test
    void rejectsInvalidDate() throws Exception {
        Path file = tempDir.resolve("cookie_log.csv");
        Files.writeString(file, "cookie,timestamp\n");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CliArgs.parse(new String[]{"-f", file.toString(), "-d", "09-12-2018"}));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid date"));
    }
}
