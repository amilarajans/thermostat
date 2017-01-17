/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.storage.mongodb.internal;

import java.util.ArrayList;
import java.util.List;

import org.bson.AbstractBsonReader;
import org.bson.AbstractBsonReader.State;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DoubleCodec;
import org.bson.codecs.EncoderContext;

public class DoubleArrayCodec implements Codec<double[]> {
    
    private final DoubleCodec primitiveCodec = new DoubleCodec();
    
    @Override
    public void encode(BsonWriter writer, double[] value,
            EncoderContext encoderContext) {
        writer.writeStartArray();
        for (double d: value) {
            writer.writeDouble(d);
        }
        writer.writeEndArray();
    }

    @Override
    public Class<double[]> getEncoderClass() {
        return double[].class;
    }

    @Override
    public double[] decode(BsonReader reader, DecoderContext decoderContext) {
        if (reader.getCurrentBsonType() != BsonType.ARRAY) {
            throw new IllegalStateException("Expected array type, was: " + reader.getCurrentBsonType());
        }
        reader.readStartArray();
        List<Double> values = new ArrayList<>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            double val = primitiveCodec.decode(reader, decoderContext);
            values.add(val);
        }
        reader.readEndArray();
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

}
