package tokens;

import inputHandler.Locator;

public class StringLiteralToken extends TokenImp {
	protected String value;

	protected StringLiteralToken(Locator location, String lexeme) {
		super(location, lexeme);
	}
	protected void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}

	public static StringLiteralToken make(Locator location, String lexeme) {
		StringLiteralToken  result = new StringLiteralToken (location, lexeme);
		result.setValue(lexeme);
		return result;
	}

	@Override
	protected String rawString() {
		return "string, \"" + value + "\"";
	}
}
