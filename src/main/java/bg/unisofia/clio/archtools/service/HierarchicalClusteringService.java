package bg.unisofia.clio.archtools.service;

import bg.unisofia.clio.archtools.model.point.DoublePointWithLabel;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class HierarchicalClusteringService extends ClusteringService {

    private static final Comparator<Cluster> CLUSTER_MAX_DISTANCE_COMPARATOR = new Comparator<Cluster>() {
        @Override
        public int compare(Cluster c1, Cluster c2) {
            Double d1 = c1.getDistance();
            Double d2 = c2.getDistance();

            if (d1 == null && d2 == null) {
                return 1;
            } else if (d1 != null && d2 == null) {
                return -1;
            } else if (d1 == null && d2 != null) {
                return 1;
            } else {
                return (int) (c2.getDistance() - c1.getDistance());
            }
        }
    };

    public LinkageStrategy linkageStrategy;


    @Override
    public void computeClusters() throws Exception {

        Workbook wb = WorkbookFactory.create(inputFile);
        Sheet inputSheet = wb.getSheet(inputSheetName);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        if (inputSheet == null) {
            throw new IllegalArgumentException(String.format("Unknown input sheet %s in file %s", inputSheetName, inputFile));
        }

        List<Integer> featureColumns = collectFeatureColumns(wb, inputSheet);
        int numberOfFeatures = featureColumns.size();

        // collect observations
        List<DoublePointWithLabel> observations = collectObservations(inputSheet, evaluator, featureColumns);

        // normalize observations
        if (normalize) normalize(numberOfFeatures, observations);

        // compute distance matrix
        double[][] matrix = new double[observations.size()][observations.size()];
        for (int i = 0; i < observations.size(); i++) {
            double[] features1 = observations.get(i).getPoint();
            for (int j = 0; j < observations.size(); j++) {
                double[] features2 = observations.get(j).getPoint();
                // pre-compute and store the distance between the items at positions i and k in the distance matrix
                matrix[i][j] = distanceMeasure.compute(features1, features2);
            }
        }

        // extract labels
        String[] itemNames = new String[observations.size()];
        HashMap<String, CellStyle> itemStyles = new HashMap<>(observations.size());
        for (int i = 0; i < observations.size(); i++) {
            itemNames[i] = stringValueOf(observations.get(i).label);
            itemStyles.put(stringValueOf(observations.get(i).label), observations.get(i).cellStyle);
        }

        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster topCluster = alg.performClustering(matrix, itemNames, linkageStrategy);

        List<Cluster> clusters = topKClusters(topCluster, numberOfClusters);

        Sheet outputSheet = wb.getSheet(outputSheetName);
        if (outputSheet != null) {
            wb.removeSheetAt(wb.getSheetIndex(outputSheet));
        }
        outputSheet = wb.createSheet(outputSheetName);

        int i = 0;
        int k = 0;
        for (Cluster cluster : clusters) {
            k++;
            for (String item : getItems(cluster)) {
                Row row = outputSheet.createRow(i);
                row.createCell(0).setCellValue(item);
                row.getCell(0).setCellStyle(itemStyles.get(item));
                row.createCell(1).setCellValue(k);
                row.getCell(1).setCellStyle(itemStyles.get(item));
                i++;
            }
            i++;
        }

        if (outputFile == null) {
            outputFile = new File(inputFile.getAbsolutePath().replace(".xlsx", ".clustered.xlsx"));
        }
        wb.write(new FileOutputStream(outputFile));
    }

    private List<Cluster> topKClusters(Cluster topCluster, int numberOfClusters) {
        PriorityQueue<Cluster> heap = new PriorityQueue<>(numberOfClusters, CLUSTER_MAX_DISTANCE_COMPARATOR);

        heap.add(topCluster);

        // split head until heap size equals the required number of clusters
        while (heap.size() < numberOfClusters) {
            Cluster splitCandidate = heap.peek();

            if (splitCandidate.getChildren().isEmpty()) {
                // next split is not possible, return current heap
                return new ArrayList<>(Arrays.asList(heap.toArray(new Cluster[heap.size()])));
            } else {
                // next split is possible, replace split candidate with its children
                heap.remove(splitCandidate);
                heap.addAll(splitCandidate.getChildren());
            }
        }

        return new ArrayList<>(Arrays.asList(heap.toArray(new Cluster[heap.size()])));
    }

    private List<String> getItems(Cluster cluster) {
        PriorityQueue<Cluster> heap = new PriorityQueue<>(100, CLUSTER_MAX_DISTANCE_COMPARATOR);

        heap.add(cluster);

        // split head until heap size equals the required number of clusters
        while (!heap.peek().getChildren().isEmpty()) {
            heap.addAll(heap.poll().getChildren());
        }

        List<String> result = new ArrayList<>(heap.size());
        for (Cluster c : heap) {
            result.add(c.getName());
        }

        return result;
    }
}
