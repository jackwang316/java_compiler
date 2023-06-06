package lexicalAnalyzer;


import logging.TanLogger;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.PushbackCharStream;
import tokens.CharacterLiteralToken;
import tokens.FloatingLiteralToken;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
import tokens.StringLiteralToken;
import tokens.IntegerLiteralToken;
import tokens.Token;

import static lexicalAnalyzer.PunctuatorScanningAids.*;

public class LexicalAnalyzer extends ScannerImp implements Scanner {
	private static final int OCTAL_ASCII_MIN = 0;
	private static final int OCTAL_ASCII_MAX = 127;
	private static final int PRINTABLE_ASCII_MIN = 32;
	private static final int PRINTABLE_ASCII_MAX = 126;
	private static final int OCTAL_DIGIT_MIN = 0;
	private static final int OCTAL_DIGIT_MAX = 7;
	private static final int OCTAL_LENGTH = 3;

	public static LexicalAnalyzer make(String filename) {
		InputHandler handler = InputHandler.fromFilename(filename);
		PushbackCharStream charStream = PushbackCharStream.make(handler);
		return new LexicalAnalyzer(charStream);
	}

	public LexicalAnalyzer(PushbackCharStream input) {
		super(input);
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// Token-finding main dispatch	

	@Override
	protected Token findNextToken() {
		LocatedChar ch = nextNonWhitespaceChar();
		while(ch.isCommentStart()){	//Uses while to handle cases where comments end with # that causes lexical error.
			skipComment();
			ch = nextNonWhitespaceChar();
		}
		if(ch.isDigit()) {
			return scanNumber(ch);
		}
		else if(ch.isCharacterStart()) {
			return scanChar(ch);
		}
		else if(ch.isAlpha()) {
			return scanIdentifier(ch);
		}
		else if(ch.isStringStartorEnd()) {
			return scanString(ch);
		}
		else if(isPunctuatorStart(ch)) {
			return PunctuatorScanner.scan(ch, input);
		}
		else if(isEndOfInput(ch)) {
			return NullToken.make(ch);
		}
		else {
			lexicalError(ch);
			return findNextToken();
		}
	}

	private Token scanString(LocatedChar ch) {
		StringBuffer buffer = new StringBuffer();
		LocatedChar cur = input.next();
		
		while(!cur.isStringStartorEnd()) {
			buffer.append(cur.getCharacter());
			cur = input.next();
		}
		return StringLiteralToken.make(ch, buffer.toString());
	}

	private Token scanChar(LocatedChar ch) {
		StringBuffer buffer = new StringBuffer();
		LocatedChar cur = ch;
		if(ch.isASCIICharStart()) {
			for(int i = 0; i < OCTAL_LENGTH; i++) {
				cur = input.next();
				if(!cur.isDigit()) {
					lexicalError("Octal digit is not a digit", cur);
					input.pushback(cur);
					return findNextToken();
				}
				
				int val = cur.getCharacter() - '0';
				if(val > OCTAL_DIGIT_MAX || val < OCTAL_DIGIT_MIN) {
					lexicalError("Octal digit is outside acceptable range", cur);
					return findNextToken();
				}
				buffer.append(cur.getCharacter());
			}
			cur = input.next();
			if(cur.isDigit()) {
				lexicalError("Too many octal digits provided", cur);
				return findNextToken();
			}
			input.pushback(cur);
			int octalToDec = Integer.parseInt(buffer.toString(), 8);
			if(octalToDec > OCTAL_ASCII_MAX || octalToDec < OCTAL_ASCII_MIN) {
				lexicalError("Octal value outside acceptable range", cur);
				return findNextToken();
			}
			String result = Character.toString((char) octalToDec);
			return CharacterLiteralToken.make(ch, result);
		} else {
			cur = input.next();
			char c = cur.getCharacter();
			if(cur.getCharacter() > PRINTABLE_ASCII_MAX || cur.getCharacter() < PRINTABLE_ASCII_MIN) {
				lexicalError("Character outside valid range", cur);
				return findNextToken();
			}
			cur = input.next();
			if(!cur.isCharacterEnd()) {
				lexicalError("Character not ended with ' ", cur);
				input.pushback(cur);
			}
			return CharacterLiteralToken.make(ch, Character.toString(c));
		}
	}

	private LocatedChar nextNonWhitespaceChar() {
		LocatedChar ch = input.next();
		while(ch.isWhitespace()) {
			ch = input.next();
		}
		return ch;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Integer lexical analysis	

	private Token scanNumber(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentDigits(buffer);

		if (input.peek().getCharacter() == '.') {
			LocatedChar decimal = input.next();
			buffer.append(decimal.getCharacter());
			if (!input.peek().isDigit()) {
				lexicalError("Malformed floating point literal", decimal);
				return findNextToken();
			}
			appendSubsequentDigits(buffer);
			LocatedChar next = input.next();
			if (next.getCharacter() == 'e' || next.getCharacter() == 'E') {
                buffer.append(next.getCharacter());
                next = input.next();
                if(next.getCharacter() == '+' || next.getCharacter() == '-') {
                    buffer.append(next.getCharacter());
                    if(!input.peek().isDigit()) {
                        lexicalError("Malformed floating-point literal", next);
                        return findNextToken();
                    } 
                    next = input.next();
                    buffer.append(next.getCharacter());
                    appendSubsequentDigits(buffer);
                    next = input.next();
                } else {
                    lexicalError("Malformed floating-point literal", next);
                    return findNextToken();
                }
			}
			input.pushback(next);
			return FloatingLiteralToken.make(firstChar, buffer.toString());
		}
		else {
			return IntegerLiteralToken.make(firstChar, buffer.toString());
		}
	}
	private void appendSubsequentDigits(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isDigit()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Identifier and keyword lexical analysis	

	private Token scanIdentifier(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentLowercase(buffer);

		String lexeme = buffer.toString();
		if(Keyword.isAKeyword(lexeme)) {
			return LextantToken.make(firstChar, lexeme, Keyword.forLexeme(lexeme));
		}
		else {
			return IdentifierToken.make(firstChar, lexeme);
		}
	}
	private void appendSubsequentLowercase(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isIdentifierSubChar()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Punctuator lexical analysis	
	// old method left in to show a simple scanning method.
	// current method is the algorithm object PunctuatorScanner.java

	@SuppressWarnings("unused")
	private Token oldScanPunctuator(LocatedChar ch) {
		
		switch(ch.getCharacter()) {
		case '*':
			return LextantToken.make(ch, "*", Punctuator.MULTIPLY);
		case '+':
			return LextantToken.make(ch, "+", Punctuator.ADD);
		case '>':
			return LextantToken.make(ch, ">", Punctuator.GREATER);
		case ':':
			if(ch.getCharacter()=='=') {
				return LextantToken.make(ch, ":=", Punctuator.ASSIGN);
			}
			else {
				lexicalError(ch);
				return(NullToken.make(ch));
			}
		case ',':
			return LextantToken.make(ch, ",", Punctuator.PRINT_SEPARATOR);
		case ';':
			return LextantToken.make(ch, ";", Punctuator.TERMINATOR);
		default:
			lexicalError(ch);
			return(NullToken.make(ch));
		}
	}

	private void skipComment() {
		LocatedChar ch = input.next();
		while(!ch.isCommentEnd()) {
			ch = input.next();
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// Character-classification routines specific to tan scanning.	

	private boolean isPunctuatorStart(LocatedChar lc) {
		char c = lc.getCharacter();
		return isPunctuatorStartingCharacter(c);
	}

	private boolean isEndOfInput(LocatedChar lc) {
		return lc == LocatedCharStream.FLAG_END_OF_INPUT;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Error-reporting	

	private void lexicalError(LocatedChar ch) {
		TanLogger log = TanLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + ch);
	}

	private void lexicalError(String errorMsg, LocatedChar decimal) {
		TanLogger log = TanLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + errorMsg + " at " + decimal.getLocation());
	}
}
