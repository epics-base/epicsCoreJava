/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.text;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for frequently used NumberFormats.
 *
 * @author carcassi
 */
public final class NumberFormats {
    private static final Locale currentLocale;
    private static final DecimalFormatSymbols symbols;

    static {
        Locale newLocale = Locale.getDefault();
        DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(newLocale);
        newSymbols.setNaN("NaN");
        newSymbols.setInfinity("Infinity");
        currentLocale = newLocale;
        symbols = newSymbols;
    }

    private NumberFormats() {
        // Prevent instances
    }

    private static final Map<Integer, DecimalFormat> precisionFormats =
            new ConcurrentHashMap<Integer, DecimalFormat>();

    /**
     * Creates a new number format that formats a number with the given
     * number of precision digits.
     *
     * @param precision number of digits past the decimal point
     * @return a number format
     */
    private static DecimalFormat createPrecisionFormat(int precision) {
        if (precision < 0)
            throw new IllegalArgumentException("Precision must be non-negative");

        if (precision == 0)
            return new DecimalFormat("0", symbols);

        StringBuilder sb = new StringBuilder("0.");
        for (int i = 0; i < precision; i++) {
            sb.append("0");
        }
        return new DecimalFormat(sb.toString(), symbols);
    }

    /**
     * Returns a number format that formats a number with the given
     * number of precision digits. Parsing is not currently supported.
     *
     * @param precision number of digits past the decimal point
     * @return a number format
     */
    public static NumberFormat precisionFormat(int precision) {
        NumberFormat format = precisionFormats.get(precision);
        if (format == null) {
            precisionFormats.put(precision, createPrecisionFormat(precision));
        }
        return precisionFormats.get(precision);
    }

    private static final NumberFormat toStringFormat = new NumberFormat() {

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(number);
            return toAppendTo;
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(number);
            return toAppendTo;
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    /**
     * Returns the NumberFormat that uses the standard text representation
     * (toString) for numbers. Parsing is not currently supported.
     *
     * @return a number format
     */
    public static NumberFormat toStringFormat() {
        return toStringFormat;
    }

    @SuppressWarnings("serial")
    static class PrintfFormat extends java.text.NumberFormat {

        private final String format;

        public PrintfFormat(String printfFormat) {
            // probe precisionFormat
            boolean allOK = true;
            try {
                String.format(currentLocale, printfFormat, 0.0);
            } catch (Throwable th) {
                allOK = false;
            }
            // accept it if all is OK
            this.format = allOK ? printfFormat : null;
        }

        private String internalFormat(double number) {
            if (format != null) {
                return String.format(currentLocale, format, number);
            } else {
                return String.valueOf(number);
            }
        }

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(internalFormat(number));
            return toAppendTo;
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(internalFormat(number));
            return toAppendTo;
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    private static final Map<String, NumberFormat> printfFormats =
            new ConcurrentHashMap<String, NumberFormat>();

    public static NumberFormat printfFormat(String format) {
        if (format == null
                || format.trim().length() == 0
                || format.equals("%s")) {
            return NumberFormats.toStringFormat();
        } else {
            NumberFormat printfFormat = printfFormats.get(format);
            if (printfFormat != null) {
                return printfFormat;
            } else {
                printfFormat = new PrintfFormat(format);
                printfFormats.put(format, printfFormat);
                return printfFormat;
            }
        }
    }
}
