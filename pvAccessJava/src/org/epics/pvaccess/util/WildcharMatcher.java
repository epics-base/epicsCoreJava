package org.epics.pvaccess.util;

/**
 * A Unix-like wildchar matcher. Supported wild-characters: '*', '?'; sets:
 * [a-z], '!' negation Examples: '[a-g]li?n' matches 'florian' '[!abc]e'
 * matches 'smile' '[-z] matches 'a' Rules for sets: RegEx definition of the
 * valid set is: [!]?(-.)?((.-.)|(.))(.-)? a-z : match any letter between 'a'
 * and 'z' inclusively [-a : match everything up to and including 'a' (only
 * valid at beginning) a-] : match everything from 'a' (only valid at the end)
 * a   : match exactly 'a' !a  : not operator, match everything except 'a'
 * (only allowed at beginning) \a  : treat a literally (useful for specifying
 * '!]-' in sets. Note that     \t\b\n... are not processed.  Wildchar rules:
 * : match any number (0..inf) number of occurences of any character ?     :
 * match exactly and only one occurence of any character ab    : match exactly
 * 'ab' [..]: same as , but character must match the set.
 */
public class WildcharMatcher
{
	/** Value of initial state */
	private static final int INITIAL = 0;

	/** Value of final state */
	private static final int FINAL = 2;

	/** Value of error state */
	private static final int ERROR = 99;

	/** Any character (except control, unless escaped) */
	private static final int TOKEN_CHAR = 0;

	/** Token for end of set: ] */
	private static final int TOKEN_END = 1;

	/** Token for negation: */
	private static final int TOKEN_NOT = 2;

	/** Token for range specification: - */
	private static final int TOKEN_MINUS = 3;

	/**
	 * Transition table holds the nextState used in set parsing. Rows define
	 * states, columns define tokens. transitions[1][3] = 5 means: if in state
	 * 1 next token is 3, goto state 5
	 */
	private static final int[][] TRANSITIONS = {
		{ 1, FINAL, 3, 4 },
		{ 1, FINAL, ERROR, 5 },
		{ ERROR, ERROR, ERROR, ERROR },
		{ 1, FINAL, ERROR, 4 },
		{ 6, ERROR, ERROR, ERROR },
		{ 6, FINAL, ERROR, ERROR },
		{ 1, FINAL, ERROR, ERROR }
	};

	private static int getToken(final char ch)
	{
		switch (ch) {
		case ']':
			return TOKEN_END;

		case '!':
			return TOKEN_NOT;

		case '-':
			return TOKEN_MINUS;

		default:
			return TOKEN_CHAR;
		}
	}

	/**
	 * DFA for parsing set strings. DFA was obtained from JFlex using the rule
	 * : macro: CHAR = [^-\]\!] (everything except ], ! and - rule :
	 * [!]?(-{CHAR})?(({CHAR}-{CHAR})|({CHAR}))({CHAR}-)?\] Result of
	 * optimized NDFA is Character classes: class 0: [0-'
	 * ']['"'-',']['.'-'\']['^'-65535]  class 1: [']']  class 2: ['!']  class
	 * 3: ['-']  Transition graph (for class goto state) State 0: 0 -&gt; 1, 1 -&gt;
	 * 2, 2 -&gt; 3, 3 -&gt; 4 State 1: 0 -&gt; 1, 1 -&gt; 2, 3 -&gt; 5 State [FINAL] State
	 * 3: 0 -&gt; 1, 1 -&gt; 2, 3 -&gt; 4 State 4: 0 -&gt; 6 State 5: 0 -&gt; 6, 1 -&gt; 2 State
	 * 6: 0 -&gt; 1, 1 -&gt; 2
	 *
	 * @param pattern DOCUMENT ME!
	 * @param offset DOCUMENT ME!
	 * @param ch DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static boolean testSet(final String pattern, int offset,
	    final char ch)
	{
		final int n = pattern.length();

		int state = INITIAL;
		int nextToken = ' ';
		char nextChar = ' ';
		char ch1 = ' ';

		boolean found = false;

		boolean negate = false;

		while (!found) {
			// Check for offset in case of final state, which is over the limit,
			// if ] is at the end of the string.
			if (offset < n) {
				nextChar = pattern.charAt(offset);

				if (nextChar == '\\') {
					// Any escaped sequence is two characters, otherwise error will
					// be throws, since this is an invalid sequence anyway
					nextChar = pattern.charAt(offset + 1);
					nextToken = TOKEN_CHAR;
					offset++;
				} else {
					nextToken = getToken(nextChar);
				}
			}

			switch (state) {
			case INITIAL:

				if (nextToken == TOKEN_NOT) {
					negate = true;

					break;
				}

			// No break, states 0, 1, 3, 6 have same next condition.
			case 1:

				if (nextToken == TOKEN_END) {
					return true;
				}

			case 3:
			case 6:

				if (nextToken == TOKEN_CHAR) {
					found = (ch == nextChar);
					ch1 = nextChar;
				}

				break;

			case 4:

				// condition [-a...
				found = (ch <= nextChar);

				break;

			case 5:

				if (nextToken == TOKEN_CHAR) {
					// condition ...a-z...
					found = ((ch >= ch1) && (ch <= nextChar));
				}

				if (nextToken == TOKEN_END) {
					// condition ...a-]
					found = (ch >= ch1);
				}

				break;

			default:}

			// Lookup next state in transition table and check for valid pattern			
			state = TRANSITIONS[state][nextToken];

			if (state == ERROR) {
				return false;

				// don't bother, this is a no match anyway
				// throw new RuntimeException("Invalid pattern");
			}

			if (state == FINAL) {
				return found ^ negate;
			}

			offset++;
		}

		return found ^ negate;
	}

	/**
	 * Recursive method for parsing the string. To avoid copying the strings,
	 * the method accepts offset indices into both parameters.
	 *
	 * @param pattern Pattern used in parsing
	 * @param ofp Offset into pattern string (ofp &gt; 0)
	 * @param str String to test
	 * @param ofs Offset into test string (ofs &gt; 0);
	 *
	 * @return boolean Do the strings match
	 */
	public static boolean parse(final String pattern, final int ofp,
	    final String str, final int ofs)
	{
		final int lp = pattern.length();
		final int ls = str.length();

		// index into pattern string
		int ip = ofp;

		// index into test string;
		int is = ofs;

		char chp;
		char chs;

		// Match happens only, if we parse both strings exactly to the end
		while ((ip < lp)) {
			chp = pattern.charAt(ip);

			switch (chp) {
			case '[':

				// System.out.println("[ "+chp+", "+chs);
				// Each set must be close with a ], otherwise it is invalid.
				int end = pattern.indexOf("]", ip);

				if (end == -1) {
					return false;
				}

				// Is this set followed by a *	
				boolean isWildchar = ((end + 1) < lp)
					&& (pattern.charAt(end + 1) == '*');

				if (is < ls) {
					chs = str.charAt(is);
				} else {
					return parse(pattern, end + 2, str, is);
				}

				// Does this character match
				boolean thisChar = testSet(pattern, ip + 1, chs);

				// Check for single character match only if there is no
				// * at the end.
				if (!thisChar && !isWildchar) {
					// Return only if this character does not match
					return false;
				}

				if (isWildchar) {
					// If this character does not match, maybe this set
					// can be skipped entirely
					if (!thisChar) {
						ip = end + 2;

						break;
					}

					// Special case when this character matches, although
					// it should not: a[a-z]*z == az
					if (parse(pattern, end + 2, str, is)) {
						return true;
					}

					// Try to match next character
					if (parse(pattern, ip, str, is + 1)) {
						return true;
					}
				}

				// Single character matched, set was processed, since
				// no * was at the end.
				ip = end + 1;
				is++;

				break;

			case '?':

				// Obvious
				ip++;
				is++;

				break;

			case '*':

				// Trailing asterisk means that string matches till the end.
				// Also, checks if this is last char in the string
				if (ip + 1 == lp) {
					return true;
				}

				// Skip the *
				do {
					ip++;
					chp = pattern.charAt(ip);
				} while ((ip + 1 < lp) && (chp == '*'));

				// But perform a special check and solve it by recursing
				// from new position
				if (chp == '?') {
					if (parse(pattern, ip, str, is)) {
						return true;
					}
				}

				// Iterate through all possible matches in the test string
				int i = is;

				while (i < ls) {
					/*
					 * Would be nice to skip unmatchable characters,
					 * but it's too much fuss
					while ((i < ls) && (str.charAt(i) != chp)) {
					    i++;
					    if (i == ls) {
					        return false;
					    }
					}
					*/

					// Stupid brute force, but isn't as bad as it seems.
					// Try all possible matches in the test string.
					if (parse(pattern, ip, str, i)) {
						return true;
					}

					i++;
				}

				break;

			default:

				// Literal match
				if (is == ls || pattern.charAt(ip) != str.charAt(is)) {
					return false;
				}

				ip++;
				is++;
			}
		}

		// There could be several * at the end of the pattern, although the
		// test string is at the end.
		while ((ip < lp) && ((pattern.charAt(ip)) == '*')) {
			ip++;
		}

		// Same condition as with while loop
		return (is == ls) && (ip == lp);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param pattern DOCUMENT ME!
	 * @param str DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static boolean match(final String pattern, final String str)
	{
		return parse(pattern, 0, str, 0);
	}

}

/* __oOo__ */
