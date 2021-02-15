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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.epics.vtype.VType;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Custom Gson object creator having some TypeAdapters and options
 *
 * @author <a href="mailto:changj@frib.msu.edu">Genie Jhang</a>
 */

public class CustomGson {
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(VType.class, new GsonVTypeHandler())
                .registerTypeAdapter(Date.class, new GsonDateHandler())
                .registerTypeAdapter(DecimalFormat.class, new GsonDecimalFormatHandler())
                .serializeNulls()
                .serializeSpecialFloatingPointValues()
                .create();
    }
}
