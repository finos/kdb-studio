package studio.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import studio.kdb.K;
import studio.kdb.KFormatContext;

class ExcelExporter {
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat();

    private static synchronized String sd(String s, java.util.Date x) {
        FORMATTER.applyPattern(s);
        return FORMATTER.format(x);
    }

    public void exportTableX(final JFrame frame, final JTable table, final File file,
                             final boolean openIt) {

        final TableModel model = table.getModel();
        final String message = "Exporting data to " + file.getAbsolutePath();
        final String note = "0% complete";
        String title = "Studio for kdb+";
        UIManager.put("ProgressMonitor.progressText", title);

        final int min = 0;
        final int max = 100;
        final ProgressMonitor pm = new ProgressMonitor(frame, message, note, min, max);
        pm.setMillisToDecideToPopup(100);
        pm.setMillisToPopup(100);
        pm.setProgress(0);

        Runnable runner = () -> {
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("KDB Studio Query");
                Row headerRow = sheet.createRow(0);
                CellStyle headerCellStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerCellStyle.setFont(headerFont);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(model.getColumnName(i));
                    cell.setCellStyle(headerCellStyle);
                }
                int maxRow = model.getRowCount();
                int lastProgress = 0;
                for (int i = 0; i < model.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Cell cell = row.createCell(j);
                        K.KBase b = (K.KBase) model.getValueAt(i, j);
                        if (!b.isNull()) {
                            if (table.getColumnClass(j) == K.KSymbolVector.class) {
                                cell.setCellValue(((K.KSymbol)b).s);
                            } else if (table.getColumnClass(j) == K.KDateVector.class) {
                                cell.setCellValue(sd("yyyy-MM-dd", ((K.KDate) b).toDate()));
                            } else if (table.getColumnClass(j) == K.KTimeVector.class) {
                                cell.setCellValue(sd("HH:mm:ss.SSS", ((K.KTime) b).toTime()));
                            } else if (table.getColumnClass(j) == K.KTimestampVector.class) {
                                char[] cs = sd("yyyy-MM-dd HH:mm:ss.SSS",
                                        ((K.KTimestamp) b).toTimestamp()).toCharArray();
                                cs[10] = 'T';
                                cell.setCellValue(new String(cs));
                            } else if (table.getColumnClass(j) == K.KMonthVector.class) {
                                cell.setCellValue(sd("yyyy-MM", ((K.Month) b).toDate()));
                            } else if (table.getColumnClass(j) == K.KMinuteVector.class) {
                                cell.setCellValue(sd("HH:mm", ((K.Minute) b).toDate()));
                            } else if (table.getColumnClass(j) == K.KSecondVector.class) {
                                cell.setCellValue(sd("HH:mm:ss", ((K.Second) b).toDate()));
                            } else if (table.getColumnClass(j) == K.KBooleanVector.class) {
                                cell.setCellValue(((K.KBoolean) b).b ? 1 : 0);
                            } else if (table.getColumnClass(j) == K.KDoubleVector.class) {
                                cell.setCellValue(((K.KDouble) b).toDouble());
                            } else if (table.getColumnClass(j) == K.KFloatVector.class) {
                                cell.setCellValue(((K.KFloat) b).f);
                            } else if (table.getColumnClass(j) == K.KLongVector.class) {
                                cell.setCellValue(((K.KLong) b).toLong());
                            } else if (table.getColumnClass(j) == K.KIntVector.class) {
                                cell.setCellValue(((K.KInteger) b).toInt());
                            } else if (table.getColumnClass(j) == K.KShortVector.class) {
                                cell.setCellValue(((K.KShort) b).s);
                            } else if (table.getColumnClass(j) == K.KCharacterVector.class) {
                                cell.setCellValue(
                                        new String(new char[] {((K.KCharacter) b).c}));
                            } else {
                                cell.setCellValue(b.toString(KFormatContext.NO_TYPE));
                            }
                        } else {
                            cell.setCellValue("");
                        }
                    }

                    if (pm.isCanceled()) {
                        break;
                    } else {
                        final int progress = (100 * i) / maxRow;
                        if (progress > lastProgress) {
                            lastProgress = progress;
                            final String note1 = "" + progress + "% complete";
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    pm.setProgress(progress);
                                    pm.setNote(note1);
                                }
                            });

                            Thread.yield();
                        }
                    }
                }

                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();
                if ((!pm.isCanceled()) && openIt) {
                    openTable(file);
                }
            } catch (Exception e) {
                StudioOptionPane.showError("\nThere was an error encoding the K types into Excel types.\n\n" +
                                e.getMessage() + "\n\n",
                        "Studio for kdb+");
            } finally {
                pm.close();
            }
        };

        Thread t = new Thread(runner);
        t.setName("Excel Exporter");
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public void openTable(File file) {
        try {
            Runtime run = Runtime.getRuntime();
            String lcOSName = System.getProperty("os.name").toLowerCase();
            boolean MAC_OS_X = lcOSName.startsWith("mac os x");
            Process p = null;
            if (MAC_OS_X) {
                p = run.exec("open " + file);
            } else {
                run.exec("cmd.exe /c start " + file);
            }
        } catch (IOException e) {
            StudioOptionPane.showError("\nThere was an error opening excel.\n\n" + e.getMessage() +
                            "\n\nPerhaps you do not have Excel installed,\nor .xls files are not associated with Excel",
                    "Studio for kdb+");
        }
    }
}
