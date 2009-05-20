/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;
import org.epics.pvData.pv.*;

/**
 * @author mrk
 *
 */
public interface PVCopyIterator {
    PVStructure getPVStructure();
    int getOffset(PVField pvField);
    PVField getPVField(int offset);
    void setModified(int offset);
    void clearModified(int offset);
    void clearModified();
    boolean isModified(int offset);
    boolean isModified();
    boolean startIterator(boolean  onlyModified);
    boolean next();
    int getOffset();
    PVField getPVField();
    
}
