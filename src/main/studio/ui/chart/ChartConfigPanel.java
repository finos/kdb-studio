package studio.ui.chart;

import org.jfree.chart.plot.DefaultDrawingSupplier;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ChartConfigPanel extends Box implements ActionListener {

    private Chart chart;
    private List<String> names;
    private List<Integer> xIndex;
    private List<Integer> yIndex;

    private JComboBox<ChartType> comboCharType;
    private JComboBox<String> comboX;
    private JCheckBox chkAll;
    private JCheckBox[] chkY;
    private LegendIcon[] icons;
    private JPanel pnlLagend;

    private JColorChooser colorChooser;
    private LegendIcon colorChoosePreviewIcon;

    private final static Border EMPTY_BORDER = BorderFactory.createEmptyBorder(2,0,2,0);
    private final static Border SELECTED_BORDER = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

    private static Paint[] colors = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE;
    private static Shape[] shapes = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE;
    private static BasicStroke[] strokes = new BasicStroke[] {
            new BasicStroke(1f),
            new BasicStroke(1f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,1f,new float[] {10,10},0f
            ),
            new BasicStroke(1f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,1f,new float[] {10,5},0f
            ),
            new BasicStroke(1f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,1f,new float[] {5,5},0f
            ),
            new BasicStroke(1f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,1f,new float[] {1.5f,3},0f
            ),
            new BasicStroke(1f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,1f,new float[] {10,3,3,3},0f
            ),
    };

    private static class StrokeWidth {
        String title;
        float width;
        StrokeWidth(String title, float width) {
            this.title = title;
            this.width = width;
        }
    }

    private static StrokeWidth[] strokeWidths = new StrokeWidth[] {
            new StrokeWidth("x 1", 1),
            new StrokeWidth("x 1.5", 1.5f),
            new StrokeWidth("x 2", 2),
            new StrokeWidth("x 3", 3),
    };


    //@TODO: May be it is better to have a cache of all possible strokes to avoid unneseccary garbage ?
    private static BasicStroke strokeWithWidth(BasicStroke stroke, float width) {
        if (stroke.getLineWidth() == width) return stroke;

        return new BasicStroke(width, stroke.getEndCap(), stroke.getLineJoin(),
                stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase());
    }

    private static BasicStroke defaultStroke = strokeWithWidth(strokes[0], 2f);

    public ChartConfigPanel(Chart chart, List<String> names, List<Integer> xIndex, List<Integer> yIndex) {
        super(BoxLayout.Y_AXIS);
        this.chart = chart;
        this.names = names;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        int count = yIndex.size();
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        Box boxStyle = Box.createHorizontalBox();
        boxStyle.add(new JLabel("Type: "));

        comboCharType = new JComboBox<>(ChartType.values());
        comboCharType.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboCharType.getPreferredSize().height));
        comboCharType.addActionListener(this::charTypeSelected);
        ChartType chartType = (ChartType) comboCharType.getSelectedItem();

        boxStyle.add(comboCharType);
        boxStyle.add(Box.createHorizontalGlue());
        add(boxStyle);

        Box boxDomain = Box.createHorizontalBox();
        boxDomain.add(new JLabel("Domain axis: "));

        String[] xItems = xIndex.stream().map(names::get).toArray(String[]::new);
        comboX = new JComboBox<>(xItems);
        comboX.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboX.getPreferredSize().height));
        comboX.addActionListener(this);
        boxDomain.add(comboX);
        boxDomain.add(Box.createHorizontalGlue());
        add(boxDomain);

        Box boxSeries = Box.createHorizontalBox();
        boxSeries.add(new JLabel("Series:"));
        boxSeries.add(Box.createHorizontalGlue());

        chkAll = new JCheckBox("All", true);
        chkAll.addActionListener(this::allSeriesClicked);
        boxSeries.add(chkAll);

        add(boxSeries);

        pnlLagend = new JPanel();
        GroupLayout layout = new GroupLayout(pnlLagend);
        pnlLagend.setLayout(layout);

        GroupLayout.ParallelGroup chkGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup iconGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup rowsGroup = layout.createSequentialGroup();

        chkY = new JCheckBox[count];
        icons = new LegendIcon[count];
        JComponent[] iconPanels = new JComponent[count];
        for (int i=0; i<count; i++) {
            chkY[i] = new JCheckBox(names.get(yIndex.get(i)), true);
            chkY[i].addActionListener(this);

            icons[i] = new LegendIcon(colors[i % colors.length],shapes[i % shapes.length], defaultStroke);
            icons[i].setChartType(chartType);
            iconPanels[i] = new JLabel(icons[i]);
            iconPanels[i].setBorder(EMPTY_BORDER);
            final int theIndex = i;
            iconPanels[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (chkY[theIndex].isEnabled()) {
                        legendPressed(theIndex, e);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (chkY[theIndex].isEnabled()) {
                        iconPanels[theIndex].setBorder(SELECTED_BORDER);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    iconPanels[theIndex].setBorder(EMPTY_BORDER);
                }
            });

            chkGroup.addComponent(chkY[i]);
            iconGroup.addComponent(iconPanels[i]);

            rowsGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(chkY[i])
                                        .addComponent(iconPanels[i]) );
        }

        Box glue = Box.createVerticalBox();

        chkGroup.addComponent(glue);
        rowsGroup.addComponent(glue);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                                        .addGroup(chkGroup)
                                        .addGroup(iconGroup)
                                  );

        layout.setVerticalGroup(rowsGroup);

        add(new JScrollPane(pnlLagend));
        validateState();

        colorChooser = new JColorChooser();

        colorChoosePreviewIcon = new LegendIcon(null, null, null);
        colorChooser.getSelectionModel().addChangeListener(e-> colorChoosePreviewIcon.setColor(colorChooser.getColor()));

        colorChooser.setPreviewPanel(new JLabel(colorChoosePreviewIcon, SwingConstants.CENTER));
    }

    public int getDomainIndex() {
        return xIndex.get(comboX.getSelectedIndex());
    }

    public boolean isSeriesEnables(int index) {
        return chkY[index].isSelected() && chkY[index].isEnabled();
    }

    public Paint getColor(int index) {
        return icons[index].getColor();
    }

    public Shape getShape(int index) {
        return icons[index].getShape();
    }

    public Stroke getStroke(int index) {
        return icons[index].getStroke();
    }

    public ChartType getChartType(int index) {
        return icons[index].getChartType();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        pnlLagend.repaint();
        validateState();
        chart.createPlot();
    }

    private void validateState() {
        for (JCheckBox checkBox: chkY) {
            checkBox.setEnabled(true);
        }
        int x = xIndex.get(comboX.getSelectedIndex());
        int index = yIndex.indexOf(x);
        if (index > -1) {
            chkY[index].setEnabled(false);
        }
    }

    private void allSeriesClicked(ActionEvent e) {
        for (JCheckBox checkBox: chkY) {
            checkBox.setSelected(chkAll.isSelected());
        }
        actionPerformed(e);
    }

    private void charTypeSelected(ActionEvent e) {
        ChartType chartType = (ChartType) comboCharType.getSelectedItem();
        for (LegendIcon icon: icons) {
            icon.setChartType(chartType);
        }
        actionPerformed(e);
    }

    private void legendPressed(int index, MouseEvent e) {
        Paint theColor = icons[index].getColor();
        Shape theShape = icons[index].getShape();
        BasicStroke theStroke = icons[index].getStroke();
        ChartType theChartType = icons[index].getChartType();

        JPopupMenu popup = new JPopupMenu();

        JMenuItem menu = new JMenuItem("Change color", new SquareIcon(theColor, 15));
        menu.addActionListener(actionEvent -> showChangeColor(index, actionEvent));
        popup.add(menu);

        JMenu subMenu = new JMenu("Change type");
        for (ChartType chartType : ChartType.values()) {
            LegendIcon icon = new LegendIcon(theColor, theShape, theStroke);
            icon.setChartType(chartType);

            JCheckBoxMenuItem item = new JCheckBoxMenuItem(chartType.toString(), icon, theChartType == chartType);
            item.addActionListener(actionEvent -> changeChartType(index, chartType, actionEvent));
            subMenu.add(item);
        }
        popup.add(subMenu);

        if (icons[index].getChartType().hasShape()) {
            subMenu = new JMenu("Change shape");
            for (Shape shape: shapes) {
                LegendIcon icon = new LegendIcon(theColor, shape, theStroke);
                icon.setChartType(theChartType);

                JCheckBoxMenuItem item = new JCheckBoxMenuItem("", icon, theShape == shape);
                item.addActionListener(actionEvent -> changeShape(index, shape, actionEvent));
                subMenu.add(item);
            }
            popup.add(subMenu);

            menu = new JMenuItem("Set this shape to all");
            menu.addActionListener(actionEvent -> setShapeToAll(index, actionEvent));
            popup.add(menu);
        }

        if (icons[index].getChartType().hasLine()) {
            float theWidth = theStroke.getLineWidth();
            subMenu = new JMenu("Change stroke");
            for (BasicStroke stroke: strokes) {
                BasicStroke aStroke = strokeWithWidth(stroke, theWidth);
                LegendIcon icon = new LegendIcon(theColor, null, aStroke);
                JCheckBoxMenuItem item = new JCheckBoxMenuItem("", icon, theStroke.equals(strokeWithWidth(stroke, theWidth)));
                item.addActionListener(actionEvent -> changeStroke(index, aStroke, actionEvent));
                subMenu.add(item);
            }
            subMenu.addSeparator();

            for (StrokeWidth strokeWidth: strokeWidths) {
                boolean selected = theWidth == strokeWidth.width;
                LegendIcon icon = new LegendIcon(theColor, null, strokeWithWidth(theStroke, strokeWidth.width));

                JCheckBoxMenuItem item = new JCheckBoxMenuItem(strokeWidth.title, icon, selected);
                item.addActionListener(actionEvent -> changeStrokeWidth(index, strokeWidth.width, actionEvent));
                subMenu.add(item);
            }
            popup.add(subMenu);

            menu = new JMenuItem("Set this stroke to all");
            menu.addActionListener(actionEvent -> setStrokeToAll(index, actionEvent));
            popup.add(menu);
        }

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showChangeColor(int index, ActionEvent e) {
        Paint paint = icons[index].getColor();
        Color color = Color.BLACK;
        if (paint instanceof Color) {
            color = (Color)paint;
        }
        colorChooser.setColor(color);
        colorChoosePreviewIcon.setColor(color);
        colorChoosePreviewIcon.setShape(icons[index].getShape());
        colorChoosePreviewIcon.setStroke(icons[index].getStroke());
        colorChoosePreviewIcon.setChartType(icons[index].getChartType());
        JDialog dialog = JColorChooser.createDialog(pnlLagend, "Choose color", true, colorChooser, actionEvent -> changeColor(index, actionEvent), null);
        dialog.setVisible(true);
    }

    private void changeColor(int index, ActionEvent e) {
        icons[index].setColor(colorChooser.getColor());
        actionPerformed(e);
    }

    private void changeChartType(int index, ChartType chartType, ActionEvent e) {
        icons[index].setChartType(chartType);
        actionPerformed(e);
    }

    private void changeShape(int index, Shape shape, ActionEvent e) {
        icons[index].setShape(shape);
        actionPerformed(e);
    }

    private void setShapeToAll(int index, ActionEvent e) {
        Shape shape = icons[index].getShape();
        for (LegendIcon icon: icons) {
            icon.setShape(shape);
        }
        actionPerformed(e);
    }

    private void changeStroke(int index, BasicStroke stroke, ActionEvent e) {
        icons[index].setStroke(stroke);
        actionPerformed(e);
    }

    private void changeStrokeWidth(int index, float width, ActionEvent e) {
        icons[index].setStroke(strokeWithWidth(icons[index].getStroke(), width));
        actionPerformed(e);
    }

    private void setStrokeToAll(int index, ActionEvent e) {
        BasicStroke stroke = icons[index].getStroke();
        for (LegendIcon icon: icons) {
            icon.setStroke(stroke);
        }
        actionPerformed(e);
    }

}
