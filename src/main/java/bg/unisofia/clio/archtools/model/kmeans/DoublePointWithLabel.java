package bg.unisofia.clio.archtools.model.kmeans;

import org.apache.commons.math3.ml.clustering.DoublePoint;

public class DoublePointWithLabel extends DoublePoint {

    public final String label;

    public DoublePointWithLabel(String label, double[] point) {
        super(point);
        this.label = label;
    }

    public DoublePointWithLabel(String label, int[] point) {
        super(point);
        this.label = label;
    }
}
