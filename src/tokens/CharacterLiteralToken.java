package tokens;

import inputHandler.Locator;

public class CharacterLiteralToken extends TokenImp {
	protected char value;

	protected CharacterLiteralToken(Locator location, String lexeme) {
		super(location, lexeme);
	}
	protected void setValue(char value) {
		this.value = value;
	}
	public char getValue() {
		return value;
	}

	public static CharacterLiteralToken make(Locator location, String lexeme) {
		CharacterLiteralToken  result = new CharacterLiteralToken (location, lexeme);
		result.setValue(lexeme.charAt(0));
		return result;
	}

	@Override
	protected String rawString() {
		return "characterConst, ^" + value + "^";
	}
}
