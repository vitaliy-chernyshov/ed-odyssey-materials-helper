package nl.jixxed.eliteodysseymaterials.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CountingInputStream;
import javafx.application.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.jixxed.eliteodysseymaterials.enums.JournalEventType;
import nl.jixxed.eliteodysseymaterials.service.event.EventService;
import nl.jixxed.eliteodysseymaterials.service.event.JournalInitEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileProcessor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String EVENT = "event";
    private static long position = 0L;

    public static synchronized void resetAndProcessJournal(final File file) {
        position = 0L;
        processJournalFast(file);
    }

    @SuppressWarnings("java:S2674")
    private static synchronized void processJournalFast(final File file) {
        EventService.publish(new JournalInitEvent(false));
        try (final CountingInputStream is = new CountingInputStream(Files.newInputStream(Paths.get(file.toURI()), StandardOpenOption.READ))) {
            final Map<JournalEventType, String> messages = new EnumMap<>(JournalEventType.class);
            final List<String> horizonsMaterialMessages = new ArrayList<>();
            if (file.length() >= position) {
                // Skip over already processed bytes.
                is.skip(position);
            }
            final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            final BufferedReader lineReader = new BufferedReader(reader);

            // Process all lines.
            String line;
            while ((line = lineReader.readLine()) != null) {
                //try to read line as json, if exception occurs we get JsonProcessingException and can try to read again later
                final JsonNode jsonNode = OBJECT_MAPPER.readTree(line);

                if (jsonNode.get(EVENT) != null) {
                    final JournalEventType journalEventType = JournalEventType.forName(jsonNode.get(EVENT).asText());
                    if (JournalEventType.MATERIALCOLLECTED.equals(journalEventType) || JournalEventType.MATERIALTRADE.equals(journalEventType)) {
                        horizonsMaterialMessages.add(line);
                    } else if (!JournalEventType.UNKNOWN.equals(journalEventType)) {
                        if (JournalEventType.MATERIALS.equals(journalEventType)) {
                            horizonsMaterialMessages.clear();
                        }
                        messages.put(journalEventType, line);
                    }
                }
                position = is.getCount();
            }

            messages.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> {
                        try {
                            return OBJECT_MAPPER.readTree(entry.getValue()).get("timestamp").asText();
                        } catch (final JsonProcessingException e) {
                            log.error("Failed to read timestamp for event", e);
                        }
                        return "";
                    }))
                    .filter(entry -> (!entry.getKey().equals(JournalEventType.BACKPACKCHANGE) && !entry.getKey().equals(JournalEventType.BACKPACK)) || backpackAfterShiplocker(messages, entry))
                    .map(Map.Entry::getValue)
                    .forEach(message -> Platform.runLater(() -> MessageHandler.handleMessage(message, file)));

            horizonsMaterialMessages.forEach(message -> Platform.runLater(() -> MessageHandler.handleMessage(message, file)));

        } catch (final JsonProcessingException e) {
            log.error("Read error", e);
        } catch (final IOException e) {
            log.error("Error processing journal", e);
        }
        Platform.runLater(() -> EventService.publish(new JournalInitEvent(true)));
    }

    private static boolean backpackAfterShiplocker(final Map<JournalEventType, String> messages, final Map.Entry<JournalEventType, String> entry) {
        try {
            return ZonedDateTime.parse(OBJECT_MAPPER.readTree(entry.getValue()).get("timestamp").asText()).isAfter(ZonedDateTime.parse(OBJECT_MAPPER.readTree(messages.get(JournalEventType.SHIPLOCKER)).get("timestamp").asText()));
        } catch (final JsonProcessingException e) {
            return false;
        }
    }

    @SuppressWarnings("java:S2674")
    public static synchronized void processJournal(final File file) {
        try (final CountingInputStream is = new CountingInputStream(Files.newInputStream(Paths.get(file.toURI()), StandardOpenOption.READ))) {
            if (file.length() >= position) {
                // Skip over already processed bytes.
                is.skip(position);
            }
            final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            final BufferedReader lineReader = new BufferedReader(reader);

            // Process all lines.
            String line;
            while ((line = lineReader.readLine()) != null) {
                final String finalLine = line;
                //try to read line as json, if exception occurs we get JsonProcessingException and can try to read again later
                OBJECT_MAPPER.readTree(finalLine);
                position = is.getCount();
                Platform.runLater(() -> MessageHandler.handleMessage(finalLine, file));
            }
        } catch (final JsonProcessingException e) {
            log.error("Read error", e);
        } catch (final IOException e) {
            log.error("Error processing journal", e);
        }
    }

    public static synchronized void processShipLockerBackpackFleetCarrier(final File file, final JournalEventType journalEventType) {
        Platform.runLater(() -> MessageHandler.handleMessage(file, journalEventType));
    }
}


