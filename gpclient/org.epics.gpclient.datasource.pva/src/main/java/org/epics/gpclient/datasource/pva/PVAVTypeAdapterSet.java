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
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import static org.epics.gpclient.datasource.pva.PVAToVTypes.*;
import org.epics.vtype.VByte;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VLong;
import org.epics.vtype.VShort;
import org.epics.vtype.VUByte;
import org.epics.vtype.VUInt;
import org.epics.vtype.VULong;
import org.epics.vtype.VUShort;

/**
 *
 * @author msekoranja
 */
class PVAVTypeAdapterSet implements PVATypeAdapterSet {
    
	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	
    @Override
    public Set<PVATypeAdapter> getAdapters() {
        return converters;
    }

    // Numeric scalars
    //-----------------

    final static PVATypeAdapter vDoubleAdapter = new PVATypeAdapter(VDouble.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvDouble)}) {
        @Override
        public VDouble createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vDoubleOf(valueField, message, disconnected);
            } else {
                return vDoubleOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vFloatAdapter = new PVATypeAdapter(VFloat.class,
            new String[]{"epics:nt/NTScalar:1.", "float"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvFloat)}) {
        @Override
        public VFloat createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vFloatOf(valueField, message, disconnected);
            } else {
                return vFloatOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vULongAdapter = new PVATypeAdapter(VULong.class,
            new String[]{"epics:nt/NTScalar:1.", "ulong"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvULong)}) {
        @Override
        public VULong createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vULongOf(valueField, message, disconnected);
            } else {
                return vULongOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vLongAdapter = new PVATypeAdapter(VLong.class,
            new String[]{"epics:nt/NTScalar:1.", "long"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvLong)}) {
        @Override
        public VLong createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vLongOf(valueField, message, disconnected);
            } else {
                return vLongOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vUIntAdapter = new PVATypeAdapter(VUInt.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvUInt)}) {
        @Override
        public VUInt createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vUIntOf(valueField, message, disconnected);
            } else {
                return vUIntOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vIntAdapter = new PVATypeAdapter(VInt.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvInt)}) {
        @Override
        public VInt createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vIntOf(valueField, message, disconnected);
            } else {
                return vIntOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vUShortAdapter = new PVATypeAdapter(VUShort.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvUShort)}) {
        @Override
        public VUShort createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vUShortOf(valueField, message, disconnected);
            } else {
                return vUShortOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vShortAdapter = new PVATypeAdapter(VShort.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvShort)}) {
        @Override
        public VShort createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vShortOf(valueField, message, disconnected);
            } else {
                return vShortOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vUByteAdapter = new PVATypeAdapter(VUByte.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvUByte)}) {
        @Override
        public VUByte createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vUByteOf(valueField, message, disconnected);
            } else {
                return vUByteOf(message, disconnected);
            }
        }
    };

    final static PVATypeAdapter vByteAdapter = new PVATypeAdapter(VByte.class,
            new String[]{"epics:nt/NTScalar:1.", "double"},
            new Field[]{fieldCreate.createScalar(ScalarType.pvByte)}) {
        @Override
        public VByte createValue(PVStructure message, PVField valueField, boolean disconnected) {
            if (valueField != null) {
                return vByteOf(valueField, message, disconnected);
            } else {
                return vByteOf(message, disconnected);
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
        newFactories.add(vDoubleAdapter);
        newFactories.add(vFloatAdapter);
        newFactories.add(vULongAdapter);
        newFactories.add(vLongAdapter);
        newFactories.add(vUIntAdapter);
        newFactories.add(vIntAdapter);
        newFactories.add(vUShortAdapter);
        newFactories.add(vShortAdapter);
        newFactories.add(vUByteAdapter);
        newFactories.add(vByteAdapter);

        // Add all ARRAYs
        newFactories.add(ToVArrayDouble);

        converters = Collections.unmodifiableSet(newFactories);
    }
}
