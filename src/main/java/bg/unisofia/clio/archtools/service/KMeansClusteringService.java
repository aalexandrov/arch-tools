package bg.unisofia.clio.archtools.service;

import bg.unisofia.clio.archtools.model.point.DoublePointWithLabel;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class KMeansClusteringService extends ClusteringService {

    public int maxIterations = 10000;


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

        KMeansPlusPlusClusterer<DoublePointWithLabel> alg = new KMeansPlusPlusClusterer<>(numberOfClusters, maxIterations, distanceMeasure);
        List<CentroidCluster<DoublePointWithLabel>> clusters = alg.cluster(observations);

        Sheet outputSheet = wb.getSheet(outputSheetName);
        if (outputSheet != null) {
            wb.removeSheetAt(wb.getSheetIndex(outputSheet));
        }
        outputSheet = wb.createSheet(outputSheetName);

        int i = 0;
        int k = 0;
        for (CentroidCluster<DoublePointWithLabel> cluster : clusters) {
            k++;
            for (DoublePointWithLabel point : cluster.getPoints()) {
                Row row = outputSheet.createRow(i);
                row.createCell(0).setCellValue(point.label);
                row.getCell(0).setCellStyle(point.cellStyle);
                row.createCell(1).setCellValue(k);
                row.getCell(1).setCellStyle(point.cellStyle);
                i++;
            }
            i++;
        }

        if (outputFile == null) {
            outputFile = new File(inputFile.getAbsolutePath().replace(".xlsx", ".clustered.xlsx"));
        }
        wb.write(new FileOutputStream(outputFile));
    }
}
