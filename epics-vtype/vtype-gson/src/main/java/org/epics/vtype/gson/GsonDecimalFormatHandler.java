/*
 * Copyright (C) 2020 Facility for Rare Isotope Beams
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact Information: Facility for Rare Isotope Beam,
 *                      Michigan State University,
 *                      East Lansing, MI 48824-1321
 *                      http://frib.msu.edu
 */
package org.epics.vtype.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

/**
 * Gson De/serializer for DecimalFormat object
 *
 * @author <a href="mailto:changj@frib.msu.edu">Genie Jhang</a>
 */

public class GsonDecimalFormatHandler implements JsonSerializer<DecimalFormat>, JsonDeserializer<DecimalFormat> {
    public JsonElement serialize(DecimalFormat src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toPattern());
    }

    public DecimalFormat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new DecimalFormat(json.getAsString());
    }
}
