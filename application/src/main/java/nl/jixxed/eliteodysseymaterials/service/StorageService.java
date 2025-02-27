package nl.jixxed.eliteodysseymaterials.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.jixxed.eliteodysseymaterials.domain.AnyRelevantStorage;
import nl.jixxed.eliteodysseymaterials.domain.Storage;
import nl.jixxed.eliteodysseymaterials.enums.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageService {
    @Getter
    private static final Map<Raw, Integer> raw = new EnumMap<>(Raw.class);
    @Getter
    private static final Map<Encoded, Integer> encoded = new EnumMap<>(Encoded.class);
    @Getter
    private static final Map<Manufactured, Integer> manufactured = new EnumMap<>(Manufactured.class);
    @Getter
    private static final Map<Commodity, Integer> commodities = new EnumMap<>(Commodity.class);
    @Getter
    private static final Map<Good, Storage> goods = new EnumMap<>(Good.class);
    @Getter
    private static final Map<Asset, Storage> assets = new EnumMap<>(Asset.class);
    @Getter
    private static final Map<Data, Storage> data = new EnumMap<>(Data.class);
    @Getter
    private static final Map<Consumable, Storage> consumables = new EnumMap<>(Consumable.class);

    static {
        initCounts();
    }

    public static Map<OdysseyMaterial, Storage> getMaterials(final OdysseyStorageType storageType) {
        return (Map<OdysseyMaterial, Storage>) switch (storageType) {
            case GOOD -> goods;
            case DATA -> data;
            case ASSET -> assets;
            case TRADE ->
                    Map.of(TradeOdysseyMaterial.ANY_RELEVANT, new AnyRelevantStorage(), TradeOdysseyMaterial.NOTHING, new Storage(0, 0, 0));
            case CONSUMABLE -> Collections.emptyMap();
            case OTHER -> consumables;
        };
    }

    public static Storage getMaterialStorage(final OdysseyMaterial odysseyMaterial) {
        if (odysseyMaterial instanceof Good) {
            return goods.get(odysseyMaterial);
        } else if (odysseyMaterial instanceof Asset) {
            return assets.get(odysseyMaterial);
        } else if (odysseyMaterial instanceof Data) {
            return data.get(odysseyMaterial);
        }
        throw new IllegalArgumentException("Unknown material type");
    }

    public static void addMaterial(final HorizonsMaterial material, final Integer amount) {
        if (material instanceof Raw rawMaterial) {
            raw.put(rawMaterial, raw.get(material) + amount);
        } else if (material instanceof Encoded encodedMaterial) {
            encoded.put(encodedMaterial, encoded.get(material) + amount);
        } else if (material instanceof Manufactured manufacturedMaterial) {
            manufactured.put(manufacturedMaterial, manufactured.get(material) + amount);
        } else if (material instanceof Commodity commodity) {
            commodities.put(commodity, commodities.get(material) + amount);
        }
    }

    public static Integer getMaterialCount(final HorizonsMaterial material) {
        if (material instanceof Raw) {
            return raw.get(material);
        } else if (material instanceof Encoded) {
            return encoded.get(material);
        } else if (material instanceof Manufactured) {
            return manufactured.get(material);
        } else if (material instanceof Commodity) {
            return commodities.get(material);
        }
        throw new IllegalArgumentException("Unknown material type");
    }

    public static void resetShipLockerCounts() {
        getAssets().values().forEach(value -> value.setValue(0, StoragePool.SHIPLOCKER));
        getData().values().forEach(value -> value.setValue(0, StoragePool.SHIPLOCKER));
        getGoods().values().forEach(value -> value.setValue(0, StoragePool.SHIPLOCKER));
    }

    public static void resetFleetCarrierCounts() {
        getAssets().values().forEach(value -> value.setValue(0, StoragePool.FLEETCARRIER));
        getData().values().forEach(value -> value.setValue(0, StoragePool.FLEETCARRIER));
        getGoods().values().forEach(value -> value.setValue(0, StoragePool.FLEETCARRIER));
    }

    public static void resetBackPackCounts() {
        getAssets().values().forEach(value -> value.setValue(0, StoragePool.BACKPACK));
        getData().values().forEach(value -> value.setValue(0, StoragePool.BACKPACK));
        getGoods().values().forEach(value -> value.setValue(0, StoragePool.BACKPACK));
    }

    public static void resetHorizonsMaterialCounts() {
        Arrays.stream(Raw.values()).forEach(material ->
                getRaw().put(material, 0)
        );
        Arrays.stream(Encoded.values()).forEach(material ->
                getEncoded().put(material, 0)
        );
        Arrays.stream(Manufactured.values()).forEach(material ->
                getManufactured().put(material, 0)
        );
        Arrays.stream(Commodity.values()).forEach(material ->
                getCommodities().put(material, 0)
        );
    }

    private static void initCounts() {
        Arrays.stream(Asset.values()).forEach(material ->
                getAssets().put(material, new Storage())
        );
        Arrays.stream(Data.values()).forEach(material ->
                getData().put(material, new Storage())
        );
        Arrays.stream(Good.values()).forEach(material ->
                getGoods().put(material, new Storage())
        );
        Arrays.stream(Raw.values()).forEach(material ->
                getRaw().put(material, 0)
        );
        Arrays.stream(Encoded.values()).forEach(material ->
                getEncoded().put(material, 0)
        );
        Arrays.stream(Manufactured.values()).forEach(material ->
                getManufactured().put(material, 0)
        );
        Arrays.stream(Commodity.values()).forEach(material ->
                getCommodities().put(material, 0)
        );

    }

    public static Integer getStorageTotal(final OdysseyStorageType storageType, final StoragePool... storagePools) {
        return Arrays.stream(storagePools)
                .map(storagePool -> getMaterials(storageType).values().stream()
                        .map(storage -> storage.getValue(storagePool))
                        .mapToInt(Integer::intValue)
                        .sum())
                .mapToInt(Integer::intValue)
                .sum();
    }
}
