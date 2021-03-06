/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * JUnit test for BitSet.
 * NOTE not complete.
 * @author mse
 *
 */
public class NumberFormatTest extends TestCase {


	public void testNumberFormat() {
		Locale.setDefault(new Locale("en", "US"));
		NumberFormat nf = new NumberFormatDouble("%12.2f");
		StringBuffer sb = new StringBuffer();
		FieldPosition fp = new FieldPosition(0);
		sb = nf.format(1.12, sb, fp);
		System.out.println(sb.toString());
		ParsePosition pp = new ParsePosition(0);
		double xxx = (nf.parse(sb.toString(),pp)).doubleValue();
		System.out.println("parse is "+ xxx);
	}
	
	static class NumberFormatDouble extends NumberFormat {
		private static final long serialVersionUID = -609491739577318372L;
		private final String format;
		
		private NumberFormatDouble (String format) {
			this.format = format;
		}
		@Override
		public StringBuffer format(double arg0, StringBuffer arg1,FieldPosition arg2) {
			return arg1.append(String.format(format, arg0));
		}
		@Override
		public StringBuffer format(long arg0, StringBuffer arg1,FieldPosition arg2) {
			throw new IllegalArgumentException("long not supported");
		}
		@Override
		public Number parse(String arg0, ParsePosition arg1) {
			return Double.parseDouble(arg0.substring(arg1.getIndex()));
		}
	}
}
