package bg.unisofia.clio.archtools.model.distance;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

import java.util.List;

public enum Distance {

    EUCLIDEAN(new EuclideanDistance()),
    MANHATTAN(new ManhattanDistance());

    public DistanceMeasure measure;

    private Distance(DistanceMeasure measure) {
        this.measure = measure;
    }
}
