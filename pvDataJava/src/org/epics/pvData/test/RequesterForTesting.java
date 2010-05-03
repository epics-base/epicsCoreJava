/**
 * 
 */
package org.epics.pvData.test;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.Requester;

/**
 * @author mrk
 *
 */
public class RequesterForTesting implements Requester {
	private String requesterName = null;
	
	public RequesterForTesting(String requesterName) {
		this.requesterName = requesterName;
	}
	/* (non-Javadoc)
     * @see org.epics.ioc.util.Requester#getRequestorName()
     */
    public String getRequesterName() {
        return requesterName;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
     */
    public void message(String message, MessageType messageType) {
        System.out.println(message);
        
    }
}
