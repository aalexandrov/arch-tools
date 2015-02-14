package bg.unisofia.clio.archtools.model.point;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.poi.ss.usermodel.CellStyle;

public class DoublePointWithLabel extends DoublePoint {

    public final String label;

    public final CellStyle cellStyle;

    public DoublePointWithLabel(String label, double[] point, CellStyle cellStyle) {
        super(point);
        this.label = label;
        this.cellStyle = cellStyle;
    }

    public DoublePointWithLabel(String label, int[] point, CellStyle cellStyle) {
        super(point);
        this.label = label;
        this.cellStyle = cellStyle;
    }
}
