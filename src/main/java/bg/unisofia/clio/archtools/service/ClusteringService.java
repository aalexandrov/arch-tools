package bg.unisofia.clio.archtools.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;

public abstract class ClusteringService {

    protected static final int IDX_ITEM_NAME = 0;

    public File inputFile;

    public File outputFile;

    public String inputSheetName;

    public String outputSheetName;

    public int numberOfClusters;

    public static String[] getInputSheets(File inputFile) throws Exception {
        Workbook wb = WorkbookFactory.create(inputFile);

        String[] sheets = new String[wb.getNumberOfSheets()];
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            sheets[i] = wb.getSheetName(i);
        }

        return sheets;
    }

    public abstract void computeClusters() throws Exception;
}
