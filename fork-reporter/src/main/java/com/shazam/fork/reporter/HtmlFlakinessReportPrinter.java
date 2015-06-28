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

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.shazam.fork.io.Files.copyResource;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteDirectory;

public class HtmlFlakinessReportPrinter implements FlakinessReportPrinter {
    private static final String INDEX_FILENAME = "index.html";
    private static final String STATIC = "static";
    private final File output;
    private final HtmlGenerator htmlGenerator;
    private static final String[] STATIC_ASSETS = {
            "fork-history.css",
            "pool-flakiness.css",
    };

    public HtmlFlakinessReportPrinter(File output, HtmlGenerator htmlGenerator) {
        this.output = output;
        this.htmlGenerator = htmlGenerator;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void printReport(FlakinessReport flakinessReport) {
        recreateOutputFolder();
        copyAssets();
        HtmlFlakyTestIndex htmlFlakyTestIndex = HARDCODED_createIndexHtmlModel();
        htmlGenerator.generateHtml("templates/index.html", output, INDEX_FILENAME, htmlFlakyTestIndex);

        List<HtmlFlakyTestPool> htmlFlakyTestPool = HARDCODED_createPoolHtmlModel();
        generatePoolHtmls(htmlFlakyTestPool, "templates/pool.html", new File(output, "pools"));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void generatePoolHtmls(List<HtmlFlakyTestPool> htmlFlakyTestPools, String htmlPoolTemplateResource, File output) {
        output.mkdirs();
        for (HtmlFlakyTestPool htmlFlakyTestPool : htmlFlakyTestPools) {
            htmlGenerator.generateHtml(htmlPoolTemplateResource, output, htmlFlakyTestPool.poolName + ".html", htmlFlakyTestPool);
        }
    }

    private List<HtmlFlakyTestPool> HARDCODED_createPoolHtmlModel() {
        List<String> ids = asList(
                "1710","1711","1712","1713","1714","1715","1716","1717","1718","1719",
                "1720","1721","1722","1723","1724","1725","1726","1727","1728","1729",
                "1730","1731","1732","1733","1734");
        List<TestHistory> tests = asList(
                getTestHistory1("com.shazam.android.acceptancetests.musicdetails.streaming.MusicDetailsTaggingWithRdioTest", "userDoesNotSeeAdAfterSuccessfulTag"),
                getTestHistory1("com.shazam.android.acceptancetests.SanityTest", "hasFacebookInstalled"),
                getTestHistory1("com.shazam.android.acceptancetests.musicdetails.streaming.RdioTest", "userCanListenToMusic"),
                getTestHistory1("com.shazam.android.acceptancetests.notifications.gcm.Notificationstest", "goesToExploreScreen")
        );

        return asList(
                new HtmlFlakyTestPool("all=sw0-up", ids, tests),
                new HtmlFlakyTestPool("all=sw720-up", ids, tests)
        );
    }

    private TestHistory getTestHistory1(String className, String methodName) {
        return new TestHistory(className, methodName, asList(
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.MISSING),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.FAIL),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.FAIL),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.FAIL),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.FAIL),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.PASS),
                        new HtmlTestInstance(HtmlTestInstance.Status.FAIL)
                ));
    }

    private HtmlFlakyTestIndex HARDCODED_createIndexHtmlModel() {
        List<PoolOption> poolOptions = asList(
                new PoolOption("all=sw0-up", "Phones Sw0 up"),
                new PoolOption("all=sw720-up", "Tablets Sw720 up")
        );
        return new HtmlFlakyTestIndex("Shazam on Android Master", poolOptions);
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
