package havabol;

public class Parser {

    public Scanner scan;
    public SymbolTable st;

    public Parser(String SourceFileNm) {
        try {
            Scanner scan = new Scanner(SourceFileNm);
        }catch (Exception e) {
        }
        SymbolTable st = new SymbolTable();
    }

    public ResultValue statements() {
        if (scan.currenToken.prim
    }

}



GOALS:

expr() - returns a ResultValue
