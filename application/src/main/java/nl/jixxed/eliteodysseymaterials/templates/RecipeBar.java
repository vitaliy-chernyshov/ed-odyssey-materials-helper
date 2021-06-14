package nl.jixxed.eliteodysseymaterials.templates;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import nl.jixxed.eliteodysseymaterials.RecipeConstants;
import nl.jixxed.eliteodysseymaterials.domain.*;
import nl.jixxed.eliteodysseymaterials.enums.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RecipeBar extends Accordion {
    private final List<Ingredient> ingredients = new ArrayList<>();
    private static final ApplicationState APPLICATION_STATE = ApplicationState.getInstance();
    private final List<EngineerTitledPane> engineerTitledPanes = new ArrayList<>();
    private final List<EngineerModuleLabel> moduleEngineerLabels = new ArrayList<>();
    private final Settings settings;
    private final Legend legend = new Legend();

    public RecipeBar(final Application application) {
        this.settings = new Settings(application);
        final List<TitledPane> titledPanes = RecipeConstants.RECIPES.entrySet().stream()
                .map(this::createCategoryTitledPane)
                .sorted(Comparator.comparing(Labeled::getText))
                .collect(Collectors.toList());
        final TitledPane about = new TitledPane("About", this.settings);
        titledPanes.add(about);
        final TitledPane legend = new TitledPane("Legend", this.legend);
        titledPanes.add(legend);
        this.getPanes().addAll(titledPanes.toArray(new TitledPane[0]));
        this.setExpandedPane(about);
    }


    private TitledPane createCategoryTitledPane(final Map.Entry<String, Map<String, ? extends Recipe>> recipesEntry) {
        final Accordion recipesAccordion = new Accordion(recipesEntry.getValue().entrySet().stream()
                .map(this::createRecipeTitledPane)
                .sorted(Comparator.comparing(Labeled::getText))
                .toArray(TitledPane[]::new));
        recipesAccordion.setPrefHeight(500);
        final TitledPane categoryTitledPane = new TitledPane(recipesEntry.getKey(), recipesAccordion);
        categoryTitledPane.getStyleClass().add("category-title-pane");
        return categoryTitledPane;
    }

    private TitledPane createRecipeTitledPane(final Map.Entry<String, ? extends Recipe> recipe) {
        final VBox content = new VBox();
        final List<Ingredient> ingredients = new ArrayList<>();
        ingredients.addAll(getRecipeIngredients(recipe, Good.class, StorageType.GOOD, APPLICATION_STATE.getGoods()));
        ingredients.addAll(getRecipeIngredients(recipe, Asset.class, StorageType.ASSET, APPLICATION_STATE.getAssets()));
        ingredients.addAll(getRecipeIngredients(recipe, Data.class, StorageType.DATA, APPLICATION_STATE.getData()));

        if (recipe.getValue() instanceof EngineerRecipe) {
            ingredients.addAll(((EngineerRecipe) recipe.getValue()).getOther().stream()
                    .map(other -> {
                                final Ingredient newIngredient = new Ingredient(other);
                                this.ingredients.add(newIngredient);
                                return newIngredient;
                            }
                    ).sorted(Comparator.comparing(Ingredient::getName))
                    .collect(Collectors.toList()));
        }
        try {
            if (!(recipe.getValue() instanceof EngineerRecipe) || ingredients.stream().noneMatch(ingredient -> StorageType.OTHER.equals(ingredient.getType()))) {
                content.getChildren().add(new FXMLLoader(getClass().getResource("IngredientHeader.fxml")).load());
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        content.getChildren().addAll(ingredients);
        if (recipe.getValue() instanceof ModuleRecipe) {
            final Label engineerLabelHeader = new Label("Engineers crafting this blueprint:");
            engineerLabelHeader.getStyleClass().add("engineerLabelHeader");
            content.getChildren().addAll(engineerLabelHeader);

            final Label[] engineerLabels = ((ModuleRecipe) recipe.getValue()).getEngineers().stream()
                    .sorted(Comparator.comparing(Engineer::friendlyName))
                    .map(engineer -> {
                        final EngineerModuleLabel label = new EngineerModuleLabel(engineer);
                        this.moduleEngineerLabels.add(label);
                        return label;
                    }).toArray(Label[]::new);

            final FlowPane flowPane = new FlowPane(engineerLabels);
            flowPane.getStyleClass().add("engineerFlow");
            content.getChildren().addAll(flowPane);
        }
        final TitledPane recipeTitledPane = createTitledPane(recipe, content);
        recipeTitledPane.getStyleClass().add("blueprint-title-pane");
        if (recipe.getValue() instanceof EngineerRecipe && ((EngineerRecipe) recipe.getValue()).isCompleted()) {
            recipeTitledPane.getStyleClass().add("completed");
        } else {
            recipeTitledPane.getStyleClass().add("regular");
        }
        recipeTitledPane.setPrefHeight(150);
        return recipeTitledPane;
    }

    private List<Ingredient> getRecipeIngredients(final Map.Entry<String, ? extends Recipe> recipe, final Class<? extends Material> materialClass, final StorageType storageType, final Map<? extends Material, Storage> materialMap) {
        return recipe.getValue().getMaterialCollection(materialClass).entrySet().stream()
                .map(material ->
                        {
                            final Ingredient newIngredient = new Ingredient(storageType, material.getKey(), material.getValue().toString(), materialMap.get(material.getKey()).getTotalValue().toString());
                            this.ingredients.add(newIngredient);
                            return newIngredient;
                        }
                ).sorted(Comparator.comparing(Ingredient::getName))
                .collect(Collectors.toList());
    }

    private TitledPane createTitledPane(final Map.Entry<String, ? extends Recipe> recipe, final VBox content) {
        final TitledPane recipeTitledPane;
        if (recipe.getValue() instanceof EngineerRecipe) {
            recipeTitledPane = new EngineerTitledPane(recipe.getKey(), content, (EngineerRecipe) recipe.getValue());
            this.engineerTitledPanes.add((EngineerTitledPane) recipeTitledPane);
        } else {
            recipeTitledPane = new TitledPane(recipe.getKey(), content);
        }
        return recipeTitledPane;
    }


    public void updateIngredients() {
        this.ingredients.forEach(Ingredient::update);
    }

    public void updateIngredientsValues() {
        this.ingredients.forEach(ingredient -> {
            switch (ingredient.getType()) {
                case ASSET -> ingredient.setAmountAvailable(APPLICATION_STATE.getAssets().get(Asset.forName(ingredient.getCode())).getTotalValue());
                case GOOD -> ingredient.setAmountAvailable(APPLICATION_STATE.getGoods().get(Good.forName(ingredient.getCode())).getTotalValue());
                case DATA -> ingredient.setAmountAvailable(APPLICATION_STATE.getData().get(Data.forName(ingredient.getCode())).getTotalValue());
                case OTHER -> {
                }
            }
        });
    }

    public void updateEngineerStyles() {
        this.engineerTitledPanes.forEach(titledPane -> {
            if (titledPane.getEngineerRecipe().isCompleted() && !titledPane.getStyleClass().contains("completed")) {
                titledPane.getStyleClass().add("completed");
            }
        });
        this.moduleEngineerLabels.forEach(label -> label.updateStyle(APPLICATION_STATE.isEngineerUnlocked(label.getEngineer())));
    }
}