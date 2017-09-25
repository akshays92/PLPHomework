package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}



	/**
	 * Simple test case with an empty program.  This test 
	 * expects an SyntaxException because all legal programs must
	 * have at least an identifier
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  This is not legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //Create a parser
		thrown.expect(SyntaxException.class);
		try {
		parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}

	
	/** Another example.  This is a legal program and should pass when 
	 * your parser is implemented.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //
		parser.parse();
	}
	

	/**
	 * This example invokes the method for expression directly. 
	 * Effectively, we are viewing Expression as the start
	 * symbol of a sub-language.
	 *  
	 * Although a compiler will always call the parse() method,
	 * invoking others is useful to support incremental development.  
	 * We will only invoke expression directly, but 
	 * following this example with others is recommended.  
	 * 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void expression1() throws SyntaxException, LexicalException {
		String input = "2*2+5+x>2*2+5+x==d6a09aa6aa42d705e4f7b01eaa57a671f05da686ded99c77294d3135563820a>=2*2+5+x!=2*2+5+x<=2*2+5+x|2*2+5+x>2*2+5+x==2*2+5+x<2*2+5+x&2*2+5+x>=2*2+5+x!=2*2+5+x<=2*2+5+x";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	
	
	@Test
	/**
	 * PLEASE DELETE THIS TEST CASE AFTER TESTING YOUR PROGRAM
	 * @throws SyntaxException
	 * @throws LexicalException
	 * @author AkshaySharma
	 */
	public void expressionTest3() throws SyntaxException, LexicalException {
		String k = "2*2+5+x>2*2+5+x==d6a09aa6aa42d705e4f7b01eaa57a671f05da686ded99c77294d3135563820a>=2*2+5+x!=2*2+5+x<=2*2+5+x|2*2+5+x>2*2+5+x==2*2+5+x<2*2+5+x&2*2+5+x>=2*2+5+x!=2*2+5+x<=2*2+5+x";	
		String[] input = {k, k+"?"+k+":"+k}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.orExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	
	
	
	/**
	 * these test cases invoke SimpleParser.orExpression() 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void orExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"2*2+5+x>2*2+5+x==2*2+5+x<2*2+5+x&2*2+5+x>=2*2+5+x!=2*2+5+x<=2*2+5+x|2*2+5+x>2*2+5+x==2*2+5+x<2*2+5+x&2*2+5+x>=2*2+5+x!=2*2+5+x<=2*2+5+x"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.orExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	
	
	/**
	 * these test cases invoke SimpleParser.andExpression() 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void andExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"2*2+5+x>2*2+5+x==2*2+5+x<2*2+5+x&2*2+5+x>=2*2+5+x!=2*2+5+x<=2*2+5+x"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.andExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	
	/**
	 * these test cases invoke SimpleParser.eqExpression() 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void eqExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"2*2+5+x>2*2+5+x==2*2+5+x<2*2+5+x","2*2+5+x>=2*2+5+x!=2*2+5+x<=2*2+5+x"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.eqExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	
	
	/**
	 * these test cases invoke SimpleParser.addExpression() 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void relExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"2*2+5+x>2*2+5+x","2*2+5+x<2*2+5+x","2*2+5+x>=2*2+5+x","2*2+5+x<=2*2+5+x"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.relExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	//Negative test for add expression
		@Test
		public void relExpressionTest2() throws SyntaxException, LexicalException {
			String[] input = {"2*2+>5+x"}; 
			thrown.expect(SyntaxException.class);
			try{
			for(String x : input){
				show(x);
				Scanner scanner = new Scanner(x).scan(); 
				show(scanner); 
				SimpleParser parser = new SimpleParser(scanner);  
				parser.relExpression();  //Call expression directly.  
			}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
		}
	
	
	
	/**
	 * these test cases invoke SimpleParser.addExpression() 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void addExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"2*2+5-x"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.addExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	//Negative test for add expression
		@Test
		public void addExpressionTest2() throws SyntaxException, LexicalException {
			String[] input = {"2*2+>5+x"}; 
			thrown.expect(SyntaxException.class);
			try{
			for(String x : input){
				show(x);
				Scanner scanner = new Scanner(x).scan(); 
				show(scanner); 
				SimpleParser parser = new SimpleParser(scanner);  
				parser.addExpression();  //Call expression directly.  
			}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
		}
	
	
	
	/**
	 * these test cases invoke SimpleParser.multExpression() 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void multExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"++--!!variable[k,l]*+x/-y%!r"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.multExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	//Negative test for mult expression
		@Test
		public void multExpressionTest2() throws SyntaxException, LexicalException {
			String[] input = {"++,--!!variable[k,l]*+x/-y%!r"}; 
			thrown.expect(SyntaxException.class);
			try{
			for(String x : input){
				show(x);
				Scanner scanner = new Scanner(x).scan(); 
				show(scanner); 
				SimpleParser parser = new SimpleParser(scanner);  
				parser.multExpression();  //Call expression directly.  
			}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
		}
	
	
	/**
	 * these test cases invoke SimpleParser.unaryExpression() 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void unaryExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"+x","-y","!r","+a","+X","!Y","!Z","--A",
				"++++123"/*integer literal*/, "(ij)" /* dummy expression */, "cos(2)", "atan(x)" /*Function Applications*/,
				"++--!!chakka[k,l]"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.unaryExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	//Negative test for unary expression
	@Test
	public void unaryExpressionTest2() throws SyntaxException, LexicalException {
		String[] input = {
				"++--!!chakka[k,l,]"}; 
		thrown.expect(SyntaxException.class);
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.unaryExpression();  //Call expression directly.  
		}} catch(SyntaxException e){System.out.println(e.getMessage());throw e;}	
	}
	

	
	/**
	 * these test cases invoke SimpleParser.primary() 
	 */
	@Test
	public void primaryTest1() throws SyntaxException, LexicalException {
		String[] input = {"123"/*integer literal*/, "(ij)" /* dummy expression */, "cos(2)", "atan(x)" /*Function Applications*/ }; 
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.primary();  //Call expression directly.  
		}	
	}
	@Test
	public void primaryTest2() throws SyntaxException, LexicalException {
		String[] input = {"x"}; 
		try{
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.primary();  //Call expression directly.  
		}	}
		catch(Exception e){System.out.println(e.getMessage());}
	}
	
	
	
	
	/**
	 * these test cases invoke SimpleParser.lhs() 
	 */
	@Test
	public void lhstest1() throws SyntaxException, LexicalException {
		String[] input = {"AKSHAY", "akshay[[x,y]]", "kk[[r,A]]", "m[[r,A]]"}; 
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.lhs();  //Call expression directly.  
		}	
	}
	//Negative test cases for FUnctionApplicationtest
	@Test
	public void lhstest2() throws SyntaxException, LexicalException {
		String[] input = {"Sharma", "cos[56]"}; 
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);
			try{
			parser.lhs();  //Call expression directly.  
			} catch (Exception e){System.out.println(e.getMessage());}
		}	
	} 
	
	
	/**
	 * these test cases invoke SimpleParser.FunctionApplication() 
	 */
	@Test
	public void FunctionApplicationtest1() throws SyntaxException, LexicalException {
		String[] input = {"cos(2)", "atan(x)", "polar_a(6)", "sin[4,3]", "abs[x,y]"}; 
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.functionApplication();  //Call expression directly.  
		}	
	}
	//Negative test cases for FUnctionApplicationtest
	@Test
	public void FunctionApplicationtest2() throws SyntaxException, LexicalException {
		String[] input = {"x", "cos[5]","abs[5]"}; 
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);
			try{
			parser.functionApplication();  //Call expression directly.  
			} catch (Exception e){System.out.println(e.getMessage());}
		}	
	}
	
	/**
	 * these test cases invoke SimpleParser.IdentOrPixelSelectorExpression()
	 */
	@Test
	public void identOrPixelSelectorExpressionTest1() throws SyntaxException, LexicalException {
		String[] input = {"akshay","i[i,j]34"};
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.identOrPixelSelectorExpression();  //Call expression directly.  
		}	
	} 
	
	
	/**
	 * these test cases invoke SimpleParser.functionName()
	 */
	@Test
	public void FunctionNametest1() throws SyntaxException, LexicalException {
		String[] input = {"sin","cos", "atan", "cart_x", "cart_y", "polar_a", "polar_r", "abs"}; //TODO add log here
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);  
			parser.functionName();  //Call expression directly.  
		}	
	} 
	//Testcase to invoke functionName() negatively
	@Test
	public void FunctionNametest2() throws SyntaxException, LexicalException {
		String[] input = {"x"}; 
		for(String x : input){
			show(x);
			Scanner scanner = new Scanner(x).scan(); 
			show(scanner); 
			SimpleParser parser = new SimpleParser(scanner);
			try{
			parser.functionName();  //Call expression directly.  
			} catch (Exception e){System.out.println(e.getMessage());}
		}	
	} 
	
	
	
	/**
	 * these test cases invoke SimpleParser.lhsSelector() 
	 */
	@Test
	public void lhsSelectorTest1() throws SyntaxException, LexicalException {
		String input = "[x,y]";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.lhsSelector();  //Call expression directly.  
	}
	@Test
	public void lhsSelectorTest2() throws SyntaxException, LexicalException {
		String input = "[r,A]";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.lhsSelector();  //Call expression directly.  
	}
	//this test case invokes lhsSelector() negatively
	@Test
	public void lhsSelectorTest3() throws SyntaxException, LexicalException {
		String input = "[rA]";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);
		try{
		parser.lhsSelector();  //Call expression directly.  
		} catch(Exception e) {System.out.println(e.getMessage());}
	}
	
	
	/**
	 * this positive test case invokes the method for SimpleParser.XySelector directly
	 */
	@Test
	public void PositiveXySelectorTest() throws SyntaxException, LexicalException {
		String input = "x,y";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.xySelector();  //Call expression directly.  
	}
	
	/**
	 * this negative test case invokes the method for SimpleParser.XySelector directly
	 */
	@Test
	public void NegativeXySelectorTest() throws SyntaxException, LexicalException {
		String input = "xy,";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		try{
		parser.xySelector();  //Call expression directly.  
		} catch (Exception e){System.out.println(e.getMessage());}
	}
	
	
	/**
	 * this positive test case invokes the method for SimpleParser.RaSelector directly
	 */
	@Test
	public void PositiveRaSelectorTest() throws SyntaxException, LexicalException {
		String input = "r,A";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.raSelector();  //Call expression directly.  
	}
	
	/**
	 * this negative test case invokes the method for SimpleParser.RaSelector directly
	 */
	@Test
	public void NegativeRaSelectorTest() throws SyntaxException, LexicalException {
		String input = ",rA";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		try{
		parser.raSelector();  //Call expression directly.  
		} catch (Exception e){System.out.println(e.getMessage());}
	}
	
	/**
	 * tests SimpleParser.Selector only for terminal symbols
	 */
	@Test
	public void selectorTest1() throws SyntaxException, LexicalException {
		String input = "xy";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		thrown.expect(SyntaxException.class);
		try{
		parser.selector();  //Call expression directly.  
		} catch (SyntaxException e){System.out.println(e.getMessage());throw e;}
	}
	@Test
	public void selectorTest2() throws SyntaxException, LexicalException {
		String s="2*2+5+x>2*2+5+x==d6a09aa6aa42d705e4f7b01eaa57a671f05da686ded99c77294d3135563820a>=2*2+5+x!=2*2+5+x<=2*2+5+x|2*2+5+x>2*2+5+x==2*2+5+x<2*2+5+x&2*2+5+x>=2*2+5+x!=2*2+5+x<=2*2+5+x";
		String[] input1 = {s,s+"?"+s+":"+s}; 
		String input=input1[0]+","+input1[1];
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		try{
			parser.selector();  //Call expression directly.  
			} catch (SyntaxException e){System.out.println(e.getMessage());throw e;}
		}
	
	
	}

