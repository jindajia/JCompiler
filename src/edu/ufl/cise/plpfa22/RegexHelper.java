package edu.ufl.cise.plpfa22;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RegexHelper {
    public static Pattern idfStart() {
        return Pattern.compile("[a-zA-Z]|(\\_)|(\\$)", Pattern.CASE_INSENSITIVE);
    }
    public static Pattern idfPart() {
        return Pattern.compile("("+idfStart().pattern()+")|(\\d)", Pattern.CASE_INSENSITIVE);
    }
    public static Pattern idfPattern() {
        return Pattern.compile("("+idfStart()+")("+idfPart()+")*");
    }
    public static Pattern whiteSpace() {
        return Pattern.compile("(\\r|\\n|\\t|\\s)");
    }
    public static void main(String[] args) throws Exception {
        String line = """ 
            aaa bbb cad 12 de""";
        Matcher m = whiteSpace().matcher(line);
        while (m.find()) {
            System.out.println(m.start());
        }
    }
}
