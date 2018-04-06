/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.pva;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.gpclient.datasource.pva.adapters.PVAToVTypes;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import static org.epics.gpclient.datasource.pva.adapters.PVAToVTypes.*;

/**
 *
 * @author msekoranja
 */
public class PVAVTypeAdapterSet implements PVATypeAdapterSet {
    
	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	
    @Override
    public Set<PVATypeAdapter> getAdapters() {
        return converters;
    }
    
    // TODO startsWith
    
    //  -> VDouble
    final static PVATypeAdapter ToVDouble = new PVATypeAdapter(
            VDouble.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{
                fieldCreate.createScalar(ScalarType.pvDouble)
            }) {
        @Override
        public VDouble createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vDoubleOf(valueField, message, disconnected);
            } else {
                return vDoubleOf(message, disconnected);
            }
        }
    };

    //  -> VDoubleArray
    final static PVATypeAdapter ToVArrayDouble = new PVATypeAdapter(
    		VDoubleArray.class,
    		new String[] { "epics:nt/NTScalarArray:1.", "double[]" },
    		fieldCreate.createScalarArray(ScalarType.pvDouble))
    	{
            @Override
            public VDoubleArray createValue(final PVStructure message, PVField valueField, boolean disconnected) {
            	if (valueField != null)
            		return PVAToVTypes.vDoubleArrayOf(valueField, message, disconnected);
            	else
            		return PVAToVTypes.vDoubleArrayOf(message, disconnected);
            }
        };
        
    public static final Set<PVATypeAdapter> converters;
    
    static {
    	// preserve order
        Set<PVATypeAdapter> newFactories = new HashSet<PVATypeAdapter>();
        
        // Add all SCALARs
        newFactories.add(ToVDouble);

        // Add all ARRAYs
        newFactories.add(ToVArrayDouble);

        converters = Collections.unmodifiableSet(newFactories);
    }
}
