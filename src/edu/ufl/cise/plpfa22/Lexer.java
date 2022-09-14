package edu.ufl.cise.plpfa22;
import edu.ufl.cise.plpfa22.Token;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.IToken.SourceLocation;

import java.util.ArrayList;
import java.util.*;

public class Lexer implements ILexer{
    private static ArrayList<String> KWLIST = new ArrayList<>(Arrays.asList("CONST", "VAR", "PROCEDURE", "CALL", "BEGIN", "END", "IF", "THEN", "WHILE", "DO"));
    private static ArrayList<String> BOOLLIST = new ArrayList<>(Arrays.asList("TRUE", "FALSE"));
    private static enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS};
    final StringBuilder INPUT;

    int current_pos;
    int current_row;
    int current_col;

    State state;
    int start_pos;
    int start_row;
    int start_col;
    public Lexer(String input) {
        this.INPUT = new StringBuilder(input);
        this.current_pos = 0;
        this.current_col = 1;
        this.current_row = 1;

        this.state = State.START;
        this.start_pos = current_pos;
        this.start_col = current_col;
        this.start_row = current_row;
    }
    public boolean isFinalState() {
        switch (this.state){
            case IN_IDENT:
            case HAVE_ZERO:
            case IN_FLOAT:
            case IN_NUM:
            case HAVE_MINUS:
                return true;
            default:
                return false;
        }
    }
    public void setStateToStart() {
        start_pos = current_pos;
        start_row = current_row;
        start_col = current_col;
        state = State.START;
    }
    public String currentTokenString() {
        return this.INPUT.substring(start_pos, current_pos);
    }
    public static boolean isReserved(String str) {
        if (KWLIST.contains(str.toUpperCase()) || BOOLLIST.contains(str.toUpperCase())) {
            return true;
        }
        return false;
    }
    public static Kind reservedKind(String str) throws LexicalException{
        if (KWLIST.contains(str.toUpperCase())) {
            int index = KWLIST.indexOf(str.toUpperCase());
            switch(index) {
                case 0:
                    return Kind.KW_CONST;
                case 1:
                    return Kind.KW_VAR;
                case 2:
                    return Kind.KW_PROCEDURE;
                case 3:
                    return Kind.KW_CALL;
                case 4:
                    return Kind.KW_BEGIN;
                case 5:
                    return Kind.KW_END;
                case 6:
                    return Kind.KW_IF;
                case 7:
                    return Kind.KW_THEN;
                case 8:
                    return Kind.KW_WHILE;
                case 9:
                    return Kind.KW_DO;
                default:
                    throw new LexicalException("reserved Kind parse error: " + str);
            }
        } else if (BOOLLIST.contains(str.toUpperCase())) {
            return Kind.BOOLEAN_LIT;
        } else {
            throw new LexicalException("reserved Kind parse error: " + str);
        }
    }
    public Kind currentKind() throws LexicalException {
        switch (this.state) {
            case HAVE_DOT:
            case HAVE_EQ:
            case START:
                throw new LexicalException("error token detected", start_row, start_col);
            case HAVE_MINUS:
                return Kind.MINUS;
            case HAVE_ZERO:
            case IN_FLOAT:
            case IN_NUM:
                return Kind.NUM_LIT;
            case IN_IDENT:
                if (isReserved(currentTokenString())) {
                    return reservedKind(currentTokenString());
                } else {
                    return Kind.IDENT;
                }
            default:
                throw new LexicalException("error token detected", start_row, start_col);
        }
    }
    public IToken currentToken() throws LexicalException{
        if (isFinalState()) {
            Kind kind = currentKind();
            Token t = new Token(this.INPUT.substring(start_pos, current_pos), kind, new SourceLocation(start_row, start_col));
            return t;
        } else if (state==State.START && current_pos>=this.INPUT.length()) {
            Token t = new Token("", Kind.EOF, new SourceLocation(start_row, start_col));
            return t;
        } else {
            return new Token("", Kind.ERROR, new SourceLocation(start_row, start_col));
        }
    }
    public void nextLine() throws LexicalException {
        current_pos++;
        current_row++;
        current_col = 1;
    }
    public void nextCol() throws LexicalException {
        current_pos++;
        current_col++;
    }
    @Override
    public IToken next() throws LexicalException {
        IToken token = null;
        if (current_pos>=this.INPUT.length()) {
            return new Token("", Kind.EOF, new SourceLocation(current_row, current_col));
        }
        char c = this.INPUT.charAt(current_pos);
        while(canAddTocurrentToken(c)) {
            if (c==' '||c=='\t') {
                nextCol();
                setStateToStart();
            } else if (c=='\n'||c=='\r') {
                nextLine();
                setStateToStart();
            } else {
                nextCol();
            }
            if (current_pos>=this.INPUT.length()) {
                break;
            }
            c = this.INPUT.charAt(current_pos);
        }
        token = currentToken();
        setStateToStart();
        return token;
    }

    @Override
    public IToken peek() throws LexicalException {
        
        return null;
    }
    
}
