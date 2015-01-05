package bg.unisofia.clio.archtools.model.distance;

import java.util.List;

public enum Distance {

    EUCLIDEAN(new Euclidean()),
    MANHATTAN(new Manhattan());

    public Metric metric;

    private Distance(Metric metric) {
        this.metric = metric;
    }

    public static interface Metric {
        public abstract double apply(List<Double> x, List<Double> y);
    }

    public static final class Euclidean implements Metric {
        @Override
        public double apply(List<Double> x, List<Double> y) {
            double distance = 0.0;
            for (int k = 0; k < x.size(); k++) {
                distance += (x.get(k) - y.get(k)) * (x.get(k) - y.get(k));
            }
            return Math.sqrt(distance);
        }
    }

    public static final class Manhattan implements Metric {
        @Override
        public double apply(List<Double> x, List<Double> y) {
            double distance = 0.0;
            for (int k = 0; k < x.size(); k++) {
                distance += Math.abs(x.get(k) - y.get(k));
            }
            return distance;
        }
    }
}
