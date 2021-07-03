package nl.jixxed.eliteodysseymaterials.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.jixxed.eliteodysseymaterials.domain.ApplicationState;
import nl.jixxed.eliteodysseymaterials.enums.Engineer;
import nl.jixxed.eliteodysseymaterials.enums.EngineerState;
import nl.jixxed.eliteodysseymaterials.enums.JournalEventType;
import nl.jixxed.eliteodysseymaterials.enums.StoragePool;
import nl.jixxed.eliteodysseymaterials.service.event.EngineerEvent;
import nl.jixxed.eliteodysseymaterials.service.event.EventService;
import nl.jixxed.eliteodysseymaterials.service.event.JournalProcessedEvent;
import nl.jixxed.eliteodysseymaterials.service.event.StorageEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class FileProcessor {

    private static int lineNumber = 0;
    private static final ApplicationState APPLICATION_STATE = ApplicationState.getInstance();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AssetParser ASSET_PARSER = new AssetParser();
    private static final DataParser DATA_PARSER = new DataParser();
    private static final GoodParser GOOD_PARSER = new GoodParser();

    public static void resetAndProcessJournal(final File file) {
        lineNumber = 0;
        processJournal(file);
    }

    public static JsonNode processJournal(final File file) {
        JsonNode jsonNode = null;
        try (final Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
            int cursor = 0;
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                cursor++;
                //if multiple lines with the same timestamp are written, the lines can be blank until all lines are written.
//                System.out.println(line.length() + ": " + line);
                if (line.isBlank()) {
                    System.out.println("BLANK LINE");
                    break;
                }
                if (cursor > lineNumber) {
                    lineNumber++;
                    jsonNode = processJournalMessage(line, file);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return jsonNode;
    }

    public static JsonNode processShipLockerBackPack(final File file) {
        JsonNode jsonNode = null;
        try {
            final String shipLocker = Files.readString(file.toPath());
            System.out.println(shipLocker);
            jsonNode = processJournalMessage(shipLocker, file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return jsonNode;

    }


    private static JsonNode processJournalMessage(final String message, final File file) {
        JsonNode jsonNode = null;
        try {
            jsonNode = OBJECT_MAPPER.readTree(message);
            if (jsonNode.get("event") != null) {
                System.out.println("event: " + jsonNode.get("event").asText());
                final JournalEventType journalEventType = JournalEventType.forName(jsonNode.get("event").asText());
                switch (journalEventType) {
                    case ENGINEERPROGRESS -> processEngineerProgressMessage(jsonNode);
                    case EMBARK -> APPLICATION_STATE.resetBackPackCounts();
                    case SHIPLOCKER -> processShipLockerMaterialsMessage(jsonNode, StoragePool.SHIPLOCKER);
                    case BACKPACK -> processShipLockerMaterialsMessage(jsonNode, StoragePool.BACKPACK);
                }
                if (!JournalEventType.UNKNOWN.equals(journalEventType)) {
                    EventService.publish(new JournalProcessedEvent(jsonNode.get("timestamp").asText(), journalEventType, file));
                }
            } else {
                System.out.println("EVENT NULL: " + jsonNode.toPrettyString());
            }
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonNode;
    }

    private static void processEngineerProgressMessage(final JsonNode journalMessage) {
        journalMessage.get("Engineers").elements().forEachRemaining(item ->
        {
            final String engineer = item.get("Engineer").asText();
            switch (engineer) {
                case "Domino Green" -> APPLICATION_STATE.setEngineerState(Engineer.DOMINO_GREEN, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Hero Ferrari" -> APPLICATION_STATE.setEngineerState(Engineer.HERO_FERRARI, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Jude Navarro" -> APPLICATION_STATE.setEngineerState(Engineer.JUDE_NAVARRO, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Kit Fowler" -> APPLICATION_STATE.setEngineerState(Engineer.KIT_FOWLER, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Oden Geiger" -> APPLICATION_STATE.setEngineerState(Engineer.ODEN_GEIGER, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Terra Velasquez" -> APPLICATION_STATE.setEngineerState(Engineer.TERRA_VELASQUEZ, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Uma Laszlo" -> APPLICATION_STATE.setEngineerState(Engineer.UMA_LASZLO, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Wellington Beck" -> APPLICATION_STATE.setEngineerState(Engineer.WELLINGTON_BECK, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                case "Yarden Bond" -> APPLICATION_STATE.setEngineerState(Engineer.YARDEN_BOND, EngineerState.valueOf(item.get("Progress").asText().toUpperCase()));
                default -> {
                }
            }
            EventService.publish(new EngineerEvent());
        });
    }


    private static void processShipLockerMaterialsMessage(final JsonNode journalMessage, final StoragePool storagePool) {
        if (journalMessage.get("Items") == null || journalMessage.get("Components") == null || journalMessage.get("Data") == null) {
            return;
        }
        switch (storagePool) {
            case SHIPLOCKER -> APPLICATION_STATE.resetShipLockerCounts();
            case BACKPACK -> APPLICATION_STATE.resetBackPackCounts();
        }
        ASSET_PARSER.parse(journalMessage.get("Components").elements(), storagePool, APPLICATION_STATE.getAssets(), null);
        GOOD_PARSER.parse(journalMessage.get("Items").elements(), storagePool, APPLICATION_STATE.getGoods(), APPLICATION_STATE.getUnknownGoods());
        DATA_PARSER.parse(journalMessage.get("Data").elements(), storagePool, APPLICATION_STATE.getData(), APPLICATION_STATE.getUnknownData());
        EventService.publish(new StorageEvent(storagePool));
    }


}
