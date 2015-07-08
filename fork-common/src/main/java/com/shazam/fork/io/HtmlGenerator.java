/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.io;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class HtmlGenerator {
    private final MustacheFactory mustacheFactory;

    public HtmlGenerator(MustacheFactory mustacheFactory) {
        this.mustacheFactory = mustacheFactory;
    }

    public void generateHtml(String htmlTemplateResource, File output, String filename, Object... htmlModels) {
        FileWriter writer = null;
        try {
            Mustache mustache = mustacheFactory.compile(htmlTemplateResource);
            File indexFile = new File(output, filename);
            writer = new FileWriter(indexFile);
            mustache.execute(writer, htmlModels);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(writer);
        }
    }

}
