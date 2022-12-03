package edu.ufl.cise.plpfa22;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.CodeGenUtils.GenClass;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.VarDec;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	final String packageName;
	final String className;
	final String sourceFileName;
	final String fullyQualifiedClassName; 
	final String classDesc;
	
	ClassWriter classWriter;

	String currentClass;

	public CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		this.classDesc="L"+this.fullyQualifiedClassName+';';
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		ClassWriter classWriter = (ClassWriter)arg;
		List<GenClass> list = new LinkedList<>();
		for (ConstDec constDec : block.constDecs) {
			constDec.visit(this, classWriter);
		}
		for (VarDec varDec : block.varDecs) {
			varDec.visit(this, classWriter);
		}
		for (ProcDec procDec: block.procedureDecs) {
			list.add((GenClass)procDec.visit(this, classWriter));
		}
		//Creates a MethodVisitor for run method
		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		methodVisitor.visitCode();
		//add instructions from statement to method
		block.statement.visit(this, methodVisitor);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0,0);
		methodVisitor.visitEnd();
		return list;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		this.currentClass = className;
		ASTVisitor scopes = CompilerComponentFactory.getScopeVisitor();
		program.visit(scopes, null);
		MethodVisitor methodVisitor;
		//create a classWriter and visit it
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//Hint:  if you get failures in the visitMaxs, try creating a ClassWriter with 0
		// instead of ClassWriter.COMPUTE_FRAMES.  The result will not be a valid classfile,
		// but you will be able to print it so you can see the instructions.  After fixing,
		// restore ClassWriter.COMPUTE_FRAMES
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object", new String[] { "java/lang/Runnable" });

		// for (ProcDec proc:program.block.procedureDecs) {
		// 	String procName = String.valueOf(proc.ident.getText());
		// 	classWriter.visitSource(className+".java", null); 
		// 	classWriter.visitNestMember(fullyQualifiedClassName+"$"+procName);
		// 	classWriter.visitInnerClass(fullyQualifiedClassName+"$"+procName, fullyQualifiedClassName, procName, 0);
		// }

		//create init method code
		methodVisitor = classWriter.visitMethod(0, "<init>", "()V", null, null);
		methodVisitor.visitCode();
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0, 0);
		methodVisitor.visitEnd();

		//create main method code
		methodVisitor = classWriter.visitMethod(ACC_STATIC | ACC_PUBLIC, "main", "([Ljava/lang/String;)V", null, null);
		methodVisitor.visitCode();
		methodVisitor.visitTypeInsn(NEW, fullyQualifiedClassName);
		methodVisitor.visitInsn(DUP);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, fullyQualifiedClassName, "<init>", "()V", false); 
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, fullyQualifiedClassName, "run", "()V", false);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0,0);
		methodVisitor.visitEnd();

		List<GenClass> l = new LinkedList<GenClass>();
		//visit the block, passing it the methodVisitor
		List<GenClass> list = (List<GenClass>)program.block.visit(this, classWriter);
		//finish up the class
        classWriter.visitEnd();
        //return the bytes making up the classfile
		l.add(new GenClass(fullyQualifiedClassName, classWriter.toByteArray()));
		l.addAll(list);
		return l;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		methodVisitor.visitVarInsn(ALOAD, 0);
		Declaration dec = statementAssign.ident.getDec();
		if (dec instanceof VarDec){
			VarDec varDec = (VarDec)dec;
			int nest = varDec.getNest();
			if (!currentClass.equals(className)){
				methodVisitor.visitFieldInsn(GETFIELD, fullyQualifiedClassName+"$"+currentClass, "this&"+nest, "L"+fullyQualifiedClassName+";");
			}
		}

		statementAssign.expression.visit(this, arg);
        statementAssign.ident.visit(this, arg);
		return null;
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		ClassWriter classWriter = (ClassWriter)arg;
		FieldVisitor fieldVisitor; 
		String name = String.valueOf(varDec.ident.getText());
		switch(varDec.getType()) {
			case BOOLEAN:
				fieldVisitor = classWriter.visitField(0, name, "Z", null, null);
				fieldVisitor.visitEnd(); 
				break;
			case NUMBER:
				fieldVisitor = classWriter.visitField(0, name, "I", null, null);
				fieldVisitor.visitEnd(); 
				break;
			case PROCEDURE:
				break;
			case STRING:
				fieldVisitor = classWriter.visitField(0, name, "Ljava/lang/String;", null, null);
				fieldVisitor.visitEnd(); 
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
	/* 	visitStatementCall
		• Create instance of class corresponding to procedure
		• The <init> method takes instance of lexically enclosing class as parameter. If the procedure is
		enclosed in this one, ALOAD_0 works. (Recall that we are in a virtual method, run, so the JVM will have automatically loaded “this” into local variable slot 0.) Otherwise follow the chain of this$n references to find an instance of the enclosing class of the procedure. (Use nesting levels)
		• Invoke run method.*/	
		String procName = String.valueOf(statementCall.ident.getText());
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		methodVisitor.visitTypeInsn(NEW, fullyQualifiedClassName+"$"+procName); 
		methodVisitor.visitInsn(DUP);
		methodVisitor.visitVarInsn(ALOAD,0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, fullyQualifiedClassName+"$"+procName, "<init>","(L"+fullyQualifiedClassName+";)V",false);
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, fullyQualifiedClassName+"$"+procName, "run", "()V", false);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		statementOutput.expression.visit(this, arg);
		Type etype = statementOutput.expression.getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		String printlnSig = "(" + JVMType +")V";
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSig, false);
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        for(Statement statement:statementBlock.statements) {
            statement.visit(this, arg);
        }
        return null;	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
        statementIf.expression.visit(this, arg);
		mv.visitInsn(ICONST_1);
		Label label0 = new Label();
		mv.visitJumpInsn(IF_ICMPNE, label0);
        statementIf.statement.visit(this, arg);
		mv.visitLabel(label0);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		Type argType = expressionBinary.e0.getType();
		Kind op = expressionBinary.op.getKind();
		switch (argType) {
		case NUMBER -> {
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
			case PLUS -> mv.visitInsn(IADD);
			case MINUS -> mv.visitInsn(ISUB);
			case TIMES -> mv.visitInsn(IMUL);
			case DIV -> mv.visitInsn(IDIV);
			case MOD -> mv.visitInsn(IREM);
			case EQ -> {
				Label labelNumEqFalseBr = new Label();
				mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
				mv.visitInsn(ICONST_1);
				Label labelPostNumEq = new Label();
				mv.visitJumpInsn(GOTO, labelPostNumEq);
				mv.visitLabel(labelNumEqFalseBr);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(labelPostNumEq);
			}
			case NEQ -> {
				Label labelNumEqFalseBr = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, labelNumEqFalseBr);
				mv.visitInsn(ICONST_1);
				Label labelPostNumEq = new Label();
				mv.visitJumpInsn(GOTO, labelPostNumEq);
				mv.visitLabel(labelNumEqFalseBr);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(labelPostNumEq);			
			}
			case LT -> {
				Label label0 = new Label();
				mv.visitJumpInsn(IF_ICMPLT, label0);
				mv.visitInsn(ICONST_0);
				Label label1 = new Label();
				mv.visitJumpInsn(GOTO, label1);
				mv.visitLabel(label0);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(label1);				
			}
			case LE -> {
				Label label0 = new Label();
				mv.visitJumpInsn(IF_ICMPLE, label0);
				mv.visitInsn(ICONST_0);
				Label label1 = new Label();
				mv.visitJumpInsn(GOTO, label1);
				mv.visitLabel(label0);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(label1);
			}
			case GT -> {
				Label label0 = new Label();
				mv.visitJumpInsn(IF_ICMPGT, label0);
				mv.visitInsn(ICONST_0);
				Label label1 = new Label();
				mv.visitJumpInsn(GOTO, label1);
				mv.visitLabel(label0);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(label1);				
			}
			case GE -> {
				Label label0 = new Label();
				mv.visitJumpInsn(IF_ICMPGE, label0);
				mv.visitInsn(ICONST_0);
				Label label1 = new Label();
				mv.visitJumpInsn(GOTO, label1);
				mv.visitLabel(label0);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(label1);			
			}
			default -> {
				throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
			}
			}
			;
		}
		case BOOLEAN -> {
			expressionBinary.e0.visit(this, arg);
			expressionBinary.e1.visit(this, arg);
			switch (op) {
				case EQ -> {
					Label labelNumEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
					mv.visitInsn(ICONST_1);
					Label labelPostNumEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumEq);
					mv.visitLabel(labelNumEqFalseBr);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumEq);
				}
				case GE -> {
					Label label0 = new Label();
					mv.visitJumpInsn(IF_ICMPGE, label0);
					mv.visitInsn(ICONST_0);
					Label label1 = new Label();
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(label1);
				}
				case GT -> {
					Label label0 = new Label();
					mv.visitJumpInsn(IF_ICMPGT, label0);
					mv.visitInsn(ICONST_0);
					Label label1 = new Label();
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(label1);
				}
				case LE -> {
					Label label0 = new Label();
					mv.visitJumpInsn(IF_ICMPLE, label0);
					mv.visitInsn(ICONST_0);
					Label label1 = new Label();
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(label1);
				}
				case LT -> {
					Label label0 = new Label();
					mv.visitJumpInsn(IF_ICMPLT, label0);
					mv.visitInsn(ICONST_0);
					Label label1 = new Label();
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(label1);
				}
				case NEQ -> {
					Label labelNumEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, labelNumEqFalseBr);
					mv.visitInsn(ICONST_1);
					Label labelPostNumEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumEq);
					mv.visitLabel(labelNumEqFalseBr);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumEq);		
				}
				case PLUS -> {
					mv.visitInsn(IADD);
					mv.visitInsn(ICONST_1);
					Label label0 = new Label();
					Label label1 = new Label();
					mv.visitJumpInsn(IF_ICMPGE, label0);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(label1);
				}
				case TIMES -> {
					mv.visitInsn(IADD);
					mv.visitInsn(ICONST_2);
					Label label0 = new Label();
					Label label1 = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, label0);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(label1);
				}
				default -> throw new UnsupportedOperationException();
				
			}
		}
		case STRING -> {
			switch (op) {
				case EQ -> {
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
				}
				case NEQ -> {
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
					mv.visitInsn(ICONST_0);
					Label labelNumEqFalseBr = new Label();
					mv.visitJumpInsn(IF_ICMPNE, labelNumEqFalseBr);
					mv.visitInsn(ICONST_1);
					Label labelPostNumEq = new Label();
					mv.visitJumpInsn(GOTO, labelPostNumEq);
					mv.visitLabel(labelNumEqFalseBr);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(labelPostNumEq);
				}
				case LT -> {
					expressionBinary.e1.visit(this, arg);
					expressionBinary.e0.visit(this, arg);
					Label label0 = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(IF_ICMPEQ, label0);
					Label label1 = new Label();
					expressionBinary.e1.visit(this, arg);
					expressionBinary.e0.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(label1);
				}
				case LE -> {
					expressionBinary.e1.visit(this, arg);
					expressionBinary.e0.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
				}
				case GT -> {
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					Label label0 = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(IF_ICMPEQ, label0);
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					Label label1 = new Label();
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z", false);
					mv.visitJumpInsn(GOTO, label1);
					mv.visitLabel(label0);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(label1);
				}
				case GE -> {
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z", false);
				}
				case PLUS -> {
					expressionBinary.e0.visit(this, arg);
					expressionBinary.e1.visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
				}
				default ->	throw new UnsupportedOperationException();
			}
		}
		default -> {
			throw new IllegalStateException("code gen bug in visitExpressionBinary");
		}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		Declaration dec = expressionIdent.getDec();
		if (dec instanceof ConstDec) {
			ConstDec constDec = (ConstDec)dec;
			methodVisitor.visitLdcInsn(constDec.val);
			methodVisitor.visitEnd();
		} else if (dec instanceof VarDec) {
			VarDec varDec = (VarDec)dec;
			String name = String.valueOf(varDec.ident.getText());
			String despt = "";
			switch (varDec.getType()) {
				case BOOLEAN:
					despt = "Z";
					break;
				case NUMBER:
					despt = "I";
					break;
				case PROCEDURE:
					break;
				case STRING:
					despt = "Ljava/lang/String;";
					break;
				default:
					break;
				
			}
			methodVisitor.visitVarInsn(ALOAD, 0);
			int nest = varDec.getNest();
			if (!currentClass.equals(className)){
				methodVisitor.visitFieldInsn(GETFIELD, fullyQualifiedClassName+"$"+currentClass, "this&"+nest, "L"+fullyQualifiedClassName+";");
			}
			methodVisitor.visitFieldInsn(GETFIELD, fullyQualifiedClassName, name, despt);
			methodVisitor.visitEnd();
		} else if (dec instanceof ProcDec) {
			ProcDec procDec = (ProcDec)dec;
			String type = fullyQualifiedClassName+"$"+String.valueOf(procDec.ident.getText());
			methodVisitor.visitTypeInsn(NEW, type); 
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitVarInsn(ALOAD,0);
			methodVisitor.visitMethodInsn(INVOKESPECIAL,type, "<init>", "(L"+fullyQualifiedClassName+";)V",false);
			methodVisitor.visitMethodInsn(INVOKEVIRTUAL,type, "run", "()V", false);
			methodVisitor.visitEnd();
		}
		return null;
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
		return null;	
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getBooleanValue());
		return null;	
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
	/* 	• create a classWriter object for new class
		• add field for reference to enclosing class (this$n where n is nesting level)
		• create init method that takes an instance of enclosing class as parameter and initializes this$n, then invokes superclass constructor (java/lang/Object).
		• Visit block to create run method 
	*/
		String tempName = currentClass;
		ClassWriter tempClassWriter = classWriter;
		currentClass = String.valueOf(procDec.ident.getText());
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		String procedureName = String.valueOf(procDec.ident.getText());
		classWriter.visit(V18, ACC_SUPER, fullyQualifiedClassName+"$"+procedureName, null, "java/lang/Object", new String[] { "java/lang/Runnable" });
		
		FieldVisitor fieldVisitor = classWriter.visitField(ACC_FINAL | ACC_SYNTHETIC, "this$"+procDec.getNest(), "L"+fullyQualifiedClassName+";", null, null); 
		fieldVisitor.visitEnd();
		// classWriter.visitSource("Var2.java", null); classWriter.visitNestHost("edu/ufl/cise/plpfa22/codeGenSamples/Var2");
		// classWriter.visitInnerClass("edu/ufl/cise/plpfa22/codeGenSamples/Var2$p", "edu/ufl/cise/plpfa22/codeGenSamples/Var2", "p", 0);
		MethodVisitor methodVisitor = classWriter.visitMethod(0, "<init>","(L"+fullyQualifiedClassName+";)V", null, null);
		methodVisitor.visitCode();
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitVarInsn(ALOAD, 1);
		methodVisitor.visitFieldInsn(PUTFIELD, fullyQualifiedClassName+"$"+procedureName, "this$"+procDec.getNest(), "L"+fullyQualifiedClassName+";");
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0, 0);
		methodVisitor.visitEnd();

		//visit the block, passing it the methodVisitor
		procDec.block.visit(this, classWriter);
		classWriter.visitEnd();
		GenClass genClass = new GenClass(fullyQualifiedClassName+"$"+procedureName, classWriter.toByteArray());
		classWriter = tempClassWriter;
		currentClass = tempName;
		return genClass;
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		ClassWriter classWriter = (ClassWriter)arg;
		FieldVisitor fieldVisitor;
		String name = String.valueOf(constDec.ident.getText());
		switch(constDec.getType()) {
			case BOOLEAN:
				fieldVisitor = classWriter.visitField(0, name, "Z", null, null);
				fieldVisitor.visitEnd(); 
				break;
			case NUMBER:
				fieldVisitor = classWriter.visitField(0, name, "I", null, null);
				fieldVisitor.visitEnd(); 
				break;
			case PROCEDURE:
				break;
			case STRING:
				fieldVisitor = classWriter.visitField(0, name, "Ljava/lang/String;", null, null);
				fieldVisitor.visitEnd(); 
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor)arg;
		Declaration dec = ident.getDec();
		if (dec instanceof VarDec) {
			VarDec varDec = (VarDec)dec;
			int nest = varDec.getNest();
			String name = String.valueOf(varDec.ident.getText());
			String type = "";
			switch (varDec.getType()) {
				case BOOLEAN:
					type = "Z";
					break;
				case NUMBER:
					type = "I";
					break;
				case PROCEDURE:
					break;
				case STRING:
					type = "Ljava/lang/String;";
					break;
				default:
					break;
				
			}
			if (nest>0) {
				methodVisitor.visitFieldInsn(PUTFIELD, fullyQualifiedClassName+"$"+name,"this&"+nest, "L"+fullyQualifiedClassName+";");
			} else {

				methodVisitor.visitFieldInsn(PUTFIELD, fullyQualifiedClassName, name, type);
			}
		}
		return null;
	}

}
