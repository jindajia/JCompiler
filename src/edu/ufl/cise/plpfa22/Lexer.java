package edu.ufl.cise.plpfa22;
import edu.ufl.cise.plpfa22.Token;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.IToken.SourceLocation;

import java.util.ArrayList;
import java.util.*;

public class Lexer implements ILexer{
    private static ArrayList<String> KWLIST = new ArrayList<>(Arrays.asList("CONST", "VAR", "PROCEDURE", "CALL", "BEGIN", "END", "IF", "THEN", "WHILE", "DO"));
    private static ArrayList<String> BOOLLIST = new ArrayList<>(Arrays.asList("TRUE", "FALSE"));
    private static enum State {START, IN_IDENT, IN_STRING, IN_STRING_HAVE_BS, IN_NUM, HAVE_CN, HAVE_LT, HAVE_GT, HAVE_DIV, IN_COMMENT, IN_COMMENT_HAVE_BS};
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
    public Kind currentKind(){
        switch (this.state) {
            case START:
                return Kind.ERROR;
            case IN_NUM:
                return Kind.NUM_LIT;
            case IN_IDENT:
                if (isReserved(currentTokenString())) {
                    try {
                        return reservedKind(currentTokenString());
                    } catch (LexicalException e) {
                        e.printStackTrace();
                        return Kind.ERROR;
                    }
                } else {
                    return Kind.IDENT;
                }
            case HAVE_CN:
                return Kind.ERROR;
            case HAVE_DIV:
                return Kind.DIV;  
            case HAVE_GT:
                return Kind.GT;
            case HAVE_LT:
                return Kind.LT;
            case IN_STRING:
                return Kind.ERROR;
            case IN_COMMENT:
                return Kind.ERROR;
            case IN_COMMENT_HAVE_BS:
                return Kind.ERROR;
            case IN_STRING_HAVE_BS:
                return Kind.ERROR;
            default:
                return Kind.ERROR;

        }
    }
    public IToken currentToken() throws LexicalException{
        if (state==State.START && current_pos>=this.INPUT.length()) {
            Token t = new Token("", Kind.EOF, new SourceLocation(start_row, start_col));
            return t;
        } else {
            Kind kind = currentKind();
            Token t = new Token(this.INPUT.substring(start_pos, current_pos), kind, new SourceLocation(start_row, start_col));
            return t;
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
    public static boolean isDigit(char c) {
        if(c>='0' && c<='9') {
            return true;
        }
        return false;
    }
    public static boolean isIdent_Start (char c) {
        if (c>='A' && c<='Z') {
            return true;
        } else if (c>='a' && c<='z') {
            return true;
        } else if (c=='$' || c=='_') {
            return true;
        } else {
            return false;
        }
    }
    public static boolean isIdent_Part (char c) {
        if (isDigit(c) || isIdent_Start(c)) {
            return true;
        } return false;
    }
    public boolean canAddToCurrentToken(char c) {
        switch (this.state) {
            case HAVE_CN,HAVE_GT,HAVE_LT:
                if (c=='=') {
                    return true;
                } return false;
            case IN_STRING: 
                return true;
            case IN_STRING_HAVE_BS:
                if (Arrays.asList('b','t','n','f','r','\"','\'','\\').contains(c)) {
                    return true;
                } return false;
            case HAVE_DIV:
                if (c=='/') {
                    return true;
                } return false;
            case IN_IDENT:
                if (Character.isJavaIdentifierPart(c)) {
                    return true;
                } return false;
            case START:
                if (c==':' || c=='<' || c=='>' || c=='"'|| c=='/' || Character.isWhitespace(c)) {
                    return true;
                } return false;
            case IN_COMMENT:
                return true;
            case IN_COMMENT_HAVE_BS:
                return true;
            case IN_NUM:
                if (Character.isDigit(c)) {
                    return true;
                } return false;
            default:
                return false;

        }
    }
    // return Token if after adding char is a final state, otherwise return null
    public Token generateFinalToken(char c) throws LexicalException{
        switch (this.state) {
            case START:
                switch (c) {
                    case '.':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.DOT, new SourceLocation(start_row, start_col));
                    case ';':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.SEMI, new SourceLocation(start_row, start_col));
                    case ',':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.COMMA, new SourceLocation(start_row, start_col));
                    case '(':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.LPAREN, new SourceLocation(start_row, start_col));
                    case ')':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.RPAREN, new SourceLocation(start_row, start_col));
                    case '+':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.PLUS, new SourceLocation(start_row, start_col));
                    case '-':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.MINUS, new SourceLocation(start_row, start_col));
                    case '*':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.TIMES, new SourceLocation(start_row, start_col));
                    case '%':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.MOD, new SourceLocation(start_row, start_col));
                    case '?':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.QUESTION, new SourceLocation(start_row, start_col));
                    case '!':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.BANG, new SourceLocation(start_row, start_col));
                    case '#':
                        return new Token(INPUT.substring(start_pos, current_pos+1), Kind.NEQ, new SourceLocation(start_row, start_col));
                    case '"':
                        return null;
                    case '/':
                        return null;
                    case ':':
                        return null;
                    case '>':
                        return null;
                    case '<':
                        return null;
                    default:
                        if (Character.isDigit(c) || Character.isJavaIdentifierStart(c) || Character.isWhitespace(c)) {
                            return null;
                        } else {
                            throw new LexicalException("unrecognized character", current_row, current_col);
                        }
                }
            case HAVE_CN:
                if (c == '=') {
                    return new Token(INPUT.substring(start_pos, current_pos+1), Kind.ASSIGN, new SourceLocation(start_row, start_col));
                } else {
                    throw new LexicalException("unrecognized character", current_row, current_col);
                }
            case HAVE_GT:
                if (c == '=') {
                    return new Token(INPUT.substring(start_pos, current_pos+1), Kind.GE, new SourceLocation(start_row, start_col));
                } else {
                    return null;
                }
            case HAVE_LT:
                if (c == '=') {
                    return new Token(INPUT.substring(start_pos, current_pos+1), Kind.LE, new SourceLocation(start_row, start_col));
                } else {
                    return null;
                }
            case HAVE_DIV:
                return null;
            case IN_IDENT:
                return null;
            case IN_NUM:
                return null;
            case IN_STRING:
                if (c=='\\') {
                    return null;
                } else if (c=='"') {
                    return new Token(INPUT.substring(start_pos, current_pos+1), Kind.STRING_LIT, new SourceLocation(start_row, start_col));
                } else {
                    return null;
                }
            case IN_STRING_HAVE_BS:
                switch (c) {
                    case  'b', 't', 'n', 'f', 'r', '"', '\'', '\\':
                        return null;
                    default:
                        throw new LexicalException("unrecognized character", current_row, current_col);
                }
            case IN_COMMENT:
                if (c=='\n' || c=='\r') {
                    return new Token(INPUT.substring(start_pos, current_pos+1), Kind.COMMENT, new SourceLocation(start_row, start_col));
                } else {
                    return null;
                }
            case IN_COMMENT_HAVE_BS:
                if (c=='n' || c=='r') {
                    return new Token(INPUT.substring(start_pos, current_pos+1), Kind.COMMENT, new SourceLocation(start_row, start_col));
                } else {
                    return null;
                }
            default:
                throw new LexicalException("unrecognized state", current_row, current_col);

        }
    }
    public void updateState(char c) {
        switch (this.state) {
            case HAVE_CN:
                break;
            case HAVE_DIV:
                if (c == '/') {
                    this.state = State.IN_COMMENT;
                }
                break;
            case HAVE_GT:
                break;
            case HAVE_LT:
                break;
            case IN_COMMENT:
                if (c=='\\') {
                    this.state = State.IN_COMMENT_HAVE_BS;
                } 
                break;
            case IN_COMMENT_HAVE_BS:
                if (c!='n' && c!='r') {
                    this.state = State.IN_COMMENT;
                }
                break;
            case IN_IDENT:
                break;
            case IN_NUM:
                break;
            case IN_STRING:
                if (c=='\\') {
                    this.state = State.IN_STRING_HAVE_BS;
                } else if (c=='"') {
                    this.state = State.START;
                }
                break;
            case IN_STRING_HAVE_BS:
                switch (c) {
                    case  'b', 't', 'n', 'f', 'r', '"', '\'', '\\':
                        this.state = State.IN_STRING;
                    default:
                        break;
                }
                break;
            case START:
                switch (c){
                    case '"':
                        this.state = State.IN_STRING;
                        break;
                    case '/':
                        this.state = State.HAVE_DIV;
                        break;
                    case ':':
                        this.state = State.HAVE_CN;
                        break;
                    case '>':
                        this.state = State.HAVE_GT;
                        break;
                    case '<':
                        this.state = State.HAVE_LT;
                        break;
                    default:
                        if (Character.isDigit(c)) {
                            this.state = State.IN_NUM;
                        } else if (Character.isJavaIdentifierStart(c)) {
                            this.state = State.IN_IDENT;
                        }
                        break;
                    }
                break;
            default:
                break;
            
        }
    }
    @Override
    public IToken next() throws LexicalException {
        if (current_pos>=this.INPUT.length()) {
            return new Token("", Kind.EOF, new SourceLocation(current_row, current_col));
        }
        IToken token;
        while (current_pos<this.INPUT.length()) {
            char c = this.INPUT.charAt(current_pos);
            token = generateFinalToken(c);
            if (token != null) {//token final state
                if (c=='\n' || c=='\r') {
                    nextLine();
                } else {
                    nextCol();
                }
                setStateToStart();
                if (token.getKind() != Kind.COMMENT) {
                    return token;
                } else {
                    token = null;
                    continue;
                }
            } else if (canAddToCurrentToken(c)){//token unfinish state
                updateState(c);
                if (c=='\n' || c=='\r') {
                    nextLine();
                } else {
                    nextCol();
                }
                if (this.state == State.START && Character.isWhitespace(c)) {
                    setStateToStart();
                }
            } else {//token finish state
                token = currentToken();
                setStateToStart();
                return token;
            }
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
