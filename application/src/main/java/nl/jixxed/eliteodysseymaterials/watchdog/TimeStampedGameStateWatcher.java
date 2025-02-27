package nl.jixxed.eliteodysseymaterials.watchdog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.jixxed.eliteodysseymaterials.enums.StoragePool;
import nl.jixxed.eliteodysseymaterials.service.event.BackpackEvent;
import nl.jixxed.eliteodysseymaterials.service.event.Event;
import nl.jixxed.eliteodysseymaterials.service.event.EventService;
import nl.jixxed.eliteodysseymaterials.service.event.ShipLockerEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
public class TimeStampedGameStateWatcher {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final GameStateWatcher gameStateWatcher = new GameStateWatcher();
    private final AtomicReference<String> timeStamp = new AtomicReference<>("");
    private final StoragePool storagePool;
    private final Consumer<File> fileProcessor;
    private File file = null;
    private Consumer<? extends Event> eventConsumer;

    public TimeStampedGameStateWatcher(final File folder, final Consumer<File> fileProcessor, final String filename, final StoragePool storagePool) {
        this.storagePool = storagePool;
        this.fileProcessor = fileProcessor;
        this.gameStateWatcher.watch(folder, this::process, filename, storagePool);
        if (storagePool == StoragePool.SHIPLOCKER) {
            final Consumer<ShipLockerEvent> consumer = (ShipLockerEvent shipLockerEvent) -> {
                this.timeStamp.set(shipLockerEvent.getTimestamp());
                this.process(this.file);
            };
            this.eventConsumer = consumer;
            EventService.addListener(this, ShipLockerEvent.class, consumer);
        } else if (storagePool == StoragePool.BACKPACK) {
            final Consumer<BackpackEvent> consumer = (BackpackEvent backpackEvent) -> {
                this.timeStamp.set(backpackEvent.getTimestamp());
                this.process(this.file);
            };
            this.eventConsumer = consumer;
            EventService.addListener(this, BackpackEvent.class, consumer);
        }
    }

    public void stop() {
        this.gameStateWatcher.stop();
        EventService.removeListener(this.eventConsumer, this.storagePool == StoragePool.SHIPLOCKER ? ShipLockerEvent.class : BackpackEvent.class);
    }

    @SuppressWarnings("java:S1192")
    private synchronized void process(final File file) {
        this.file = file;
        if (this.file != null) {
            try {
                final String message = Files.readString(file.toPath());

                final JsonNode jsonNode = OBJECT_MAPPER.readTree(message);
                if (jsonNode.get("event") != null
                        && jsonNode.get("event").asText().equalsIgnoreCase(this.storagePool.name())
                        && jsonNode.get("timestamp") != null
                        && jsonNode.get("timestamp").asText().equals(this.timeStamp.get())) {
                    this.fileProcessor.accept(file);
                    this.file = null;
                    log.info("Process " + jsonNode.get("event").asText());
                }
            } catch (final IOException e) {
                log.error("Error processing journal message", e);
            }
        }
    }
}
