/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.array.mockapi;

import org.epics.util.array.ListNumber;

/**
 *
 * @author carcassi
 */
public interface NumericArrayField {
    public ListNumber get();
    public void put(int offset, ListNumber data);
}
