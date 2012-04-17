/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.accessSecurity;

/**
 * @author mrk
 *
 */
public class AccessSecurityLevelFactory {
	
	public static AccessSecurityLevel get() {
		return accessSecurityLevel;
	}
	
	private static final AccessSecurityLevel accessSecurityLevel = new AccessSecurityLevelImpl();
	private static final String[] names = {"internal","configuration","calibration","runtime"};
	private static class AccessSecurityLevelImpl implements AccessSecurityLevel {
		/* (non-Javadoc)
		 * @see org.epics.pvdata.accessSecurity.AccessSecurityLevel#getLevel(java.lang.String)
		 */
		@Override
		public int getLevel(String name) throws NoSuchFieldException {
			for(int index=0; index<names.length; index++) {
				if(name.equals(names[index])) return index;
			}
			throw new NoSuchFieldException(name);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.accessSecurity.AccessSecurityLevel#getName(int)
		 */
		@Override
		public String getName(int level) throws IndexOutOfBoundsException {
			if(level<0 || level>=names.length) {
				throw new IndexOutOfBoundsException();
			}
			return names[level];
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.accessSecurity.AccessSecurityLevel#getNames()
		 */
		@Override
		public String[] getNames() {
			return names;
		}
	}

}
