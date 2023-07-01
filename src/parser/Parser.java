package parser;

import java.util.Arrays;

import logging.TanLogger;
import parseTree.*;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabSpaceNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.types.PrimitiveType;
import parseTree.nodeTypes.StringConstantNode;
import tokens.*;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import lexicalAnalyzer.Scanner;


public class Parser {
	private Scanner scanner;
	private Token nowReading;
	private Token previouslyRead;
	
	public static ParseNode parse(Scanner scanner) {
		Parser parser = new Parser(scanner);
		return parser.parse();
	}
	public Parser(Scanner scanner) {
		super();
		this.scanner = scanner;
	}
	
	public ParseNode parse() {
		readToken();
		return parseProgram();
	}

	////////////////////////////////////////////////////////////
	// "program" is the start symbol S
	// S -> MAIN mainBlock
	
	private ParseNode parseProgram() {
		if(!startsProgram(nowReading)) {
			return syntaxErrorNode("program");
		}
		ParseNode program = new ProgramNode(nowReading);
		
		expect(Keyword.MAIN);
		ParseNode mainBlock = parseBlockStatements();
		program.appendChild(mainBlock);
		
		if(!(nowReading instanceof NullToken)) {
			return syntaxErrorNode("end of program");
		}
		
		return program;
	}
	private boolean startsProgram(Token token) {
		return token.isLextant(Keyword.MAIN);
	}
	
	
	///////////////////////////////////////////////////////////
	// mainBlock and subBlock
	private ParseNode parseBlockStatements() {
		if(!startsBlockStatements(nowReading)) {
			return syntaxErrorNode("BlockStatements");
		}
		ParseNode blockStatement = new BlockStatementNode(nowReading);
		expect(Punctuator.OPEN_BRACE);
		
		while(startsStatement(nowReading)) {
			ParseNode statement = parseStatement();
			blockStatement.appendChild(statement);
		}
		expect(Punctuator.CLOSE_BRACE);
		return blockStatement;
	}

	private boolean startsBlockStatements(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE);
	}
	
	
	///////////////////////////////////////////////////////////
	// statements
	
	// statement-> declaration | printStmt
	private ParseNode parseStatement() {
		if(!startsStatement(nowReading)) {
			return syntaxErrorNode("statement");
		}
		if(startsDeclaration(nowReading)) {
			return parseDeclaration();
		}
		if(startsMutation(nowReading)) {
			return parseMutation();
		}
		if(startsPrintStatement(nowReading)) {
			return parsePrintStatement();
		}
		if(startsBlockStatements(nowReading)){
			return parseBlockStatements();
		}
		return syntaxErrorNode("statement");
	}
	
	private boolean startsStatement(Token token) {
		return startsPrintStatement(token) ||
			   startsMutation(token) ||
			   startsDeclaration(token) ||
			   startsBlockStatements(token);
	}
	

	// printStmt -> PRINT printExpressionList TERMINATOR
	private ParseNode parsePrintStatement() {
		if(!startsPrintStatement(nowReading)) {
			return syntaxErrorNode("print statement");
		}
		ParseNode result = new PrintStatementNode(nowReading);
		
		readToken();
		result = parsePrintExpressionList(result);
		
		expect(Punctuator.TERMINATOR);
		return result;
	}
	
	private boolean startsPrintStatement(Token token) {
		return token.isLextant(Keyword.PRINT);
	}	
	
	private boolean startsMutation(Token token) {
		return startsIdentifier(token);
	}
	
	// This adds the printExpressions it parses to the children of the given parent
	// printExpressionList -> printSeparator* (expression printSeparator+)* expression? (note that this is nullable)

	private ParseNode parsePrintExpressionList(ParseNode parent) {
		if(!startsPrintExpressionList(nowReading)) {
			return syntaxErrorNode("printExpressionList");
		}
		
		while(startsPrintSeparator(nowReading)) {
			parsePrintSeparator(parent);
		}
		while(startsExpression(nowReading)) {
			parent.appendChild(parseExpression());
			if(nowReading.isLextant(Punctuator.TERMINATOR)) {
				return parent;
			}
			do {
				parsePrintSeparator(parent);
			} while(startsPrintSeparator(nowReading));
		}
		return parent;
	}	
	private boolean startsPrintExpressionList(Token token) {
		return startsExpression(token) || startsPrintSeparator(token) || token.isLextant(Punctuator.TERMINATOR);
	}

	private ParseNode parseMutation() {
		if(!startsMutation(nowReading)) {
			return syntaxErrorNode("reassignment");
		}
		ParseNode identifier = parseIdentifier();
		expect(Punctuator.ASSIGN);
		ParseNode expression = parseExpression();
		expect(Punctuator.TERMINATOR);
		
		return AssignmentStatementNode.withChildren(identifier, expression);
	}
	
	// This adds the printSeparator it parses to the children of the given parent
	// printSeparator -> PRINT_SEPARATOR | PRINT_SPACE | PRINT_TAB_SPACE |PRINT_NEWLINE 
	
	private void parsePrintSeparator(ParseNode parent) {
		if(!startsPrintSeparator(nowReading)) {
			ParseNode child = syntaxErrorNode("print separator");
			parent.appendChild(child);
			return;
		}
		
		if(nowReading.isLextant(Punctuator.PRINT_NEWLINE)) {
			readToken();
			ParseNode child = new NewlineNode(previouslyRead);
			parent.appendChild(child);
		}		
		else if(nowReading.isLextant(Punctuator.PRINT_SPACE)) {
			readToken();
			ParseNode child = new SpaceNode(previouslyRead);
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Punctuator.PRINT_TAB)) {
			readToken();
			ParseNode child = new TabSpaceNode(previouslyRead);
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Punctuator.PRINT_SEPARATOR)) {
			readToken();
		} 
	}
	private boolean startsPrintSeparator(Token token) {
		return token.isLextant(Punctuator.PRINT_SEPARATOR, Punctuator.PRINT_SPACE, Punctuator.PRINT_TAB, Punctuator.PRINT_NEWLINE);
	}
	
	
	// declaration -> CONST identifier := expression TERMINATOR
	private ParseNode parseDeclaration() {
		if(!startsDeclaration(nowReading)) {
			return syntaxErrorNode("declaration");
		}
		Token declarationToken = nowReading;
		readToken();
		
		ParseNode identifier = parseIdentifier();
		expect(Punctuator.ASSIGN);
		ParseNode initializer = parseExpression();
		expect(Punctuator.TERMINATOR);
		
		return DeclarationNode.withChildren(declarationToken, identifier, initializer);
	}
	private boolean startsDeclaration(Token token) {
		return token.isLextant(Keyword.CONST) || token.isLextant(Keyword.VAR);
	}


	
	///////////////////////////////////////////////////////////
	// expressions
	// expr                     -> LogicalOrExpression
	// LogicalOrExpression      -> LogicalAndExpression [OR LogicalAndExpression]*
	// LogicalAndExpression     -> ComparisonExpression [AND ComparisonExpression]*
	// comparisonExpression     -> additiveExpression [> additiveExpression]?
	// additiveExpression       -> multiplicativeExpression [+ multiplicativeExpression]*  (left-assoc)
	// multiplicativeExpression -> atomicExpression [MULT atomicExpression]*  (left-assoc)
	// atomicExpression         -> unaryExpression | literal
	// unaryExpression			-> UNARYOP atomicExpression
	// literal                  -> intNumber | identifier | booleanConstant

	// expr  -> comparisonExpression
	private ParseNode parseExpression() {		
		if(!startsExpression(nowReading)) {
			return syntaxErrorNode("expression");
		}
		return parseLogicalOrExpression();
	}
	private boolean startsExpression(Token token) {
		return startsLogicalOrExpression(token);
	}
	private ParseNode parseLogicalOrExpression() {
		if(!startsLogicalOrExpression(nowReading)) {
			return syntaxErrorNode("logical or expression");
		}
		
		ParseNode left = parseLogicalAndExpression();
		while(nowReading.isLextant(Punctuator.LOGICAL_OR)) {
			Token orToken = nowReading;
			readToken();
			ParseNode right = parseLogicalAndExpression();
			
			left = OperatorNode.withChildren(orToken, left, right);
		}
		return left;
	}
	private boolean startsLogicalOrExpression(Token token) {
		return startsLogicalAndExpression(token);
	}
	private ParseNode parseLogicalAndExpression() {
		if(!startsLogicalAndExpression(nowReading)) {
			return syntaxErrorNode("logical and expression");
		}
		
		ParseNode left = parseComparisonExpression();
		while(nowReading.isLextant(Punctuator.LOGICAL_AND)) {
			Token andToken = nowReading;
			readToken();
			ParseNode right = parseComparisonExpression();
			
			left = OperatorNode.withChildren(andToken, left, right);
		}
		return left;
	}
	private boolean startsLogicalAndExpression(Token token) {
		return startsComparisonExpression(token);
	}

	// comparisonExpression -> additiveExpression [> additiveExpression]?
	private ParseNode parseComparisonExpression() {
		if(!startsComparisonExpression(nowReading)) {
			return syntaxErrorNode("comparison expression");
		}
		
		ParseNode left = parseAdditiveExpression();
		while(nowReading.isLextant(Punctuator.COMPARISION)) {
			Token compareToken = nowReading;
			readToken();
			ParseNode right = parseAdditiveExpression();
			
			left = OperatorNode.withChildren(compareToken, left, right);
		}
		return left;

	}
	private boolean startsComparisonExpression(Token token) {
		return startsAdditiveExpression(token);
	}

	// additiveExpression -> multiplicativeExpression [+ multiplicativeExpression]*  (left-assoc)
	private ParseNode parseAdditiveExpression() {
		if(!startsAdditiveExpression(nowReading)) {
			return syntaxErrorNode("additiveExpression");
		}
		
		ParseNode left = parseMultiplicativeExpression();
		while(nowReading.isLextant(Punctuator.ADD, Punctuator.SUBTRACT)) {
			Token additiveToken = nowReading;
			readToken();
			ParseNode right = parseMultiplicativeExpression();
			
			left = OperatorNode.withChildren(additiveToken, left, right);
		}
		return left;
	}
	private boolean startsAdditiveExpression(Token token) {
		return startsMultiplicativeExpression(token);
	}	

	// multiplicativeExpression -> atomicExpression [MULT atomicExpression]*  (left-assoc)
	private ParseNode parseMultiplicativeExpression() {
		if(!startsMultiplicativeExpression(nowReading)) {
			return syntaxErrorNode("multiplicativeExpression");
		}
		
		ParseNode left = parseAtomicExpression();
		while(nowReading.isLextant(Punctuator.MULTIPLY, Punctuator.DIVIDE)) {
			Token multiplicativeToken = nowReading;
			readToken();
			ParseNode right = parseAtomicExpression();
			
			left = OperatorNode.withChildren(multiplicativeToken, left, right);
		}
		return left;
	}
	private boolean startsMultiplicativeExpression(Token token) {
		return startsAtomicExpression(token);
	}
	
	// atomicExpression         -> unaryExpression | literal
	private ParseNode parseAtomicExpression() {
		if(!startsAtomicExpression(nowReading)) {
			return syntaxErrorNode("atomic expression");
		}
		if(startsUnaryExpression(nowReading)) {
			return parseUnaryExpression();
		}
		if(startsCastExpression(nowReading)) {
			return parseCastExpression();
		}
		if(startsParenthesis(nowReading)) {
			return parseParenthesisExpression();
		}
		return parseLiteral();
	}
	private ParseNode parseParenthesisExpression() {
		if(!startsParenthesis(nowReading)) {
			return syntaxErrorNode("parenthesis");
		}
		readToken();
		ParseNode expression = parseExpression();
		expect(Punctuator.CLOSE_PARENTHESIS);
		return expression;
	}
	private boolean startsParenthesis(Token token) {
		return token.isLextant(Punctuator.OPEN_PARENTHESIS);
	}
	
	private ParseNode parseCastExpression() {
		if(!startsCastExpression(nowReading)) {
			return syntaxErrorNode("casting");
		}
		readToken();
		String castType = nowReading.getLexeme();
		if(!Keyword.isCastType(castType)) {
			return syntaxErrorNode("invalid cast type");
		}
		readToken();
		expect(Punctuator.GREATER);
		ParseNode expression = parseParenthesisExpression();
		return TypeNode.withChildren(expression, PrimitiveType.parseType(castType));
		
	}
	private boolean startsAtomicExpression(Token token) {
		return startsLiteral(token) || startsUnaryExpression(token) || startsCastExpression(token) || startsParenthesis(token);
	}

	private boolean startsCastExpression(Token token) {
		return token.isLextant(Punctuator.LESS);
	}
	// unaryExpression			-> UNARYOP atomicExpression
	private ParseNode parseUnaryExpression() {
		if(!startsUnaryExpression(nowReading)) {
			return syntaxErrorNode("unary expression");
		}
		Token operatorToken = nowReading;
		readToken();
		ParseNode child = parseAtomicExpression();
		
		return OperatorNode.withChildren(operatorToken, child);
	}
	private boolean startsUnaryExpression(Token token) {
		return token.isLextant(Punctuator.ADD, Punctuator.SUBTRACT, Punctuator.LOGICAL_NOT);
	}
	
	// literal -> number | identifier | booleanConstant
	private ParseNode parseLiteral() {
		if(!startsLiteral(nowReading)) {
			return syntaxErrorNode("literal");
		}
		if(startsFloatLiteral(nowReading)) {
			return parseFloatLiteral();
		}
		if(startsCharacterLiteral(nowReading)) {
			return parseCharacterLiteral();
		}
		if(startsIntLiteral(nowReading)) {
			return parseIntLiteral();
		}
		if(startsIdentifier(nowReading)) {
			return parseIdentifier();
		}
		if(startsBooleanLiteral(nowReading)) {
			return parseBooleanLiteral();
		}
		if(startsStringLiteral(nowReading)) {
			return parseStringLiteral();
		}

		return syntaxErrorNode("literal");
	}
	private boolean startsStringLiteral(Token token) {
		return token instanceof StringLiteralToken;
	}
	private boolean startsFloatLiteral(Token token) {
		return token instanceof FloatingLiteralToken;
	}
	private boolean startsLiteral(Token token) {
		return startsIntLiteral(token) || startsFloatLiteral(token) || startsStringLiteral(token)
				|| startsIdentifier(token) || startsBooleanLiteral(token) || startsCharacterLiteral(token);
	}
	
	private boolean startsCharacterLiteral(Token token) {
		return token instanceof CharacterLiteralToken;
	}

	// number (literal)
	private ParseNode parseIntLiteral() {
		if(!startsIntLiteral(nowReading)) {
			return syntaxErrorNode("integer constant");
		}
		readToken();
		return new IntegerConstantNode(previouslyRead);
	}
	private boolean startsIntLiteral(Token token) {
		return token instanceof IntegerLiteralToken;
	}

	// identifier (terminal)
	private ParseNode parseIdentifier() {
		if(!startsIdentifier(nowReading)) {
			return syntaxErrorNode("identifier");
		}
		readToken();
		return new IdentifierNode(previouslyRead);
	}
	private boolean startsIdentifier(Token token) {
		return token instanceof IdentifierToken;
	}
	
	private ParseNode parseCharacterLiteral() {
		if(!startsCharacterLiteral(nowReading)) {
			return syntaxErrorNode("character constant");
		}
		readToken();
		return new CharacterConstantNode(previouslyRead);
	}

	private ParseNode parseFloatLiteral() {
		if(!startsFloatLiteral(nowReading)) {
			return syntaxErrorNode("floating constant");
		}
		readToken();
		return new FloatingConstantNode(previouslyRead);
	}
	
	private ParseNode parseStringLiteral() {
		if(!startsStringLiteral(nowReading)) {
			return syntaxErrorNode("string constant");
		}
		readToken();
		return new StringConstantNode(previouslyRead);
	}

	// boolean literal
	private ParseNode parseBooleanLiteral() {
		if(!startsBooleanLiteral(nowReading)) {
			return syntaxErrorNode("boolean constant");
		}
		readToken();
		return new BooleanConstantNode(previouslyRead);
	}
	private boolean startsBooleanLiteral(Token token) {
		return token.isLextant(Keyword.TRUE, Keyword.FALSE);
	}

	private void readToken() {
		previouslyRead = nowReading;
		nowReading = scanner.next();
	}	
	
	// if the current token is one of the given lextants, read the next token.
	// otherwise, give a syntax error and read next token (to avoid endless looping).
	private void expect(Lextant ...lextants ) {
		if(!nowReading.isLextant(lextants)) {
			syntaxError(nowReading, "expecting " + Arrays.toString(lextants));
		}
		readToken();
	}	
	private ErrorNode syntaxErrorNode(String expectedSymbol) {
		syntaxError(nowReading, "expecting " + expectedSymbol);
		ErrorNode errorNode = new ErrorNode(nowReading);
		readToken();
		return errorNode;
	}
	private void syntaxError(Token token, String errorDescription) {
		String message = "" + token.getLocation() + " " + errorDescription;
		error(message);
	}
	private void error(String message) {
		TanLogger log = TanLogger.getLogger("compiler.Parser");
		log.severe("syntax error: " + message);
	}	
}