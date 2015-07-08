/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.reporter;

import com.shazam.fork.io.HtmlGenerator;
import com.shazam.fork.reporter.html.*;
import com.shazam.fork.reporter.model.FlakinessReport;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.shazam.fork.io.Files.copyResource;
import static org.apache.commons.io.FileUtils.deleteDirectory;

public class HtmlFlakinessReportPrinter implements FlakinessReportPrinter {
    private static final String INDEX_FILENAME = "index.html";
    private static final String STATIC = "static";
    private final File output;
    private final HtmlGenerator htmlGenerator;
    private final TestToHtmlFlakinessReportConverter converter;
    private static final String[] STATIC_ASSETS = {
            "fork-history.css",
            "pool-flakiness.css",
    };

    public HtmlFlakinessReportPrinter(File output, HtmlGenerator htmlGenerator, TestToHtmlFlakinessReportConverter converter) {
        this.output = output;
        this.htmlGenerator = htmlGenerator;
        this.converter = converter;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void printReport(FlakinessReport flakinessReport) {
        recreateOutputFolder();
        copyAssets();
        HtmlFlakyTestIndex htmlFlakyTestIndex = converter.convertToIndex(flakinessReport);
        htmlGenerator.generateHtml("templates/index.html", output, INDEX_FILENAME, htmlFlakyTestIndex);

        List<HtmlFlakyTestPool> htmlFlakyTestPool = converter.convertToPools(flakinessReport);
        generatePoolHtmls(htmlFlakyTestPool, "templates/pool.html", new File(output, "pools"));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void generatePoolHtmls(List<HtmlFlakyTestPool> htmlFlakyTestPools, String htmlPoolTemplateResource, File output) {
        output.mkdirs();
        for (HtmlFlakyTestPool htmlFlakyTestPool : htmlFlakyTestPools) {
            htmlGenerator.generateHtml(htmlPoolTemplateResource, output, htmlFlakyTestPool.poolName + ".html", htmlFlakyTestPool);
        }
    }

    private void copyAssets() {
        File staticOutput = new File(output, STATIC);
        for (String asset : STATIC_ASSETS) {
            copyResource("/static/", asset, staticOutput);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void recreateOutputFolder() {
        try {
            deleteDirectory(output);
            output.mkdirs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
