/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.sound.sampled.Line;

public class Scanner {

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, KW_x/* x */, KW_X/* X */, KW_y/* y */, 
		KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, 
		KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, 
		KW_polar_r/* polar_r */, KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, 
		KW_log/* log */, KW_image/* image */, KW_int/* int */, KW_boolean/* boolean */, KW_url/* url */, 
		KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, OP_EXCL/* ! */, OP_Q/* ? */, 
		OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, OP_AND/* & */, 
		OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/*
																																																																																																																																																																																																							 * -
																																																																																																																																																																																																							 * >
																																																																																																																																																																																																							 */, OP_LARROW/*
																																																																																																																																																																																																										 * <
																																																																																																																																																																																																										 * -
																																																																																																																																																																																																										 */, LPAREN/* ( */, RPAREN/* ) */, LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}

	public static enum State {
		START, IN_DIGIT, IN_IDENT, AFTER_FWD_SLASH, AFTER_EQUALS, AFTER_LESS_THAN, AFTER_GRATER_THAN, 
		AFTER_EXCLAIMATION, AFTER_MINUS, AFTER_MUL, INSIDE_STRING_LITERAL, AFTER_SLASH_R, INSIDE_COMMENT, INSIDE_ESCAPE_SEQUENCE
	}

	/**
	 * Class to represent Tokens.
	 * 
	 * This is defined as a (non-static) inner class which means that each Token
	 * instance is associated with a specific Scanner instance. We use this when
	 * some token methods access the chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 * 
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			} else
				return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the enclosing "
		 * characters and convert escaped characters to the represented
		 * character. For example the two characters \ t in the char array
		 * should be converted to a single tab character in the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial
																// and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); // for completeness, line termination
											// chars not allowed in String
											// literals
						break;
					case 'n':
						sb.append('\n'); // for completeness, line termination
											// chars not allowed in String
											// literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition: This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)
					+ "," + pos + "," + length + "," + line + "," + pos_in_line
					+ "]";
		}

		/**
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle
		 * .com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object is the same
		 * class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated
		 * with.
		 * 
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/**
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.
	 */
	static final char EOFchar = 0;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input
																				// string
																				// terminated
																				// with
																				// null
																				// char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}

	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		int pos = 0;
		State state = State.START;
		StringBuffer identifierSB = new StringBuffer();
		int line = 1;
		int posInLine = 1;
		System.out.println("chars length:" + chars.length);
		int startPos = 0;
		while (pos < chars.length) {
			char ch = chars[pos];
			switch (state) {
			// Case START of the DFA machine starts here
			case START: {
				startPos = pos;
				switch (ch) {
				// white space
				case 32: {
					pos++;
					posInLine++;
				}
					break;
				// horizontal tab
				case 9: {
					pos++;
					posInLine++;
				}
					break;
				// form feed
				case 12: {
					pos++;
					posInLine++;
				}
					break;

				case '?': {
					tokens.add(new Token(Kind.OP_Q, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case ':': {
					tokens.add(new Token(Kind.OP_COLON, startPos, pos
							- startPos + 1, line, posInLine));
					posInLine++;
					pos++;
				}
					break;
				case '&': {
					tokens.add(new Token(Kind.OP_AND, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case '|': {
					tokens.add(new Token(Kind.OP_OR, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case '+': {
					tokens.add(new Token(Kind.OP_PLUS, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case '%': {
					tokens.add(new Token(Kind.OP_MOD, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case '@': {
					tokens.add(new Token(Kind.OP_AT, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case '(': {
					tokens.add(new Token(Kind.LPAREN, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case ')': {
					tokens.add(new Token(Kind.RPAREN, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case '[': {
					tokens.add(new Token(Kind.LSQUARE, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case ']': {
					tokens.add(new Token(Kind.RSQUARE, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case ';': {
					tokens.add(new Token(Kind.SEMI, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;
				case ',': {
					tokens.add(new Token(Kind.COMMA, startPos, pos - startPos
							+ 1, line, posInLine));
					pos++;
					posInLine++;
				}
					break;

				case EOFchar: {
					// tokens.add(new Token(Kind.EOF,startPos, pos-startPos+1,
					// line, posInLine));
					tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
					pos++;
					posInLine++;
				}
					break;

				// handling cases for new line
				case '\n': {
					posInLine = 1;
					line++;
					pos++;
				}
					break;
				// handling cases for new line
				case '\r': {
					state = State.AFTER_SLASH_R;
					posInLine = 1;
					line++;
					pos++;
				}
					break;

				case '=': {
					state = State.AFTER_EQUALS;
					pos++;
					posInLine++;
				}
					break;

				case '/': {
					state = state.AFTER_FWD_SLASH;
					pos++;
					posInLine++;
				}
					break;

				case '<': {
					state = state.AFTER_LESS_THAN;
					pos++;
					posInLine++;

				}
					break;

				case '>': {

					state = State.AFTER_GRATER_THAN;
					pos++;
					posInLine++;

				}
					break;

				case '!': {

					state = State.AFTER_EXCLAIMATION;
					pos++;
					posInLine++;

				}
					break;

				case '-': {

					state = State.AFTER_MINUS;
					pos++;
					posInLine++;

				}
					break;

				case '*': {

					state = State.AFTER_MUL;
					pos++;
					posInLine++;

				}
					break;

				case '"': {
					state = State.INSIDE_STRING_LITERAL;
					pos++;
					posInLine++;
				}
					break;

				default: {
					if (Character.isDigit(ch)) {
						if (ch == '0') {
							tokens.add(new Token(Kind.INTEGER_LITERAL,
									startPos, pos - startPos + 1, line,
									posInLine));
							pos++;
							posInLine++;
						} else
							state = State.IN_DIGIT;
					}

					else if (isIdentifierStart(ch)) {
						identifierSB.delete(0, identifierSB.length());
						state = State.IN_IDENT;
					}

					else
						throw new LexicalException(
								"The Scanner cannot scan the Character found at line:"
										+ line + " character: " + posInLine
										+ " " + ch, pos);
				}

				}
			}
				break;
			// Case START of the DFA machine ends here

			case IN_DIGIT: {
				if (Character.isDigit(ch)) {
					pos++;
					posInLine++;
				} else {
					state = State.START;
					tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos
							- startPos, line, posInLine - (pos - startPos)));
				}
			}
				break;
			case IN_IDENT: {

				if (isIdentifierPart(ch)) {
					identifierSB.append(ch);
					pos++;
					posInLine++;
				} else {
					state = State.START;
					String identifier = identifierSB.toString();
					if (isAReservedWord(identifier)) {
						tokens.add(new Token(typeOfReservedWord(identifier),
								startPos, pos - startPos, line, posInLine
										- (pos - startPos)));
					} else {
						tokens.add(new Token(Kind.IDENTIFIER, startPos, pos
								- startPos, line, posInLine - (pos - startPos)));
					}
				}

			}
				break;

			case AFTER_LESS_THAN: {
				state = State.START;
				if (chars[pos] == '=') {
					tokens.add(new Token(Kind.OP_LE, startPos, pos - startPos
							+ 1, line, posInLine - (pos - startPos)));
					pos++;
					posInLine++;
				} else if (chars[pos] == '-') {
					tokens.add(new Token(Kind.OP_LARROW, startPos, pos
							- startPos + 1, line, posInLine - (pos - startPos)));
					pos++;
					posInLine++;
				} else {
					tokens.add(new Token(Kind.OP_LT, startPos, pos - 1
							- startPos + 1, line, posInLine - 1));
				}
			}
				break;

			case AFTER_GRATER_THAN: {
				state = State.START;
				if (chars[pos] == '=') {
					tokens.add(new Token(Kind.OP_GE, startPos, pos - startPos
							+ 1, line, posInLine - (pos - startPos)));
					pos++;
					posInLine++;
				} else {
					tokens.add(new Token(Kind.OP_GT, startPos, pos - 1
							- startPos + 1, line, posInLine - 1));
				}
			}
				break;

			case AFTER_MUL: {
				state = State.START;
				if (chars[pos] == '*') {
					tokens.add(new Token(Kind.OP_POWER, startPos, pos
							- startPos + 1, line, posInLine - (pos - startPos)));
					pos++;
					posInLine++;
				} else {
					tokens.add(new Token(Kind.OP_TIMES, startPos, pos - 1
							- startPos + 1, line, posInLine - 1));
				}
			}
				break;

			case AFTER_EXCLAIMATION: {
				state = State.START;
				if (chars[pos] == '=') {
					tokens.add(new Token(Kind.OP_NEQ, startPos, pos - startPos
							+ 1, line, posInLine - (pos - startPos)));
					pos++;
					posInLine++;
				} else {
					tokens.add(new Token(Kind.OP_EXCL, startPos, pos - 1
							- startPos + 1, line, posInLine - 1));
				}
			}
				break;

			case AFTER_MINUS: {
				state = State.START;
				if (chars[pos] == '>') {
					tokens.add(new Token(Kind.OP_RARROW, startPos, pos
							- startPos + 1, line, posInLine - (pos - startPos)));
					pos++;
					posInLine++;
				} else {
					tokens.add(new Token(Kind.OP_MINUS, startPos, pos - 1
							- startPos + 1, line, posInLine - 1));
				}
			}
				break;

			case AFTER_SLASH_R: {
				if (ch == '\n')
					pos++;
				else
					state = State.START;
			}
				break;

			case AFTER_FWD_SLASH: {
				if (ch == '/') {
					// start of a comment, ignore all stuff in the next state
					// till new line is encountered
					state = State.INSIDE_COMMENT;
					pos++;
					posInLine++;

				} else {
					tokens.add(new Token(Kind.OP_DIV, startPos, pos - startPos,
							line, posInLine - 1));
					state = State.START;
				}
			}
				break;

			case INSIDE_COMMENT: {
				if (ch == '\n') {
					posInLine = 1;
					line++;
					pos++;
					state = State.START;
				}

				else if (ch == '\r') {
					state = State.AFTER_SLASH_R;
					posInLine = 1;
					line++;
					pos++;
				} else {
					posInLine++;
					pos++;
				}
			}
				break;

			case AFTER_EQUALS: {
				if (ch == '=') {
					int len = pos - startPos + 1;
					tokens.add(new Token(Kind.OP_EQ, startPos, len, line,
							posInLine - len + 1));
					pos++;
					posInLine++;
					state = State.START;

				} else {
					int len = pos - 1 - startPos + 1;
					tokens.add(new Token(Kind.OP_ASSIGN, startPos, len, line,
							posInLine - len));
					state = State.START;
				}
			}
				break;

			case INSIDE_STRING_LITERAL: {
				if (ch == '"') {
					pos++;
					posInLine++;
					state = State.START;
					tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos
							- startPos, line, posInLine - (pos - startPos)));

				} else if (ch == EOFchar) {
					throw new LexicalException(
							"String literal unclosed at line:" + line, pos);
				} else if (ch == '\n') {
					throw new LexicalException(
							"String literal encountered new line at line:"
									+ line, pos);
				} else if (ch == '\r') {
					throw new LexicalException(
							"String literal encountered new line at line:"
									+ line, pos);
				} else if (ch == '\\') {
					pos++;
					posInLine++;
					state = State.INSIDE_ESCAPE_SEQUENCE;
				} else {
					pos++;
					posInLine++;
				}

			}
				break;

			case INSIDE_ESCAPE_SEQUENCE: {
				HashSet<Character> hs = new HashSet<Character>();
				hs.add('b');
				hs.add('t');
				hs.add('n');
				hs.add('f');
				hs.add('r');
				hs.add('"');
				hs.add('\'');
				hs.add('\\');

				if (hs.contains(ch)) {
					pos++;
					posInLine++;
					state = State.INSIDE_STRING_LITERAL;
				} else {
					throw new LexicalException(
							"Illegal Escape Sequence encountered at line:"
									+ line + " position:" + posInLine, pos);
				}
			}
				break;

			}
		}

		/*
		 * int line = 1; int posInLine = 1; tokens.add(new Token(Kind.EOF,
		 * pos,0, line, posInLine));
		 */
		return this;

	}

	/**
	 * The following function checks if the input argument can be a valid start
	 * of an identifier for our language or no
	 */

	private boolean isIdentifierStart(char c) {
		HashSet<Character> startSet = new HashSet<Character>();

		for (int i = 97; i <= 122; i++) {
			startSet.add((char) i);
		}
		for (int i = 65; i <= 90; i++) {
			startSet.add((char) i);
		}
		startSet.add('$');
		startSet.add('_');

		return startSet.contains(c);
	}

	/**
	 * The following function checks if the character passed in the arguments
	 * can be a part of a valid identifier in the language
	 */

	private boolean isIdentifierPart(char c) {
		HashSet<Character> startSet = new HashSet<Character>();

		for (int i = 97; i <= 122; i++) {
			startSet.add((char) i);
		}
		for (int i = 65; i <= 90; i++) {
			startSet.add((char) i);
		}
		startSet.add('$');
		startSet.add('_');
		for (int i = 48; i <= 57; i++) {
			startSet.add((char) i);
		}
		return startSet.contains(c);
	}

	/*
	 * Initializes the hashmap for reserved words
	 */
	private HashMap<String, Kind> initializeReservedWordList() {

		HashMap<String, Kind> reservedWords = new HashMap<String, Kind>();
		reservedWords.put("true", Kind.BOOLEAN_LITERAL);
		reservedWords.put("false", Kind.BOOLEAN_LITERAL);
		reservedWords.put("x", Kind.KW_x);
		reservedWords.put("X", Kind.KW_X);
		reservedWords.put("y", Kind.KW_y);
		reservedWords.put("Y", Kind.KW_Y);
		reservedWords.put("r", Kind.KW_r);
		reservedWords.put("R", Kind.KW_R);
		reservedWords.put("a", Kind.KW_a);
		reservedWords.put("A", Kind.KW_A);
		reservedWords.put("Z", Kind.KW_Z);
		reservedWords.put("DEF_X", Kind.KW_DEF_X);
		reservedWords.put("DEF_Y", Kind.KW_DEF_Y);
		reservedWords.put("SCREEN", Kind.KW_SCREEN);
		reservedWords.put("cart_x", Kind.KW_cart_x);
		reservedWords.put("cart_y", Kind.KW_cart_y);
		reservedWords.put("polar_a", Kind.KW_polar_a);
		reservedWords.put("polar_r", Kind.KW_polar_r);
		reservedWords.put("abs", Kind.KW_abs);
		reservedWords.put("sin", Kind.KW_sin);
		reservedWords.put("cos", Kind.KW_cos);
		reservedWords.put("atan", Kind.KW_atan);
		reservedWords.put("log", Kind.KW_log);
		reservedWords.put("image", Kind.KW_image);
		reservedWords.put("int", Kind.KW_int);
		reservedWords.put("boolean", Kind.KW_boolean);
		reservedWords.put("url", Kind.KW_url);
		reservedWords.put("file", Kind.KW_file);

		return reservedWords;
	}

	/*
	 * returns a boolean which states if an identifier string is a reserved word
	 * or no
	 */
	private boolean isAReservedWord(String word) {
		return this.initializeReservedWordList().containsKey(word);
	}

	/*
	 * returns the type of reserved word if a string is a reserved word
	 */
	private Kind typeOfReservedWord(String word) {
		return this.initializeReservedWordList().get(word);
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This
	 * means that the next call to nextToken or peek will return the same Token
	 * as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}