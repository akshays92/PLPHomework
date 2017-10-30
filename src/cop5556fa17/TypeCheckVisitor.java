package cop5556fa17;

import javax.lang.model.element.ElementKind;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		 SymbolTable symbolTable = new SymbolTable();

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		/*
		 * Expression_Unary ::= op Expression
			Expression_Unary.Type <=
				let t = Expression.Type in 
                   if op ∈ {EXCL} && (t == BOOLEAN || t == INTEGER) then t
                   else if op {PLUS, MINUS} && t == INTEGER then INTEGER
		    	   else Ʇ
            REQUIRE:  Expression_ Unary.Type ≠ Ʇ 
		 */
		// TODO Auto-generated method stub
		expression_Unary.e.visit(this, arg);
		if (expression_Unary.op.equals(Kind.OP_EXCL) && ((expression_Unary.e.getUtilType().equals(Type.BOOLEAN))||(expression_Unary.e.getUtilType().equals(Type.INTEGER))))
			expression_Unary.setUtilType(expression_Unary.e.getUtilType());
		else if ((expression_Unary.op.equals(Kind.OP_PLUS)||expression_Unary.op.equals(Kind.OP_PLUS))&&(expression_Unary.e.getUtilType().equals(Type.INTEGER))) 
			expression_Unary.setUtilType(Type.INTEGER);
		else expression_Unary.setUtilType(null);
		if (expression_Unary.getUtilType().equals(null)) throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		/*
		 * Index ::= Expression0 Expression1
		 *	REQUIRE: Expression0.Type == INTEGER &&  Expression1.Type == INTEGER
		 *	Index.isCartesian <= !(Expression0 == KW_r && Expression1 == KW_a)
		 */
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if (!(index.e0.getUtilType()==Type.INTEGER && index.e1.getUtilType()==Type.INTEGER)) throw new UnsupportedOperationException(); 
		index.setCartesian(!(index.e0.firstToken.isKind(Kind.KW_r) && index.e1.firstToken.isKind(Kind.KW_a)));
		return arg;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		/**
		 *Source_StringLiteral ::=  fileOrURL
		 *  Source_StringLIteral.Type <= if isValidURL(fileOrURL) then URL else FILE
		 */
		try{
		new java.net.URL(source_StringLiteral.fileOrUrl);
		source_StringLiteral.setUtilType(Type.URL);
		} catch (Exception e){
			source_StringLiteral.setUtilType(Type.FILE);
		}
		return arg;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		/*
		 *Source_CommandLineParam  ::= ExpressionparamNum
		 * Source_CommandLineParam .Type <= ExpressionparamNum.Type
		 * REQUIRE:  Source_CommandLineParam .Type == INTEGER
		 */
		source_CommandLineParam.paramNum.visit(this, arg);
		source_CommandLineParam.setUtilType(source_CommandLineParam.paramNum.getUtilType());
		if (!(source_CommandLineParam.getUtilType()==Type.INTEGER))	throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		/*
		 * Source_Ident.Type <= symbolTable.lookupType(name)
         *  REQUIRE:  Source_Ident.Type == FILE || Source_Ident.Type == URL
		 */
		source_Ident.setUtilType(symbolTable.lookupDec(source_Ident.name).getType());
		Type source_IdentType = source_Ident.getUtilType();
		if(!(source_IdentType==Type.FILE)||!(source_IdentType==Type.URL)) throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		/*
		 * Expression_PredefinedName ::=  predefNameKind
			Expression_PredefinedName.TYPE <= INTEGER
		 */
		expression_PredefinedName.setUtilType(Type.INTEGER);
		return arg;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		/*
		 * LHS ::= name Index
		 *  LHS.Declaration <= symbolTable.lookupDec(name)
         *  LHS.Type <= LHS.Declaration.Type
         *  LHS.isCarteisan <= Index.isCartesian
		 */
		lhs.lhsDeclaration=symbolTable.lookupDec(lhs.name);
		lhs.setUtilType(lhs.lhsDeclaration.getType());
		lhs.isCarteisan=lhs.index.isCartesian();
		return arg;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		/*
		 * Sink_SCREEN ::= SCREEN
		 *  Sink_SCREEN.Type <= SCREEN
		 */
		sink_SCREEN.setUtilType(Type.SCREEN);
		return arg;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		/*
		 * Sink_Ident ::= name
		 *  Sink_Ident.Type <= symbolTable.lookupType(name) 
         *  REQUIRE:  Sink_Ident.Type  == FILE
		 */
		
		Declaration d = symbolTable.lookupDec(sink_Ident.name);
		sink_Ident.setUtilType(d.getType());
		if (d.getType()!=Type.FILE) throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
