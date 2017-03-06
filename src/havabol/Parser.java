package havabol;

public class Parser {

    public Scanner scan;
    public SymbolTable st;
    public ParserException error;

    public Parser(String SourceFileNm) {
        try {
            this.scan = new Scanner(SourceFileNm);
        }catch (Exception e) {
        }
        this.st = new SymbolTable();
    }

    public void statements() throws Exception {
        while (! this.scan.getNext().isEmpty()) { 
            System.out.println(scan.currentToken.tokenStr);
            // if we come across a declare statement. i.e. Int, String, Bool ...
            if (scan.currentToken.subClassif == 12) {
                // if nextToken is not an identifier throw an error
                if (scan.nextToken.subClassif != 1) {
                    error = new ParserException(scan.nextToken.iSourceLineNr, 
                        "Identifier expected after declare statement:",
                        scan.sourceFileNm);
                    System.err.println(error);
                    throw new Exception();
                } else {
                    this.scan.getNext();
                    //put this token into symbol table!!!
                    this.st.putSymbol(this.scan.currentToken.tokenStr, 
                            new STEntry(this.scan.currentToken.tokenStr, 
                                this.scan.currentToken.primClassif, 
                                this.scan.currentToken.subClassif));
                    if (this.scan.nextToken.tokenStr.equals("=")) {   
                        this.assign(this, this.scan.currentToken);
                    }
                }

            } else if (scan.currentToken.tokenStr.toLowerCase().equals("if")) {
                this.ifStmt(this);
            } else if (scan.currentToken.tokenStr.toLowerCase().equals("while")) {
                this.whileStmt(this);
            } else if (scan.currentToken.subClassif == 1) {
                if (this.st.getSymbol(this.scan.currentToken.tokenStr) == null) {
                    System.err.println("Symbol "+this.scan.currentToken.tokenStr+" is not in Symbol Table!");
                    this.st.printTable();
                    throw new Exception();
                }
            }
        
        }
        this.st.printTable();
    }

    public void assign(Parser parse, Token curSymbol) throws Exception {
        parse.scan.getNext();
        parse.scan.getNext();
        System.err.println(parse.scan.nextToken.tokenStr);
        if (parse.scan.nextToken.tokenStr.equals(";")) {
            parse.st.getSymbol(curSymbol.tokenStr).value = parse.scan.currentToken.tokenStr;
        } else {
            this.expr(curSymbol, parse);
        }
    }

    public void ifStmt(Parser parse) {
        System.out.println("Im an if statement!!!");
    }

    public void whileStmt(Parser parse) {
        System.out.println("Im a while statement!!");
    }

    public void expr(Token curSymbol, Parser parse) {

    }

}



//GOALS:
// QUESTION?  -- What does statements() return????
//expr() - returns a ResultValue
