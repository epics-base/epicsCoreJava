/**
 * 
 */
package org.epics.pvaccess.client.pvds.util;

import java.nio.charset.Charset;

/**
 * @author msekoranja
 *
 */
public class StringToByteArraySerializator implements ToByteArraySerializator<String> {

	private static final Charset utf8Charset = Charset.forName("UTF-8");
	
	public static final StringToByteArraySerializator INSTANCE = new StringToByteArraySerializator();

	private StringToByteArraySerializator()
	{
	}
	
	@Override
    public byte[] toBytes(String str)
    {
    	return str.getBytes(utf8Charset);
    }

}
