package cop5556fa17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.*;
import static cop5556fa17.Scanner.Kind.*;

/**
 * 
 * This Class Simple parser checks the input program for legality of inputs syntactically with the below defined LL1 grammar. 
 * Legal LL1 grammar for this top down parser comprises of
 * 
 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
 * Declaration :: =  VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration  
 * VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
 * VarType ::= KW_int | KW_boolean
 * SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
 * SourceSinkType := KW_url | KW_file
 * ImageDeclaration::=KW_image  (LSQUARE Expression COMMA Expression RSQUARE | ε) IDENTIFIER ( OP_LARROW Source | ε )
 * Statement ::= IDENTIFIER (ImageOutStatementTaill | ImageInStatementTaill | AssignmentStatementTail)
 * ImageOutStatement ::= IDENTIFIER ImageOutStatementTaill
 * ImageOutStatementTaill ::= OP_RARROW Sink
 * Sink ::= IDENTIFIER | KW_SCREEN
 * ImageInStatement ::= IDENTIFIER ImageInStatementTaill
 * ImageInStatementTaill ::= OP_LARROW Source
 * Source ::= STRING_LITERAL | OP_AT Expression | IDENTIFIER
 * AssignmentStatement ::= IDENTIFIER AssignmentStatementTail
 * AssignmentStatementTail ::= lhstail OP_ASSIGN Expression // 
 * Expression ::= OrExpression ( OP_Q Expression OP_COLON EXPRESSION|ε)
 * OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
 * AndExpression ::= EqExpression ( OP_AND  EqExpression )*
 * EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
 * RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
 * AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
 * MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
 * UnaryExpression ::= OP_PLUS UnaryExpression | OP_MINUS UnaryExpression | UnaryExpressionNotPlusMinus
 * UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary | IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
 * Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
 * IdentOrPixelSelectorExpression::=  IDENTIFIER (LSQUARE Selector RSQUARE   | ε)
 * Lhs::=  IDENTIFIER lhstail
 * lhstail ::= (LSQUARE LhsSelector RSQUARE )  | ε //for eg [x,y]
 * FunctionApplication ::= FunctionName (LPAREN Expression RPAREN | LSQUARE Selector RSQUARE)
 * FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs | KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r
 * LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUARE
 * XySelector ::= KW_x COMMA KW_y
 * RaSelector ::= KW_r COMMA KW_A
 * Selector ::= Expression COMMA Expression
 * 
 * 
 * @author akshay (akshay.sharma@ufl.edu)
 *
 */

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			// and found token "+t.getText());
			super(message+" and found token \""+t.getText()+"\" (type: "+t.kind.toString()+") at line "+t.line+"  "+t.pos_in_line);
			this.t = t;
		}

	}
	
	
	/* 
	 * Class Variables Scanner - to get the list of keywords form the enum
	 * 	token - to do actual parsing
	 */
	Scanner scanner;
	Token t;
	HashSet<Kind> FunctionNameHS;
	HashSet<Kind> UnaryExpressionNotPlusMinusHS;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		
		//Initialize HashSet for FunctionName
		FunctionNameHS = new HashSet<Kind>();
		 FunctionNameHS.add(KW_sin);
		 FunctionNameHS.add(KW_cos);
		 FunctionNameHS.add(KW_atan);
		 FunctionNameHS.add(KW_abs);
		 FunctionNameHS.add(KW_cart_x);
		 FunctionNameHS.add(KW_cart_y);
		 FunctionNameHS.add(KW_polar_a);
		 FunctionNameHS.add(KW_polar_r);
		 
		//Initialize HashSet for UnaryExpressionNotPlusMinus
		 UnaryExpressionNotPlusMinusHS = new HashSet<Kind>();
		 UnaryExpressionNotPlusMinusHS.add(KW_x);
		 UnaryExpressionNotPlusMinusHS.add(KW_y);
		 UnaryExpressionNotPlusMinusHS.add(KW_r);
		 UnaryExpressionNotPlusMinusHS.add(KW_a);
		 UnaryExpressionNotPlusMinusHS.add(KW_X);
		 UnaryExpressionNotPlusMinusHS.add(KW_Y);
		 UnaryExpressionNotPlusMinusHS.add(KW_Z);
		 UnaryExpressionNotPlusMinusHS.add(KW_A);
		 UnaryExpressionNotPlusMinusHS.add(KW_R);
		 UnaryExpressionNotPlusMinusHS.add(KW_DEF_X);
		 UnaryExpressionNotPlusMinusHS.add(KW_DEF_Y);
		 
		 
	}
	
	
	/**
	 * consume function which replaces t with the next token of the program
	 * @param void
	 * @return void
	 */
	void consume(){
		t=scanner.nextToken();
	}
	
	
	/**
	 * match function defined below checks the front of the token queue and matches 
	 * the type of token with the type passed on as the arguments
	 */
	void match(Kind kind) throws SyntaxException{
		if(t.isKind(kind)){
			consume();
		}
		else{
			throw new SyntaxException(t, "Syntax Exception: (Expected a token of type/s :"+
					kind.toString()+")");
		}
	}
	
	
	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();
		return program;
	}
	
	
	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		Token firstToken = t;
		
		match(IDENTIFIER);
		while (((t.isKind(KW_int)||t.isKind(KW_boolean))||(t.isKind(KW_url)||t.isKind(KW_file))||(t.isKind(KW_image)))||(t.isKind(IDENTIFIER))){
		if((t.isKind(KW_int)||t.isKind(KW_boolean))||(t.isKind(KW_url)||t.isKind(KW_file))||(t.isKind(KW_image))) {
			decsAndStatements.add(declaration());
			match(SEMI);
		}
		else if (t.isKind(IDENTIFIER)) {
			decsAndStatements.add(statement());
			match(SEMI);
		}
	}
		return new Program(firstToken, firstToken,decsAndStatements);
		
	}
	
	
	/**
	 * Declaration :: =  VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration  
	 * @throws SyntaxException
	 */
	Declaration declaration() throws SyntaxException {
		Declaration declaration = null;
		if (t.isKind(KW_int)||t.isKind(KW_boolean)) declaration =  variableDeclaration();
		else if (t.isKind(KW_image)) declaration = imageDeclaration();
		else if(t.isKind(KW_url)||t.isKind(KW_file)) declaration = sourceSinkDeclaration();
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(KW_int.toString()+", ");
			sb.append(KW_boolean.toString()+", ");
			sb.append(KW_url.toString()+", ");
			sb.append(KW_file.toString()+", ");
			sb.append(KW_image.toString());
		    //sb.append("Assignment Statement");
			throw new SyntaxException(t, sb.toString()+")");
		}
		return declaration;
	}
	
	/**
	 * VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
	 * @throws SyntaxException
	 */
	Declaration_Variable variableDeclaration() throws SyntaxException {
		Declaration_Variable variableDeclaration = null;
			Token firstToken = t;
			Token type = varType();
			Token name = t;
			Expression e = null;
			match(IDENTIFIER);
			if(t.isKind(OP_ASSIGN)){
				match(OP_ASSIGN);
				e = expression();
			}
			else{}
			variableDeclaration = new Declaration_Variable(firstToken, type, name,e );
			return variableDeclaration;
	}
	
	/**
	 * VarType ::= KW_int | KW_boolean
	 * @throws SyntaxException
	 */
	Token varType() throws SyntaxException {
			Token ft = null;
		if (t.isKind(KW_int)||t.isKind(KW_boolean)) {
			ft = t;
			consume();
		}
		  //In case stuff goes wrong
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(KW_int.toString()+", ");
			sb.append(KW_boolean.toString());
		    //sb.append("Assignment Statement");
			throw new SyntaxException(t, sb.toString()+")");
		}
		return ft;
		
	}
	
	
	/**
	 * SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
	 * @throws SyntaxException
	 */
	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException {
		Declaration_SourceSink sourceSinkDeclaration = null;
		Token firstToken = t;
		Source source = null;
		Token name = null;
			sourceSinkType();
			name = t;
			match(IDENTIFIER);
			match(OP_ASSIGN);
			source = source();
			sourceSinkDeclaration = new Declaration_SourceSink(firstToken, firstToken, name, source);
			return sourceSinkDeclaration;
	}
	
	/**
	 * SourceSinkType := KW_url | KW_file
	 * @throws SyntaxException
	 */
	void sourceSinkType() throws SyntaxException {
			
		if (t.isKind(KW_url)||t.isKind(KW_file)) consume();
		  //In case stuff goes wrong
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(KW_url.toString()+", ");
			sb.append(KW_file.toString());
		    //sb.append("Assignment Statement");
			throw new SyntaxException(t, sb.toString()+")");
		}
		
	}
	
	
	/**
	 * ImageDeclaration::=KW_image  (LSQUARE Expression COMMA Expression RSQUARE | ε) IDENTIFIER ( OP_LARROW Source | ε )   
	 * @throws SyntaxException
	 */
	Declaration_Image imageDeclaration() throws SyntaxException {
		Declaration_Image imageDeclaration = null;
		Token firstToken = t;
		Expression xSize=null;
		Expression ySize = null;
		Token name = null;
		Source source = null;
		match(KW_image);
		if (t.isKind(LSQUARE)){
			match(LSQUARE);
			xSize=expression();
			match(COMMA);
			ySize=expression();
			match(RSQUARE);
		}
		else {}
		name = t;
		match(IDENTIFIER);
		if (t.isKind(OP_LARROW)){
			match(OP_LARROW);
			source = source();
		}
		else {}
		/**
		 * 
		 * In case stuff goes wrong
		 	else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(OP_LARROW.toString()+", ");
			sb.append(OP_RARROW.toString()+", ");
		    sb.append("Assignment Statement");
			throw new SyntaxException(t, sb.toString()+")");
		}
		*/
		imageDeclaration = new Declaration_Image(firstToken, xSize, ySize, name, source);
		return imageDeclaration;
	}
	
	
	/**
	 * Statement ::= IDENTIFIER (ImageOutStatementTaill|ImagementInStatementTail|assignmentStatementTail)
	 * @throws SyntaxException
	 */
	Statement statement() throws SyntaxException {
		Statement statement = null;
		Token firstToken = t;
		match(IDENTIFIER);
		if(t.isKind(OP_RARROW)) statement =  new Statement_Out(firstToken,firstToken, imageOutStatementTaill());//imageOutStatementTaill();
		else if (t.isKind(OP_LARROW)) statement = new Statement_In(firstToken,firstToken, imageInStatementTaill()); //imageInStatementTaill();
		else if (t.isKind(LSQUARE)||t.isKind(OP_ASSIGN)) statement = assignmentStatementTail(firstToken);
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(OP_LARROW.toString()+", ");
			sb.append(OP_RARROW.toString()+", ");
		    sb.append("Assignment Statement");
			throw new SyntaxException(t, sb.toString()+")");
		}
		return statement;
	}
	
	
	
	/**
	 * ImageOutStatement ::= IDENTIFIER ImageOutStatementTaill
	 * @throws SyntaxException
	 */
	Statement_Out imageOutStatement() throws SyntaxException {
		Token ft=t;
		Sink s=null;
		match(IDENTIFIER);
		s=imageOutStatementTaill();
		return new Statement_Out(ft, ft, s);
	}
	
	/**
	 * ImageOutStatementTaill ::= OP_RARROW Sink
	 * @throws SyntaxException
	 */
	Sink imageOutStatementTaill() throws SyntaxException {
		Sink s= null;
		match(OP_RARROW);
		s=sink();
		return s;
	}
	
	/**
	 * Sink ::= IDENTIFIER | KW_SCREEN
	 * @throws SyntaxException
	 */
	Sink sink() throws SyntaxException {
		Sink s=null;
		if(t.isKind(IDENTIFIER)){
			s=new Sink_Ident(t,t);
			match(IDENTIFIER);
			}
		else if (t.isKind(KW_SCREEN)){
			s=new Sink_SCREEN(t);
			match(KW_SCREEN);
		}			
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(KW_SCREEN.toString()+", ");
		    sb.append(IDENTIFIER.toString()+", ");
			throw new SyntaxException(t, sb.toString()+")");
		}
		return s;
	}
	
	/**
	 * ImageInStatement ::= IDENTIFIER ImageInStatementTaill
	 * @throws SyntaxException
	 */
	Statement_In imageInStatement() throws SyntaxException {
		Source s=null;
		Token ft=t;
		match(IDENTIFIER);
		s=imageInStatementTaill();
		return new Statement_In(ft, ft, s);
	}
	
	/**
	 * ImageInStatementTaill ::= OP_LARROW Source
	 * @throws SyntaxException
	 */
	Source imageInStatementTaill() throws SyntaxException {
		Source s = null;
		match(OP_LARROW);
		s=source();
		return s;
		
	}
	
	
	/**
	 * Source ::= STRING_LITERAL | OP_AT Expression | IDENTIFIER
	 * @throws SyntaxException
	 */
	Source source() throws SyntaxException {
		Source s = null;
		Token FirstToken = t;
		if(t.isKind(STRING_LITERAL)) {
			s=new Source_StringLiteral(t, t.getText());
			consume();
		}		
		else if (t.isKind(IDENTIFIER)){
			s=new Source_Ident(t, t);
			consume();
		}
		else if (t.isKind(OP_AT)){
			match(OP_AT);
			s = new Source_CommandLineParam(FirstToken,expression());
		}
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected a STRING_LITERAL, @ or IDENTIFIER) ");
			throw new SyntaxException(t, sb.toString()+")");
		}
		return s;
	}
	
	/**
	 * AssignmentStatement ::= IDENTIFIER AssignmentStatementTail
	 * @throws SyntaxException
	 */
	Statement_Assign assignmentStatement() throws SyntaxException {
		Token firstToksn = t;
		match(IDENTIFIER);
		return assignmentStatementTail(firstToksn);
	}
	
	/**
	 * AssignmentStatementTail ::= lhstail OP_ASSIGN Expression
	 * @throws SyntaxException
	 */
	Statement_Assign assignmentStatementTail(Token Identifier) throws SyntaxException {
		Index i =lhstail();
		match(OP_ASSIGN);
		Expression e = expression();
		return new Statement_Assign(Identifier, new LHS(Identifier, Identifier, i), e);
	}

	
	
	/**
	 * Expression ::= OrExpression ( OP_Q Expression OP_COLON EXPRESSION|ε)
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		Expression result = null;
		Token firstToken = t;
		Expression condition = orExpression();
		if(t.isKind(OP_Q)){
			match(OP_Q);
			Expression trueExpression = expression();
			match(OP_COLON);
			Expression falseExpression = expression();
			result = new Expression_Conditional(firstToken, condition, trueExpression, falseExpression);
		}
		else result = condition;
		return result;
	}
	
	
	/**
	 * OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
	 * @throws SyntaxException
	 */
	Expression orExpression() throws SyntaxException {
		Token firstToken = t;
		Expression bExpression = andExpression();
		if (!t.isKind(OP_OR)){
			return bExpression;
		}
		else{		
		while (t.isKind(OP_OR)){
			Token op = t;
			consume();
			Expression e1 =andExpression();
			bExpression = new Expression_Binary(firstToken,bExpression , op, e1);
		}}
		return bExpression;
	}
	
	
	/**
	 * AndExpression ::= EqExpression ( OP_AND  EqExpression )*
	 * @throws SyntaxException
	 */
	Expression andExpression() throws SyntaxException {
		Token firstToken = t;
		Expression bExpression = eqExpression();
		if (!t.isKind(OP_AND)){
			return bExpression;
		}
		else{
		while (t.isKind(OP_AND)){
			Token op = t;
			consume();
			Expression e1 =eqExpression();
			bExpression = new Expression_Binary(firstToken,bExpression , op, e1);
		}}
		return bExpression;
	}
	
	
	/**
	 * EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
	 * @throws SyntaxException
	 */
	Expression eqExpression() throws SyntaxException {
		Token firstToken = t;
		Expression bExpression = relExpression();
		if (!(t.isKind(OP_EQ)||t.isKind(OP_NEQ))){
			return bExpression;
		}
		else{
		while (t.isKind(OP_EQ)||t.isKind(OP_NEQ)){
			Token op = t;
			consume();
			Expression e1 = relExpression();
			bExpression = new Expression_Binary(firstToken,bExpression , op, e1);
		}}
		return bExpression;
	}
	
	
	/**
	 * RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
	 * @throws SyntaxException
	 */
	Expression relExpression() throws SyntaxException {
		Token firstToken = t;
		Expression bExpression = addExpression();
		if (!(t.isKind(OP_LT)||t.isKind(OP_LE)||t.isKind(OP_GT)||t.isKind(OP_GE))){
			return bExpression;
		}
		else{
		while (t.isKind(OP_LT)||t.isKind(OP_LE)||t.isKind(OP_GT)||t.isKind(OP_GE)){
			Token op = t;
			consume();
			Expression e1 = addExpression();
			bExpression = new Expression_Binary(firstToken,bExpression , op, e1);
		}}
		return bExpression;
	}
	
	/**
	 * AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
	 * @throws SyntaxException
	 */
	Expression addExpression() throws SyntaxException {
		Token firstToken = t;
		Expression bExpression = multExpression();
		if (!(t.isKind(OP_PLUS)||t.isKind(OP_MINUS))){
			return bExpression;
		}
		else{
		while (t.isKind(OP_PLUS)||t.isKind(OP_MINUS)){
			Token op = t;
			consume();
			Expression e1 = multExpression();
			bExpression = new Expression_Binary(firstToken,bExpression , op, e1);
		}}
		return bExpression;
	}
	
	/**
	 * MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
	 * @throws SyntaxException
	 */
	Expression multExpression() throws SyntaxException {
		Token firstToken = t;
		Expression bExpression=unaryExpression();
		if (!(t.isKind(OP_TIMES)||t.isKind(OP_DIV)||t.isKind(OP_MOD))){
			return bExpression;
		}
		else{
		while (t.isKind(OP_TIMES)||t.isKind(OP_DIV)||t.isKind(OP_MOD)){
			Token op = t;
			consume();
			Expression e1 = unaryExpression();
			bExpression = new Expression_Binary(firstToken,bExpression , op, e1);
		}}
		return bExpression;
	}
	
	/**
	 * UnaryExpression ::= OP_PLUS UnaryExpression | OP_MINUS UnaryExpression | UnaryExpressionNotPlusMinus
	 * @throws SyntaxException
	 */
	Expression unaryExpression() throws SyntaxException{
		Expression  unaryExpression = null;
		Token firstToken = t;
		//check for plus
		if(t.isKind(OP_PLUS)){
			match(OP_PLUS);
			unaryExpression = new Expression_Unary(firstToken,firstToken,unaryExpression());
		}
		//check for minus
		else if(t.isKind(OP_MINUS)){
			match(OP_MINUS);
			unaryExpression = new Expression_Unary(firstToken,firstToken,unaryExpression());
		}
		//check for unaryExpressionNotPlusMinus
		else if((UnaryExpressionNotPlusMinusHS.contains(t.kind))||( (t.isKind(INTEGER_LITERAL))||(t.isKind(LPAREN))||(FunctionNameHS.contains(t.kind)) )||t.isKind(BOOLEAN_LITERAL) ||
				 (t.isKind(IDENTIFIER))||(t.isKind(OP_EXCL))){
			unaryExpression = unaryExpressionNotPlusMinus();
		}
		//else throw error
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected a mathematical function or a token of type/s :x,y,r,a,X,Y,Z,A,R,DEF_X, DEF_Y,");
			sb.append(OP_EXCL.toString()+", ");
		    sb.append(IDENTIFIER.toString()+", ");
		    sb.append(INTEGER_LITERAL.toString()+", ");
		    sb.append(OP_PLUS.toString()+", ");
		    sb.append(OP_MINUS.toString()+", ");
		    sb.append(LPAREN.toString()+", ");
			throw new SyntaxException(t, sb.toString()+")");
		}
		
		return unaryExpression;
		
	}
	
	
	/**
	 * UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary | IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
	 * @throws SyntaxException
	 */
	Expression unaryExpressionNotPlusMinus() throws SyntaxException{
		Expression unaryExpressionNotPlusMinus = null;
		//check for x,y,r,a,X,Y,Z,A,R,DEF_X,DEF_Y
		Token firstToken = t;
		if(UnaryExpressionNotPlusMinusHS.contains(t.kind)){
			unaryExpressionNotPlusMinus = new Expression_PredefinedName(t, t.kind);
			consume();
		}
		//check for primary
		else if ( (t.isKind(INTEGER_LITERAL))||(t.isKind(LPAREN))||(FunctionNameHS.contains(t.kind)||t.isKind(BOOLEAN_LITERAL)) ){
			unaryExpressionNotPlusMinus = primary();
		}
		//check for IdentOrPixelSelectorExpression
		else if (t.isKind(IDENTIFIER)){
			unaryExpressionNotPlusMinus = identOrPixelSelectorExpression();
		}
		else if (t.isKind(OP_EXCL)){
			match(OP_EXCL);
			unaryExpressionNotPlusMinus = new Expression_Unary(firstToken, firstToken,unaryExpression());
			
		}
		
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected a mathematical function or a token of type/s :x,y,r,a,X,Y,Z,A,R,DEF_X, DEF_Y,");
			sb.append(OP_EXCL.toString()+", ");
		    sb.append(IDENTIFIER.toString()+", ");
		    sb.append(INTEGER_LITERAL.toString()+", ");
		    sb.append(LPAREN.toString()+", ");
			throw new SyntaxException(t, sb.toString()+")");
		}
		return unaryExpressionNotPlusMinus;
	}
	
	/**
	 * Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | Boolean_literal
	 * @throws SyntaxException
	 */
	Expression primary() throws SyntaxException{
		Expression primary = null;
		
		if(t.isKind(INTEGER_LITERAL)){
			primary = new Expression_IntLit(t, t.intVal());
			match(INTEGER_LITERAL);
		}
		else if(t.isKind(BOOLEAN_LITERAL)){
			primary= new Expression_BooleanLit(t, t.boolVal());
			match(BOOLEAN_LITERAL);
		}		
		else if (t.isKind(LPAREN)){
			match(LPAREN);
			primary=expression();
			match(RPAREN);
		}
		else if (FunctionNameHS.contains(t.kind)){
			primary=functionApplication();
			}
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected a token of type/s :");
			Iterator<Kind> it = FunctionNameHS.iterator();
			sb.append(INTEGER_LITERAL.toString()+", ");
		    sb.append(LPAREN.toString()+", "); 
			while(it.hasNext()){
		        sb.append(it.next().toString()+", ");
		     }
		     
			throw new SyntaxException(t, sb.toString()+")");
		}
		
		return primary;
		
	}
	
	/**
	 * IdentOrPixelSelectorExpression::=  IDENTIFIER (LSQUARE Selector RSQUARE   | ε)
	 * @throws SyntaxException
	 */
	Expression identOrPixelSelectorExpression() throws SyntaxException{
		Expression identOrPixelSelectorExpression = null;
		Token firstToken = t;
		match(IDENTIFIER);
		if(t.isKind(LSQUARE)){
			match(LSQUARE);
			identOrPixelSelectorExpression = new Expression_PixelSelector(firstToken,firstToken,selector());
			match(RSQUARE);
		}
		else{
			identOrPixelSelectorExpression=new Expression_Ident(firstToken, firstToken);
			}
		return identOrPixelSelectorExpression;
		
	}
	
	/**
	 * Lhs::=  IDENTIFIER lhstail
	 * @throws SyntaxException
	 */
	LHS lhs()throws SyntaxException{
		LHS lhs = null;
		Token ft = t;
		match(IDENTIFIER);	
		lhs = new LHS(ft,ft,lhstail());
		//TODO Index in lhstail() can return null Make a testcase to exploit this
		return lhs;
	}
	
	/**
	 * lhstail ::=( LSQUARE LhsSelector RSQUARE   | ε )
	 * @throws SyntaxException
	 */
	Index lhstail() throws SyntaxException{
		Index lhstail=null;
		if(t.isKind(LSQUARE)){
			match(LSQUARE);
			lhstail= lhsSelector();
			match(RSQUARE);
			}
		return lhstail;
	}
	
	/**
	 * FunctionApplication ::= FunctionName (LPAREN Expression RPAREN | LSQUARE Selector RSQUARE)
	 * @return 
	 * @throws SyntaxException
	 */
	
	Expression functionApplication() throws SyntaxException{
		Token firstToken = t;
		Kind function=functionName();
		Expression functionApplication = null;
		switch(t.kind){
		case LPAREN:
			match(LPAREN);
			functionApplication = new Expression_FunctionAppWithExprArg(firstToken,function,expression());
			match(RPAREN);
			break;
		case LSQUARE:
			match(LSQUARE);
			functionApplication = new Expression_FunctionAppWithIndexArg(firstToken, function, selector());
			match(RSQUARE);
			break;
			
		default:
			throw new SyntaxException(t, "Syntax Exception: (Expected a token of type/s :"+
					LPAREN.toString()+
			" or "+LSQUARE.toString() +
			")");
		}
		return functionApplication;
		
	}
	
	
	/**
	 * FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs | KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r
	 * @throws SyntaxException
	 */
	Kind functionName() throws SyntaxException{
		Kind functionName=t.kind;
		if(FunctionNameHS.contains(t.kind)){
			consume();
		}
		else {StringBuffer sb = new StringBuffer();
		sb.append("Syntax Exception: (Expected a token of type/s :");
		Iterator<Kind> it = FunctionNameHS.iterator();
	     while(it.hasNext()){
	        sb.append(it.next().toString()+", ");
	     }
		throw new SyntaxException(t, sb.toString()+")");}
		return functionName;
	}
	
	
	/**
	 * LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUARE
	 * @throws SyntaxException
	 */
	
	Index lhsSelector() throws SyntaxException{
		Index lhsSelector = null;
		match(LSQUARE);
		switch(t.kind){
		case KW_x:
			lhsSelector=xySelector();
			break;
		case KW_r:
			lhsSelector=raSelector();
			break;
		default:
			throw new SyntaxException(t, "Syntax Exception: (Expected a token of type/s :"+
																		KW_x.toString()+
																" or "+KW_r.toString() +
																")");
		}
		match(RSQUARE);
		return lhsSelector;
	}
	
	/**
	 * XySelector ::= KW_x COMMA KW_y
	 * @throws SyntaxException
	 */
	Index xySelector() throws SyntaxException{
		Token firstToken = t;
		match(KW_x);
		Expression e0=new Expression_PredefinedName(firstToken, KW_x);
		match(COMMA);
		Token firstTokene1=t;
		match(KW_y);
		Expression e1=new Expression_PredefinedName(firstTokene1, KW_y);
		return new Index(firstToken, e0, e1);
	}

	
	/**
	 * RaSelector ::= KW_r COMMA KW_A
	 * @throws SyntaxException
	 */
	Index raSelector() throws SyntaxException{
		Token firstToken = t;
		match(KW_r);
		Expression e0=new Expression_PredefinedName(firstToken, KW_r);
		match(COMMA);
		Token firstTokene1=t;
		match(KW_A);
		Expression e1=new Expression_PredefinedName(firstTokene1, KW_A);
		return new Index(firstToken, e0, e1);
	}
	
	
	/**
	 * Selector ::= Expression COMMA Expression
	 * @throws SyntaxException
	 */
	Index selector() throws SyntaxException{
		Token firstToken = t;
		Expression e0=expression();
		match(COMMA);
		Expression e1=expression();
		return new Index(firstToken, e0, e1);
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
	
	class FirstSets{
		HashMap<String, HashSet<Kind>> firstSets;
		public FirstSets()
		{
			HashMap<String, HashSet<Kind>> firstSets = new HashMap<String, HashSet<Kind>>();
			
		}
	}
}
