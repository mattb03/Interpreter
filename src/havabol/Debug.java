package havabol;


    public class Debug {
    public static boolean assign;
    public static boolean expr;
    public static boolean token;
    public Scanner scan;

    public Debug(Scanner scan) {
        this.assign = false;
        this.expr = false;
        this.token = false;
        this.scan = scan;
    }

    public void turnOn(String type) throws ParserException {
        if (type.equals("Assign")) {
            this.assign = true;
        } else if (type.equals("Expr")) {
            this.expr = true;
        } else if (type.equals("Token")) {
            this.token = true;
        } else if (type.equals("Exit")) {
        	System.exit(-1);
        } else {
            throw new ParserException(
                scan.currentToken.iSourceLineNr,
                "Invalid debug state. ", scan.sourceFileNm,"");
        }
    }

    public void turnOff(String type) {
        if (type.equals("Assign")) {
            this.assign = false;
        } else if (type.equals("Expr")) {
            this.expr = false;
        } else if (type.equals("Token")) {
            this.token = false;
        }
    }
}
