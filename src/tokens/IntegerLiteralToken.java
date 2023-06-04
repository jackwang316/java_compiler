package tokens;

import inputHandler.Locator;

public class IntegerLiteralToken extends TokenImp {
	protected int value;
	
	protected IntegerLiteralToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public static IntegerLiteralToken make(Locator locator, String lexeme) {
		IntegerLiteralToken result = new IntegerLiteralToken(locator, lexeme);
		result.setValue(Integer.parseInt(lexeme));
		return result;
	}
	
	@Override
	protected String rawString() {
		return "int, " + value;
	}
}
