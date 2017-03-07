package havabol;

public class Parser {

    public Scanner scan;
    public SymbolTable st;
    public ParserException error;

    public Parser(String SourceFileNm) throws Exception {
        this.scan = new Scanner(SourceFileNm);
        this.st = new SymbolTable();
    }

    public void statements() throws Exception, ParserException {
        while (! this.scan.getNext().isEmpty()) {
            //System.out.println(scan.currentToken.tokenStr);

            if (this.scan.currentToken.tokenStr.toLowerCase().equals("print")) {
                if (this.scan.nextToken.tokenStr.equals("(")) {
                    this.scan.getNext();  // on '('
                    this.scan.getNext(); // on value inside print( )
                    Token val = this.scan.currentToken;  // assigned this to val
                    if (this.scan.nextToken.tokenStr.equals(")")) {
                        this.scan.getNext();  // on ')'
                        if (this.scan.nextToken.tokenStr.equals(";")) {
                            STEntry arg = this.st.getSymbol(val.tokenStr);
                            if (arg == null) {  // NOT in symbol table, thus a string literal
                                System.out.println(val.tokenStr);
                            } else {
                                System.out.println(arg.value);
                            }

                            //this.st.putSymbol(val.tokenStr, new STEntry(
                                        //val.tokenStr, val.primClassif,
                                        //val.subClassif));
                        } else {
                            throw new ParserException(
                                this.scan.currentToken.iSourceLineNr,
                                 "Expected ';' ", this.scan.sourceFileNm,"");
                        }
                    } else {
                        throw new ParserException(
                            this.scan.currentToken.iSourceLineNr,
                             "Expected ')' ", this.scan.sourceFileNm, "");
                    }
                }
            // if we come across a declare statement. i.e. Int, String, Bool ...
            } else if (scan.currentToken.subClassif == 12) {
                // if nextToken is not an identifier throw an error
                if (scan.nextToken.subClassif != 1) {
                    throw new ParserException(scan.nextToken.iSourceLineNr,
                        "Identifier expected after declare statement:",
                        scan.sourceFileNm, "");
                } else {
                        //INTEGER = 2;
                        //FLOAT = 3;
                        //BOOLEAN = 4;
                        //STRING = 5;
                        //DATE = 6;
                    int type = 7;
                    String token = this.scan.currentToken.tokenStr;
                    if (token.equals("Int"))
                        type = 2;
                    else if (token.equals("Float"))
                        type = 3;
                    else if (token.equals("Bool"))
                        type = 4;
                    else if (token.equals("String"))
                        type = 5;
                    else if (token.equals("Date"))
                        type = 6;
                    this.scan.getNext();
                    //put this token into symbol table!!!
                    this.st.putSymbol(this.scan.currentToken.tokenStr,
                            new STEntry(this.scan.currentToken.tokenStr,
                                this.scan.currentToken.primClassif,
                                this.scan.currentToken.subClassif));
                    // set the type in the symbol table for the identifier
                    this.st.getSymbol(this.scan.currentToken.tokenStr).type = type;

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
                    throw new ParserException(scan.currentToken.iSourceLineNr,
                        "Symbol "+this.scan.currentToken.tokenStr+
                        "is not in Symbol Table.", scan.sourceFileNm, "");
                }
            }
        }
        //this.st.printTable();
    }

    public void assign(Parser parse, Token curSymbol) throws Exception {
        int type = parse.st.getSymbol(curSymbol.tokenStr).type;
        parse.scan.getNext();
        parse.scan.getNext();
        if (parse.scan.nextToken.tokenStr.equals(";")) {
            if (type != parse.scan.currentToken.subClassif) {
                throw new ParserException(scan.currentToken.iSourceLineNr,
                    "Incompatible type.", scan.sourceFileNm, parse.scan.lines[parse.scan.line-1]);
            } else {
                parse.st.getSymbol(curSymbol.tokenStr).value = parse.scan.currentToken.tokenStr;
            }
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
