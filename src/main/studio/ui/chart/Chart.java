package studio.ui.chart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.*;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import studio.kdb.Config;
import studio.kdb.K;
import studio.kdb.KTableModel;
import studio.kdb.ToDouble;
import studio.ui.StudioOptionPane;
import studio.ui.Util;
import studio.utils.WindowsAppUserMode;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.*;


public class Chart implements ComponentListener {

    private static final Logger log = LogManager.getLogger();
    private static final Config config = Config.getInstance();

    private static final int CONFIG_UPDATE_DELAY = 1000;

    private Timer configUpdateTimer;

    private KTableModel table;
    private ChartPanel chartPanel = null;
    private JFrame frame;
    private JPanel contentPane;
    private ChartConfigPanel pnlConfig;

    private List<Integer> yIndex;

    private interface KBase2RegularTimePeriod {
        RegularTimePeriod convert(K.KBase value);
    }

    private final static Set<Class> domainKClass = new HashSet<>();
    private final static Set<Class> rangeKClass = new HashSet<>();
    private final static Map<Class, KBase2RegularTimePeriod> regularTimePeriodConverters = new HashMap<>();

    static {
        List<Class> classes = Arrays.asList(
            K.KIntVector.class,
            K.KDoubleVector.class,
            K.KFloatVector.class,
            K.KShortVector.class,
            K.KLongVector.class,

            //@TODO: it's better to show temporal types as doubles rather not shown them at all
            K.KDateVector.class,
            K.KTimeVector.class,
            K.KTimestampVector.class,
            K.KTimespanVector.class,
            K.KDatetimeVector.class,
            K.KMonthVector.class,
            K.KSecondVector.class,
            K.KMinuteVector.class);

        rangeKClass.addAll(classes);
        domainKClass.addAll(classes);

        regularTimePeriodConverters.put(K.KDateVector.class, v -> new Day(((K.KDate)v).toDate()));
        regularTimePeriodConverters.put(K.KTimeVector.class, v -> new Millisecond(((K.KTime)v).toTime()));
        regularTimePeriodConverters.put(K.KTimestampVector.class, v -> new Millisecond(((K.KTimestamp)v).toTimestamp()));
        regularTimePeriodConverters.put(K.KTimespanVector.class, v -> new Millisecond(((K.KTimespan)v).toTime()));
        regularTimePeriodConverters.put(K.KDatetimeVector.class, v -> new Millisecond(((K.KDatetime)v).toTimestamp()));
        regularTimePeriodConverters.put(K.KMonthVector.class, v -> new Month(((K.Month)v).toDate()));
        regularTimePeriodConverters.put(K.KSecondVector.class, v -> new Second(((K.Second)v).toDate()));
        regularTimePeriodConverters.put(K.KMinuteVector.class, v -> new Minute(((K.Minute)v).toDate()));
    }

    private static StandardChartTheme currentTheme = new StandardChartTheme("JFree");
    static {
        currentTheme.setXYBarPainter(new StandardXYBarPainter());
    }

    public Chart(KTableModel table) {
        this.table = table;
        initComponents();
    }

    private void initComponents() {
        List<String> names = new ArrayList<>();
        List<Integer> xIndex = new ArrayList<>();
        yIndex = new ArrayList<>();
        for (int index = 0; index<table.getColumnCount(); index++) {
            names.add(table.getColumnName(index));
            Class clazz = table.getColumnClass(index);
            if (domainKClass.contains(clazz)) xIndex.add(index);
            if (rangeKClass.contains(clazz)) yIndex.add(index);
        }

        if (xIndex.size() == 0 || yIndex.size() ==0) {
            log.info("Nothing to chart. Number of columns for x axes is {}. Number of columns for y axes is {}", xIndex.size(), yIndex.size());
            StudioOptionPane.showWarning(null, "It turns out that nothing is found to chart.", "Nothing to chart");
            return;
        }

        contentPane = new JPanel(new BorderLayout());
        pnlConfig = new ChartConfigPanel(this, names, xIndex, yIndex);
        contentPane.add(pnlConfig, BorderLayout.EAST);

        createPlot();

        configUpdateTimer = new Timer(CONFIG_UPDATE_DELAY, e -> saveFrameBounds());

        WindowsAppUserMode.setChartId();
        try {
            frame = new JFrame();
            updateTitle(null);
            frame.setContentPane(contentPane);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setIconImage(Util.CHART_BIG_ICON.getImage());

            frame.setBounds(config.getBounds(Config.CHART_BOUNDS));
            frame.addComponentListener(this);
            frame.setVisible(true);
            frame.requestFocus();
            frame.toFront();
        } finally {
            WindowsAppUserMode.setMainId();
        }
    }

    private void updateTitle(JFreeChart chart) {
        String title = "Studio for kdb+ [chart]";
        if (chart != null) {
            TextTitle chartTitle = chart.getTitle();
            if (chartTitle != null && chartTitle.isVisible()) {
                String text = chartTitle.getText();
                if (text != null && ! text.trim().equals("")) {
                    title = text.trim();
                }
            }
        }

        if (! title.equals(frame.getTitle())) {
            frame.setTitle(title);
        }
    }

    private void saveFrameBounds() {
        SwingUtilities.invokeLater( () -> {
            configUpdateTimer.stop();
            if (frame == null) return;

            config.setBounds(Config.CHART_BOUNDS, frame.getBounds());
        });
    }

    private void updateFrameBounds() {
        configUpdateTimer.restart();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        updateFrameBounds();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        updateFrameBounds();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        updateFrameBounds();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        updateFrameBounds();
    }

    void createPlot() {
        if (chartPanel !=null ) {
            contentPane.remove(chartPanel);
            chartPanel = null;
        }

        JFreeChart chart = createChart();
        if (chart != null) {
            chart.addChangeListener(e -> updateTitle(chart) );
            chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setMouseZoomable(true, true);
            contentPane.add(chartPanel, BorderLayout.CENTER);
        }

        contentPane.revalidate();
        contentPane.repaint();
    }

    private JFreeChart createChart() {
        NumberAxis yAxis = new NumberAxis("");
        yAxis.setAutoRangeIncludesZero(false);

        XYPlot plot = new XYPlot(null, null, yAxis, null);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        ValueAxis xAxis = null;
        int datasetIndex = 0;
        for (int index = 0; index<yIndex.size(); index++) {
            if (! pnlConfig.isSeriesEnables(index)) continue;

            IntervalXYDataset dataset = getDateset(yIndex.get(index));
            boolean timeSeries = dataset instanceof TimeSeriesCollection;

            if (xAxis == null) {
                if (timeSeries) {
                    xAxis = new DateAxis("");
                    xAxis.setLowerMargin(0.02);  // reduce the default margins
                    xAxis.setUpperMargin(0.02);
                } else {
                    NumberAxis axis = new NumberAxis("");
                    axis.setAutoRangeIncludesZero(false);
                    xAxis = axis;
                }
                plot.setDomainAxis(xAxis);
            }

            XYToolTipGenerator toolTipGenerator = timeSeries ? StandardXYToolTipGenerator.getTimeSeriesInstance() :
                                                                new StandardXYToolTipGenerator();

            XYItemRenderer renderer;

            ChartType chartType = pnlConfig.getChartType(index);
            if (chartType == ChartType.BAR) {
                renderer = new BarRenderer();
            } else {
                renderer = new XYLineAndShapeRenderer(chartType.hasLine(), chartType.hasShape());
            }
            renderer.setDefaultToolTipGenerator(toolTipGenerator);
            renderer.setSeriesPaint(0, pnlConfig.getColor(index));
            renderer.setSeriesShape(0, pnlConfig.getShape(index));
            renderer.setSeriesStroke(0, pnlConfig.getStroke(index));
            ((AbstractRenderer)renderer).setAutoPopulateSeriesPaint(false);
            ((AbstractRenderer)renderer).setAutoPopulateSeriesShape(false);
            ((AbstractRenderer)renderer).setAutoPopulateSeriesStroke(false);

            plot.setRenderer(datasetIndex, renderer);
            plot.setDataset(datasetIndex, dataset);
            datasetIndex++;
        }
        if (xAxis == null) return null;

        JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT,
                plot, false);
        currentTheme.apply(chart);
        return chart;
    }

    private IntervalXYDataset getDateset(int col) {
        int xIndex = pnlConfig.getDomainIndex();

        Class xClazz = table.getColumnClass(xIndex);
        KBase2RegularTimePeriod converter = regularTimePeriodConverters.get(xClazz);
        if (converter == null) {
            XYSeriesCollection collection = new XYSeriesCollection();
            collection.setAutoWidth(true);
            XYSeries series = new XYSeries(table.getColumnName(col));
            for (int row = 0; row < table.getRowCount(); row++) {
                K.KBase xValue = (K.KBase)table.getValueAt(row, xIndex);
                K.KBase yValue = (K.KBase)table.getValueAt(row, col);
                if (xValue.isNull() || yValue.isNull()) continue;

                double x = ((ToDouble)xValue).toDouble();
                double y = ((ToDouble)yValue).toDouble();
                series.add(x, y);
            }
            collection.addSeries(series);
            return collection;
        } else {
            TimeSeriesCollection collection = new TimeSeriesCollection();
            TimeSeries series = new TimeSeries(table.getColumnName(col));
            for (int row = 0; row < table.getRowCount(); row++) {
                K.KBase xValue = (K.KBase) table.getValueAt(row, xIndex);
                if (xValue.isNull()) continue;

                RegularTimePeriod period = converter.convert(xValue);
                K.KBase value = (K.KBase) table.getValueAt(row, col);
                if (value.isNull()) continue;

                series.addOrUpdate(period, ((ToDouble)value).toDouble());
            }
            collection.addSeries(series);

            return collection;
        }
    }
}

