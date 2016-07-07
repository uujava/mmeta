// copyright 2009 ActiveVideo; license: MIT; see license.txt
package mmeta;

/// Thrown when a syntax error is found
public class SyntaxError extends Error {
    private static final long serialVersionUID = 1625531475408759945L;

    private final int offset;
    private final int line;
    private final int column;

    public SyntaxError(String msg, String expected, int pos, String string, Object[] list) {
        super(makeMsg(msg, expected, pos, string, list));
        offset = pos;
        line=0;
        column=0;
    }

    public SyntaxError(String message, String expected, String string, Object[] list, Position pos) {
        super(makeMsg(message, expected, pos.pos(), string, list));
        offset = pos.pos();
        line = pos.line();
        column = pos.col();
    }

    private static String makeMsg(String msg, String expected, int pos, String string, Object[] list) {
        if (string != null) {
            int nl = 1;
            int nlpos = 0;
            for (int i = 0; i < pos; i++) if (string.charAt(i) == '\n') { nlpos = i; nl++; }

            if (msg.length() > 0) msg = "expected "+ msg;
            else msg = "expected "+ expected;

            String s = string.substring(pos, Math.min(pos + 13, string.length()));
            if (pos >= string.length()) {
              s = "<EOF>";
            }
            int n = s.indexOf('\n');
            if (n > 0) s = s.substring(0, n);
            msg = msg + " before '"+ printableControlChars(s) +"'";

            return ""+ msg +" (at line: "+ nl +", char: "+ (pos - nlpos) +")";
        } else {
            if (msg.length() > 0) msg = "expected "+ msg;
            else msg = "expected "+ expected;

            msg = msg + " before '"+ BaseParser.print_r(list[pos]) +"'";
            return ""+ msg +" (at pos: "+ pos +")";
        }
    }

    private static final String[][] CTRL_CHARS = {
            {"\b", "\\b"},
            {"\n", "\\n"},
            {"\t", "\\t"},
            {"\f", "\\f"},
            {"\r", "\\r"}
    };

    //TODO handle unprintable unicode
    private static String printableControlChars(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for(int i=0; i < s.length(); i++){
            CharSequence repl = s.subSequence(i, i + 1);
            for (int j = 0; j < CTRL_CHARS.length; j++) {
                String[] ctrlChar = CTRL_CHARS[j];
                if(repl.equals(ctrlChar[0])){
                    repl = ctrlChar[1];
                    break;
                }
            }
            sb.append(repl);
        }
        return sb.toString();
    }

    public int getOffset() {
        return offset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }


}


