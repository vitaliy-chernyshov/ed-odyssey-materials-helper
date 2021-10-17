module nl.jixxed.eliteodysseymaterials {
    requires jdk.crypto.ec;
    requires javafx.fxml;
    requires javafx.controls;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.reactivex.rxjava3;
    requires java.net.http;
    requires org.jfxtras.styles.jmetro;
    requires org.slf4j;
    requires static lombok;
    opens nl.jixxed.eliteodysseymaterials to javafx.graphics;
    opens nl.jixxed.eliteodysseymaterials.templates to javafx.fxml;
    opens nl.jixxed.eliteodysseymaterials.trade.message.outbound to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.trade.message.outbound.payload to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.trade.message.common to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.trade.message.inbound to com.fasterxml.jackson.databind;
    exports nl.jixxed.eliteodysseymaterials;
    exports nl.jixxed.eliteodysseymaterials.enums;
    exports nl.jixxed.eliteodysseymaterials.domain;
}