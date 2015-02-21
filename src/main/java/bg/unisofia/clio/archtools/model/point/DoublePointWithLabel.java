package bg.unisofia.clio.archtools.model.point;

import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;

public class DoublePointWithLabel extends DoublePoint {

    public final CellValue label;

    public final CellStyle cellStyle;

    public DoublePointWithLabel(CellValue label, double[] point, CellStyle cellStyle) {
        super(point);
        this.label = label;
        this.cellStyle = cellStyle;
    }

    public DoublePointWithLabel(CellValue label, int[] point, CellStyle cellStyle) {
        super(point);
        this.label = label;
        this.cellStyle = cellStyle;
    }
}
