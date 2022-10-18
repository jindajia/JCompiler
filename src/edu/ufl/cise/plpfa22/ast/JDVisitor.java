package edu.ufl.cise.plpfa22.ast;



import edu.ufl.cise.plpfa22.PLPException;
import edu.ufl.cise.plpfa22.ScopeException;
import edu.ufl.cise.plpfa22.ast.SymbolTable.SymbolEntry;

public class JDVisitor implements ASTVisitor{
    SymbolTable symbolTable;
    public JDVisitor() {
        symbolTable = new SymbolTable();
    }
    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        symbolTable.entryScope();
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
        symbolTable.closeScope();
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
        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        if (!symbolTable.insertAttribute(String.valueOf(varDec.ident.getText()), varDec)) {
            throw new ScopeException("visitVarDec error: var declaration insert failed");
        }
        varDec.nest = symbolTable.getLevel();
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        statementCall.ident.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        statementInput.ident.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        statementOutput.expression.visit(this, arg);
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
        statementIf.statement.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        statementWhile.expression.visit(this, arg);
        statementWhile.statement.visit(this, arg);
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        Integer left = (Integer)expressionBinary.e0.visit(this, arg);
        Integer right = (Integer)expressionBinary.e1.visit(this, arg);
        Integer val;
        switch(expressionBinary.op.getKind()) {
            case DIV:
                val = left/right;
                return val;
            case EQ:
                break;
            case GE:
                break;
            case GT:
                break;
            case LE:
                break;
            case LT:
                break;
            case MINUS:
                val = left - right;
                return val;
            case MOD:
                val = left % right;
                return val;
            case NEQ:
                break;
            case PLUS:
                val = left + right;
                return val;
            case TIMES:
                val = left * right;
                return val;
            default:
                break;
            
        }
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        String name = String.valueOf(expressionIdent.firstToken.getText());
        SymbolEntry entry = symbolTable.lookupEntry(name);
        if (entry == null) {
            throw new ScopeException("visitExpressionIdent error: entry is null");
        }
        Object attr = entry.attribute;
        if (attr == null || !(attr instanceof Declaration)) {
            throw new ScopeException("visitExpressionIdent error: attribute is null or not a Declaration class");
        }
        expressionIdent.dec = (Declaration)attr;
        expressionIdent.nest = symbolTable.getLevel();
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        return (Integer)expressionNumLit.firstToken.getIntValue();
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        return (String)expressionStringLit.firstToken.getStringValue();
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        return (Boolean)expressionBooleanLit.firstToken.getBooleanValue();
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        if (!symbolTable.insertProcedure(String.valueOf(procDec.ident.getText()), procDec)) {
            throw new ScopeException("visitProcedure error: procedure declaration insert failed");
        }
        procDec.nest = 0;
        // procDec.block.visit(this, arg);
        return null;    
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        if (!symbolTable.insertAttribute(String.valueOf(constDec.ident.getText()), constDec)) {
            throw new ScopeException("visitConstDec error: const declaration insert failed");
        }
        constDec.nest = symbolTable.getLevel();
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        String name = String.valueOf(ident.firstToken.getText());
        SymbolEntry entry = symbolTable.lookupEntry(name);
        if (entry == null) {
            throw new ScopeException("visitIdent error: entry is null");
        }
        Object attr = entry.attribute;
        if (attr == null || !(attr instanceof Declaration)) {
            throw new ScopeException("visitExpressionIdent error: attribute is null or not a Declaration class");
        }        
        ident.dec = (Declaration)attr;;
        ident.nest = symbolTable.getLevel();
        return null;
    }
    
}
