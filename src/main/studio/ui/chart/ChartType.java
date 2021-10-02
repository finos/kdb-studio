package studio.ui.chart;

public enum ChartType {

    LINE("line", true, false),
    SHAPE("shape", false, true),
    LINE_SHAPE("line and shape", true, true),
    BAR("bar", false, false);


    private String title;
    private boolean line;
    private boolean shape;


    ChartType(String title, boolean line, boolean shape) {
        this.title = title;
        this.line = line;
        this.shape = shape;
    }

    public boolean hasLine() {
        return line;
    }

    public boolean hasShape() {
        return shape;
    }

    @Override
    public String toString() {
        return title;
    }
}
