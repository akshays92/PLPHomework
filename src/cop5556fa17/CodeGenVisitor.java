package cop5556fa17;

import java.awt.image.ImageFilter;
import java.util.ArrayList;
import java.util.jar.Attributes.Name;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// if GRADE, generates code to add string to log
		// CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		// and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		// generates code to add string to log
		// CodeGenUtils.genLog(GRADE, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		// handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		
		cw.visitField(ACC_STATIC, "x", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "y", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "X", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "Y", "I", null, 0).visitEnd();

		// Sets max stack size and number of local vars.
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily set the parameter in the ClassWriter constructor to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// declares the required variable in generated byte code and assigns the
		// available value or default value to it
		switch (declaration_Variable.getUtilType()) {
		case BOOLEAN:
			cw.visitField(ACC_STATIC, declaration_Variable.name, "Z", null,
					false);
			if (!(declaration_Variable.e == null)) {
				declaration_Variable.e.visit(this, arg);
				mv.visitFieldInsn(PUTSTATIC, className,
						declaration_Variable.name, "Z");
			}
			break;
		case INTEGER:
			cw.visitField(ACC_STATIC, declaration_Variable.name, "I", null, 0);
			if (!(declaration_Variable.e == null)) {
				declaration_Variable.e.visit(this, arg);
				mv.visitFieldInsn(PUTSTATIC, className,
						declaration_Variable.name, "I");
			}

			break;
		default:
			throw new UnsupportedOperationException();
		}
		return arg;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {

		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);

		Label l0 = new Label();
		Label l1 = new Label();

		switch (expression_Binary.op) {
		case OP_AND:
			mv.visitInsn(IAND);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_OR:
			mv.visitInsn(IOR);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_DIV:
			mv.visitInsn(IDIV);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_MINUS:
			mv.visitInsn(ISUB);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_MOD:
			mv.visitInsn(IREM);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_PLUS:
			mv.visitInsn(IADD);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_TIMES:
			mv.visitInsn(IMUL);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_EQ:
			mv.visitJumpInsn(IF_ICMPEQ, l0);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_NEQ:
			mv.visitJumpInsn(IF_ICMPNE, l0);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_GE:
			mv.visitJumpInsn(IF_ICMPGE, l0);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_GT:
			mv.visitJumpInsn(IF_ICMPGT, l0);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_LE:
			mv.visitJumpInsn(IF_ICMPLE, l0);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, l1);
			break;
		case OP_LT:
			mv.visitJumpInsn(IF_ICMPLT, l0);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, l1);
			break;
		}
		mv.visitLabel(l0);
		mv.visitLdcInsn(true);
		mv.visitLabel(l1);

		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getUtilType());
		return null;

	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// throw new UnsupportedOperationException();
		expression_Unary.e.visit(this, arg);
		switch (expression_Unary.op) {
		case OP_PLUS:
			// DO NOTHING just chill
			break;
		case OP_MINUS:
			mv.visitInsn(INEG);
			break;
		case OP_EXCL:
			switch (expression_Unary.e.getUtilType()) {
			case INTEGER:
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
				break;
			case BOOLEAN:
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IXOR);
				break;
			default:
			}
			break;
		default:

		}
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getUtilType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		if (index.e0 != null)
			index.e0.visit(this, arg);
		if (index.e1 != null)
			index.e1.visit(this, arg);
		if (!index.isCartesian()) {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"cart_y", RuntimeFunctions.cart_ySig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// throw new UnsupportedOperationException();
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name,
				ImageSupport.ImageDesc);
		if (expression_PixelSelector.index != null)
			expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel",
				ImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// throw new UnsupportedOperationException();
		expression_Conditional.condition.visit(this, arg);
		Label falseLabel = new Label();
		Label endLabel = new Label();
		mv.visitJumpInsn(IFEQ, falseLabel);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(falseLabel);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitLabel(endLabel);
		// CodeGenUtils.genLogTOS(GRADE, mv,
		// expression_Conditional.trueExpression.getUtilType());
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// throw new UnsupportedOperationException();
		cw.visitField(ACC_STATIC, declaration_Image.name,
				ImageSupport.ImageDesc, null, null);
		if (declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);
			if ((declaration_Image.xSize != null)
					&& (declaration_Image.ySize != null)) {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)" + ImageSupport.IntegerDesc, false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)" + ImageSupport.IntegerDesc, false);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,
						"readImage", ImageSupport.readImageSig, false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,
						"readImage", ImageSupport.readImageSig, false);
			}
		} else {
			if ((declaration_Image.xSize != null)
					&& (declaration_Image.ySize != null)) {
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,
						"makeImage", ImageSupport.makeImageSig, false);
			} else {
				mv.visitLdcInsn(256);
				mv.visitLdcInsn(256);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,
						"makeImage", ImageSupport.makeImageSig, false);
			}
		}
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name,
				ImageSupport.ImageDesc);
		return null;

	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		if (!(source_CommandLineParam.paramNum == null))
			source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return arg;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// throw new UnsupportedOperationException();
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name,
				ImageSupport.StringDesc);
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// throw new UnsupportedOperationException();
		cw.visitField(ACC_STATIC, declaration_SourceSink.name,
				ImageSupport.StringDesc, null, null).visitEnd();
		if (declaration_SourceSink.source != null)
			declaration_SourceSink.source.visit(this, arg);
		mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name,
				ImageSupport.StringDesc);
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// puts the value of the integer literal on top of the stack
		mv.visitLdcInsn(expression_IntLit.value);
		// CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {

		// throw new UnsupportedOperationException();
		if (expression_FunctionAppWithExprArg.arg != null)
			expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if (expression_FunctionAppWithExprArg.function == Kind.KW_abs)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs",
					RuntimeFunctions.absSig, false);
		return null;

	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		if (expression_FunctionAppWithIndexArg.arg.e0 != null)
			expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		if (expression_FunctionAppWithIndexArg.arg.e1 != null)
			expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		switch (expression_FunctionAppWithIndexArg.function) {
		case KW_cart_x:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"cart_x", RuntimeFunctions.cart_xSig, false);
			break;
		case KW_cart_y:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"cart_y", RuntimeFunctions.cart_ySig, false);
			break;
		case KW_polar_r:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"polar_r", RuntimeFunctions.polar_rSig, false);
			break;
		case KW_polar_a:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"polar_a", RuntimeFunctions.polar_aSig, false);
			break;
		default:
		}

		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		switch (expression_PredefinedName.kind) {
		case KW_x:
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			break;
		case KW_X:
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			break;
		case KW_y:
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			break;
		case KW_Y:
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			break;
		case KW_r:
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"polar_r", RuntimeFunctions.polar_rSig, false);
			break;
		case KW_R:
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"polar_r", RuntimeFunctions.polar_rSig, false);
			break;
		case KW_a:
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"polar_a", RuntimeFunctions.polar_aSig, false);
			break;
		case KW_A:
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,
					"polar_a", RuntimeFunctions.polar_aSig, false);
			break;
			
		case KW_DEF_X:
			mv.visitLdcInsn(256);
			break;
		case KW_DEF_Y:
			mv.visitLdcInsn(256);
			break;
		case KW_Z:
			mv.visitLdcInsn(0xFFFFFF);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	/**
	 * For Integers and booleans, the only "sink"is the screen, so generate code
	 * to print to console. For Images, load the Image onto the stack and visit
	 * the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		switch (statement_Out.getDec().getUtilType()) {
		case INTEGER:
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
					"(I)V", false);
			break;
		case BOOLEAN:
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print",
					"(Z)V", false);
			break;
		case IMAGE:
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name,
					ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, Type.IMAGE);
			statement_Out.sink.visit(this, arg);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return arg;

	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 * In HW5, you only need to handle INTEGER and BOOLEAN Use
	 * java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean to convert
	 * String to actual type.
	 * 
	 * 
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		/*
		 * Generate code to get value from the source and store it in variable
		 * name. For Assignment 5, the only source that needs to be handled is
		 * the command line.
		 * 
		 * Visit source to leave string representation of the value on top of
		 * stack Convert to a value of correct type: If name.type == INTEGER
		 * generate code to invoke Java.lang.Integer.parseInt. If BOOLEAN,
		 * invoke java/lang/Boolean.parseBoolean
		 */
		if (!(statement_In.source == null)) {
			statement_In.source.visit(this, arg);
		}
		switch (statement_In.getDec().getUtilType()) {
		case INTEGER:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt",
					"(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
			break;
		case BOOLEAN:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean",
					"parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
			break;
		case IMAGE:
			Declaration_Image declaration_Image = (Declaration_Image) statement_In
					.getDec();
			if ((declaration_Image.xSize != null)
					&& (declaration_Image.ySize != null)) {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)" + ImageSupport.IntegerDesc, false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)" + ImageSupport.IntegerDesc, false);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,
						"readImage", ImageSupport.readImageSig, false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,
						"readImage", ImageSupport.readImageSig, false);
			}
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name,
					ImageSupport.ImageDesc);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return arg;

	}

	// /**
	// * In HW5, only handle INTEGER and BOOLEAN types.
	// */
	// @Override
	// public Object visitStatement_Transform(Statement_Assign statement_Assign,
	// Object arg) throws Exception {
	// // (see comment)
	// throw new UnsupportedOperationException();
	// }

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// puts the literal on top of the stack into the static field mentioned
		// in the lhs
		switch (lhs.getUtilType()) {
		case INTEGER:
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
			break;
		case BOOLEAN:
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
			break;
		case IMAGE:
			mv.visitFieldInsn(GETSTATIC, className, lhs.name,
					ImageSupport.ImageDesc);
//			if (lhs.index != null)
//				lhs.index.visit(this, arg);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,
					"setPixel", ImageSupport.setPixelSig, false);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return arg;

	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame",
				ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// throw new UnsupportedOperationException();
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name,
				ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write",
				ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// Puts the value of the boolean literal on top of the stack
		mv.visitLdcInsn(expression_BooleanLit.value);
		// CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// puts the value of the identifier on top of the stack
		switch (expression_Ident.getUtilType()) {
		case INTEGER:
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
			break;
		case BOOLEAN:
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
			break;
		default:
			throw new UnsupportedOperationException();
		}
		// CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getUtilType());
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO
		// visit expression and then visit lhs
		if (statement_Assign.lhs.getUtilType() == Type.IMAGE) {
			Label xLabel = new Label();
			Label yLabel = new Label();
			Label end1 = new Label();
			Label end2 = new Label();
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
			mv.visitLdcInsn(0);
			mv.visitLdcInsn(0);
			mv.visitLabel(yLabel);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name,ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitJumpInsn(IF_ICMPGE, end2);
			mv.visitLdcInsn(0);
			mv.visitLdcInsn(0);
			mv.visitLabel(xLabel);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitJumpInsn(IF_ICMPGE, end1);
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitLdcInsn(1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, xLabel);
			mv.visitLabel(end1);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitLdcInsn(1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, yLabel);
			mv.visitLabel(end2);
		}

		else {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		return null;
	}

}
