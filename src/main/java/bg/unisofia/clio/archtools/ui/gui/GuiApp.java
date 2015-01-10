package bg.unisofia.clio.archtools.ui.gui;

import bg.unisofia.clio.archtools.service.ClusteringService;
import bg.unisofia.clio.archtools.ui.gui.form.ClusteringForm;

import javax.swing.*;


public class GuiApp {

    public static void main(String[] args) {
        // set theme
        try {
            UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // start application
        new ClusteringForm(new ClusteringService());
    }
}
