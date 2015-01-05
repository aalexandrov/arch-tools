package bg.unisofia.clio.archtools.service;

import bg.unisofia.clio.archtools.model.distance.Distance;
import com.apporiented.algorithm.clustering.*;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class ClusteringService {

    private static final int IDX_ITEMNAME = 0;

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

    public File inputFile;

    public File outputFile;

    public String inputSheetName;

    public String outputSheetName;

    public int numberOfClusters;

    public LinkageStrategy linkageStrategy;

    public Distance.Metric distanceMetric;


    public String[] getInputSheets(File inputFile) throws Exception {
        Workbook wb = WorkbookFactory.create(inputFile);

        String[] sheets = new String[wb.getNumberOfSheets()];
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            sheets[i] = wb.getSheetName(i);
        }

        return sheets;
    }


    public void computeClusters() throws Exception {

        Workbook wb = WorkbookFactory.create(inputFile);
        Sheet inputSheet = wb.getSheet(inputSheetName);

        if (inputSheet == null) {
            throw new IllegalArgumentException(String.format("Unknown input sheet %s in file %s", inputSheetName, inputFile));
        }

        // get title row
        Row titleRow = inputSheet.getRow(0);

        // collect columns to be used as features
        List<Integer> featureColumns = new ArrayList<>();
        for (Cell cell : titleRow) {
            CellStyle cellStyle = cell.getCellStyle();
            if (!cellStyle.getHidden() && wb.getFontAt(cellStyle.getFontIndex()).getUnderline() != 0) {
                featureColumns.add(cell.getColumnIndex());
            }
        }

        // collect observations
        List<String> itemNames = new ArrayList<>();
        List<ArrayList<Double>> itemFeatures = new ArrayList<>();
        for (Row row : inputSheet) {
            if (row.getRowNum() != titleRow.getRowNum() && !"".equals(row.getCell(IDX_ITEMNAME, Row.CREATE_NULL_AS_BLANK).toString())) {
                // System.out.println("Adding " + row.getCell(IDX_ITEMNAME).toString() + " to the itemset");
                // construct feature columns ArrayList
                ArrayList<Double> features = new ArrayList<>(featureColumns.size());
                for (int i = 0; i < featureColumns.size(); i++) {
                    Cell x = row.getCell(featureColumns.get(i));
                    if (x == null) {
                        features.add(i, 0.0);
                    } else if (x.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        features.add(i, x.getNumericCellValue());
                    } else {
                        features.add(i, 0.0);
                    }
                }
                // add to items set
                itemNames.add(row.getCell(IDX_ITEMNAME).toString());
                itemFeatures.add(features);
            }
        }

        // compute distance matrix
        double[][] matrix = new double[itemNames.size()][itemNames.size()];
        for (int i = 0; i < itemNames.size(); i++) {
            ArrayList<Double> features1 = itemFeatures.get(i);
            for (int j = 0; j < itemNames.size(); j++) {
                ArrayList<Double> features2 = itemFeatures.get(j);
                // pre-compute and store the distance between the items at positions i and k in the distance matrix
                matrix[i][j] = distanceMetric.apply(features1, features2);
            }
        }


        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster topCluster = alg.performClustering(matrix, itemNames.toArray(new String[itemNames.size()]), linkageStrategy); //TODO: linkage as parameter

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
                row.createCell(1).setCellValue(k);
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
