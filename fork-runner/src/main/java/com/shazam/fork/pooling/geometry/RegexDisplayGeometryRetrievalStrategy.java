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
package com.shazam.fork.pooling.geometry;

import com.android.ddmlib.IDevice;
import com.shazam.fork.model.DisplayGeometry;
import com.shazam.fork.system.adb.CollectingShellOutputReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Runs a command on a device and sniffs its output for geometry by regex. Main aim is to determine shortest width.
 */
public class RegexDisplayGeometryRetrievalStrategy implements DisplayGeometryRetrievalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RegexDisplayGeometryRetrievalStrategy.class);
    private final String command;
    private final String[] regexes;
    private final CommandOutputLogger commandOutputLogger;

    public RegexDisplayGeometryRetrievalStrategy(String command, CommandOutputLogger commandOutputLogger, String... regexes) {
        this.command = command;
        this.commandOutputLogger = commandOutputLogger;
        this.regexes = regexes;
    }

    @Override
    public DisplayGeometry retrieveGeometry(IDevice device) {
        return getDisplayGeometry(device, commandOutputLogger);
    }

    //TODO Ugly method. Break this up.
    DisplayGeometry getDisplayGeometry(IDevice device, CommandOutputLogger commandOutputLogger) {
        DisplayGeometry displayGeometry = null;
        CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
        String serial = device.getSerialNumber();

        try {
            device.executeShellCommand(command, receiver);
            String commandOutput = receiver.getOutput();
            commandOutputLogger.logCommandOutput(serial, commandOutput);
            Pattern xxnnn = Pattern.compile("(\\w+?)(\\d+)");
            for (String regex : regexes) {
                Pattern pattern;
                try {
                    pattern = Pattern.compile(regex);
                } catch (PatternSyntaxException e) {
                    logger.warn("Pattern error for " + regex, e);
                    continue;
                }
                Matcher promising = pattern.matcher(commandOutput);
                while (promising.find()) {
                    DisplayGeometry found;
                    if (promising.group(1).matches("\\d+")) {
                        // EXPECT px, py, scale-or-dpi-or-empty-for-1.0
                        if (promising.groupCount() != 3) {
                            logger.warn("Expected 3 groups for " + regex);
                            break;
                        }
                        int d1 = Integer.parseInt(promising.group(1));
                        int d2 = Integer.parseInt(promising.group(2));
                        String dd = promising.group(3);
                        double density = "".equals(dd)
                                        ? 1.0 : dd.matches("\\d+")
                                        ? Integer.parseInt(dd) / 160.0 : Double.parseDouble(dd);
                        found = new DisplayGeometry(d1, d2, density);
                    } else {
                        // EXPECT xxddd for e.g. sw720
                        Map<String, Integer> map = new HashMap<>();
                        for (int i = 1; i <= promising.groupCount(); ++i) {
                            Matcher param = xxnnn.matcher(promising.group(i));
                            if (param.matches()) {
                                map.put(param.group(1), Integer.parseInt(param.group(2)));
                            }
                        }
                        Integer sw = map.get("sw");
                        if (sw == null) {
                            continue;
                        }
                        found = new DisplayGeometry(sw);
                    }
                    if (displayGeometry == null) {
                        displayGeometry = found;
                    } else {
                        if (!found.matches(displayGeometry)) {
                            logger.warn("Conflicting geometry found for {}: {} and {} with {}", serial,
                                    displayGeometry, found, regex);
                            displayGeometry = null;
                            break;
                        }
                    }
                }
                if (displayGeometry != null) {
                    logger.debug("Device {} found {} with {} {}", serial, displayGeometry, command, regex);
                    break;
                }
            }
            return displayGeometry;
        } catch (Exception e) { // No special handling for now, so catch any exception
            logger.warn("Error when executing " + command + " on "+ serial, e);
        }

        return null;
    }
}
