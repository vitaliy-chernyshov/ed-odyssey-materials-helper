package nl.jixxed.eliteodysseymaterials.templates;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import nl.jixxed.eliteodysseymaterials.builder.BoxBuilder;
import nl.jixxed.eliteodysseymaterials.builder.FlowPaneBuilder;
import nl.jixxed.eliteodysseymaterials.builder.LabelBuilder;
import nl.jixxed.eliteodysseymaterials.builder.ResizableImageViewBuilder;
import nl.jixxed.eliteodysseymaterials.domain.ApplicationState;
import nl.jixxed.eliteodysseymaterials.enums.*;
import nl.jixxed.eliteodysseymaterials.service.ImageService;
import nl.jixxed.eliteodysseymaterials.service.LocaleService;
import nl.jixxed.eliteodysseymaterials.service.NotificationService;
import nl.jixxed.eliteodysseymaterials.service.event.BlueprintClickEvent;
import nl.jixxed.eliteodysseymaterials.service.event.EventService;
import nl.jixxed.eliteodysseymaterials.service.event.LocationChangedEvent;
import nl.jixxed.eliteodysseymaterials.templates.destroyables.DestroyableResizableImageView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class EngineerCard extends VBox {
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    protected static final ApplicationState APPLICATION_STATE = ApplicationState.getInstance();
    static final Function<BlueprintName, HBox> RECIPE_TO_ENGINEER_BLUEPRINT_LABEL = recipeName -> BoxBuilder.builder()
            .withNodes(LabelBuilder.builder()
                            .withStyleClass("engineer-bullet")
                            .withNonLocalizedText("\u2022")
                            .withOnMouseClicked(event -> EventService.publish(new BlueprintClickEvent(recipeName)))
                            .build(),
                    LabelBuilder.builder()
                            .withStyleClass("engineer-blueprint")
                            .withText(LocaleService.getStringBinding(recipeName.getLocalizationKey()))
                            .withOnMouseClicked(event -> EventService.publish(new BlueprintClickEvent(recipeName)))
                            .build()).buildHBox();
    static final String ENGINEER_CATEGORY_STYLE_CLASS = "engineer-category";

    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    protected final Engineer engineer;

    protected DestroyableResizableImageView image;
    protected Label name;
    private Label engineerLocation;
    private Label engineerDistance;
    private DestroyableResizableImageView copyIcon;
    protected FlowPane location;
    Label unlockRequirementsTitle;
    List<HBox> unlockRequirementsLabels;
    Separator unlockSeparator;

    EngineerCard(final Engineer engineer) {
        this.engineer = engineer;
        initComponents();
        initEventHandling(engineer);
    }

    private void initEventHandling(final Engineer engineer) {
        EventService.addListener(this, LocationChangedEvent.class, locationChangedEvent -> this.engineerDistance.setText("(" + NUMBER_FORMAT.format(engineer.getDistance(locationChangedEvent.getCurrentStarSystem().getX(), locationChangedEvent.getCurrentStarSystem().getY(), locationChangedEvent.getCurrentStarSystem().getZ())) + "Ly)"));
    }

    private void initComponents() {
        this.image = getEngineerImageView();
        this.name = getEngineerName();
        this.location = getEngineerLocation();
        this.unlockRequirementsTitle = getUnlockRequirementsTitle();
        this.unlockRequirementsLabels = getUnlockRequirements();
        this.unlockSeparator = new Separator(Orientation.HORIZONTAL);
        this.getStyleClass().add("engineer-card");
    }

    List<HBox> getUnlockRequirements() {
        return this.engineer.getPrerequisites().stream()
                .map(prerequisite -> BoxBuilder.builder()
                        .withNodes(LabelBuilder.builder()
                                        .withStyleClass("engineer-bullet")
                                        .withNonLocalizedText(Boolean.TRUE.equals((prerequisite.isCompleted())) ? "\u2714" : "\u2022")
                                        .withOnMouseClicked(event -> EventService.publish(new BlueprintClickEvent(prerequisite.getBlueprintName())))
                                        .build(),
                                LabelBuilder.builder()
                                        .withStyleClass("engineer-prerequisite")
                                        .withText(LocaleService.getStringBinding(prerequisite.getLocalisationKey()))
                                        .withOnMouseClicked(event -> EventService.publish(new BlueprintClickEvent(prerequisite.getBlueprintName())))
                                        .build()).buildHBox())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Label getUnlockRequirementsTitle() {
        return LabelBuilder.builder()
                .withStyleClass(ENGINEER_CATEGORY_STYLE_CLASS)
                .withText(LocaleService.getStringBinding("tab.engineer.unlock.prerequisites"))
                .build();
    }


    private FlowPane getEngineerLocation() {
        this.engineerLocation = LabelBuilder.builder()
                .withStyleClass("engineer-location")
                .withNonLocalizedText(this.engineer.getSettlement().getSettlementName() + " | " + this.engineer.getStarSystem().getName())
                .build();

        this.engineerDistance = LabelBuilder.builder()
                .withStyleClass("engineer-distance")
                .withNonLocalizedText("(0Ly)")
                .build();

        this.copyIcon = ResizableImageViewBuilder.builder()
                .withStyleClass("engineer-copy-icon")
                .withImage("/images/other/copy.png")
                .build();

        return FlowPaneBuilder.builder().withStyleClass("engineer-location-line")
                .withOnMouseClicked(event -> {
                    copyLocationToClipboard();
                    NotificationService.showInformation(NotificationType.COPY, "Clipboard", "System name copied.");
                })
                .withNodes(this.engineerLocation, new StackPane(this.copyIcon), this.engineerDistance)
                .build();

    }

    private void copyLocationToClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(this.engineer.getStarSystem().getName());
        clipboard.setContent(content);
    }


    private Label getEngineerName() {
        return LabelBuilder.builder()
                .withStyleClass("engineer-name")
                .withText(LocaleService.getStringBinding(this.engineer.getLocalizationKey()))
                .withOnMouseClicked(event -> EventService.publish(new BlueprintClickEvent(this.engineer.isOdyssey() ? OdysseyBlueprintName.forEngineer(this.engineer) : HorizonsBlueprintName.forEngineer(this.engineer))))
                .build();
    }

    private DestroyableResizableImageView getEngineerImageView() {
        return ResizableImageViewBuilder.builder()
                .withStyleClass("engineer-image")
                .withPreserveRatio(true)
                .withImage(ImageService.getImage("/images/engineer/locked.png"))
                .build();

    }
}
