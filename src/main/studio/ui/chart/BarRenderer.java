package studio.ui.chart;

import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;


public class BarRenderer extends XYBarRenderer {
    @Override
    public Range findDomainBounds(XYDataset dataset) {
        return super.findDomainBounds(dataset, false);
    }
}
