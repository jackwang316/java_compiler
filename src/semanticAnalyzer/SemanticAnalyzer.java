package semanticAnalyzer;

import parseTree.*;


public class SemanticAnalyzer {
	ParseNode ASTree;
	
	public static ParseNode analyze(ParseNode ASTree) {
		SemanticAnalyzer analyzer = new SemanticAnalyzer(ASTree);
		analyzer.preprocess();
		return analyzer.analyze();
	}
	public SemanticAnalyzer(ParseNode ASTree) {
		this.ASTree = ASTree;
	}
	
	public void preprocess() {
		ASTree.accept(new Preprocessor());
	}

	public ParseNode analyze() {
		ASTree.accept(new SemanticAnalysisVisitor());
		
		return ASTree;
	}
}
