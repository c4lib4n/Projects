import org.example.BarcodeGeneratorGUI;

import javax.swing.SwingUtilities;

public class BarcodeGenerator {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            BarcodeGeneratorGUI gui = new BarcodeGeneratorGUI();
            gui.setVisible(true);
        });
    }
}
