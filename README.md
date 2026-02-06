# Most Active Cookie 

Command-line program to find the most active cookie(s) for a given UTC date from a cookie log CSV.


## Build

Requirement:  **Java 17+** and **Maven 3.9+** version.

```bash
mvn clean package
```
This produces:

- `target/most-active-cookie-1.0.0.jar`

## Run

```bash
java -jar target/most-active-cookie-1.0.0.jar -f cookie_log.csv -d 2018-12-09
```

Output is written to stdout(most active cookie), e.g.

```
AtY0laUfhglK3lC7
```

## Test

```bash
mvn test
```
## Project Structure

- src/main/java → production code
- src/test/java → unit tests
- target/ → Maven build output

## Design notes

- Uses `java.time.OffsetDateTime` to parse timestamps and converts to UTC before extracting the date.
- Reads the CSV line-by-line (streaming) and counts only rows for the requested date.
- Since the log is sorted newest-first, once we have started counting the target date and then encounter an earlier date, we stop reading.
- Output is sorted lexicographically to keep results deterministic.

## Assumptions made

- `-d` is a date in UTC (`yyyy-MM-dd`).
- Input CSV has a header row and is sorted by timestamp descending.
- The program may load counts into memory (as stated).
