package studio.kdb;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class KFormatContext {

    private final static NumberFormat RAW_FORMAT = new DecimalFormat("#.#######");
    private final static NumberFormat COMMA_FORMAT = new DecimalFormat("#,###.#######");

    private boolean showType;
    private boolean showThousandsComma;

    public final static KFormatContext DEFAULT = new KFormatContext();
    public final static KFormatContext NO_TYPE = new KFormatContext(false, false);

    public KFormatContext(boolean showType, boolean showThousandsComma) {
        this.showType = showType;
        this.showThousandsComma = showThousandsComma;
    }

    public KFormatContext(KFormatContext formatContext) {
        this(formatContext.showType, formatContext.showThousandsComma);
    }

    public KFormatContext() {
        this(true, false);
    }

    public NumberFormat getNumberFormat() {
        return showThousandsComma ? COMMA_FORMAT : RAW_FORMAT;
    }

    public boolean showType() {
        return showType;
    }

    public KFormatContext setShowType(boolean showType) {
        this.showType = showType;
        return this;
    }

    public boolean showThousandsComma() {
        return showThousandsComma;
    }

    public KFormatContext setShowThousandsComma(boolean showThousandsComma) {
        this.showThousandsComma = showThousandsComma;
        return this;
    }
}
