package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.Arrays;

import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
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

public class Parser implements IParser{
    final Lexer lexer;
    IToken token = null;

    public Parser(ILexer lexer) {
        this.lexer = (Lexer) lexer;
    }
    @Override
    public ASTNode parse() throws PLPException {
        consume();
        return program();
        // return null;
    }
    
    private IToken consume() throws LexicalException{
        token = lexer.next();
        return token;
    }

    private Expression const_val() throws PLPException{
        Expression node = null;
        switch(token.getKind()){
            case BOOLEAN_LIT:
                node = new ExpressionBooleanLit(token);
                consume();
                break;
            case NUM_LIT:
                node = new ExpressionNumLit(token);
                consume();
                break;
            case STRING_LIT:
                node = new ExpressionStringLit(token);
                consume();
                break;
            default:
                throw new SyntaxException("Couldn't identified as const_val", token.getSourceLocation());
        }
        return node;
    }

    private Expression expression() throws PLPException {
        IToken firsToken = token;
        Expression left = null;
        Expression right = null;
        left = additive_expression();
        while(Arrays.asList(Kind.LT, Kind.GT, Kind.EQ, Kind.NEQ, Kind.LE, Kind.GE).contains(token.getKind())) {
            IToken op = token;
            consume();
            right = additive_expression();
            left = new ExpressionBinary(firsToken, left, op, right);
        }
        return left;
    }

    private Expression additive_expression() throws PLPException {
        IToken firsToken = token;
        Expression left = null;
        Expression right = null;
        left = multiplicative_expression();
        while(Arrays.asList(Kind.PLUS, Kind.MINUS).contains(token.getKind())) {
            IToken op = token;
            consume();
            right = multiplicative_expression();
            left = new ExpressionBinary(firsToken, left, op, right);
        }
        return left;
    }

    private Expression multiplicative_expression() throws PLPException {
        IToken firsToken = token;
        Expression left = null;
        Expression right = null;
        left = primary_expression();
        while(Arrays.asList(Kind.TIMES, Kind.DIV, Kind.MOD).contains(token.getKind())) {
            IToken op = token;
            consume();
            right = primary_expression();
            left = new ExpressionBinary(firsToken, left, op, right);
        }
        return left;
    }

    private Expression primary_expression() throws PLPException {
        Expression left = null;
        if (token.getKind() == Kind.IDENT) {
            left = new ExpressionIdent(token);
            consume();
        } else if (token.getKind() == Kind.LPAREN) {
            consume();
            left = expression();
            if (token.getKind() != Kind.RPAREN) {
                throw new SyntaxException("Couldn't find right Parenthesis", token.getSourceLocation());
            } 
            consume();
        } else {
            left = const_val();
        }
        return left;
    }


    private Statement statement() throws PLPException {
        Statement left = null;
        IToken firsToken = token;
        switch(token.getKind()) {
            case IDENT: {
                IToken id = token;
                Expression expr;
                consume();
                if (token.getKind()!=Kind.ASSIGN) {
                    throw new SyntaxException("Couldn't find := Symbol", token.getSourceLocation());
                }
                consume();
                expr = expression();
                left = new StatementAssign(firsToken, new Ident(id), expr);
            }
                break;
            case QUESTION:
                consume();
                if (token.getKind() == Kind.IDENT) {
                    left = new StatementInput(firsToken, new Ident(token));
                    consume();
                } else {
                    throw new SyntaxException("StatementQuestion couldn't find Identifier", token.getSourceLocation());
                }
                break;
            case BANG:{
                consume();
                Expression expr = expression();
                left = new StatementOutput(firsToken, expr);
            }
                break;
            case EOF:
                left = new StatementEmpty(firsToken);
                break;
            case KW_BEGIN:{
                consume();
                Statement stmt;
                ArrayList<Statement> statements = new ArrayList<>();
                stmt = statement();
                statements.add(stmt);
                while(token.getKind()==Kind.SEMI) {
                    consume();
                    statements.add(statement());
                }
                if (token.getKind()!=Kind.KW_END) {
                    throw new SyntaxException("StatementBlock couldn't find \'End\'", token.getSourceLocation()); 
                }
                consume();
                left = new StatementBlock(firsToken, statements);
            }
                break;
            case KW_CALL:
                consume();
                if (token.getKind() != Kind.IDENT) {
                    throw new SyntaxException("StatementCall couldn't find Identifier", token.getSourceLocation());
                }
                left = new StatementCall(firsToken, new Ident(token));
                consume();
                break;
            case KW_IF:{
                consume();
                Expression expr;
                Statement stmt;
                expr = expression();
                if (token.getKind()!=Kind.KW_THEN) {
                    throw new SyntaxException("StatementIf couldn't find kew word \"THEN\"", token.getSourceLocation());
                }
                consume();
                stmt = statement();
                left = new StatementIf(firsToken, expr, stmt);
            }    
                break;
            case KW_WHILE:{
                consume();
                Expression expr;
                Statement stmt;
                expr = expression();
                if (token.getKind()!=Kind.KW_DO) {
                    throw new SyntaxException("StatementWhile couldn't find \"DO\"", token.getSourceLocation());
                }
                consume();
                stmt = statement();
                left = new StatementWhile(firsToken, expr, stmt);
            }
                break;
            default:
                left = new StatementEmpty(firsToken);
                break;
        
        }
        return left;
    }

    private Block block() throws PLPException {
        Block left;
        IToken tempToken;
        IToken firstToken = token;
        ArrayList<ConstDec> cstDecList = new ArrayList<>();
        ArrayList<VarDec> varDecList = new ArrayList<>();
        ArrayList<ProcDec> proDecList = new ArrayList<>();
        while(token.getKind()==Kind.KW_CONST) {
            tempToken = token;
            consume();
            if (token.getKind()!=Kind.IDENT) {
                throw new SyntaxException("ConstDec Conldn't find Identifier", token.getSourceLocation());
            }
            IToken id = token;
            consume();
            if (token.getKind()!=Kind.EQ) {
                throw new SyntaxException("ConstDec Conldn't find EQ", token.getSourceLocation());
            }
            consume();
            Expression expr = const_val();
            Object val;
            if (expr instanceof ExpressionNumLit) {
                val = expr.firstToken.getIntValue();
            } else if (expr instanceof ExpressionStringLit) {
                val = expr.firstToken.getStringValue();
            } else if (expr instanceof ExpressionBooleanLit) {
                val = expr.firstToken.getBooleanValue();
            } else {
                throw new SyntaxException("ConstDec ConstVal format wrong", token.getSourceLocation());
            }
            ConstDec constDec = new ConstDec(tempToken, id, val);
            cstDecList.add(constDec);
            while(token.getKind() == Kind.COMMA) {
                tempToken = token;
                consume();
                if (token.getKind()!=Kind.IDENT) {
                    throw new SyntaxException("ConstDec couldn't find Identifier", token.getSourceLocation());
                }
                id = token;
                consume();
                if (token.getKind()!=Kind.EQ) {
                    throw new SyntaxException("ConstDec couldn't find EQ", token.getSourceLocation());
                }
                consume();
                expr = const_val();
                if (expr instanceof ExpressionNumLit) {
                    val = expr.firstToken.getIntValue();
                } else if (expr instanceof ExpressionStringLit) {
                    val = expr.firstToken.getStringValue();
                } else if (expr instanceof ExpressionBooleanLit) {
                    val = expr.firstToken.getBooleanValue();
                } else {
                    throw new SyntaxException("ConstDec ConstVal format wrong", token.getSourceLocation());
                }
                constDec = new ConstDec(tempToken, id, val);
                cstDecList.add(constDec);
            }
            if (token.getKind()!=Kind.SEMI) {
                throw new SyntaxException("ConsDec conldn't find \';\'", token.getSourceLocation());
            }
            consume();
        }

        while (token.getKind()==Kind.KW_VAR) {
            tempToken = token;
            consume();
            if (token.getKind()!=Kind.IDENT) {
                throw new SyntaxException("VarDec couldn't find Identifier", token.getSourceLocation());
            }
            IToken id = token;
            VarDec varDec = new VarDec(tempToken, id);
            varDecList.add(varDec);
            consume();
            while (token.getKind()==Kind.COMMA) {
                tempToken = token;
                consume();
                if (token.getKind()!=Kind.IDENT) {
                    throw new SyntaxException("VarDec couldn't find Identifier", token.getSourceLocation());
                }
                id = token;
                varDec = new VarDec(tempToken, id);
                varDecList.add(varDec);
                consume();
            }       
            if (token.getKind()!=Kind.SEMI) {
                throw new SyntaxException("VarDec couldn't find \';\'", token.getSourceLocation());
            }
            consume();
        }

        while (token.getKind()==Kind.KW_PROCEDURE) {
            tempToken = token;
            consume();
            if (token.getKind()!=Kind.IDENT) {
                throw new SyntaxException("ProcedureDec couldn't find Identifier", token.getSourceLocation());
            }
            IToken id = token;
            consume();
            if (token.getKind()!=Kind.SEMI) {
                throw new SyntaxException("ProcedureDec couldn't find \";\"", token.getSourceLocation());
            }
            consume();
            Block block = block();
            if (token.getKind()!=Kind.SEMI) {
                throw new SyntaxException("ProcedureDec couldn't find \";\"", token.getSourceLocation());
            }
            ProcDec procDec = new ProcDec(tempToken, id, block);
            proDecList.add(procDec);
            consume();
        }
        Statement statement = statement();
        left = new Block(firstToken, cstDecList, varDecList, proDecList, statement);
        return left;
    }
    private Program program() throws PLPException {
        Program left;
        IToken firsToken = token;
        Block block = block();
        if (token.getKind()!=Kind.DOT) {
            throw new SyntaxException("Program Couldn't find \'.\'");
        }
        consume();
        if (token.getKind()!=Kind.EOF) {
            throw new SyntaxException("Invalid Text after Program",token.getSourceLocation());
        }
        left = new Program(firsToken, block);
        return left;
    }
}
