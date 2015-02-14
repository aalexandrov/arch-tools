package bg.unisofia.clio.archtools.ui.gui.form;

import bg.unisofia.clio.archtools.model.distance.Distance;
import bg.unisofia.clio.archtools.model.hac.Linkage;
import bg.unisofia.clio.archtools.service.ClusteringService;
import bg.unisofia.clio.archtools.service.HierarchicalClusteringService;
import bg.unisofia.clio.archtools.service.KMeansClusteringService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ClusteringForm extends JFrame {

    private JFileChooser fileChooser = new JFileChooser();
    private JButton fileChooserButton;
    private JPanel mainPanel;
    private JLabel fileLabel;
    private JComboBox<String> inputSheetName;
    private JTextField outputSheetName;
    private JComboBox<Integer> numberOfClusters;
    private JComboBox<Linkage> linkage;
    private JComboBox<Distance> distance;
    private JButton computeClustersButton;
    private JRadioButton hacAlgorithm;
    private JRadioButton kMeansAlgorithm;
    private JCheckBox normalizeSample;

    private File inputFile = null;

    public ClusteringForm() {
        super("Archaeology Tools");

        // compute the CWD
        File cwd = new File(ClusteringForm.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        while (!cwd.isDirectory()) {
            cwd = cwd.getParentFile();
        }

        // configure widgets:
        // init number of clusters
        numberOfClusters.setModel(new DefaultComboBoxModel<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
        numberOfClusters.setSelectedIndex(2);
        // init linkage strategy combo box
        linkage.setModel(new DefaultComboBoxModel<>(Linkage.values()));
        linkage.setSelectedItem(Linkage.COMPLETE);
        // init distance metric combo box
        distance.setModel(new DefaultComboBoxModel<>(Distance.values()));
        distance.setSelectedItem(Distance.MANHATTAN);
        // init file chooser
        fileChooser.setCurrentDirectory(cwd);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", new String[]{".xls", ".xlsx"}));
        // clustering button
        computeClustersButton.setEnabled(false);

        // configure root JFrame
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();

        // on file chooser click
        fileChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int returnVal = fileChooser.showOpenDialog(ClusteringForm.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    inputFile = fileChooser.getSelectedFile();

                    try {
                        inputSheetName.setModel(new DefaultComboBoxModel<>(ClusteringService.getInputSheets(inputFile)));
                        inputSheetName.setSelectedIndex(0);
                        computeClustersButton.setEnabled(true);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ClusteringForm.this, "Cannot read input sheet names from file " + inputFile.getPath());
                        computeClustersButton.setEnabled(false);
                    }

                    fileLabel.setText(inputFile.getAbsolutePath());
                }

            }
        });

        // on algorithm change (HAC)
        ActionListener algorithmChange = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JRadioButton button = (JRadioButton) event.getSource();

                if (button == hacAlgorithm) {
                    linkage.setEnabled(true);
                    distance.setEnabled(true);
                } else if (button == kMeansAlgorithm) {
                    linkage.setEnabled(false);
                    distance.setEnabled(false);
                }
            }
        };
        hacAlgorithm.addActionListener(algorithmChange);
        kMeansAlgorithm.addActionListener(algorithmChange);
        // disable by default
        linkage.setEnabled(false);
        distance.setEnabled(false);

        // on file chooser click
        inputSheetName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                outputSheetName.setText(inputSheetName.getSelectedItem() + " clustered");
            }
        });

        computeClustersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // sanity check
                if (inputFile == null) {
                    JOptionPane.showMessageDialog(ClusteringForm.this, "Please select a file first!");
                    return;
                }

                ClusteringService clusteringService;

                // set clustering parameters
                if (hacAlgorithm.isSelected()) {
                    // configure clustering service
                    HierarchicalClusteringService hierarchicalClusteringService = new HierarchicalClusteringService();
                    hierarchicalClusteringService.inputFile = inputFile;
                    hierarchicalClusteringService.inputSheetName = (String) inputSheetName.getSelectedItem();
                    hierarchicalClusteringService.outputSheetName = inputSheetName.getSelectedItem() + " clustered (HAC)";
                    hierarchicalClusteringService.numberOfClusters = (Integer) numberOfClusters.getSelectedItem();
                    hierarchicalClusteringService.linkageStrategy = ((Linkage) linkage.getSelectedItem()).strategy;
                    hierarchicalClusteringService.distanceMeasure = ((Distance) distance.getSelectedItem()).measure;
                    hierarchicalClusteringService.normalize = normalizeSample.isSelected();
                    // set clustering service
                    clusteringService = hierarchicalClusteringService;
                } else { // k-means (default)
                    // configure clustering service
                    KMeansClusteringService kMeansClusteringService = new KMeansClusteringService();
                    kMeansClusteringService.inputFile = inputFile;
                    kMeansClusteringService.inputSheetName = (String) inputSheetName.getSelectedItem();
                    kMeansClusteringService.outputSheetName = inputSheetName.getSelectedItem() + " clustered (K-Means)";
                    kMeansClusteringService.numberOfClusters = (Integer) numberOfClusters.getSelectedItem();
                    kMeansClusteringService.distanceMeasure = ((Distance) distance.getSelectedItem()).measure;
                    kMeansClusteringService.normalize = normalizeSample.isSelected();
                    // set clustering service
                    clusteringService = kMeansClusteringService;
                }

                // perform clustering
                try {
                    clusteringService.computeClusters();
                    JOptionPane.showMessageDialog(ClusteringForm.this, "Clusters written in " + clusteringService.outputFile.getPath() + ", sheet \"" + clusteringService.outputSheetName + "\"");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ClusteringForm.this, "Error while clustering items on " + inputFile.getPath() + ", sheet \"" + clusteringService.inputSheetName + "\"");
                }
            }
        });

        setVisible(true);
    }
}
