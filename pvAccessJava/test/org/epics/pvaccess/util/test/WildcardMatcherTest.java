/*
 * Copyright (c) 2004 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.pvaccess.util.test;

import junit.framework.TestCase;

import org.epics.pvaccess.util.WildcharMatcher;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class WildcardMatcherTest extends TestCase {

   	/**
	 * Constructor for WildcardMatcherTest.
	 * @param methodName
	 */
	public WildcardMatcherTest(String methodName) {
		super(methodName);
	}

	/**
	 * Conversion test.
	 */
	public void testWildcard()
	{
		assertTrue(WildcharMatcher.match("[-aa-]*", "01 abAZ"));
		assertTrue(WildcharMatcher.match("[\\!a\\-bc]*", "!!!b-bb-"));
		assertTrue(WildcharMatcher.match("*zz", "zz"));
		assertTrue(WildcharMatcher.match("[abc]*zz", "zz"));

		assertTrue(!WildcharMatcher.match("[!abc]*a[def]", "xyzbd"));
		assertTrue(WildcharMatcher.match("[!abc]*a[def]", "xyzad"));
		assertTrue(WildcharMatcher.match("[a-g]l*i?", "gloria"));
		assertTrue(WildcharMatcher.match("[!abc]*e", "smile"));
		assertTrue(WildcharMatcher.match("[-z]", "a"));
		assertTrue(!WildcharMatcher.match("[]", ""));
		assertTrue(WildcharMatcher.match("[a-z]*", "java"));
		assertTrue(WildcharMatcher.match("*.*", "command.com"));
		assertTrue(!WildcharMatcher.match("*.*", "/var/etc"));
		assertTrue(WildcharMatcher.match("**?*x*[abh-]*Q", "XYZxabbauuZQ"));
	}
}
