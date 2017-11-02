package cop5556fa17;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.TypeCheckVisitor.SemanticException;


public class TypeCheckTest3 {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	
	/**
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallest() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIndex() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}
	 
	 @Test
	 public void testDec1() throws Exception {
	 String input = "prog file abc=def;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testDec2() throws Exception {
	 String input = "prog int k	= 2;";
	 typeCheck(input);
	 }
	 
	 /**
	  * This program does not declare k. The TypeCheckVisitor should
	  * throw a SemanticException in a fully implemented assignment.
	  * @throws Exception
	  */
	 @Test
	 public void testUndec() throws Exception {
	 String input = "prog k = 42;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testUndec1() throws Exception {
	 String input = "Prog url ident = \"asdfadf\";";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testUndec2() throws Exception {
	 String input = "prog boolean k=true; int k = 5;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testUndec3() throws Exception {
	 String input = "prog int k=1; int b=2; int c = k+b;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testUndec4() throws Exception {
	 String input = "prog int k=1; int b=2; boolean c = k+b;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec5() throws Exception {
	 String input = "prog image image1; image image2; image1 <- image2;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec6() throws Exception {
	 String input = "prog  image image1; image image2; image1 <- @1;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec7() throws Exception {
	 String input = "prog file imagefile  = \"imageFile2017.\"; image image1;  image1 <- imagefile;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec8() throws Exception {
	 String input = "prog file aa=\"http://example.com/\";";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec9() throws Exception {
	 String input = "raktima url ab=\"world.docx\";";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec10() throws Exception {
	 String input = "abc int def;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec11() throws Exception {
	 String input = "abc";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec12() throws Exception {
	 String input = "prog int k=2; k=++++k;";
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec13() throws Exception {
	 String input = "raktima int k=+r;";
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec14() throws Exception {
	 String input = "raktima int k=+r;";
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec15() throws Exception {
	 String input = "raktima int k=sin(2);";
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec16() throws Exception {
	 String input = "raktima int k=2;k[[x,y]]=2;";
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec17() throws Exception {
	 String input = "raktima int k=2;k[[r,a]]=2;";
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec18() throws Exception {
	 String input = "raktima b[[x,y]]=x;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec19() throws Exception {
	 String input = "raktima int b=2;b[[x,y]]=x;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec20() throws Exception {
	 String input = "raktima url b=\"http://www.google.com\";";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec21() throws Exception {
	 String input = "raktima url b=\"http://www.google.com\";";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec22() throws Exception {
	 String input = "raktima int b=2; int c=4; c=b==c;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec23() throws Exception {
	 String input = "prog int k=k+1;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec24() throws Exception {
	 String input = "prog url b=c;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec25() throws Exception {
	 String input = "prog url b=\"https://www.google.com\"; "
	 		         + "url c=b;";
	 //thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 @Test
	 public void testUndec26() throws Exception {
	 String input = "prog int filepng=2;int png=3; image[filepng,png] imageName <- imagepng;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 


}