package nl.jixxed.eliteodysseymaterials.service;

import com.sun.jna.NativeLibrary;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.jixxed.eliteodysseymaterials.constants.PreferenceConstants;
import nl.jixxed.eliteodysseymaterials.enums.ApplicationLocale;
import nl.jixxed.eliteodysseymaterials.service.ar.OcrConstants;
import nl.jixxed.eliteodysseymaterials.service.event.ARLocaleChangeEvent;
import nl.jixxed.eliteodysseymaterials.service.event.EventService;
import nl.jixxed.eliteodysseymaterials.service.event.TerminateApplicationEvent;
import nl.jixxed.tess4j.ITesseract;
import nl.jixxed.tess4j.Tesseract1;
import nl.jixxed.tess4j.TesseractException;
import nl.jixxed.tess4j.util.LoadLibs;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class OCRService {
    private static final ITesseract instance;  // JNA Interface Mapping
    private static final String TESS_DATA_PATH = "tess.data.path";
    private static final String LEPT_DATA_PATH = "lept.data.path";

    static {

        System.setProperty(TESS_DATA_PATH, OcrConstants.TESS4J_DIR);
        System.setProperty(LEPT_DATA_PATH, OcrConstants.TESS4J_DIR);
        instance = new Tesseract1();
        instance.setDatapath(Path.of(OcrConstants.TESS4J_DIR, "tessdata").toString());
        instance.setLanguage(ApplicationLocale.valueOf(PreferencesService.getPreference(PreferenceConstants.AR_LOCALE, "ENGLISH")).getIso6392B());

        instance.setVariable("tessedit_char_whitelist", LocaleService.getDataCharacterForCurrentARLocale());
        EventService.addStaticListener(ARLocaleChangeEvent.class, arLocaleChangeEvent -> {
                    instance.setLanguage(arLocaleChangeEvent.getLocale().getIso6392B());
                    instance.setVariable("tessedit_char_whitelist", LocaleService.getDataCharacterForCurrentARLocale());
                }
        );
        EventService.addStaticListener(TerminateApplicationEvent.class, event -> {
                    NativeLibrary.getInstance(LoadLibs.getTesseractLibName()).dispose();
                    NativeLibrary.getInstance(nl.jixxed.lept4j.util.LoadLibs.getLeptonicaLibName()).dispose();
                }
        );
    }

    static synchronized String imageToString(final BufferedImage image) throws TesseractException {
        return instance.doOCR(image);
    }
}
