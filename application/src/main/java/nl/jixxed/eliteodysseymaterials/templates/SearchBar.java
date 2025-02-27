package nl.jixxed.eliteodysseymaterials.templates;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.extern.slf4j.Slf4j;
import nl.jixxed.eliteodysseymaterials.constants.PreferenceConstants;
import nl.jixxed.eliteodysseymaterials.enums.Expansion;
import nl.jixxed.eliteodysseymaterials.enums.FontSize;
import nl.jixxed.eliteodysseymaterials.enums.OdysseyTabs;
import nl.jixxed.eliteodysseymaterials.service.PreferencesService;
import nl.jixxed.eliteodysseymaterials.service.event.*;

@Slf4j
class SearchBar extends HBox {

    private static final String FX_FONT_SIZE_DPX = "-fx-font-size: %dpx";
    private Button button;
    private MaterialSearchBar materialSearchBar;
    private TradeSearchBar tradeSearchBar;

    SearchBar() {
        initComponents();
        initEventHandling();
    }

    private void initComponents() {
        this.getStyleClass().add("root");
        initMenuButton();
        this.materialSearchBar = new MaterialSearchBar();
        this.tradeSearchBar = new TradeSearchBar();

        applyFontSizingHack();

        HBox.setHgrow(this.materialSearchBar, Priority.ALWAYS);
        HBox.setHgrow(this.tradeSearchBar, Priority.ALWAYS);
        this.getChildren().addAll(this.button, this.materialSearchBar);
    }

    private void applyFontSizingHack() {
        //hack for component resizing on other fontsizes
        final Integer fontSize = FontSize.valueOf(PreferencesService.getPreference(PreferenceConstants.TEXTSIZE, "NORMAL")).getSize();
        final String fontStyle = String.format(FX_FONT_SIZE_DPX, fontSize);
        this.styleProperty().set(fontStyle);
        this.button.styleProperty().set(fontStyle);
    }


    private void initMenuButton() {
        this.button = new Button();
        this.button.setText(isRecipeBarVisible() ? "<" : ">");
        this.button.getStyleClass().addAll("root", "menubutton");
        this.button.setOnAction(event -> {
            this.button.setText(isRecipeBarVisible() ? ">" : "<");
            EventService.publish(new MenuButtonClickedEvent(Expansion.ODYSSEY));
        });
    }

    private void initEventHandling() {
        EventService.addListener(this, BlueprintClickEvent.class, blueprintClickEvent -> this.button.setText("<"));
        //hack for component resizing on other fontsizes
        EventService.addListener(this, AfterFontSizeSetEvent.class, fontSizeEvent -> {
            final String fontStyle = String.format(FX_FONT_SIZE_DPX, fontSizeEvent.getFontSize());
            this.styleProperty().set(fontStyle);
            this.button.styleProperty().set(fontStyle);
        });
        EventService.addListener(this, OdysseyTabSelectedEvent.class, event -> {
            if (OdysseyTabs.TRADE.equals(event.getSelectedTab())) {
                if (this.getChildren().contains(this.materialSearchBar)) {
                    this.getChildren().remove(this.materialSearchBar);
                    this.getChildren().add(this.tradeSearchBar);
                }
            } else {
                if (this.getChildren().contains(this.tradeSearchBar)) {
                    this.getChildren().remove(this.tradeSearchBar);
                    this.getChildren().add(this.materialSearchBar);
                }
            }
        });
    }

    private boolean isRecipeBarVisible() {
        return PreferencesService.getPreference(PreferenceConstants.RECIPES_VISIBLE, Boolean.TRUE);
    }
}
