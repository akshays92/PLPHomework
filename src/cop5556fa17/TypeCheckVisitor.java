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
		/*
		 * Declaration_Variable ::=  Type name (Expression | ε )
				REQUIRE:  symbolTable.lookupType(name) = ε
              	symbolTable.insert(name, Declaration_Variable)
				Declaration_Variable.Type <= Type
               	REQUIRE if (Expression !=  ε) Declaration_Variable.Type == Expression.Type
		 */
		//visit expression
		if(!(declaration_Variable.e==null)) declaration_Variable.e.visit(this, arg);
		
		//check if symbol is not already present in symbol table
		if (!(symbolTable.lookupDec(declaration_Variable.name)==null)) 
			throw new SemanticException(declaration_Variable.firstToken,"Semmantic exception in visitDeclaration_Variable : variable already declared"); 
		
		//insert symbol in symbol table
		symbolTable.insert(declaration_Variable.name, declaration_Variable);
		
		//set declaration variable type
		declaration_Variable.setUtilType(TypeUtils.getType(declaration_Variable.type));
		
		//if expression is not null,check expression type should be declaration variable type 
		if(!(declaration_Variable.e==null)) {
			declaration_Variable.e.visit(this, arg);
			if (!(declaration_Variable.getUtilType()==declaration_Variable.e.getUtilType())) throw new 
			SemanticException(declaration_Variable.firstToken,"Semmantic exception in visitDeclaration_Variable : expression type is not equal to dec variable type"); 
		}
		return arg;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		/*
		 * Expression_Binary ::= Expression0 op Expression1
		 * REQUIRE:  Expression0.Type == Expression1.Type  && Expression_Binary.Type ≠ Ʇ
			Expression_Binary.type <=   
 				if op ∈ {EQ, NEQ} then BOOLEAN
                else if (op ∈ {GE, GT, LT, LE} && Expression0.Type == INTEGER) then BOOLEAN
                else if (op ∈ {AND, OR}) &&	(Expression0.Type == INTEGER || Expression0.Type ==BOOLEAN)	then Expression0.Type
                else if op ∈ {DIV, MINUS, MOD, PLUS, POWER, TIMES} && Expression0.Type == INTEGER
			then INTEGER
		else Ʇ
		 */
		
		//check e1 and dif not null visit it
		if (!(expression_Binary.e0==null)) expression_Binary.e0.visit(this, arg);
		
		//check e2 and if not null visit it
		if (!(expression_Binary.e1==null)) expression_Binary.e1.visit(this, arg);
		
		//check if e1 and e2 have same types
		if(!(expression_Binary.e0.getUtilType()==expression_Binary.e1.getUtilType())) 
			throw new SemanticException(expression_Binary.firstToken,"Semmantic exception in expressionBinary : type of e1 is not same as type of e2");
				
		
		//setting type for expression binary
		if(expression_Binary.op==Kind.OP_EQ||expression_Binary.op==Kind.OP_NEQ) expression_Binary.setUtilType(Type.BOOLEAN);
		else if ((expression_Binary.op==Kind.OP_GE||expression_Binary.op==Kind.OP_GT||expression_Binary.op==Kind.OP_LE||expression_Binary.op==Kind.OP_LT)
				&& expression_Binary.e0.getUtilType()==Type.INTEGER) expression_Binary.setUtilType(Type.BOOLEAN);
		else if ((expression_Binary.op==Kind.OP_AND||expression_Binary.op==Kind.OP_OR)
				&& (expression_Binary.e0.getUtilType()==Type.INTEGER||expression_Binary.e0.getUtilType()==Type.BOOLEAN)) 
					expression_Binary.setUtilType(expression_Binary.e0.getUtilType());
		else if (
				(		expression_Binary.op==Kind.OP_DIV||expression_Binary.op==Kind.OP_MINUS||
						expression_Binary.op==Kind.OP_MOD||expression_Binary.op==Kind.OP_PLUS||
						expression_Binary.op==Kind.OP_POWER||expression_Binary.op==Kind.OP_TIMES
				)
				&& expression_Binary.e0.getUtilType()==Type.INTEGER
				)  expression_Binary.setUtilType(Type.INTEGER); 
		//code should not reach here
		else expression_Binary.setUtilType(null);
		;
		
		
		//check if type of binary expression is not null
		if(expression_Binary.getUtilType()==null) throw new SemanticException(expression_Binary.firstToken,"Semmantic exception in expressionBinary : type is null for binary expression");
		return arg;
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
		
		//check if unary expression is not null and visit it
		if(!(expression_Unary.e==null)) expression_Unary.e.visit(this, arg);
		
		//set type of unary expression
		if (expression_Unary.op==Kind.OP_EXCL && (expression_Unary.e.getUtilType()==Type.BOOLEAN||expression_Unary.e.getUtilType()==Type.INTEGER))
			expression_Unary.setUtilType(expression_Unary.e.getUtilType());
		else if ((expression_Unary.op==Kind.OP_PLUS||expression_Unary.op==Kind.OP_MINUS)&&expression_Unary.e.getUtilType()==Type.INTEGER) 
			expression_Unary.setUtilType(Type.INTEGER);
		else expression_Unary.setUtilType(null);
		if (expression_Unary.getUtilType()==null) throw new SemanticException(expression_Unary.firstToken,"Semmantic exception in expressionUnary : type is null for unary expression");
		return arg;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		/*
		 * Index ::= Expression0 Expression1
		 *	REQUIRE: Expression0.Type == INTEGER &&  Expression1.Type == INTEGER
		 *	Index.isCartesian <= !(Expression0 == KW_r && Expression1 == KW_a)
		 */
		//TODO mera pehle ka kachra and naya kachra alag alag hai, kisi se confirm karo kya karna hai 
		//visit index[e0] if not null
		if(!(index.e0==null)) index.e0.visit(this, arg);
		
		//visit index[e1] if not null
		if(!(index.e1==null)) index.e1.visit(this, arg);
		
		//throw exception if expressions inside index are not of type integer
		if(!(index.e0.getUtilType()==Type.INTEGER && index.e1.getUtilType()==Type.INTEGER)) 
			throw new SemanticException(index.firstToken,"Semmantic exception in visitIndex : expressions inside Index are not of type Integer");
		else{
			if(index.e0.getClass() == Expression_PredefinedName.class && index.e1.getClass() == Expression_PredefinedName.class){
				Expression_PredefinedName pd1 = (Expression_PredefinedName)index.e0;
				Expression_PredefinedName pd2 = (Expression_PredefinedName)index.e1;
				index.setCartesian(!(pd1.kind == Kind.KW_r && pd2.kind == Kind.KW_a));
			}
			else{
				index.setCartesian(!(index.e0.firstToken.isKind(Kind.KW_r) && index.e1.firstToken.isKind(Kind.KW_a)));
			}
		}
		return arg;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		/*
		 * Expression_PixelSelector ::=   name Index
         	name.Type <= SymbolTable.lookupType(name)
						 Expression_PixelSelector.Type <=  if name.Type == IMAGE then INTEGER 
                         else if Index == null then name.Type
                         else  Ʇ
              REQUIRE:  Expression_PixelSelector.Type ≠ Ʇ
		 */
		
		//index can be null according to concrete grammar : CHECK PARSER for more details
		//visit index if not null
		if(!(expression_PixelSelector.index==null)) expression_PixelSelector.index.visit(this, arg);
		
		//setting the type for expressionPixelSelector
		Declaration nameDeclaration = symbolTable.lookupDec(expression_PixelSelector.name);
		if (nameDeclaration==null) throw new SemanticException(expression_PixelSelector.firstToken,"Semmantic exception in expression_PixelSelector : nameDec for expression_PixelSelector is null");
		Type nameType = nameDeclaration.getUtilType();
		
		if (nameType==Type.IMAGE) expression_PixelSelector.setUtilType(Type.INTEGER);
		else if (expression_PixelSelector.index==null) expression_PixelSelector.setUtilType(nameType);
		//code should not reach here
		else expression_PixelSelector.setUtilType(null);
		
		//Check expressionPixelSelector's type and throw exception if it is null
		if (expression_PixelSelector.getUtilType()==null)
			throw new SemanticException(expression_PixelSelector.firstToken,"Semmantic exception in expression_PixelSelector : type for expression_PixelSelector is null");
		return arg;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		/*
		 * Expression_Conditional ::=  Expressioncondition Expressiontrue Expressionfalse
			REQUIRE:  Expressioncondition.Type == BOOLEAN && Expressiontrue.Type ==Expressionfalse.Type
			Expression_Conditional.Type <= Expressiontrue.Type
		 */
		
		//check if condition true expression and false expressions are not null and then visit them
		if(!(expression_Conditional.condition==null)) expression_Conditional.condition.visit(this, arg);
		if(!(expression_Conditional.trueExpression==null)) expression_Conditional.trueExpression.visit(this, arg);
		if(!(expression_Conditional.falseExpression==null)) expression_Conditional.falseExpression.visit(this, arg);
		
		//type for condition should be boolean type for true
		if (!(expression_Conditional.condition.getUtilType()==Type.BOOLEAN &&
		//type for true expression should be same as type for false expression
		expression_Conditional.trueExpression.getUtilType()==expression_Conditional.falseExpression.getUtilType()) )	
		//throw exception if any of the above condition is false
			throw new SemanticException(expression_Conditional.firstToken,"Semmantic exception in expression_Conditional : type for condition expression is not boolean or type of true expression is not same as type of false expression");
		expression_Conditional.setUtilType(expression_Conditional.trueExpression.getUtilType());
		return arg;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		/*
		 * Declaration_Image  ::= name (  xSize ySize | ε) Source
			REQUIRE:  symbolTable.lookupType(name) = Ʇ
            symbolTable.insert(name, Declaration_Image)
			Declaration_Image.Type <= IMAGE   
            REQUIRE if xSize != ε then ySize != ε && xSize.Type == INTEGER && ySize.type == INTEGER
		 */
		
		//if source is not null, visit source
		if(!(declaration_Image.source==null))declaration_Image.source.visit(this, arg);
		
		//if x and y are not null, visit them
		if(!(declaration_Image.xSize==null))declaration_Image.xSize.visit(this, arg);
		if(!(declaration_Image.ySize==null))declaration_Image.ySize.visit(this, arg);
		
		//symbol table should not already contain the identifier
		if (!(symbolTable.lookupDec(declaration_Image.name)==null)) 
			throw new SemanticException(declaration_Image.firstToken,"Semmantic exception in Declaration_Image: variable already present");
		//insert dec_image in symbol table and set its type
		symbolTable.insert(declaration_Image.name, declaration_Image);
		declaration_Image.setUtilType(Type.IMAGE);
		
		//if xsize and ysize are not null, they should be of type integer
		if(!(declaration_Image.xSize==null)){
			if (	!((!(declaration_Image.ySize==null))	&&	(declaration_Image.xSize.getUtilType()==Type.INTEGER)	&&	(declaration_Image.ySize.getUtilType()==Type.INTEGER))	)
				throw new SemanticException(declaration_Image.firstToken,"Semmantic exception in Declaration_Image: xsize or ysize are not Integer types");
		}
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
			//This function throws an exception if the URL passed is not valid
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
		
		//visit paramnum if not null
		if(!(source_CommandLineParam.paramNum==null)) source_CommandLineParam.paramNum.visit(this, arg);
		
		//set type for expression same as type for paramnum 
		source_CommandLineParam.setUtilType(null);
		
		//now throw an exception if type for this parameter is not INTEGER
		if (!(source_CommandLineParam.paramNum.getUtilType()==Type.INTEGER))	
			throw new SemanticException(source_CommandLineParam.firstToken,"Semmantic exception in source_CommandLineParam: commandline parameter is not Integer type");
		return arg;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		/*
		 * Source_Ident.Type <= symbolTable.lookupType(name)
         *  REQUIRE:  Source_Ident.Type == FILE || Source_Ident.Type == URL
		 */
		Declaration dec = symbolTable.lookupDec(source_Ident.name);
		if (dec==null) throw new SemanticException(source_Ident.firstToken,"Semmantic exception in source_Ident: source_Ident is not declared yet");
		source_Ident.setUtilType(dec.getUtilType());
		Type source_IdentType = source_Ident.getUtilType();
		if(!((source_IdentType==Type.FILE)||(source_IdentType==Type.URL)))
		throw new SemanticException(source_Ident.firstToken,"Semmantic exception in source_Ident: source_Ident is not file or URL type");
		return arg;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		/*
		 * Declaration_SourceSink  ::= Type name  Source
              REQUIRE:  symbolTable.lookupType(name) = Ʇ
              symbolTable.insert(name, Declaration_SourceSink)
			  Declaration_SourceSink.Type <= Type
              REQUIRE Source.Type == Declaration_SourceSink.Type
		 */

		//visit source if its not null
		if(!(declaration_SourceSink.source==null))declaration_SourceSink.source.visit(this, arg);
		//throw exception if name already present 
		if(!(symbolTable.lookupDec(declaration_SourceSink.name)==null)) 
			throw new SemanticException(declaration_SourceSink.firstToken,"Semmantic exception in declaration_SourceSink: name is already present");
		
		//insert source in symbol table
		symbolTable.insert(declaration_SourceSink.name, declaration_SourceSink);
		
		//set type of declaration according to type token
		if(declaration_SourceSink.type==Scanner.Kind.KW_file) declaration_SourceSink.setUtilType(Type.FILE);
		else if (declaration_SourceSink.type==Scanner.Kind.KW_url) declaration_SourceSink.setUtilType(Type.URL);	
		
		//throw exception if type of source is not same as type of declaration
		if(!(declaration_SourceSink.source.getUtilType()==null||(declaration_SourceSink.source.getUtilType()==declaration_SourceSink.getUtilType())))
				throw new SemanticException(declaration_SourceSink.firstToken,"Semmantic exception in declaration_SourceSink: source type not equal to expression type");
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
		//visit the expression arguement if its not null
		if (!(expression_FunctionAppWithExprArg.arg==null)) expression_FunctionAppWithExprArg.arg.visit(this, arg);
		
		//check if arg is of type integer
		if (!(expression_FunctionAppWithExprArg.arg.getUtilType()==Type.INTEGER)) 
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken,"Semmantic exception in expression_FunctionAppWithExprArg: Expression arg not integer type");
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
		
		//visit index if not null
		if(!(expression_FunctionAppWithIndexArg.arg==null))  expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		
		//set type of expression as integer
		expression_FunctionAppWithIndexArg.setUtilType(Type.INTEGER);
		return arg;
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
		/*
		 * Statement_Out ::= name Sink
			Statement_Out.Declaration <= name.Declaration
               REQUIRE:  (name.Declaration != null)
               REQUIRE:  ((name.Type == INTEGER || name.Type == BOOLEAN) && Sink.Type == SCREEN)||(name.Type == IMAGE && (Sink.Type ==FILE || Sink.Type == SCREEN))

		 */
		if (!(statement_Out.sink==null)) statement_Out.sink.visit(this, arg);
		Declaration nameDeclaration = symbolTable.lookupDec(statement_Out.name);
		if(nameDeclaration==null)
			throw new SemanticException(statement_Out.firstToken,"Semmantic exception in statement_Out: statement out not declared");
		statement_Out.setDec(nameDeclaration);
		Type nameType=nameDeclaration.getUtilType();
		if (!(
				((nameType==Type.INTEGER||nameType==Type.BOOLEAN) && (statement_Out.sink.getUtilType()==Type.SCREEN) ) ||
				( nameType==Type.IMAGE && (statement_Out.sink.getUtilType()==Type.FILE||statement_Out.sink.getUtilType()==Type.SCREEN))
			)) 
			throw new SemanticException(statement_Out.firstToken,"Semmantic exception in statement_Out: statement_Out type error");
		return arg;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		/*
		 * Statement_In ::= name Source
			   Statement_In.Declaration <= name.Declaration
               REQUIRE:  (name.Declaration != null) & (name.type == Source.type)
		 */

		//check if source is not null and visit
		if(!(statement_In.source==null)) statement_In.source.visit(this, arg);
		//check if declaration is already done
		if(symbolTable.lookupDec(statement_In.name)==null)
			throw new SemanticException(statement_In.firstToken,"Semmantic exception in statement_In: statement_Out declaration missing");
		//TODO check if type for name is same as source type rakhna hai ki nikalna hai soch lo
		//if (!(symbolTable.lookupDec(statement_In.name).getUtilType()==statement_In.source.getUtilType())) 
		//	throw new SemanticException(statement_In.firstToken,"Semmantic exception in statement_In: statement_Out declaration not of type source");
		statement_In.setDec(symbolTable.lookupDec(statement_In.name));
		return arg;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		/*
		 * Statement_Assign ::=  LHS  Expression
			REQUIRE:  LHS.Type == Expression.Type
			StatementAssign.isCartesian <= LHS.isCartesian
		 */
		//check if lhs or e is not null and visit
		if(!(statement_Assign.lhs==null)) statement_Assign.lhs.visit(this, arg);
		if(!(statement_Assign.e==null)) statement_Assign.e.visit(this, arg);
		//check if lhs type is same as expression type
		if((statement_Assign.lhs.getUtilType()==Type.IMAGE)&&(statement_Assign.e.getUtilType()==Type.INTEGER)){}
		else if(!(statement_Assign.lhs.getUtilType()==statement_Assign.e.getUtilType())) 
			throw new SemanticException(statement_Assign.firstToken,"Semmantic exception in statement_Assign: lhs type is not same as expression type");
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
		if(!(lhs.index==null)) lhs.index.visit(this, arg);
		Declaration d = symbolTable.lookupDec(lhs.name);
		if(d==null) throw new SemanticException(lhs.firstToken,"Semmantic exception in lhs");
		lhs.lhsDeclaration=d;
		lhs.setUtilType(d.getUtilType());
		if(!(lhs.index==null))	lhs.isCarteisan=lhs.index.isCartesian();
		else lhs.isCarteisan=false;
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
		if(d==null) throw new SemanticException(sink_Ident.firstToken,"Semmantic exception in sink_Ident");
		sink_Ident.setUtilType(d.getUtilType());
		if (d.getUtilType()!=Type.FILE) 
			throw new SemanticException(sink_Ident.firstToken,"Semmantic exception in sink_Ident: lhs type is not same as expression type");
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
		Declaration d = symbolTable.lookupDec(expression_Ident.name);
		if(d==null) throw new SemanticException(expression_Ident.firstToken,"Semmantic exception in expression_Ident");
		expression_Ident.setUtilType(d.getUtilType());
		return arg;
	}

}
