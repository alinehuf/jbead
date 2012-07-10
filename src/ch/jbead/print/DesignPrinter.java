/** jbead - http://www.brunoldsoftware.ch
    Copyright (C) 2001-2012  Damian Brunold

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.jbead.print;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.swing.JOptionPane;

import ch.jbead.Localization;
import ch.jbead.Model;


public class DesignPrinter {

    private Model model;
    private Localization localization;
    private PrintSettings settings;
    private boolean withDraft;
    private boolean withCorrected;
    private boolean withSimulation;
    private boolean withReport;
    private boolean fullPattern = false;

    private List<PageLayout> pages = new ArrayList<PageLayout>();

    public DesignPrinter(Model model, Localization localization, PrintSettings settings,
            boolean withDraft, boolean withCorrected,
            boolean withSimulation, boolean withReport) {
        this.model = model;
        this.localization = localization;
        this.settings = settings;
        this.withDraft = withDraft;
        this.withCorrected = withCorrected;
        this.withSimulation = withSimulation;
        this.withReport = withReport;
    }

    public void setFullPattern(boolean fullPattern) {
        this.fullPattern = fullPattern;
    }

    private void layoutPages(PageFormat format) {
        int pageWidth = (int) format.getImageableWidth();
        int pageHeight = (int) format.getImageableHeight();
        PageLayout currentPage = new PageLayout(pageWidth);
        for (PartPrinter printer : getPartPrinters()) {
            List<Integer> columns = printer.layoutColumns(pageWidth, pageHeight);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                int columnWidth = columns.get(columnIndex);
                if (pageWidth < columnWidth) throw new RuntimeException("Column is wider than page!");
                if (currentPage.getUnusedWidth() < columnWidth) {
                    pages.add(currentPage);
                    currentPage = new PageLayout(pageWidth);
                }
                currentPage.addPart(new PagePart(printer, columnIndex, columnWidth));
            }
        }
        if (currentPage.getUnusedWidth() < pageWidth) {
            pages.add(currentPage);
        }
    }

    private List<PartPrinter> getPartPrinters() {
        List<PartPrinter> printers = new ArrayList<PartPrinter>();
        if (withReport) printers.add(new ReportInfosPrinter(model, localization));
        if (withDraft) printers.add(new DraftPrinter(model, localization, fullPattern));
        if (withCorrected) printers.add(new CorrectedPrinter(model, localization, fullPattern));
        if (withSimulation) printers.add(new SimulationPrinter(model, localization, fullPattern));
        if (withReport) printers.add(new BeadListPrinter(model, localization));
        return printers;
    }

    public void print(boolean showDialog) {
        try {
            int scroll = model.getScroll();
            try {
                model.setScroll(0);
                PrinterJob printjob = PrinterJob.getPrinterJob();
                if (settings.getService() != null) {
                    printjob.setPrintService(settings.getService());
                }
                PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet(settings.getAttributes());
                attrs.add(new JobName(getJobName(), null));
                if (showDialog) {
                    if (!printjob.printDialog(attrs)) return;
                    settings.setService(printjob.getPrintService());
                }
                PageFormat pageformat = printjob.getPageFormat(attrs);
                layoutPages(pageformat);
                Book book = new Book();
                for (PageLayout page : pages) {
                    book.append(page, pageformat);
                }
                printjob.setPageable(book);
                printjob.print(attrs);
            } finally {
                model.setScroll(scroll);
            }
        } catch (PrinterException e) {
            String msg = e.getMessage();
            if (msg == null) msg = e.toString();
            JOptionPane.showMessageDialog(null, localization.getString("print.failure") + ": " + msg);
        }
    }

    private String getJobName() {
        return "jbead " + System.currentTimeMillis() + " " + model.getFile().getName();
    }

}
