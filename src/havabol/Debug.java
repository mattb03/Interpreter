package havabol;
    public class Debug {
    public static boolean assign;
    public static boolean expr;
    public static boolean token;

    public Debug() {
        this.assign = false;
        this.expr = false;
        this.token = false;
    }

    public void setDebugState(Scanner scan) throws ParserException, Exception {
        scan.getNext();
        String type = scan.currentToken.tokenStr;
        String state = scan.nextToken.tokenStr;
        int lineNr = scan.currentToken.iSourceLineNr;
        boolean bstate;
        String msg;
        if (state.equals("on")) {
            bstate = true;
        } else if (state.equals("off")) {
            bstate = false;
        } else {
            msg = "Invalid debug state: ";
            msg += state;
            throw new  ParserException(lineNr, msg, scan.sourceFileNm, "");
        }

        if (type.equals("Assign")) {
            this.assign = bstate;
        } else if (type.equals("Expr")) {
            this.expr = bstate;
        } else if (type.equals("Token")) {
            this.token = bstate;
        } else {
            msg = "Invalid debug type: ";
            msg += type;
            throw new  ParserException(lineNr, msg, scan.sourceFileNm, "");
        }
        scan.getNext();
        scan.getNext();
        if (!scan.currentToken.tokenStr.equals(";")) {
            throw new ParserException(
                scan.currentToken.iSourceLineNr,
                "Expected ';' at the end of debug line.", scan.sourceFileNm,
                "");
        }

    }
}
