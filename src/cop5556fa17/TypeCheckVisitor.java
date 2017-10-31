package cop5556fa17;

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
		if (!(symbolTable.lookupDec(declaration_Variable.name).equals(null))) throw new UnsupportedOperationException(); 
		symbolTable.insert(declaration_Variable.name, declaration_Variable);
		//declaration_Variable.setUtilType(declaration_Variable.);
		declaration_Variable.setUtilType(TypeUtils.getType(declaration_Variable.type));
		if(!(declaration_Variable.e.equals(null))) {
			declaration_Variable.e.visit(this, arg);
			if (!(declaration_Variable.getUtilType().equals(declaration_Variable.e.getUtilType()))) throw new UnsupportedOperationException();
		}
		return arg;
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
		expression_PixelSelector.index.visit(this, arg);
		Type nameType = symbolTable.lookupDec(expression_PixelSelector.name).getUtilType();
		if (nameType.equals(Type.IMAGE)) expression_PixelSelector.setUtilType(Type.INTEGER);
		else if (expression_PixelSelector.index.equals(null)) expression_PixelSelector.setUtilType(nameType);
		else{}
		if (expression_PixelSelector.getUtilType().equals(null)) throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		expression_Conditional.condition.visit(this, arg);
		expression_Conditional.trueExpression.visit(this, arg);
		expression_Conditional.falseExpression.visit(this, arg);
		if (!(expression_Conditional.condition.getUtilType().equals(Type.BOOLEAN) &&  
		expression_Conditional.trueExpression.getUtilType().equals(expression_Conditional.falseExpression.getUtilType())) )	throw new UnsupportedOperationException();
		expression_Conditional.setUtilType(expression_Conditional.trueExpression.getUtilType());
		return arg;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		declaration_Image.source.visit(this, arg);
		if (!symbolTable.lookupDec(declaration_Image.name).equals(null))	throw new UnsupportedOperationException();
		symbolTable.insert(declaration_Image.name, declaration_Image);
		declaration_Image.setUtilType(Type.IMAGE);
		return arg;
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
		source_Ident.setUtilType(symbolTable.lookupDec(source_Ident.name).getUtilType());
		Type source_IdentType = source_Ident.getUtilType();
		if(!(source_IdentType==Type.FILE)||!(source_IdentType==Type.URL)) throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		declaration_SourceSink.source.visit(this, arg);
		if(!(symbolTable.lookupDec(declaration_SourceSink.name).equals(null))) throw new UnsupportedOperationException();
		symbolTable.insert(declaration_SourceSink.name, declaration_SourceSink);
				if(declaration_SourceSink.type.equals(Scanner.Kind.KW_file)) declaration_SourceSink.setUtilType(Type.FILE);
				else if (declaration_SourceSink.type.equals(Scanner.Kind.KW_url)) declaration_SourceSink.setUtilType(Type.URL);						
		return arg;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		/*
		 * Expression_IntLit ::=  value
			Expression_IntLIt.Type <= INTEGER
		 */
		expression_IntLit.setUtilType(Type.INTEGER);
		return arg;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		/*
		 * Expression_FunctionAppWithExprArg ::=  function Expression
			REQUIRE:  Expression.Type == INTEGER
            Expression_FunctionAppWithExprArg.Type <= INTEGER
		 */
		if (!(expression_FunctionAppWithExprArg.getUtilType().equals(Type.INTEGER))) throw new UnsupportedOperationException();
		expression_FunctionAppWithExprArg.setUtilType(Type.INTEGER);
		return arg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		/*
		 * Expression_FunctionAppWithIndexArg ::=   function Index
             Expression_FunctionAppWithIndexArg.Type <= INTEGER
		 */
		expression_FunctionAppWithIndexArg.setUtilType(Type.INTEGER);
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
		statement_Out.sink.visit(this, arg);
		statement_Out.setDec(symbolTable.lookupDec(statement_Out.name));
		if(symbolTable.lookupDec(statement_Out.name).equals(null))throw new UnsupportedOperationException();
		Type nameType=symbolTable.lookupDec(statement_Out.name).getUtilType();
		if (!(
				((nameType.equals(Type.INTEGER)||nameType.equals(Type.BOOLEAN))&&statement_Out.sink.getUtilType().equals(Type.SCREEN)) ||
				(nameType.equals(Type.IMAGE)&&(statement_Out.sink.getUtilType().equals(Type.FILE)||statement_Out.sink.getUtilType().equals(Type.SCREEN)))
			)) throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		statement_In.source.visit(this, arg);
		if(symbolTable.lookupDec(statement_In.name).equals(null)) throw new UnsupportedOperationException();
		if (!(symbolTable.lookupDec(statement_In.name).getUtilType().equals(statement_In.source.getUtilType()))) throw new UnsupportedOperationException();
		statement_In.setDec(symbolTable.lookupDec(statement_In.name));
		return arg;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		statement_Assign.lhs.visit(this, arg);
		statement_Assign.e.visit(this, arg);
		if(!(statement_Assign.lhs.getUtilType().equals(statement_Assign.e.getUtilType()))) throw new UnsupportedOperationException();
		statement_Assign.setCartesian(statement_Assign.lhs.isCarteisan);
		return arg;
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
		lhs.setUtilType(lhs.lhsDeclaration.getUtilType());
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
		sink_Ident.setUtilType(d.getUtilType());
		if (d.getUtilType()!=Type.FILE) throw new UnsupportedOperationException();
		return arg;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		/*
		 * Expression_BooleanLit ::=  value
			Expression_BooleanLit.Type <= BOOLEAN
		 */
		expression_BooleanLit.setUtilType(Type.BOOLEAN);
		return arg;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		/*
		 * Expression_Ident  ::=   name
			Expression_Ident.Type <= symbolTable.lookupType(name)
		 */
		expression_Ident.setUtilType(symbolTable.lookupDec(expression_Ident.name).getUtilType());
		return arg;
	}

}
