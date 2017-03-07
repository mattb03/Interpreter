package havabol;

public class Parser {

    public Scanner scan;
    public SymbolTable st;

    public Parser(String SourceFileNm, SymbolTable st) {
        try {
            Scanner scan = new Scanner(SourceFileNm);
        }catch (Exception e) {
        }
        this.st = st;
    }

    public ResultValue statements() {
        //if (scan.currenToken.prim
    }



//GOALS:

//expr() - returns a ResultValue

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
				, diagnosticTxt, scan.sourceFileNm);
	}
}
