package parseTree;

import parseTree.nodeTypes.ArrayNode;
import parseTree.nodeTypes.AssignmentStatementNode;
import parseTree.nodeTypes.BlockStatementNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.IndexNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.TabSpaceNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileStatementNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.SubrBlockNode;
import parseTree.nodeTypes.SubrCallNode;
import parseTree.nodeTypes.SubrDefinitionNode;
import parseTree.nodeTypes.SubrInvokeNode;
import parseTree.nodeTypes.SubrParameterListNode;
import parseTree.nodeTypes.SubrParameterNode;
import parseTree.nodeTypes.SubrTypeNode;

// Visitor pattern with pre- and post-order visits
public interface ParseNodeVisitor {
	
	// non-leaf nodes: visitEnter and visitLeave
	void visitEnter(OperatorNode node);
	void visitLeave(OperatorNode node);
	
	void visitEnter(BlockStatementNode node);
	void visitLeave(BlockStatementNode node);

	void visitEnter(DeclarationNode node);
	void visitLeave(DeclarationNode node);

	void visitEnter(AssignmentStatementNode node);
	void visitLeave(AssignmentStatementNode node);

	void visitEnter(ArrayNode node);
	void visitLeave(ArrayNode node);
	
	void visitEnter(ParseNode node);
	void visitLeave(ParseNode node);
	
	void visitEnter(TypeNode node);
	void visitLeave(TypeNode node);
	
	void visitEnter(PrintStatementNode node);
	void visitLeave(PrintStatementNode node);

	void visitEnter(IndexNode node);
	void visitLeave(IndexNode node);

	void visitEnter(IfStatementNode node);
	void visitLeave(IfStatementNode node);

	void visitEnter(WhileStatementNode node);
	void visitLeave(WhileStatementNode node);
	
	void visitEnter(ProgramNode node);
	void visitLeave(ProgramNode node);

	void visitEnter(SubrDefinitionNode node);
	void visitLeave(SubrDefinitionNode node);

	void visitEnter(SubrInvokeNode node);
	void visitLeave(SubrInvokeNode node);

	void visitEnter(SubrCallNode node);
	void visitLeave(SubrCallNode node);

	void visitEnter(SubrParameterListNode node);
	void visitLeave(SubrParameterListNode node);

	void visitEnter(SubrParameterNode node);
	void visitLeave(SubrParameterNode node);

	void visitEnter(ReturnNode node);
	void visitLeave(ReturnNode node);

	void visitEnter(SubrBlockNode node);
	void visitLeave(SubrBlockNode node);


	// leaf nodes: visitLeaf only
	void visit(BooleanConstantNode node);
	void visit(ErrorNode node);
	void visit(IdentifierNode node);
	void visit(IntegerConstantNode node);
	void visit(NewlineNode node);
	void visit(SpaceNode node);
	void visit(TabSpaceNode node);
	void visit(FloatingConstantNode node);
	void visit(CharacterConstantNode node);
	void visit(StringConstantNode node);
	void visit(SubrTypeNode node);

	
	public static class Default implements ParseNodeVisitor
	{
		public void defaultVisit(ParseNode node) {	}
		public void defaultVisitEnter(ParseNode node) {
			defaultVisit(node);
		}
		public void defaultVisitLeave(ParseNode node) {
			defaultVisit(node);
		}		
		public void defaultVisitForLeaf(ParseNode node) {
			defaultVisit(node);
		}
		
		public void visitEnter(OperatorNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(OperatorNode node) {
			defaultVisitLeave(node);
		}

		public void visitEnter(SubrDefinitionNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(SubrDefinitionNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(SubrInvokeNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(SubrInvokeNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(SubrCallNode node){
			defaultVisitEnter(node);
		}
		public void visitLeave(SubrCallNode node){
			defaultVisitLeave(node);
		}
		public void visitEnter(ReturnNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ReturnNode node) {
			defaultVisitLeave(node);
		}
		@Override
		public void visitEnter(SubrParameterListNode node) {
			defaultVisitEnter(node);
		}	
		public void visitLeave(SubrParameterListNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(SubrParameterNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(SubrParameterNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(AssignmentStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(AssignmentStatementNode node) {
			defaultVisitLeave(node);
		}			
		public void visitEnter(DeclarationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(DeclarationNode node) {
			defaultVisitLeave(node);
		}					
		public void visitEnter(BlockStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(BlockStatementNode node) {
			defaultVisitLeave(node);
		}				
		public void visitEnter(ParseNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ParseNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(PrintStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(PrintStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(IfStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(IfStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(WhileStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(WhileStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ProgramNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ProgramNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(TypeNode node) {
			defaultVisitEnter(node);
			
		}
		public void visitLeave(TypeNode node) {
			defaultVisitLeave(node);
			
		}
		public void visitEnter(ArrayNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ArrayNode node) {
			defaultVisitLeave(node);
		}

		public void visitEnter(IndexNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(IndexNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(SubrBlockNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(SubrBlockNode node) {
			defaultVisitLeave(node);
		}
		public void visit(BooleanConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(ErrorNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IdentifierNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IntegerConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(FloatingConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(CharacterConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(StringConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(NewlineNode node) {
			defaultVisitForLeaf(node);
		}	
		public void visit(SpaceNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(TabSpaceNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(SubrTypeNode node) {
			defaultVisitForLeaf(node);
		}

	}
}
