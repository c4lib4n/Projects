import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.PrinterResolution;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;

public class BarcodeGeneratorGUI extends JFrame {
    private JTextField barcodeDataField;
    private JPanel barcodePanel;

    public BarcodeGeneratorGUI() {
        // Set up the frame
        setTitle("Barcode Generator ---- created by @chagasda");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create components
        JPanel inputPanel = new JPanel();
        barcodeDataField = new JTextField(20);
        JButton generateButton = new JButton("Generate Barcode");
        barcodePanel = new JPanel();

        //Printing with Enter
        barcodeDataField.addActionListener(e -> generateBarcode());

        // Add components to input panel
        inputPanel.add(new JLabel("Enter barcode data: "));
        inputPanel.add(barcodeDataField);
        inputPanel.add(generateButton);

        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(barcodePanel, BorderLayout.CENTER);

        // Add button action
        generateButton.addActionListener(e -> generateBarcode());

        // Set frame properties
        pack();
        setSize(500, 300);
        setLocationRelativeTo(null);
    }

    private void generateBarcode() {
        try {
            String data = barcodeDataField.getText();
            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter barcode data");
                return;
            }


            // Generate barcode
            Barcode barcode = BarcodeFactory.createCode128(data);

            // Create print job
            PrinterJob job = PrinterJob.getPrinterJob();

            // Find Zebra ZD621 printer
            PrintService[] printServices = PrinterJob.lookupPrintServices();
            PrintService zebraPrinter = null;
            for (PrintService printer : printServices) {
                if (printer.getName().toLowerCase().contains("zd621")) {
                    zebraPrinter = printer;
                    break;
                }
            }
//just a random comment

            if (zebraPrinter != null) {
                job.setPrintService(zebraPrinter);
            }

            // Set up custom page format for 3"x5" label
            PageFormat pageFormat = job.defaultPage();
            Paper paper = new Paper();

            // Convert inches to points (1 inch = 72 points)
            double width = 5.0 * 72;
            double height = 3.0 * 72;

            // Set paper size
            paper.setSize(width, height);

            // Set imageable area (minimal margins for thermal printer)
            double margin = 0.05 * 72; // 0.05 inch margin
            paper.setImageableArea(margin, margin,
                    width - (2 * margin),
                    height - (2 * margin));

            pageFormat.setPaper(paper);
            pageFormat.setOrientation(PageFormat.PORTRAIT);

            // Set printer specific attributes
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new PrinterResolution(300, 300, PrinterResolution.DPI));
            attributes.add(new MediaPrintableArea(0, 0, 5, 3, MediaPrintableArea.INCH));


            job.setPrintable((graphics, pf, pageIndex) -> {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pf.getImageableX(), pf.getImageableY());

                // Set high quality rendering
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                try {
                    // Set black color
                    g2d.setColor(Color.BLACK);

                    // Adjust barcode size - make it smaller
                    barcode.setBarWidth(1);  // Reduce bar width
                    barcode.setBarHeight(20);  // Reduce bar height

                    // Calculate dimensions
                    double pageWidth = pf.getImageableWidth();
                    double pageHeight = pf.getImageableHeight();
                    int barcodeWidth = barcode.getWidth();
                    int barcodeHeight = barcode.getHeight();

                    // Position barcode closer to the top of the label
                    int x = (int)((pageWidth - barcodeWidth) / 2);
                    int y = 10;  // Start 10 points from the top

                    // Draw the barcode
                    barcode.draw(g2d, x, y);

                    // Draw text below barcode (optional)
                    //g2d.setFont(new Font("Arial", Font.PLAIN, 8));  // Smaller font
                    //g2d.drawString(barcode.getData(), x, y + barcodeHeight + 5);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                return Printable.PAGE_EXISTS;
            }, pageFormat);

            // Print directly with specified attributes
            job.print(attributes);

            // Clear the input field for the next barcode
            barcodeDataField.setText("");
            barcodeDataField.requestFocus();

        } catch (BarcodeException | PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Error printing barcode: " + ex.getMessage());
        }
    }

}