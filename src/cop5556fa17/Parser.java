package cop5556fa17;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;
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
	public void parse() throws SyntaxException {
		program();
		matchEOF();
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
		Program p = new Program(t, t,decsAndStatements);
		match(IDENTIFIER);
		while (((t.isKind(KW_int)||t.isKind(KW_boolean))||(t.isKind(KW_url)||t.isKind(KW_file))||(t.isKind(KW_image)))||(t.isKind(IDENTIFIER))){
		if((t.isKind(KW_int)||t.isKind(KW_boolean))||(t.isKind(KW_url)||t.isKind(KW_file))||(t.isKind(KW_image))) {
			declaration();
			match(SEMI);
		}
		else if (t.isKind(IDENTIFIER)) {
			statement();
			match(SEMI);
		}
	}
		return p;
		
	}
	
	
	/**
	 * Declaration :: =  VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration  
	 * @throws SyntaxException
	 */
	void declaration() throws SyntaxException {
			
		if (t.isKind(KW_int)||t.isKind(KW_boolean)) variableDeclaration();
		else if (t.isKind(KW_image)) imageDeclaration();
		else if(t.isKind(KW_url)||t.isKind(KW_file)) sourceSinkDeclaration();
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
		
	}
	
	/**
	 * VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
	 * @throws SyntaxException
	 */
	void variableDeclaration() throws SyntaxException {
			varType();
			match(IDENTIFIER);
			if(t.isKind(OP_ASSIGN)){
				match(OP_ASSIGN);
				expression();
			}
			else{}
	}
	
	/**
	 * VarType ::= KW_int | KW_boolean
	 * @throws SyntaxException
	 */
	void varType() throws SyntaxException {
			
		if (t.isKind(KW_int)||t.isKind(KW_boolean)) consume();
		  //In case stuff goes wrong
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(KW_int.toString()+", ");
			sb.append(KW_boolean.toString());
		    //sb.append("Assignment Statement");
			throw new SyntaxException(t, sb.toString()+")");
		}
		
	}
	
	
	/**
	 * SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
	 * @throws SyntaxException
	 */
	void sourceSinkDeclaration() throws SyntaxException {
			sourceSinkType();
			match(IDENTIFIER);
			match(OP_ASSIGN);
			source();
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
	void imageDeclaration() throws SyntaxException {
		match(KW_image);
		if (t.isKind(LSQUARE)){
			match(LSQUARE);
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		}
		else {}
		match(IDENTIFIER);
		if (t.isKind(OP_LARROW)){
			match(OP_LARROW);
			source();
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
	}
	
	
	/**
	 * Statement ::= IDENTIFIER (ImageOutStatementTaill|ImagementInStatementTail|assignmentStatementTail)
	 * @throws SyntaxException
	 */
	void statement() throws SyntaxException {
		match(IDENTIFIER);
		if(t.isKind(OP_RARROW)) imageOutStatementTaill();
		else if (t.isKind(OP_LARROW)) imageInStatementTaill();
		else if (t.isKind(LSQUARE)||t.isKind(OP_ASSIGN)) assignmentStatementTail();
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("Syntax Exception: (Expected ");
			sb.append(OP_LARROW.toString()+", ");
			sb.append(OP_RARROW.toString()+", ");
		    sb.append("Assignment Statement");
			throw new SyntaxException(t, sb.toString()+")");
		}
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
			expression();
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
	void assignmentStatement() throws SyntaxException {
		match(IDENTIFIER);
		assignmentStatementTail();
	}
	
	/**
	 * AssignmentStatementTail ::= lhstail OP_ASSIGN Expression
	 * @throws SyntaxException
	 */
	void assignmentStatementTail() throws SyntaxException {
		lhstail();
		match(OP_ASSIGN);
		expression();
	}

	
	
	/**
	 * Expression ::= OrExpression ( OP_Q Expression OP_COLON EXPRESSION|ε)
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		Expression condition,whenTrue,whenFalse;
		condition = orExpression();
		if(t.isKind(OP_Q)){
			match(OP_Q);
			whenTrue=expression();
			match(OP_COLON);
			whenFalse=expression();
		}
		return null ;
	}
	
	
	/**
	 * OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
	 * @throws SyntaxException
	 */
	Expression orExpression() throws SyntaxException {
		andExpression();
		while (t.isKind(OP_OR)){
			consume();
			andExpression();
		}
		return null;
	}
	
	
	/**
	 * AndExpression ::= EqExpression ( OP_AND  EqExpression )*
	 * @throws SyntaxException
	 */
	void andExpression() throws SyntaxException {
		eqExpression();
		while (t.isKind(OP_AND)){
			consume();
			eqExpression();
		}
	}
	
	
	/**
	 * EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
	 * @throws SyntaxException
	 */
	void eqExpression() throws SyntaxException {
		relExpression();
		while (t.isKind(OP_EQ)||t.isKind(OP_NEQ)){
			consume();
			relExpression();
		}
	}
	
	
	/**
	 * RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
	 * @throws SyntaxException
	 */
	void relExpression() throws SyntaxException {
		addExpression();
		while (t.isKind(OP_LT)||t.isKind(OP_LE)||t.isKind(OP_GT)||t.isKind(OP_GE)){
			consume();
			addExpression();
		}
	}
	
	/**
	 * AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
	 * @throws SyntaxException
	 */
	void addExpression() throws SyntaxException {
		multExpression();
		while (t.isKind(OP_PLUS)||t.isKind(OP_MINUS)){
			consume();
			multExpression();
		}
	}
	
	/**
	 * MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
	 * @throws SyntaxException
	 */
	void multExpression() throws SyntaxException {
		unaryExpression();
		while (t.isKind(OP_TIMES)||t.isKind(OP_DIV)||t.isKind(OP_MOD)){
			consume();
			unaryExpression();
		}
	}
	
	/**
	 * UnaryExpression ::= OP_PLUS UnaryExpression | OP_MINUS UnaryExpression | UnaryExpressionNotPlusMinus
	 * @throws SyntaxException
	 */
	void unaryExpression() throws SyntaxException{
		
		//check for plus
		if(t.isKind(OP_PLUS)){
			match(OP_PLUS);
			unaryExpression();
		}
		//check for minus
		else if(t.isKind(OP_MINUS)){
			match(OP_MINUS);
			unaryExpression();
		}
		//check for unaryExpressionNotPlusMinus
		else if((UnaryExpressionNotPlusMinusHS.contains(t.kind))||( (t.isKind(INTEGER_LITERAL))||(t.isKind(LPAREN))||(FunctionNameHS.contains(t.kind)) )||t.isKind(BOOLEAN_LITERAL) ||
				 (t.isKind(IDENTIFIER))||(t.isKind(OP_EXCL))){
			unaryExpressionNotPlusMinus();
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
		
	}
	
	
	/**
	 * UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary | IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
	 * @throws SyntaxException
	 */
	void unaryExpressionNotPlusMinus() throws SyntaxException{
		
		//check for x,y,r,a,X,Y,Z,A,R,DEF_X,DEF_Y
		if(UnaryExpressionNotPlusMinusHS.contains(t.kind)){
			consume();
		}
		//check for primary
		else if ( (t.isKind(INTEGER_LITERAL))||(t.isKind(LPAREN))||(FunctionNameHS.contains(t.kind)||t.isKind(BOOLEAN_LITERAL)) ){
			primary();
		}
		//check for IdentOrPixelSelectorExpression
		else if (t.isKind(IDENTIFIER)){
			identOrPixelSelectorExpression();
		}
		else if (t.isKind(OP_EXCL)){
			match(OP_EXCL);
			unaryExpression();
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
	void identOrPixelSelectorExpression() throws SyntaxException{
		match(IDENTIFIER);
		if(t.isKind(LSQUARE)){
			match(LSQUARE);
			selector();
			match(RSQUARE);
		}
		else{
			//TODO write else code to match follow set and give appropriate error message
			}
		
	}
	
	/**
	 * Lhs::=  IDENTIFIER lhstail
	 * @throws SyntaxException
	 */
	void lhs()throws SyntaxException{
		match(IDENTIFIER);
		lhstail();
		
	}
	
	/**
	 * lhstail ::=( LSQUARE LhsSelector RSQUARE   | ε )
	 * @throws SyntaxException
	 */
	void lhstail() throws SyntaxException{
		if(t.isKind(LSQUARE)){
			match(LSQUARE);
			lhsSelector();
			match(RSQUARE);}
			else{
			//TODO write else code to match follow set and give appropriate error message
			}
	}
	
	/**
	 * FunctionApplication ::= FunctionName (LPAREN Expression RPAREN | LSQUARE Selector RSQUARE)
	 * @return 
	 * @throws SyntaxException
	 */
	
	Expression functionApplication() throws SyntaxException{
		functionName();
		switch(t.kind){
		case LPAREN:
			match(LPAREN);
			expression();
			match(RPAREN);
			break;
		case LSQUARE:
			match(LSQUARE);
			selector();
			match(RSQUARE);
			break;
			
		default:
			throw new SyntaxException(t, "Syntax Exception: (Expected a token of type/s :"+
					LPAREN.toString()+
			" or "+LSQUARE.toString() +
			")");
		}
		return null;
		
	}
	
	
	/**
	 * FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs | KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r
	 * @throws SyntaxException
	 */
	void functionName() throws SyntaxException{
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
		
	}
	
	
	/**
	 * LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUARE
	 * @throws SyntaxException
	 */
	
	void lhsSelector() throws SyntaxException{
		match(LSQUARE);
		switch(t.kind){
		case KW_x:
			xySelector();
			break;
		case KW_r:
			raSelector();
			break;
		default:
			throw new SyntaxException(t, "Syntax Exception: (Expected a token of type/s :"+
																		KW_x.toString()+
																" or "+KW_r.toString() +
																")");
		}
		match(RSQUARE);			
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
