package org.example;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.PrinterResolution;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.print.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;
import org.w3c.dom.ls.LSOutput;

public class BarcodeGeneratorGUI extends JFrame {
    private JTextField barcodeDataField;
    private JPanel barcodePanel;
    private JLabel resultLabel;
    private static final String excelFilePath = "/Users/user/Downloads/name.xlsx/"; // Update with your actual file path

    public BarcodeGeneratorGUI() {
        // Set up the frame
        setTitle("Barcode Generator ---- created by @user");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create components
        JPanel inputPanel = new JPanel();
        barcodeDataField = new JTextField(20);
        JButton generateButton = new JButton("Generate Barcode");
        barcodePanel = new JPanel();

        // At the beginning when creating your panel
        inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));


        // After your existing component declarations (barcodeDataField, generateButton, etc.)
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JLabel resultLabel = new JLabel("Result: ");
        JLabel barcodeLabel = new JLabel("Enter barcode data: ");
        JLabel searchTitle = new JLabel("Search UPC barcode");

        // Make search field text bigger
        searchField.setFont(new Font("Arial", Font.PLAIN, 30));

        // Make barcode input field text bigger
        barcodeDataField.setFont(new Font("Arial", Font.PLAIN, 30));

        //Make the search button bigger
        searchButton.setFont(new Font("Arial", Font.PLAIN, 30));

        //Make the result label output bigger
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 30));

        //Make the generate barcode button bigger
        generateButton.setFont(new Font("Arial", Font.PLAIN, 30));

        //Make the Enter barcode data button bigger
        barcodeLabel.setFont(new Font("Arial", Font.PLAIN, 30));

        //Make the Search title bigger
        searchTitle.setFont(new Font("Arial", Font.PLAIN, 30));

        // Add action listener to the search button
        searchButton.addActionListener(e -> {
            String searchValue = searchField.getText();
            String result = searchExcelFile(searchValue, excelFilePath);


            if (!result.equals("No value found") && !result.startsWith("Error")) {
                // If a match is found, set the barcode data and generate it
                barcodeDataField.setText(result);
                generateBarcode(result);  // This will print the barcode
                resultLabel.setText("Result: " + result + " - Barcode printed");
            } else {
                resultLabel.setText("Result: " + result);
            }

            //resultLabel.setText("Result: " + result);


        });

        // Add these components to your inputPanel after your existing components
        inputPanel.add(searchTitle);
        inputPanel.add(searchField);
        inputPanel.add(searchButton);
        inputPanel.add(resultLabel);
        inputPanel.add(barcodeLabel);
        inputPanel.add(barcodeDataField);
        inputPanel.add(generateButton);

        //Printing with Enter
        barcodeDataField.addActionListener(e -> processBarcode());

        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(barcodePanel, BorderLayout.CENTER);

        // Add button action
        generateButton.addActionListener(e -> processBarcode());

        // Set frame properties
        pack();
        setSize(500, 300);
        setLocationRelativeTo(null);
    }

    private String searchExcelFile(String searchValue, String excelFilePath) {
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);  // Get first sheet
            String result = "No value found";

            // Iterate through each row in the sheet
            for (Row row : sheet) {
                Cell searchCell = row.getCell(1);  // First column (modify index as needed)

                // Check if cell matches search value
                if (searchCell != null && searchCell.toString().trim().equals(searchValue.trim())) {
                    Cell valueCell = row.getCell(0);  // Second column (modify index as needed)
                    if (valueCell != null) {
                        result = valueCell.toString();
                        break;
                    }
                }
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading Excel file: " + e.getMessage();
        }
    }

    private void processBarcode() {
        String data = barcodeDataField.getText();
        JLabel barcodeNotFound = new JLabel("Barcode not found in database!");

        //Make the message for not found barcode bigger
        barcodeNotFound.setFont(new Font("Arial", Font.PLAIN, 30));

        // First check if barcode exists in Excel
        if (checkBarcode(data)) {
            // If exists, generate and print the barcode
            generateBarcode(data);
        } else {
            JOptionPane.showMessageDialog(this, barcodeNotFound);
        }
    }


    private void generateBarcode(String data) {
        try {

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


    private boolean checkBarcode(String barcodeData) {
        try {
            FileInputStream file = new FileInputStream(excelFilePath);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);  // Get first sheet

            // Iterate through rows to find matching barcode
            for (Row row : sheet) {
                Cell cell = row.getCell(0);  // Assuming barcode is in first column
                if (cell != null) {
                    String cellValue = cell.toString();
                    if (cellValue.equals(barcodeData)) {
                        workbook.close();
                        file.close();
                        return true;
                    }
                }
            }

            workbook.close();
            file.close();
            return false;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading Excel file: " + e.getMessage());
            return false;
        }
    }




 /*   private void generateBarcode() {
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
    */


}
