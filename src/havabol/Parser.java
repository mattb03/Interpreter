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
        //while (! scan.getNext().isEmpty()) {
            //  come across print statement
        if (bExec == false) {
        	 //if (scan.nextToken.tokenStr.equals("else")) {
        		skipTo("endif", ";");
        	//}
        }
    	
        while (bExec == true) {
            if (scan.currentToken.tokenStr.toLowerCase().equals("print")) {
                ArrayList<Token> arglist = new ArrayList<Token>();
                if (scan.nextToken.tokenStr.equals("(")) {
                    scan.getNext();  // on '('
                    //scan.getNext(); // on value inside print( )
                    //Token val = scan.currentToken;  // assi-gned this to val
                    String tok = scan.getNext();
                    while (!scan.currentToken.tokenStr.equals(")")) {
                        if (scan.nextToken.tokenStr.equals(",")) {
                            arglist.add(scan.currentToken);
                        } else if (scan.nextToken.tokenStr.equals(")")){
                            arglist.add(scan.currentToken);
                        } else if (scan.currentToken.tokenStr.equals(",") && arglist.isEmpty()) {
                            System.err.println("SYNTAX ERROR");
                            throw new Exception();
                        } else {
                            ;
                        }
                        scan.getNext();
                    }
                    if (scan.nextToken.tokenStr.equals(";")) {
                        for (int i=0; i < arglist.size(); i++) {
                            Token token = arglist.get(i);
                            STEntry arg = st.getSymbol(token.tokenStr);
                            if (arg == null) {  // NOT in symbol table, thus a string literal
                                System.out.print(token.tokenStr);
                            } else {
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
            } else if (scan.currentToken.subClassif == 1) { // if token is an identifier
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
            // if (token == 'else' or token == 'endif')
            //     bExec = false
            
            scan.getNext();
            // if we found an 'else' or 'endif' we know weve hit the end of the statement block
            // so set bExec to false to exit the function
            if (scan.nextToken.tokenStr.equals("else") || scan.nextToken.tokenStr.equals("endif")) {
            	bExec = false;
            }
            else if (scan.getNext().isEmpty()) {
            	bExec = false;
            }
            
        }

    }




    public void assign(Token curSymbol) throws Exception {
        int type = st.getSymbol(curSymbol.tokenStr).type;
        // set the datatype in the symbol table
        //STIdentifier tempIdent = (STIdentifier)st.getSymbol(curSymbol.tokenStr);
        //tempIdent.dataType = type;

        scan.getNext(); // get equals sign
        if (!scan.currentToken.tokenStr.equals("=")) {
            System.out.println(scan.currentToken.tokenStr);
            throw new ParserException(scan.currentToken.iSourceLineNr,
                    "syntax error: ", scan.sourceFileNm, scan.lines[scan.line-1]);        }
        scan.getNext(); // get val
        if (scan.nextToken.tokenStr.equals(";")) {
            if (type != scan.currentToken.subClassif) {
                throw new ParserException(scan.currentToken.iSourceLineNr,
                    "Incompatible type.", scan.sourceFileNm, scan.lines[scan.line-1]);
            } else {
                st.getSymbol(curSymbol.tokenStr).value = scan.currentToken.tokenStr;
                // set the type here
                // but first we need to find out how to reference a child/subclass value in a hash table
                st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), type);
            }
        } else {
            //expr(curSymbol, parse);
            expr();
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

    public void skipTo(String stmt, String terminatingStr) throws Exception
    {
        while (!scan.currentToken.tokenStr.equals("else") && !scan.currentToken.tokenStr.equals("endif"))
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

    /* 1   expr := products expr'
     * 2.1 expr':= '+' products expr'
     * 2.2       |  empty
     * 3   products := operand products'
     * 4.1 products':= '*' operand products'
     * 4.2       |  empty
     * 5.1 operand := intConst
     * 5.2       |  variable
     */
    public ResultValue expr() throws Exception {
        /*
        scan.getNext();
        ResultValue res = products();               // rule 1
        ResultValue temp = new ResultValue("");
        Numeric nOp1;
        Numeric nOp2;
        // products caused currentToken to be on +, - or neither
        while (scan.currentToken.tokenStr.equals("+")
                || scan.currentToken.tokenStr.equals("-")) {    // rule 2.1 and .2
            // get the next token, should be an operand
            // functions aren't being handled yet
            scan.getNext();
            if (scan.currentToken.primClassif != Token.OPERAND)
                error("Within expression, expected operand. Found: '%s'"
                        , scan.currentToken.tokenStr);
            temp = products();                              // rule 2.1
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
        ResultValue res = operand();                // rule 3
        ResultValue temp = new ResultValue("");
        Numeric nOp1;
        Numeric nOp2;

        while (scan.currentToken.tokenStr.equals("*")   // rule 4.1 and .2
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
        ResultValue res;                // rule 3
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
