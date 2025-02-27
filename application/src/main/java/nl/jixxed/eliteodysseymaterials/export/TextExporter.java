package nl.jixxed.eliteodysseymaterials.export;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.jixxed.eliteodysseymaterials.enums.*;
import nl.jixxed.eliteodysseymaterials.service.LocaleService;
import nl.jixxed.eliteodysseymaterials.service.StorageService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextExporter {

    public static String createTextWishlist(final Map<Data, Integer> wishlistNeededDatas, final Map<Good, Integer> wishlistNeededGoods, final Map<Asset, Integer> wishlistNeededAssets) {
        final StringBuilder textBuilder = new StringBuilder();
        final Integer maxNameLengthData = wishlistNeededDatas.keySet().stream()
                .map(item -> LocaleService.getLocalizedStringForCurrentLocale(item.getLocalizationKey()).length())
                .max(Comparator.comparing(Integer::intValue)).orElse(0);
        final Integer maxNameLengthGood = wishlistNeededGoods.keySet().stream()
                .map(item -> LocaleService.getLocalizedStringForCurrentLocale(item.getLocalizationKey()).length())
                .max(Comparator.comparing(Integer::intValue)).orElse(0);
        final Integer maxNameLengthAsset = wishlistNeededAssets.keySet().stream()
                .map(item -> LocaleService.getLocalizedStringForCurrentLocale(item.getLocalizationKey()).length())
                .max(Comparator.comparing(Integer::intValue)).orElse(0);
        final Integer maxNameLength = Math.max(maxNameLengthData, Math.max(maxNameLengthGood, maxNameLengthAsset));
        final String materialColumnWidth = "%-" + (maxNameLength + 5) + "s";
        textBuilder.append(String.format(materialColumnWidth, "Material"));
        textBuilder.append(String.format("%12s", "Available"));
        textBuilder.append(String.format("%12s", "Required"));
        textBuilder.append(String.format("%12s", "Need"));
        textBuilder.append("\n\n");
        List.of(wishlistNeededDatas, wishlistNeededGoods, wishlistNeededAssets).forEach(wishlistNeededMaterials ->
                wishlistNeededMaterials.entrySet().stream()
                        .sorted(Comparator.comparing(item -> item.getKey().getStorageType()))
                        .forEach(item -> {
                            textBuilder.append(String.format(materialColumnWidth, LocaleService.getLocalizedStringForCurrentLocale(item.getKey().getLocalizationKey())));
                            final Integer total = switch (item.getKey().getStorageType()) {
                                case GOOD -> StorageService.getGoods().get(item.getKey()).getTotalValue();
                                case DATA -> StorageService.getData().get(item.getKey()).getTotalValue();
                                case ASSET -> StorageService.getAssets().get(item.getKey()).getTotalValue();
                                case TRADE, CONSUMABLE, OTHER -> 0;
                            };
                            textBuilder.append(String.format("%12s", total));
                            textBuilder.append(String.format("%12s", item.getValue()));
                            textBuilder.append(String.format("%12s", Math.max(0, item.getValue() - total)));
                            textBuilder.append("\n");
                        })
        );
        return textBuilder.toString();
    }

    public static String createTextWishlist(final Map<Raw, Integer> wishlistNeededRaw, final Map<Encoded, Integer> wishlistNeededEncoded, final Map<Manufactured, Integer> wishlistNeededManufactured, final Map<Commodity, Integer> wishlistNeededCommodity) {
        final StringBuilder textBuilder = new StringBuilder();
        final Integer maxNameLengthRaw = wishlistNeededRaw.keySet().stream()
                .map(item -> LocaleService.getLocalizedStringForCurrentLocale(item.getLocalizationKey()).length())
                .max(Comparator.comparing(Integer::intValue)).orElse(0);
        final Integer maxNameLengthEncoded = wishlistNeededEncoded.keySet().stream()
                .map(item -> LocaleService.getLocalizedStringForCurrentLocale(item.getLocalizationKey()).length())
                .max(Comparator.comparing(Integer::intValue)).orElse(0);
        final Integer maxNameLengthManufactured = wishlistNeededManufactured.keySet().stream()
                .map(item -> LocaleService.getLocalizedStringForCurrentLocale(item.getLocalizationKey()).length())
                .max(Comparator.comparing(Integer::intValue)).orElse(0);
        final Integer maxNameLengthCommodity = wishlistNeededCommodity.keySet().stream()
                .map(item -> LocaleService.getLocalizedStringForCurrentLocale(item.getLocalizationKey()).length())
                .max(Comparator.comparing(Integer::intValue)).orElse(0);
        final Integer maxNameLength = Math.max(maxNameLengthRaw, Math.max(maxNameLengthEncoded, Math.max(maxNameLengthManufactured, maxNameLengthCommodity)));
        final String materialColumnWidth = "%-" + (maxNameLength + 5) + "s";
        textBuilder.append(String.format(materialColumnWidth, "Material"));
        textBuilder.append(String.format("%12s", "Available"));
        textBuilder.append(String.format("%12s", "Required"));
        textBuilder.append(String.format("%12s", "Need"));
        textBuilder.append("\n\n");
        List.of(wishlistNeededRaw, wishlistNeededEncoded, wishlistNeededManufactured, wishlistNeededCommodity).forEach(wishlistNeededMaterials ->
                wishlistNeededMaterials.entrySet().stream()
                        .sorted(Comparator.comparing(item -> item.getKey().getStorageType()))
                        .forEach(item -> {
                            textBuilder.append(String.format(materialColumnWidth, LocaleService.getLocalizedStringForCurrentLocale(item.getKey().getLocalizationKey())));
                            final Integer total = switch (item.getKey().getStorageType()) {
                                case RAW -> StorageService.getRaw().get(item.getKey());
                                case ENCODED -> StorageService.getEncoded().get(item.getKey());
                                case MANUFACTURED -> StorageService.getManufactured().get(item.getKey());
                                case COMMODITY -> StorageService.getCommodities().get(item.getKey());
                                default -> 0;
                            };
                            textBuilder.append(String.format("%12s", total));
                            textBuilder.append(String.format("%12s", item.getValue()));
                            textBuilder.append(String.format("%12s", Math.max(0, item.getValue() - total)));
                            textBuilder.append("\n");
                        })
        );
        return textBuilder.toString();
    }
}
