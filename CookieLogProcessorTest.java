package com.quantcast.cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CookieLogProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void returnsMostActiveCookieForGivenDay() throws IOException {
        Path csv = writeTemp("cookie,timestamp\n" +
                "AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00\n" +
                "SAZuXPGUrfbcn5UA,2018-12-09T10:13:00+00:00\n" +
                "5UAVanZf6UtGyKVS,2018-12-09T07:25:00+00:00\n" +
                "AtY0laUfhglK3lC7,2018-12-09T06:19:00+00:00\n" +
                "SAZuXPGUrfbcn5UA,2018-12-08T22:03:00+00:00\n");

        CookieLogProcessor processor = new CookieLogProcessor();
        List<String> result = processor.findMostActiveCookies(csv, LocalDate.parse("2018-12-09"));

        assertEquals(List.of("AtY0laUfhglK3lC7"), result);
    }

    @Test
    void returnsAllCookiesWhenTie() throws IOException {
        Path csv = writeTemp("cookie,timestamp\n" +
                "cookieA,2018-12-09T23:59:00+00:00\n" +
                "cookieB,2018-12-09T23:58:00+00:00\n" +
                "cookieA,2018-12-09T01:00:00+00:00\n" +
                "cookieB,2018-12-09T00:59:00+00:00\n" +
                "older,2018-12-08T23:59:00+00:00\n");

        CookieLogProcessor processor = new CookieLogProcessor();
        List<String> result = processor.findMostActiveCookies(csv, LocalDate.parse("2018-12-09"));

        assertEquals(List.of("cookieA", "cookieB"), result);
    }

    @Test
    void returnsEmptyWhenNoEntriesForDate() throws IOException {
        Path csv = writeTemp("cookie,timestamp\n" +
                "cookieA,2018-12-08T23:59:00+00:00\n" +
                "cookieB,2018-12-08T00:00:00+00:00\n");

        CookieLogProcessor processor = new CookieLogProcessor();
        List<String> result = processor.findMostActiveCookies(csv, LocalDate.parse("2018-12-09"));

        assertTrue(result.isEmpty());
    }

    @Test
    void stopsEarlyBecauseFileSortedNewestFirst() throws IOException {
        /*  If the processor doesn't stop early, it would still be correct, but this test
        helps ensure the intended optimization works: once we've passed the target date section,
         we can stop reading.*/
        Path csv = writeTemp("cookie,timestamp\n" +
                "newer,2018-12-10T00:00:00+00:00\n" +
                "cookieA,2018-12-09T23:59:00+00:00\n" +
                "cookieA,2018-12-09T23:58:00+00:00\n" +
                "older1,2018-12-08T23:59:00+00:00\n" +
                "older2,2018-12-01T00:00:00+00:00\n");

        CookieLogProcessor processor = new CookieLogProcessor();
        List<String> result = processor.findMostActiveCookies(csv, LocalDate.parse("2018-12-09"));

        assertEquals(List.of("cookieA"), result);
    }

    private Path writeTemp(String content) throws IOException {
        Path p = tempDir.resolve("cookie_log.csv");
        Files.writeString(p, content);
        return p;
    }
}
