package nl.jixxed.eliteodysseymaterials.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ImportResult {
    private final ResultType resultType;
    private String message;

    public enum ResultType {
        SUCCESS_WISHLIST, ERROR_WISHLIST, SUCCESS_LOADOUT, ERROR_LOADOUT, UNKNOWN_TYPE, OTHER_ERROR, CAPI_OAUTH_TOKEN
    }
}
