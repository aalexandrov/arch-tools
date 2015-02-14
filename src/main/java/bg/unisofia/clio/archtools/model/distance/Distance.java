package bg.unisofia.clio.archtools.model.distance;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

import java.util.List;

public enum Distance {

    EUCLIDEAN(new Euclidean(), new EuclideanDistance()),
    MANHATTAN(new Manhattan(), new ManhattanDistance());

    public Metric metric;

    public DistanceMeasure measure;

    private Distance(Metric metric, DistanceMeasure measure) {
        this.metric = metric;
        this.measure = measure;
    }

    public static interface Metric {
        public abstract double apply(List<Double> x, List<Double> y);
    }

    public static final class Euclidean implements Metric {
        @Override
        public double apply(List<Double> x, List<Double> y) {
            double distance = 0.0;
            for (int i = 0; i < x.size(); i++) {
                distance += (x.get(i) - y.get(i)) * (x.get(i) - y.get(i));
            }
            return Math.sqrt(distance);
        }
    }

    public static final class Manhattan implements Metric {
        @Override
        public double apply(List<Double> x, List<Double> y) {
            double distance = 0.0;
            for (int i = 0; i < x.size(); i++) {
                distance += Math.abs(x.get(i) - y.get(i));
            }
            return distance;
        }
    }
}
