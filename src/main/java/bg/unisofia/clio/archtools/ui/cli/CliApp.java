package bg.unisofia.clio.archtools.ui.cli;

import bg.unisofia.clio.archtools.model.distance.Distance;
import bg.unisofia.clio.archtools.model.hac.Linkage;
import bg.unisofia.clio.archtools.service.HierarchicalClusteringService;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;

/**
 * Application main class.
 */
public class CliApp {

    private static final String ARG_FILE = "file";
    private static final String ARG_NUM_CLUSTERS = "num-clusters";
    private static final String ARG_INPUT_SHEET = "input-sheet";
    private static final String ARG_OUTPUT_SHEET = "output-sheet";
    private static final String ARG_LINKAGE = "linkage-strategy";
    private static final String ARG_DISTANCE = "distance-metric";

    public static void main(String[] args) {
        try {
            Namespace ns = getParser().parseArgs(args);
            // create service
            HierarchicalClusteringService clusteringService = new HierarchicalClusteringService();
            // set clustering parameters
            clusteringService.inputFile = ns.get(ARG_FILE);
            clusteringService.inputSheetName = ns.getString(ARG_INPUT_SHEET);
            clusteringService.outputSheetName = ns.getString(ARG_OUTPUT_SHEET);
            clusteringService.numberOfClusters = ns.getInt(ARG_NUM_CLUSTERS);
            clusteringService.linkageStrategy = ns.<Linkage>get(ARG_LINKAGE).strategy;
            clusteringService.distanceMetric = ns.<Distance>get(ARG_DISTANCE).metric;
            // perform clustering
            clusteringService.computeClusters();
        } catch (ArgumentParserException e) {
            e.getParser().printHelp();
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static ArgumentParser getParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("arch-tools");

        parser.addArgument("--clusters")
                .dest(ARG_NUM_CLUSTERS)
                .metavar("K")
                .type(Integer.class)
                .setDefault(3)
                .help("Number of cluster to compute (default: 3)");
        parser.addArgument("--linkage")
                .dest(ARG_LINKAGE)
                .metavar("STRATEGY")
                .type(Linkage.class)
                .choices(Linkage.values())
                .setDefault(Linkage.SINGLE)
                .help("Linkage Strategy (default: single)");
        parser.addArgument("--distance")
                .dest(ARG_DISTANCE)
                .metavar("METRIC")
                .type(Distance.class)
                .choices(Distance.values())
                .setDefault(Distance.MANHATTAN)
                .help("Distance metric (default: Manhattan)");
        parser.addArgument("file")
                .dest(ARG_FILE)
                .metavar("EXCEL-FILE")
                .type(File.class)
                .help("Excel file path");
        parser.addArgument("input-sheet")
                .dest(ARG_INPUT_SHEET)
                .metavar("INPUT-SHEET")
                .type(String.class)
                .help("Input sheet name");
        parser.addArgument("output-sheet")
                .dest(ARG_OUTPUT_SHEET)
                .metavar("OUTPUT-SHEET")
                .type(String.class)
                .help("Output sheet name");

        return parser;
    }
}
