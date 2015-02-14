package bg.unisofia.clio.archtools.service;

import bg.unisofia.clio.archtools.model.kmeans.DoublePointWithLabel;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class KMeansClusteringService extends ClusteringService {

    public int maxIterations = 100;

    public DistanceMeasure distanceMeasure;


    @Override
    public void computeClusters() throws Exception {

        Workbook wb = WorkbookFactory.create(inputFile);
        Sheet inputSheet = wb.getSheet(inputSheetName);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

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
        List<DoublePointWithLabel> itemFeatures = new ArrayList<>();
        for (Row row : inputSheet) {
            if (row.getRowNum() != titleRow.getRowNum() && !"".equals(row.getCell(IDX_ITEM_NAME, Row.CREATE_NULL_AS_BLANK).toString())) {
                // construct feature columns ArrayList
                double[] features = new double[featureColumns.size()];
                for (int i = 0; i < featureColumns.size(); i++) {
                    Cell cell = row.getCell(featureColumns.get(i));
                    CellValue value = cell != null ? evaluator.evaluate(cell) : null;

                    if (value == null) {
                        features[i] = 0.0;
                    } else if (value.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        features[i] = value.getNumberValue();
                    } else {
                        features[i] = 0.0;
                    }
                }
                // add to items set
                itemFeatures.add(new DoublePointWithLabel(row.getCell(IDX_ITEM_NAME).toString(), features));
            }
        }

        KMeansPlusPlusClusterer<DoublePointWithLabel> alg = new KMeansPlusPlusClusterer<>(numberOfClusters, maxIterations, distanceMeasure);
        List<CentroidCluster<DoublePointWithLabel>> clusters = alg.cluster(itemFeatures);

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
}
