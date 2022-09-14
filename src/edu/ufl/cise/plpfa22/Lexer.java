package edu.ufl.cise.plpfa22;
import edu.ufl.cise.plpfa22.Token;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.IToken.SourceLocation;

import java.util.ArrayList;
import java.util.*;
import org.w3c.dom.Text;
public class Lexer implements ILexer{
    private static ArrayList<String> KWLIST = new ArrayList<>(Arrays.asList("CONST", "VAR", "PROCEDURE", "CALL", "BEGIN", "END", "IF", "THEN", "WHILE", "DO"));
    private static ArrayList<String> BOOLLIST = new ArrayList<>(Arrays.asList("TRUE", "FALSE"));
    private static enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS};
    final StringBuilder INPUT;
    private ArrayList<Token> tokenList;

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
    public static boolean isReserved(String str) {
        if (KWLIST.contains(str.toUpperCase()) || BOOLLIST.contains(str.toUpperCase())) {
            return true;
        }
        return false;
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
                break;
            default:
                throw new LexicalException("error token detected", start_row, start_col);
        }
        return null;
    }
    public void dealWhiteLine() throws LexicalException {
        if (isFinalState()) {
            Kind kind = currentKind();
            Token t = new Token(this.INPUT.substring(start_pos, current_pos), kind, new SourceLocation(start_row, start_col));
        } else if (this.state == State.START) {
            //do nothing
        } else {
            throw new LexicalException("error token detected", start_row, start_col);
        }
        current_pos++;
        current_row++;
        current_col = 1;
        setStateToStart();
    }
    public void dealWhiteSpace() throws LexicalException {
        current_pos++;
        current_col++;
        setStateToStart();
    }
    @Override
    public IToken next() throws LexicalException {
        
        while(current_pos < this.INPUT.length()) {
            char c = this.INPUT.charAt(current_pos);
            switch (c){
                case '\n', '\r':
                    dealWhiteLine();
                case ' ','\t':
                    dealWhiteSpace();
                // case HAVE_ZERO:
                // case HAVE_DOT:
                // case IN_FLOAT:
                // case IN_NUM:
            }
        }

        return null;
    }

    @Override
    public IToken peek() throws LexicalException {
        
        return null;
    }
    
}
