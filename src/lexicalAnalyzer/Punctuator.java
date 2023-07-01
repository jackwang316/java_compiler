package lexicalAnalyzer;

import inputHandler.TextLocation;
import tokens.LextantToken;
import tokens.Token;


public enum Punctuator implements Lextant {
	ADD("+"), 
	SUBTRACT("-"),
	MULTIPLY("*"),
	GREATER(">"),
	DIVIDE("/"),
	GREATER_EQUAL(">="),
	LESS("<"),
	LESS_EQUAL("<="),
	EQUAL("=="),
	NOT_EQUAL("!="),
	LOGICAL_AND("&&"),
	LOGICAL_OR("||"),
	LOGICAL_NOT("!"),
	ASSIGN(":="),
	PRINT_SEPARATOR("\\"),
	PRINT_SPACE("\\s"),
	PRINT_NEWLINE("\\n"),
	PRINT_TAB("\\t"),
	TERMINATOR(";"), 
	OPEN_BRACE("{"),
	CLOSE_BRACE("}"),
	OPEN_PARENTHESIS("("),
	CLOSE_PARENTHESIS(")"),
	CAST(""),
	OPEN_BRACKET("["),
	CLOSE_BRACKET("]"),
	NULL_PUNCTUATOR("");

	private String lexeme;
	private Token prototype;
	
	private Punctuator(String lexeme) {
		this.lexeme = lexeme;
		this.prototype = LextantToken.make(TextLocation.nullInstance(), lexeme, this);
	}
	public String getLexeme() {
		return lexeme;
	}
	public Token prototype() {
		return prototype;
	}
	
	
	public static Punctuator forLexeme(String lexeme) {
		for(Punctuator punctuator: values()) {
			if(punctuator.lexeme.equals(lexeme)) {
				return punctuator;
			}
		}
		return NULL_PUNCTUATOR;
	}
	
	public static final Punctuator COMPARISION[] = new Punctuator[] {
		GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EQUAL, NOT_EQUAL
	};
	
	public static boolean isComparison(Lextant lextant) {
		for (Punctuator c: COMPARISION) {
			if (c == lextant) return true;
		}
		return false;
	}
/*
	//   the following hashtable lookup can replace the implementation of forLexeme above. It is faster but less clear. 
	private static LexemeMap<Punctuator> lexemeToPunctuator = new LexemeMap<Punctuator>(values(), NULL_PUNCTUATOR);
	public static Punctuator forLexeme(String lexeme) {
		   return lexemeToPunctuator.forLexeme(lexeme);
	}
*/
	
}


