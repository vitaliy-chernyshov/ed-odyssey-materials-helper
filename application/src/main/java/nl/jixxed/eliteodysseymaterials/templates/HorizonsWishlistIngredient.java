package nl.jixxed.eliteodysseymaterials.templates;

import lombok.EqualsAndHashCode;
import nl.jixxed.eliteodysseymaterials.enums.HorizonsMaterial;
import nl.jixxed.eliteodysseymaterials.enums.HorizonsStorageType;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class HorizonsWishlistIngredient extends HorizonsMaterialIngredient {

    private static final String INGREDIENT_FILLED_CLASS = "ingredient-filled";
    private static final String INGREDIENT_UNFILLED_CLASS = "ingredient-unfilled";

    HorizonsWishlistIngredient(final HorizonsStorageType storageType, final HorizonsMaterial horizonsMaterial, final Integer amountRequired, final Integer amountAvailable) {
        super(storageType, horizonsMaterial, amountRequired, amountAvailable);
        initComponents();
    }

    @SuppressWarnings("java:S2177")
    private void initComponents() {
        this.getStyleClass().add("wishlist-ingredient");
    }

    @Override
    protected void update() {
        if (this.getRightAmount() >= Integer.parseInt(this.getLeftAmountLabel().getText())) {
            this.getRightAmountLabel().setText(this.getRightAmount().toString());
            this.getStyleClass().removeAll(INGREDIENT_FILLED_CLASS, INGREDIENT_UNFILLED_CLASS);
            this.getStyleClass().addAll(INGREDIENT_FILLED_CLASS);
        } else {
            this.getRightAmountLabel().setText(this.getRightAmount().toString());
            this.getStyleClass().removeAll(INGREDIENT_FILLED_CLASS, INGREDIENT_UNFILLED_CLASS);
            this.getStyleClass().addAll(INGREDIENT_UNFILLED_CLASS);
        }
    }

    void highlight(final boolean enable, final Integer amountRequiredForRecipe) {
        if (enable) {
            this.getStyleClass().add("wishlist-highlight");
            this.getLeftAmountLabel().setText(amountRequiredForRecipe.toString());
        } else {
            this.getStyleClass().removeAll("wishlist-highlight");
            this.getLeftAmountLabel().setText(this.getLeftAmount().toString());
        }
        update();
    }

    void lowlight(final boolean enable) {
        if (enable) {
            this.getStyleClass().add("wishlist-lowlight");
        } else {
            this.getStyleClass().removeAll("wishlist-lowlight");
        }
    }
}
