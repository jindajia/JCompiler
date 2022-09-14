package edu.ufl.cise.plpfa22;
import edu.ufl.cise.plpfa22.Token;
import edu.ufl.cise.plpfa22.IToken.SourceLocation;

import java.util.ArrayList;
public class Lexer implements ILexer{
    private static enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS};

    final String INPUT;
    private ArrayList<Token> tokenList;

    int currentpos;
    int current_row;
    int current_col;
    public Lexer(String input) {
        this.INPUT = input;
        this.currentpos = 0;
        this.current_col = 1;
        this.current_row = 1;
    }
    // public static boolean isFinalState(State state) {
    //     switch (state){
    //         case START:
    //         case IN_IDENT:
    //         case HAVE_ZERO:
    //         case HAVE_DOT:
    //         case IN_FLOAT:
    //         case IN_NUM:
    //     }
    // }
    @Override
    public IToken next() throws LexicalException {
        State state = State.START;
        int start_pos = -1;
        SourceLocation sorce_loc = new SourceLocation(current_row, current_col);
        while(currentpos < this.INPUT.length()) {
            char c = this.INPUT.charAt(currentpos);
            switch (state){
                case START:
                    start_pos = currentpos;
                    sorce_loc = new SourceLocation(current_row, current_col);
                case IN_IDENT:
                case HAVE_ZERO:
                case HAVE_DOT:
                case IN_FLOAT:
                case IN_NUM:
            }
        }

        return null;
    }

    @Override
    public IToken peek() throws LexicalException {
        
        return null;
    }
    
}
