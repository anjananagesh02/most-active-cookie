package com.quantcast.cookie;


import java.util.List;


 // Entrypoint for the Most Active Cookie CLI.
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        try {
            CliArgs parsed = CliArgs.parse(args);

            CookieLogProcessor processor = new CookieLogProcessor();
            List<String> mostActive = processor.findMostActiveCookies(parsed.filename(), parsed.date());

            // Print results to stdout as required.
            for (String cookie : mostActive) {
                System.out.println(cookie);
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println(CliArgs.usage());
            System.exit(2);
        } catch (Exception e) {
            
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
