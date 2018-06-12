/*
 * Copyright (C) 2018 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.epics.pvaccess.util;

/**
 * This class is a simple wrapper around a boolean value. It can be used in lieu of
 * the class org.omg.CORBA.BooleanHolder in order to avoid the dependency to the 
 * Java 10 module java.corba, which is deprecated since Java 9 and subject to
 * removal in future versions.
 */
public class BooleanHolder {

	public boolean value = false;
	
	/**
	 * Instantiates an object of this class where the initial value is set to <code>false</code>.
	 */
	public BooleanHolder() {
	}
	
	/**
	 * Instantiates an object of this class.
	 * @param value The initial value.
	 */
	public BooleanHolder(boolean value) {
		this.value = value;
	}
}
