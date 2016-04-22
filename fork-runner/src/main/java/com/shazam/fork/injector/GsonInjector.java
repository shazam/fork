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
package com.shazam.fork.injector;

import com.google.gson.*;
import com.shazam.fork.ComputedPooling;

import static com.shazam.fork.ComputedPooling.Characteristic.valueOf;

public class GsonInjector {
    private static final Gson GSON;
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();

    static {
        GSON_BUILDER.registerTypeAdapter(ComputedPooling.Characteristic.class, characteristicDeserializer());
        GSON_BUILDER.registerTypeAdapter(ComputedPooling.Characteristic.class, characteristicSerializer());
        GSON = GSON_BUILDER.create();
    }

    private static JsonSerializer<ComputedPooling.Characteristic> characteristicSerializer() {
        return (src, typeOfSrc, context) -> new JsonPrimitive(src.name());
    }

    private static JsonDeserializer<ComputedPooling.Characteristic> characteristicDeserializer() {
        return (json, typeOfT, context) -> valueOf(json.getAsJsonPrimitive().getAsString());
    }

    public static Gson gson() {
        return GSON;
    }
}
