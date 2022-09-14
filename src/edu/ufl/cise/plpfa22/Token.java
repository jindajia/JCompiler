package edu.ufl.cise.plpfa22;
import javax.xml.transform.Source;

import edu.ufl.cise.plpfa22.Token;

public class Token implements IToken{
    final String TEXT;
    final SourceLocation SOURCELOCATION;
    final Kind KIND;

    public Token(String text, Kind kind, SourceLocation sourceLocation) {
        this.TEXT = text;
        this.KIND = kind;
        this.SOURCELOCATION = sourceLocation;
    }

    @Override
    public Kind getKind() {
        return this.KIND;
    }

    @Override
    public char[] getText() {
        return this.TEXT.toCharArray();
    }

    @Override
    public SourceLocation getSourceLocation() {
        return this.SOURCELOCATION;
    }

    @Override
    public int getIntValue() throws NumberFormatException{
        return Integer.parseInt(this.TEXT);
    }

    @Override
    public boolean getBooleanValue() {
        return Boolean.parseBoolean(this.TEXT);
    }

    @Override
    public String getStringValue() {
        String result = this.TEXT.substring(1,this.TEXT.length()-1);
        result = result.replace("\\n", "\n");
        result = result.replace("\\b", "\b");
        result = result.replace("\\r", "\r");
        result = result.replace("\\t", "\t");
        result = result.replace("\\f", "\f");
        result = result.replace("\\\\", "\\");
        result = result.replace("\\\"", "\"");
        result = result.replace("\\\'", "\'");


        return result;
    }
    
    public int getLength() {
        return this.TEXT.length();
    }
}
