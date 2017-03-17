package havabol;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;
import javax.print.attribute.standard.RequestingUserName;

public class Parser {

    public Scanner scan;
    public SymbolTable st;
    public ParserException error;
    // this map is for identifying if 2 operands are compatible datatypes
    public Map<Integer, Integer> validDataTypes;
    public Pattern p;
    public boolean show = false;

    public Parser(String SourceFileNm, SymbolTable st) {
        try {
            scan = new Scanner(SourceFileNm);
        }catch (Exception e) {
        }
        this.st = st;
        // create datatype key-value mappings
        validDataTypes = new HashMap<Integer, Integer>();
        validDataTypes.put(2, 3); // Integer -> Float
        validDataTypes.put(3, 2); // Float -> Integer
        validDataTypes.put(4, 5); // bool -> String
        validDataTypes.put(5, 4); // String -> bool
        p = Pattern.compile("[^0-9.]");
    }

    public void statements(boolean bExec) throws Exception, ParserException {

        if (bExec == false) {

        	if (scan.nextToken.tokenStr.equals("else")) {
        		skipTo("endif", ";");
        	}
        	else {
        		// if nextToken is not an 'else' then its an 'endif'
        		// so call getNext() once to skip over it.
        		if (scan.nextToken.tokenStr.equals("endif")) {
        			scan.getNext();
        		}
        		else {
        			skipTo("else", ":");
        		}
        	}
        }

        while (bExec == true) {
            if (scan.currentToken.tokenStr.toLowerCase().equals("print")) {
                ArrayList<Token> arglist = new ArrayList<Token>();
                if (scan.nextToken.tokenStr.equals("(")) {
                    scan.getNext();  // on '('
                    String tok = scan.getNext();
                    while (!scan.currentToken.tokenStr.equals(")")) {
                        if (scan.nextToken.tokenStr.equals(",")) {
                            arglist.add(scan.currentToken);
                        } else if (scan.nextToken.tokenStr.equals(")")){
                            arglist.add(scan.currentToken);
                        } else if (scan.currentToken.tokenStr.equals(",") && arglist.isEmpty()) {
                            throw new ParserException(
                                scan.currentToken.iSourceLineNr,
                                "Did not expect a comma. ", scan.sourceFileNm,"");
                        } else {
                            ;
                        }
                        scan.getNext();
                    }
                    if (scan.nextToken.tokenStr.equals(";")) {
                        for (int i=0; i < arglist.size(); i++) {
                            Token token = arglist.get(i);
                            STIdentifier arg = (STIdentifier)st.getSymbol(token.tokenStr);
                            if (arg == null) {  // NOT in symbol table, thus a string literal
                            	/*** take off this .trim() before pushing to github ***/
                                System.out.print(token.tokenStr);
                            } else {
                            	/*** take off this .trim() before pushing to github ***/
                                System.out.print(arg.value);
                            }
                        }
                        System.out.println();
                    } else {
                        throw new ParserException(
                            scan.currentToken.iSourceLineNr,
                             "Expected ';' ", scan.sourceFileNm,"");

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
                    int type = Token.VOID;
                    String token = scan.currentToken.tokenStr;
                    if (token.equals("Int"))
                        type = Token.INTEGER;
                    else if (token.equals("Float"))
                        type = Token.FLOAT;
                    else if (token.equals("Bool"))
                        type = Token.BOOLEAN;
                    else if (token.equals("String"))
                        type = Token.STRING;
                    else if (token.equals("Date"))
                        type = Token.DATE;
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
            } else if (scan.currentToken.subClassif == Token.IDENTIFIER) { // if token is an identifier
                //check if curToken is in symbol table, if not throw an error
                STIdentifier entry = (STIdentifier)st.getSymbol(scan.currentToken.tokenStr);
                if (entry == null) {
                    throw new Exception();
                }
                if (scan.nextToken.tokenStr.equals("=")) {
                    assign(scan.currentToken);
                }
            } else if (scan.currentToken.tokenStr.toLowerCase().equals("if")) {
                ifStmt(bExec);
                bExec = false;
            } else if (scan.currentToken.tokenStr.toLowerCase().equals("while")) {
                whileStmt();
            } else if (scan.currentToken.subClassif == 1) {
                if (st.getSymbol(scan.currentToken.tokenStr) == null) {
                    throw new ParserException(scan.currentToken.iSourceLineNr,
                        "Symbol "+scan.currentToken.tokenStr+
                        "is not in Symbol Table.", scan.sourceFileNm, "");
                }
            }
            if (bExec == true)
            	scan.getNext();

            // if we found an 'else' or 'endif' we know weve hit the end of the statement block
            // so set bExec to false to exit the function
            if (scan.nextToken.tokenStr.equals("else") || scan.nextToken.tokenStr.equals("endif")) {
            	bExec = false;
            }
            else if (scan.nextToken.tokenStr.isEmpty()) {
            	bExec = false;
            }
        }
    }

    @Override
	public String toString() {
		return "Parser [scan=" + scan + "]";
	}

    public void assign(Token curSymbol) throws Exception {
        int ltype = st.getSymbol(curSymbol.tokenStr).type;  // get type of left op
        scan.getNext(); // get equals sign
        if (!scan.currentToken.tokenStr.equals("=")) {
            throw new ParserException(scan.currentToken.iSourceLineNr,
                    "syntax error: ", scan.sourceFileNm, scan.lines[scan.line-1]);        }
        scan.getNext(); // get val (right op)
        Token rToken = scan.currentToken;
        // check if rToken is an identifier or something else
        if (rToken.subClassif == Token.IDENTIFIER) {
            // get right op type
            int rtype = st.getSymbol(rToken.tokenStr).type;
            if (scan.nextToken.tokenStr.equals(";")) {  // only one identifier
                if (ltype != rtype) {
                    if ((ltype == Token.INTEGER && rtype == Token.FLOAT) || (ltype == Token.FLOAT && rtype == Token.INTEGER)) {
                        if (ltype == Token.INTEGER) {
                            String val = st.getSymbol(rToken.tokenStr).value;
                            int index = val.indexOf(".");
                            val = val.substring(0, index);
                            st.getSymbol(curSymbol.tokenStr).value = val;
                        } else {
                            st.getSymbol(curSymbol.tokenStr).value = st.getSymbol(rToken.tokenStr).value;
                        }
                    } else {
                        throw new ParserException(scan.currentToken.iSourceLineNr,
                            "Incompatible type.", scan.sourceFileNm, scan.lines[scan.line-1]);
                    }
                } else {
                    // set make left idents value equal to right idents value
                    st.getSymbol(curSymbol.tokenStr).value = st.getSymbol(rToken.tokenStr).value;
                }
            } else {  // next token not a ';'  possible valid expression
                expr();
            }
        } else { // rToken is not an identifier
            if (scan.nextToken.tokenStr.equals(";")) {
                if (ltype != rToken.subClassif) {
                    if ((ltype == Token.INTEGER && rToken.subClassif == Token.FLOAT) || (ltype == Token.FLOAT && rToken.subClassif == Token.INTEGER)) {
                        if (ltype == Token.INTEGER) {
                            int index = rToken.tokenStr.indexOf(".");
                            rToken.tokenStr = rToken.tokenStr.substring(0, index);
                        }
                        st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr;
                        st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), ltype);
                    } else {
                        throw new ParserException(scan.currentToken.iSourceLineNr,
                            "Incompatible type.", scan.sourceFileNm, scan.lines[scan.line-1]);
                    }
                } else {
                    st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr;
                    st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), ltype);
                }
            } else {  // next token not a ';', possible expression
                expr();
            }
        }

    }

    public void ifStmt(boolean bExec) throws Exception {

        Object resTrueStmts;
        Object resFalseStmts;
        // Do we need to evaluate the condition?
        if (bExec == true)
        {
            // we are executing (not ignoring)
            ResultValue resCond = evalCond(); //new ResultValue("true"); // you can test this condition in expr
            // Did the condition return True?
            if (resCond.value == "true")
            {
                // Cond returned True, execute statements
                // skip from 'if' to the next token to avoid infinite loop
                // TODO:
                // left off here. once you get the statements() to execute properly, we need to:
                // 1. skip over the rest of the statements until we hit an 'endif'
                // 2. finish off the rest of this function
                scan.getNext();
                scan.getNext();

                statements(true); // you need to keep evaluating until you hit an 'else' or 'endif'
                // has an else so ignore these statements
                statements(false);
                // at this point we already executed the true if block before
                // the 'else' block, so we need to skip over all statements 
                // until we are at the end of the entire if block if we are not
                // already there.
                if (! scan.currentToken.tokenStr.equals("endif"))
                {
                	skipTo("endif", ";");
                }
            }
            else
            {
            	scan.getNext();
                // Cond returned False, ignore true part
                statements(false);
                // check for 'else'
                statements(true);

            }

        }
        else
        {
        	skipTo("endif", ";");
            /* well come back to this
            // we are ignoring execution
            // we want to ignore the conditional, true part, and false part
            // Should we execute evalCond ?
            skipTo("if", ":");
            //resTrueStmts = statements(false); this line is needed, but you need to figure
            // out the data type of resTrueStmts
            if (resTrueStmts.terminatingStr == "else")
            {
                if (scan.getNext() != ":")
                {
                    errorWithCurrent("Expected ':' after 'else'");
                }
                resFalseStmts = statements();
                if (resFalseStmts.terminatingStr != "endif")
                {
                    errorWithCurrent("Expected endif");
                }
            }
            if (resTrueStmts.terminatingStr != "endif")
            {
                errorWithCurrent("Expected endif");
            }
            if (scan.getNext() != ";")
            {
                errorWithCurrent("Expected ';' after 'endif'");
            }*/
        }
    }

    public void whileStmt() {
        System.out.println("Im a while statement!!");
    }

    // skipTo(...) will skip tokens until your currentToken.tokenStr = stmt
    // so when the function exits, currentToken.tokenStr = stmt
    public void skipTo(String stmt, String terminatingStr) throws Exception
    {
        while (!scan.currentToken.tokenStr.equals(stmt))
        {
        	scan.getNext();
        }
    }

    public ResultValue evalCond() throws Exception
    {
        ResultValue resVal = new ResultValue("true");

        int leftOpType = 0;
        int rightOpType = 0;

        // get the left operand
        scan.getNext();
        Token leftOp = scan.currentToken;

        // symbol table entry for the left operand
        STIdentifier leftIdent = (STIdentifier)st.getSymbol(leftOp.tokenStr);

        // the literal value of the left operand
        String leftVal;
        if (leftIdent != null)
        {
            leftVal = leftIdent.value;
            leftOpType = leftIdent.dataType;

        }
        else
        {
            leftVal = getTokenValue(leftOp);
            leftOpType = getLiteralType(leftVal);
        }

        // now get the operator
        scan.getNext();
        Token operator = scan.currentToken;

        // get the right operand
        scan.getNext();
        Token rightOp = scan.currentToken;

        // symbol table entry for the right operand
        STIdentifier rightIdent = (STIdentifier)st.getSymbol(rightOp.tokenStr);

        // the literal value of the right operand
        String rightVal;
        if (rightIdent != null)
        {
            rightVal = rightIdent.value;
            rightOpType = rightIdent.dataType;

        }
        else
        {
            rightVal = getTokenValue(rightOp);
            rightOpType = getLiteralType(rightVal);
        }
        /*
        System.out.println("leftOp: " + leftOp.tokenStr +
                "\topeator: " + operator.tokenStr +
                "\trightOp: " + rightOp.tokenStr);
        System.out.println("entry in the st: " + st.table.get(leftOp.tokenStr));
        System.out.println("entry in the st: " + st.table.get(rightOp.tokenStr));
        */

        // throw an error if theres no terminating colon
        if (! scan.nextToken.tokenStr.equals(":"))
        {
            throw new ParserException(scan.currentToken.iSourceLineNr,
                    "No terminating ':'", scan.sourceFileNm, scan.lines[scan.line-1]);
        }

        // now we have the value of each operand in string format
        // so now get the datatypes of each operand


        // types are valid if theyre the same, or if the left maps to the right
        if (leftOpType != rightOpType)
        {
            if (validDataTypes.get(leftOpType) != rightOpType ||
                (leftOpType == 4 && !rightVal.equals("T") && !rightVal.equals("F")))
            {
            // throw error
                throw new ParserException(rightOp.iSourceLineNr,
                    "Right datatype cannot be compared to the left", scan.sourceFileNm, scan.lines[scan.line-1]);
            }
        }

        // now evaluate
        boolean flag;
        // TODO:
        // we left off here. need to figure out how to put a new value into
        // the symbol table. once we can do that, we can determine whether
        // this function is done (it should be done).
        switch (leftOpType)
        {
            // were evaluating integers
            case 2:
            	// if the right is an integer and the right is a float
            	// the truncate the decimal portion
            	if (leftOpType != rightOpType) 
            	{
            		rightVal = rightVal.substring(0, rightVal.indexOf('.'));
            	}
                flag = evalIntegers(leftVal, operator.tokenStr, rightVal);
                if (flag == true)
                {

                    return resVal;
                }
                else
                {
                    resVal.value = "false";
                    return resVal;
                }

            // were evaluating floats
            case 3:
            	// if the left is a float and the right is an integer
            	// add a .0 to the end of the integer
            	if (leftOpType != rightOpType) 
            	{
            		rightVal = rightVal + ".0";
            	}
                flag = evalFloats(leftVal, operator.tokenStr, rightVal);
                if (flag == true)
                {
                    return resVal;
                }
                else
                {
                    resVal.value = "false";
                    return resVal;
                }

            // we know at this point were evaluating string or bool
            default:
                switch (operator.tokenStr)
                {
                    case "==":
                        if (leftVal.equals(rightVal))
                        {
                            // return true
                            return resVal;
                        }
                        else
                        {
                            // return false
                            resVal.value = "false";
                            return resVal;
                        }
                    case "!=":
                        if (!leftVal.equals(rightVal))
                        {
                            // return true
                            return resVal;
                        }
                        else
                        {
                            // return false
                            resVal.value = "false";
                            return resVal;
                        }

                    // bool only
                    case "&&":
                        // throw error if both are not bools
                        if (leftOpType != 4 && rightOpType != 4)
                        {
                            throw new ParserException(operator.iSourceLineNr,
                            "Invalid operator for datatypes", scan.sourceFileNm, scan.lines[scan.line-1]);
                        }
                        // we know both are bools at this point
                        if (leftVal.equals("T") && rightVal.equals("T"))
                        {
                            // return true
                            return resVal;
                        }
                        else
                        {
                            // return false
                            resVal.value = "false";
                            return resVal;
                        }
                    // bool only
                    case "||":
                        // throw error if both are not bools
                        if (leftOpType != 4 && rightOpType != 4)
                        {
                            throw new ParserException(operator.iSourceLineNr,
                            "Invalid operator for datatypes", scan.sourceFileNm, scan.lines[scan.line-1]);
                        }
                        if (leftVal.equals("F") && rightVal.equals("F"))
                        {
                            // return false
                            resVal.value = "false";
                            return resVal;
                        }
                        else
                        {
                            // return true
                            return resVal;
                        }
                }
        }
        return resVal;
    }

    // getTokenValue(Token) assumes that the Token being passed in is one of the following:
    // an identifier, a numeric constant, a string literal, or a bool string literal
    // its purpose is to return the value of the Token being passed in
    public String getTokenValue(Token tok) throws ParserException
    {

        // check if its in the symbol table
        STIdentifier ident = (STIdentifier)st.getSymbol(tok.tokenStr);
        if (tok.tokenStr.equals("loc"))
            st.putValue(ident, "TX");
        // if its not in the table, its not an identifier.
        // so it must be a numeric constant, string literal, or bool string literal
        if (ident == null)
        {
            return tok.tokenStr;
        }

        // if we hit here, we know its in the table ie. its an identifier

        // if its an identifier, get its value
        if (! st.getSymbol(tok.tokenStr).value.equals("NO VALUE"))
        {
            return st.getSymbol(tok.tokenStr).value;
        }
        // if its an identifier but has NO VALUE, then its null throw error
        // else if (st.getSymbol(tok.tokenStr).value.equals("NO VALUE"))
        else
        {
            throw new ParserException(tok.iSourceLineNr,
                    "No value for variable \"" + tok.tokenStr + "\"", scan.sourceFileNm, scan.lines[scan.line-1]);
        }

    }

    // this function is to return the datatype of a literal token. that is,
    // a token that is not an identifier, ie. not in the symbol table.
    // it takes the literal token string and returns its datatype as an int.
    public int getLiteralType (String litToken)
    {

        char[] array = litToken.toCharArray();
        int i;
        Matcher m = p.matcher(litToken);

        //if (litToken.find("[^0-9.]"))
        if (!m.find())
        {
            // if were here it must be a float or integer
            // search for a period
            for (i = 0; i < array.length; i++)
            {
                // return float if we find a period
                if (array[i] == '.')
                    return 3;
            }
            // if were here then its not a float so return integer
            return 2;
        }
        // if were here then it must be a string or bool
        else
            if (array.length < 2 && (array[0] == 'T' || array[0] == 'F'))
            {
                // if its a single T or F return bool
                return 4;
            }
            // if its not a single T or F then its a string
            return 5;
    }


    public boolean evalIntegers (String leftOp, String operator, String rightOp)
    {
        // This needs to handle Floats as well****************************************************
        int iLeft = Integer.parseInt(leftOp);
        int iRight = Integer.parseInt(rightOp);
        switch (operator)
        {
            case "==":
                if (iLeft == iRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case "!=":
                if (iLeft != iRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case ">":
                if (iLeft > iRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case ">=":
                if (iLeft >= iRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case "<":
                if (iLeft < iRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case "<=":
                if (iLeft <= iRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            default:
                return false;
        }

    }

    public boolean evalFloats (String leftOp, String operator, String rightOp)
    {

        float fLeft = Float.parseFloat(leftOp);
        float fRight = Float.parseFloat(rightOp);
        switch (operator)
        {
            case "==":
                if (fLeft == fRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case "!=":
                if (fLeft != fRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case ">":
                if (fLeft > fRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case ">=":
                if (fLeft >= fRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case "<":
                if (fLeft < fRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            case "<=":
                if (fLeft <= fRight)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            default:
                return false;
        }

    }

    // assumes that this is called when currentToken = to the first operand
    // 1 + 2;
    //
    public ResultValue expr() throws Exception {
    	/* Stack<Token> mainStack = new Stack<Token>();
    	Stack<Token> postfixStack = new Stack<Token>();
    	Token tok = new Token();
    	Token popped = new Token();

    	boolean bFound;

    	tok = scan.currentToken;

    	// go through the expr and end if there isn't a token
    	// that can be in a expr
    	while (tok.primClassif == Token.OPERAND
    			|| tok.primClassif == Token.OPERATOR
    			|| tok.tokenStr.equals("(")
    			|| tok.tokenStr.equals(")")) {
    		tok.setPrecedence();
    		switch (tok.primClassif) {
    			case Token.OPERAND:
    				postfixStack.push(tok);
    				break;
    			case Token.OPERATOR:
    				while (! mainStack.isEmpty()) {
    					// equal to or less than operators
    					if (tok.normPreced
    							> mainStack.peek().stkPreced)
    						break;
    					popped = mainStack.pop();
    					postfixStack.push(popped);
    				}
    				mainStack.push(tok);
    				break;
    			case Token.SEPARATOR:
    				switch (tok.tokenStr) {
    					case "(":
    						mainStack.push(tok);
    						break;
    					case ")":
    						bFound = false;
    						while (! mainStack.isEmpty()) {
    							popped = mainStack.pop();
    							if (popped.tokenStr.equals("(")) {
    								bFound = true;
    								break;
    							}
    							postfixStack.push(popped);
    						}
    						if (!bFound) {
    							// TODO: call errors for missing "("
    							// TODO: add function implementations here
    						}
    						break;
    					default:
    						error("Invalid separator in expression");
    						break;
    				}
    				break;
    			default:
    				error("Invalid operator/operand in expression");
    		}
    		scan.getNext();
    		tok = scan.currentToken;
    	}
    	while (! mainStack.isEmpty()) {
    		popped = mainStack.pop();
    		if (popped.tokenStr.equals("("))
    			error("Missing ')' separator");

    		postfixStack.push(popped);
    	}*/
        return null;
    }

    public void error(String fmt, Object... varArgs) throws Exception {
        String diagnosticTxt = String.format(fmt, varArgs);
        throw new ParserException(scan.currentToken.iSourceLineNr
                , diagnosticTxt, scan.sourceFileNm, "");
    }
}
