package havabol;

public class Parser {

    public Scanner scan;
    public SymbolTable st;
    public ParserException error;


    public Parser(String SourceFileNm, SymbolTable st) {
        try {
            scan = new Scanner(SourceFileNm);
        }catch (Exception e) {
        }
        this.st = st;
    }
    
    public void statements() throws Exception, ParserException {
        while (! scan.getNext().isEmpty()) {
            //System.out.println(scan.currentToken.tokenStr);

            if (scan.currentToken.tokenStr.toLowerCase().equals("print")) {
                if (scan.nextToken.tokenStr.equals("(")) {
                    scan.getNext();  // on '('
                    scan.getNext(); // on value inside print( )
                    Token val = scan.currentToken;  // assigned this to val
                    if (scan.nextToken.tokenStr.equals(")")) {
                        scan.getNext();  // on ')'
                        if (scan.nextToken.tokenStr.equals(";")) {
                            STEntry arg = st.getSymbol(val.tokenStr);
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
                                scan.currentToken.iSourceLineNr,
                                 "Expected ';' ", scan.sourceFileNm,"");
                        }
                    } else {
                        throw new ParserException(
                            scan.currentToken.iSourceLineNr,
                             "Expected ')' ", scan.sourceFileNm, "");
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
                    String token = scan.currentToken.tokenStr;
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
                    scan.getNext();
                    //put this token into symbol table!!!
                    st.putSymbol(scan.currentToken.tokenStr,
                            new STEntry(scan.currentToken.tokenStr,
                                scan.currentToken.primClassif,
                                scan.currentToken.subClassif));
                    // set the type in the symbol table for the identifier
                    st.getSymbol(scan.currentToken.tokenStr).type = type;

                    if (scan.nextToken.tokenStr.equals("=")) {
                        assign(scan.currentToken);
                    }
                }

            } else if (scan.currentToken.tokenStr.toLowerCase().equals("if")) {
                ifStmt();
            } else if (scan.currentToken.tokenStr.toLowerCase().equals("while")) {
                whileStmt();
            } else if (scan.currentToken.subClassif == 1) {
                if (st.getSymbol(scan.currentToken.tokenStr) == null) {
                    throw new ParserException(scan.currentToken.iSourceLineNr,
                        "Symbol "+scan.currentToken.tokenStr+
                        "is not in Symbol Table.", scan.sourceFileNm, "");
                }
            }
        }
        //this.st.printTable();
    }

    public void assign(Token curSymbol) throws Exception {
        int type = st.getSymbol(curSymbol.tokenStr).type;
        scan.getNext();
        scan.getNext();
        if (scan.nextToken.tokenStr.equals(";")) {
            if (type != scan.currentToken.subClassif) {
                throw new ParserException(scan.currentToken.iSourceLineNr,
                    "Incompatible type.", scan.sourceFileNm, scan.lines[scan.line-1]);
            } else {
                st.getSymbol(curSymbol.tokenStr).value = scan.currentToken.tokenStr;
            }
        } else {
            //expr(curSymbol, parse);
        	expr();
        }
    }

    public void ifStmt() {
        System.out.println("Im an if statement!!!");
    }

    public void whileStmt() {
        System.out.println("Im a while statement!!");
    }


    /* 1   expr := products expr'
     * 2.1 expr':= '+' products expr'
     * 2.2		 |	empty
     * 3   products := operand products'
     * 4.1 products':= '*' operand products'
     * 4.2 		 |	empty
     * 5.1 operand := intConst
     * 5.2		 |	variable
     */
	public ResultValue expr() throws Exception {
		/*
		scan.getNext();
		ResultValue res = products();				// rule 1
		ResultValue temp = new ResultValue("");
		Numeric nOp1;
		Numeric nOp2;
		// products caused currentToken to be on +, - or neither
		while (scan.currentToken.tokenStr.equals("+") 
				|| scan.currentToken.tokenStr.equals("-")) {	// rule 2.1 and .2
			// get the next token, should be an operand
			// functions aren't being handled yet
			scan.getNext();
			if (scan.currentToken.primClassif != Token.OPERAND)
				error("Within expression, expected operand. Found: '%s'"
						, scan.currentToken.tokenStr);
			temp = products();								// rule 2.1
			if (scan.currentToken.equals("+")) {
				nOp1 = new Numeric(this, res, "+", "1st operand");
				nOp2 = new Numeric(this, temp, "+", "2nd operand");
				res = Utility.add(this, nOp1, nOp2);
			} else {
				nOp1 = new Numeric(this, res, "-", "1st operand");
				nOp2 = new Numeric(this, temp, "-", "2nd operand");
				res = Utility.subtract(this, nOp1, nOp2);
			}
		}
		return res;
		*/
		return null;
	}
	
	// Assumption: currently on an operand
	private ResultValue products() throws Exception {
		ResultValue res = operand();				// rule 3
		ResultValue temp = new ResultValue("");
		Numeric nOp1;
		Numeric nOp2;
		
		while (scan.currentToken.tokenStr.equals("*")	// rule 4.1 and .2
				|| scan.currentToken.tokenStr.equals("/")) {
			scan.getNext();
			if (scan.currentToken.primClassif != Token.OPERAND)
				error("Within expression, expected operand. Found: '%s'"
						, scan.currentToken.tokenStr);
			temp = operand();
			if (scan.currentToken.tokenStr.equals("*")) {
				nOp1 = new Numeric(this, res, "*", "1st operand");
				nOp2 = new Numeric(this, temp, "*", "2nd operand");
				res = Utility.multiply(this, nOp1, nOp2);
			} else {
				nOp1 = new Numeric(this, res, "/", "1st operand");
				nOp2 = new Numeric(this, temp, "/", "2nd operand");
				res = Utility.divide(this, nOp1, nOp2);
			}
		}
		return res;
	}
	
	// Assumption: currently on an operand
	private ResultValue operand() throws Exception {
		ResultValue res;				// rule 3
		if (scan.currentToken.primClassif == Token.OPERAND) {
			switch (scan.currentToken.subClassif) {
			case Token.IDENTIFIER:
				// get variable value from symboltable
				STEntry var = st.getSymbol(scan.currentToken.tokenStr);
				scan.getNext();
				res = new ResultValue(var.symbol);
				res.type = var.subClassif;
				res.structure.add("variable");
				return res;
			case Token.INTEGER:
			case Token.FLOAT:
			case Token.DATE:
			case Token.STRING:
			case Token.BOOLEAN:
				res = scan.currentToken.toResult();
				scan.getNext();
				return res;
			}
		}
		error("Within operand, found '%s'"
				, scan.currentToken.tokenStr);
		return null;
	}
	
	public void error(String fmt, Object... varArgs) throws Exception {
		String diagnosticTxt = String.format(fmt, varArgs);
		throw new ParserException(scan.currentToken.iSourceLineNr
				, diagnosticTxt, scan.sourceFileNm, "");
	}
}


