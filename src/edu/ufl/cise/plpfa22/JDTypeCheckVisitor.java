package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
import edu.ufl.cise.plpfa22.ast.Expression;
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
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.ast.Types.Type;

public class JDTypeCheckVisitor implements ASTVisitor {

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        for (ConstDec constDec:block.constDecs) {
            constDec.visit(this, arg);
        }
        for (VarDec varDec:block.varDecs) {
            varDec.visit(this, arg);
        }
        for (ProcDec procDec:block.procedureDecs) {
            procDec.visit(this, arg);
        }
        for (ProcDec procDec:block.procedureDecs) {
            procDec.block.visit(this, arg);
        }
        block.statement.visit(this, arg);
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        program.block.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        statementAssign.ident.visit(this, arg);
        statementAssign.expression.visit(this, arg);
        Declaration dec = statementAssign.ident.getDec();
        if (!(dec instanceof VarDec)) {
            throw new TypeCheckException("visitStatementAssign error: only vairble type values can be assigned", dec.getSourceLocation());
        } else if (statementAssign.expression.getType()==null) {
            throw new TypeCheckException("visitStatementAssign error: right expression type can not be infered", dec.getSourceLocation());
        } else if (dec.getType()!=null && dec.getType()!=statementAssign.expression.getType()) {
            throw new TypeCheckException("visitStatementAssign error: left value type should be same with right", dec.getSourceLocation());
        } else if (dec.getType()==null){
            dec.setType(statementAssign.expression.getType());
        }
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        statementCall.ident.visit(this, arg);
        Declaration dec = statementCall.ident.getDec();
        if (!(dec instanceof ProcDec)) {
            throw new TypeCheckException("visitStatementCall error: statemenetcall type must be procedure", dec.getSourceLocation());
        }
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        statementInput.ident.visit(this, arg);
        Declaration dec = statementInput.ident.getDec();
        if (dec.getType()==null){
            throw new TypeCheckException("visitStatementInput error: statementInput declaration type is null", statementInput.getSourceLocation());
        } else if (dec.getType()!=Type.BOOLEAN && dec.getType()!=Type.NUMBER && dec.getType()!=Type.STRING) {
            throw new TypeCheckException("visitStatementInput error: statementInput type must be string, number or boolean", dec.getSourceLocation());
        }
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        statementOutput.expression.visit(this, arg);
        Expression expr = statementOutput.expression;
        if (expr.getType()==null) {
            throw new TypeCheckException("visitStatementOutput error: statementOutput expression type is null", statementOutput.getSourceLocation());
        } else if (expr.getType()!=Type.BOOLEAN && expr.getType()!=Type.NUMBER && expr.getType()!=Type.STRING) {
            throw new TypeCheckException("visitStatementOutput error: statementOutput type must be string, number or boolean", expr.getSourceLocation());
        }
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        for(Statement statement:statementBlock.statements) {
            statement.visit(this, arg);
        }        
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        statementIf.expression.visit(this, arg);
        if (statementIf.expression.getType()==null) {
            throw new TypeCheckException("visitStatementIf error: statementIf expression type is null", statementIf.expression.getSourceLocation());
        } else if (statementIf.expression.getType()!=Type.BOOLEAN) {
            throw new TypeCheckException("visitStatementIf error: statementIf expression type must be boolean", statementIf.expression.getSourceLocation());
        }
        statementIf.statement.visit(this, arg);    
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        statementWhile.expression.visit(this, arg);
        if (statementWhile.expression.getType()==null) {
            throw new TypeCheckException("visitStatementWhile error: statementWhile expression type is null", statementWhile.expression.getSourceLocation());
        } else if (statementWhile.expression.getType()!=Type.BOOLEAN) {
            throw new TypeCheckException("visitStatementWhile error: statementWhile expression type must be boolean", statementWhile.expression.getSourceLocation());
        }
        statementWhile.statement.visit(this, arg);
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        expressionBinary.e0.visit(this, arg);
        expressionBinary.e1.visit(this, arg);
        IToken op = expressionBinary.op;
        Expression e0 = expressionBinary.e0;
        Expression e1 = expressionBinary.e1;
        if (op.getKind()==Kind.PLUS) {
            if (e0.getType()==null && e1.getType()==null) {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary two expressions type are null", expressionBinary.getSourceLocation());
            } else if (e0.getType()==null && (e1.getType()==Type.NUMBER || e1.getType()==Type.STRING || e1.getType()==Type.BOOLEAN)) {
                e0.setType(e1.getType());
            } else if (e1.getType()==null && (e0.getType()==Type.NUMBER || e0.getType()==Type.STRING || e0.getType()==Type.BOOLEAN)) {
                e1.setType(e0.getType());
            } else if (e1.getType()==e0.getType() && (e0.getType()==Type.NUMBER || e0.getType()==Type.STRING || e0.getType()==Type.BOOLEAN)) {
                //do nothing
            } else {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary expression type not correct", expressionBinary.getSourceLocation());
            }
            expressionBinary.setType(e0.getType());
        } else if (op.getKind()==Kind.MINUS || op.getKind()==Kind.DIV || op.getKind()==Kind.MOD) {
            if (e0.getType()==null && e1.getType()==null) {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary two expressions type are null", expressionBinary.getSourceLocation());
            } else if (e0.getType()==null && e1.getType()==Type.NUMBER) {
                e0.setType(e1.getType());
            } else if (e1.getType()==null && e0.getType()==Type.NUMBER) {
                e1.setType(e0.getType());
            } else if (e0.getType()==e1.getType() && e0.getType()==Type.NUMBER) {
                //do nothing
            } else {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary expression type not correct", expressionBinary.getSourceLocation());
            }
            expressionBinary.setType(Type.NUMBER);
        } else if (op.getKind()==Kind.TIMES) {
            if (e0.getType()==null && e1.getType()==null) {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary two expressions type are null", expressionBinary.getSourceLocation());
            } else if (e0.getType()==null && (e1.getType()==Type.NUMBER || e1.getType()==Type.BOOLEAN)) {
                e0.setType(e1.getType());
            } else if (e1.getType()==null && (e0.getType()==Type.NUMBER) || e0.getType()==Type.BOOLEAN) {
                e1.setType(e0.getType());
            } else if (e0.getType()==e1.getType() && (e0.getType()==Type.NUMBER || e0.getType()==Type.BOOLEAN)) {
                //do nothing
            } else {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary expression type not correct", expressionBinary.getSourceLocation());
            }
            expressionBinary.setType(e0.getType());
        } else if (op.getKind()==Kind.EQ || op.getKind()==Kind.NEQ || op.getKind()==Kind.LT || op.getKind()==Kind.LE || op.getKind()==Kind.GT || op.getKind()==Kind.GE) {
            if (e0.getType()==null && e1.getType()==null) {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary two expressions type are null", expressionBinary.getSourceLocation());
            } else if (e0.getType()==null && (e1.getType()==Type.NUMBER || e1.getType()==Type.STRING || e1.getType()==Type.BOOLEAN)) {
                e0.setType(e1.getType());
            } else if (e1.getType()==null && (e0.getType()==Type.NUMBER || e0.getType()==Type.STRING || e0.getType()==Type.BOOLEAN)) {
                e1.setType(e0.getType());
            } else if (e1.getType()==e0.getType() && (e0.getType()==Type.NUMBER || e0.getType()==Type.STRING || e0.getType()==Type.BOOLEAN)) {
                //do nothing
            } else {
                throw new TypeCheckException("visitExpressionBinary error: expressionBinary expression type not correct", expressionBinary.getSourceLocation());
            }
            expressionBinary.setType(Type.BOOLEAN);
        } else {
            throw new TypeCheckException("visitExpressionBinary error: expressionBinary expression type not correct", expressionBinary.getSourceLocation());
        }
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        Declaration dec = expressionIdent.getDec();
        if (dec.getType()!=null) {
            expressionIdent.setType(dec.getType());
        }
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        expressionNumLit.setType(Type.NUMBER);
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        expressionStringLit.setType(Type.STRING);
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        expressionBooleanLit.setType(Type.BOOLEAN);
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        procDec.setType(Type.PROCEDURE);
        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        if (constDec.getType()==null) {
            if (constDec.val instanceof Integer) {
                constDec.setType(Type.NUMBER);
            } else if (constDec.val instanceof String) {
                constDec.setType(Type.STRING);
            } else if (constDec.val instanceof Boolean) {
                constDec.setType(Type.BOOLEAN);
            } else {
                throw new TypeCheckException("visitConstDec error: const value type can not be recognized", constDec.getSourceLocation());
            }
        }
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }
    
}
