package tokens;

import inputHandler.Locator;

public class FloatingLiteralToken extends TokenImp {
	protected double value;
	
	protected FloatingLiteralToken(Locator locator, String lexeme) {
		super(locator, lexeme);
	}
	protected void setValue(double value) {
		this.value = value;
	}
	public double getValue() {
		return value;
	}
	
	public static FloatingLiteralToken make(Locator locator, String lexeme) {
		FloatingLiteralToken result = new FloatingLiteralToken(locator, lexeme);
		result.setValue(Double.parseDouble(lexeme));
		return result;
	}
	
	@Override
	protected String rawString() {
		return "float, " + value;
	}
}
