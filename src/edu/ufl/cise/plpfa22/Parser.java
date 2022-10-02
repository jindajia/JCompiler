package edu.ufl.cise.plpfa22;

import java.util.Arrays;

import au.com.dius.pact.core.support.json.JsonToken.Null;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Statement;

public class Parser implements IParser{
    final Lexer lexer;
    IToken token = null;

    public Parser(ILexer lexer) {
        this.lexer = (Lexer) lexer;
    }
    @Override
    public ASTNode parse() throws PLPException {
        consume();
        // TODO return program();
        return null;
    }
    
    private IToken consume() {
        try {
            token = lexer.next();
        } catch (LexicalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return token;
    }

    private Expression const_val() throws Exception{
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

    private Expression expression() throws Exception {
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

    private Expression additive_expression() throws Exception {
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

    private Expression multiplicative_expression() throws Exception {
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

    private Expression primary_expression() throws Exception {
        Expression left = null;
        if (token.getKind() == Kind.IDENT) {
            left = new ExpressionIdent(token);
        } else if (token.getKind() == Kind.LPAREN) {
            consume();
            left = expression();
            if (token.getKind() == Kind.RPAREN) {
                consume();
            } else {
                throw new SyntaxException("Conldn't find right Parenthesis", token.getSourceLocation());
            }
        } else {
            left = const_val();
        }
        return left;
    }


    private Statement statement() throws Exception {
        return null;
    }
}
