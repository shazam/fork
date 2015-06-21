package com.shazam.fork.summary;

public class CompositeSummaryPrinter implements SummaryPrinter {

    private final SummaryPrinter[] summaryPrinters;

    public CompositeSummaryPrinter(SummaryPrinter... summaryPrinters) {
        this.summaryPrinters = summaryPrinters;
    }

    @Override
    public void print(Summary summary) {
        for (SummaryPrinter summaryPrinter : summaryPrinters) {
            summaryPrinter.print(summary);
        }
    }
}
