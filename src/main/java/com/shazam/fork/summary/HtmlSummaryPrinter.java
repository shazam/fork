/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.summary;

import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.io.Resources;

import org.lesscss.LessCompiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Collections2.transform;
import static com.shazam.fork.summary.HtmlConverters.toHtmlLogCatMessages;
import static com.shazam.fork.summary.HtmlConverters.toHtmlSummary;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.IOUtils.closeQuietly;

public class HtmlSummaryPrinter implements SummaryPrinter {
	private static final String HTML_OUTPUT = "html";
	private static final String STATIC = "static";
	private static final String INDEX_FILENAME = "index.html";
	private static final String[] STATIC_ASSETS = {
		"bootstrap-responsive.min.css",
		"bootstrap.min.css",
		"fork.css",
		"bootstrap.min.js",
		"ceiling_android.png",
        "ceiling_android-green.png",
        "ceiling_android-red.png",
		"device.png",
		"icon-devices.png",
		"icon-log.png",
		"jquery.min.js",
		"log.png"
	};
	private final File htmlOutput;
	private final File staticOutput;
	private final MustacheFactory mustacheFactory;
	private final LogCatRetriever retriever;

	public HtmlSummaryPrinter(File rootOutput, LogCatRetriever retriever) {
		this.retriever = retriever;
		htmlOutput = new File(rootOutput, HTML_OUTPUT);
		staticOutput = new File(htmlOutput, STATIC);
		mustacheFactory = new DefaultMustacheFactory();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
	public void print(Summary summary) {
        htmlOutput.mkdirs();
		copyAssets();
		generateCssFromLess();
		HtmlSummary htmlSummary = toHtmlSummary(summary);
		generateIndexHtml(htmlSummary);
		generatePoolHtml(htmlSummary);
		generatePoolTestHtml(htmlSummary);
	}

	private void copyAssets() {
		for (String asset : STATIC_ASSETS) {
			copy(asset);
		}
	}

	private void copy(String asset) {
		InputStream resourceAsStream = getClass().getResourceAsStream("/static/" + asset);
		final File assetFile = new File(staticOutput, asset);
		try {
			copyInputStreamToFile(resourceAsStream, assetFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			closeQuietly(resourceAsStream);
		}
	}

	private void generateCssFromLess() {
		try {
			LessCompiler compiler = new LessCompiler();
			String less = Resources.toString(getClass().getResource("/spoon.less"), UTF_8);
			String css = compiler.compile(less);
			File cssFile = new File(staticOutput, "spoon.css");
			writeStringToFile(cssFile, css);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void generateIndexHtml(HtmlSummary htmlSummary) {
		FileWriter writer = null;
		try {
			Mustache mustache = mustacheFactory.compile("forkpages/index.html");
			File indexFile = new File(htmlOutput, INDEX_FILENAME);
			writer = new FileWriter(indexFile);
			mustache.execute(writer, htmlSummary);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			closeQuietly(writer);
		}
	}

    /**
     * Generates an HTML page for each pool, with multiple tests
     *
     * @param htmlSummary the summary of the pool
     */
	@SuppressWarnings("ResultOfMethodCallIgnored")
    private void generatePoolHtml(HtmlSummary htmlSummary) {
		FileWriter writer = null;
		Mustache mustache = mustacheFactory.compile("forkpages/pool.html");
		File poolsDir = new File(htmlOutput, "pools");
		poolsDir.mkdirs();
		for (HtmlPoolSummary pool : htmlSummary.pools) {
			try {
				File indexFile = new File(poolsDir, pool.plainPoolName + ".html");
				writer = new FileWriter(indexFile);
				mustache.execute(writer, pool);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				closeQuietly(writer);
			}
		}
	}

	/**
     * Genarates an HTML page for each test of each pool.
     *
	 * @param htmlSummary the summary containing the results
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
    private void generatePoolTestHtml(HtmlSummary htmlSummary) {
		FileWriter writer = null;
		Mustache mustache = mustacheFactory.compile("forkpages/pooltest.html");
		for (HtmlPoolSummary pool : htmlSummary.pools) {
			for (HtmlTestResult testResult : pool.testResults) {
				try {
					File poolsDir = new File(htmlOutput, "pools/" + pool.plainPoolName);
					poolsDir.mkdirs();
					File testFile = new File(poolsDir, testResult.plainClassName + "__" + testResult.plainMethodName + ".html");
					writer = new FileWriter(testFile);
					TestIdentifier testIdentifier = new TestIdentifier(testResult.plainClassName, testResult.plainMethodName);
					List<LogCatMessage> logCatMessages = retriever.retrieveLogCat(pool.plainPoolName, testResult.deviceSerial, testIdentifier);
					testResult.logcatMessages = transform(logCatMessages, toHtmlLogCatMessages());
					mustache.execute(writer, new Object[] {testResult, pool});
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					closeQuietly(writer);
				}
			}
		}
	}
}
