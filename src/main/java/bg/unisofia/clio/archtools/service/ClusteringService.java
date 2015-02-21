package bg.unisofia.clio.archtools.service;

import bg.unisofia.clio.archtools.model.point.DoublePointWithLabel;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class ClusteringService {

    protected static final int IDX_ITEM_NAME = 0;

    public File inputFile;

    public File outputFile;

    public String inputSheetName;

    public String outputSheetName;

    public int numberOfClusters;

    public DistanceMeasure distanceMeasure;

    public boolean normalize = false;

    public static String[] getInputSheets(File inputFile) throws Exception {
        Workbook wb = WorkbookFactory.create(inputFile);

        String[] sheets = new String[wb.getNumberOfSheets()];
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            sheets[i] = wb.getSheetName(i);
        }

        return sheets;
    }

    public abstract void computeClusters() throws Exception;

    protected List<Integer> collectFeatureColumns(Workbook wb, Sheet inputSheet) {
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
        return featureColumns;
    }

    protected List<DoublePointWithLabel> collectObservations(Sheet inputSheet, FormulaEvaluator evaluator, List<Integer> featureColumns) {
        List<DoublePointWithLabel> observations = new ArrayList<>();
        for (Row row : inputSheet) {
            if (row.getRowNum() != 0 && !"".equals(row.getCell(IDX_ITEM_NAME, Row.CREATE_NULL_AS_BLANK).toString())) {
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
                CellValue label = evaluator.evaluate(row.getCell(IDX_ITEM_NAME));
                observations.add(new DoublePointWithLabel(label, features, row.getCell(IDX_ITEM_NAME).getCellStyle()));
            }
        }
        return observations;
    }

    protected void normalize(int numberOfFeatures, List<DoublePointWithLabel> observations) {
        // transpose the observation matrix into a featureSamples matrix
        double[][] featureSamples = new double[numberOfFeatures][observations.size()];
        for (int i = 0; i < observations.size(); i++) {
            double[] point = observations.get(i).getPoint();
            for (int j = 0; j < numberOfFeatures; j++) {
                featureSamples[j][i] = point[j];
            }
        }

        // standardize the feature samples
        for (int j = 0; j < numberOfFeatures; j++) {
            featureSamples[j] = StatUtils.normalize(featureSamples[j]);
        }

        // update the observations (reverse transpose)
        for (int i = 0; i < observations.size(); i++) {
            double[] point = observations.get(i).getPoint();
            for (int j = 0; j < numberOfFeatures; j++) {
                point[j] = featureSamples[j][i];
            }
        }
    }

    protected String stringValueOf(CellValue value) {
        switch (value.getCellType()) {
            case XSSFCell.CELL_TYPE_STRING:
                return value.getStringValue();
            case XSSFCell.CELL_TYPE_NUMERIC:
                return String.valueOf(value.getNumberValue());
            case XSSFCell.CELL_TYPE_BLANK:
                return "";
            case XSSFCell.CELL_TYPE_BOOLEAN:
                return String.valueOf(value.getBooleanValue());
            case XSSFCell.CELL_TYPE_ERROR:
                return "error";
            default:
                return "unknown";
        }
    }
}
