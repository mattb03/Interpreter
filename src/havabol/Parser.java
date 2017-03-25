package havabol;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;
import javax.print.attribute.standard.RequestingUserName;

public class Parser {

    public Scanner scan;
    public Debug debugger;
    public SymbolTable st;
    public ParserException error;
    public Token startOfExprToken;

    public Parser(String SourceFileNm, SymbolTable st) throws Exception {
        scan = new Scanner(SourceFileNm, debugger);
        debugger = new Debug(scan);
        this.st = st;
    }

    public void statements(boolean bCalled) throws Exception, ParserException {
        Boolean expr = debugger.expr;
        

        while (true) {
        	expr = debugger.expr;
            if (scan.currentToken.tokenStr.toLowerCase().equals("print")) {
                ArrayList<String> arglist = new ArrayList<String>();
                if (scan.nextToken.tokenStr.equals("(")) {
                    scan.getNext();  // on '('
                    scan.getNext();
                    while (!scan.currentToken.tokenStr.equals(")")) {
                        if (scan.nextToken.tokenStr.equals(",")) {
                            if (scan.currentToken.subClassif == Token.STRING) { // one variable
                                arglist.add(scan.currentToken.tokenStr);
                            } else {
                            	Token curr = scan.currentToken.saveToken();
                                ResultValue res = expr(true);
                                arglist.add(res.value);
                                if (expr) {
                                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+res.value);
                                }
                                continue;
                            }

                        } else if (scan.nextToken.tokenStr.equals(")")){
                        	if (scan.currentToken.subClassif == Token.STRING) {
                                arglist.add(scan.currentToken.tokenStr);
                            } else {
                                Token curr = scan.currentToken.saveToken();
                                ResultValue res = expr(true);
                                arglist.add(res.value);
                                if (expr) {
                                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + "= "+res.value);
                                }
                                continue;
                            }
                        } else if (scan.currentToken.tokenStr.equals(",") && arglist.isEmpty()) {
                            error("Did not expect a comma. Found -> "+scan.currentToken.tokenStr);
                        } else if (scan.currentToken.tokenStr.equals(",")) {  //  handle expr logic
                            scan.getNext(); // consume comma and call expr
                            if (scan.currentToken.subClassif == Token.STRING) {
                                arglist.add(scan.currentToken.tokenStr);
                            } else {
                                Token curr = scan.currentToken.saveToken();
                                ResultValue res = expr(true);
                                arglist.add(res.value);
                                if (expr) {
                                	if (curr.subClassif == Token.IDENTIFIER) {
	                                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
	                                    		+ ": " + curr.tokenStr + "= "+res.value);
                                	} else {
                                		System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                        		+ ": -> "+res.value);
                                	}
                                }
                                continue;
                            }
                        } else {
                        	Token curr = scan.currentToken.saveToken();
                        	ResultValue res = expr(true);
                        	arglist.add(res.value);
                        	if (expr) {
                                System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                		+ ": -> "+res.value);
                            }
                        	continue;
                        }
                        scan.getNext();
                    }
                    if (scan.nextToken.tokenStr.equals(";")) {
                        for (int i=0; i < arglist.size(); i++) {
                            String str = arglist.get(i);
                            System.out.print(str);
                        }
                        System.out.println();
                    } else {
                    	error("Print function not terminated with ';'");

                    }
                }

            } else if (scan.currentToken.tokenStr.equals("debug")) {
                    scan.getNext();
                    String type = scan.currentToken.tokenStr; // got type
                    scan.getNext();
                    String state = scan.currentToken.tokenStr;  // got state; // 'on' or 'off'
                if (state.equals("on")) {
                    debugger.turnOn(type);
                } else if (state.equals("off")) {
                    debugger.turnOff(type);
                } else {
                    throw new ParserException(
                        scan.currentToken.iSourceLineNr,
                        "Invalid debug state. Found : "+scan.currentToken.tokenStr
                        , scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr]);
                }
                scan.getNext(); // consume semicolon
                if (scan.currentToken.equals(";")) {
                    throw new ParserException(
                        scan.currentToken.iSourceLineNr,
                        "Expected ';'. Found : "+scan.currentToken.tokenStr
                        , scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr]);
                }
            // if we come across a declare statement. i.e. Int, String, Bool ...
            } else if (scan.currentToken.subClassif == Token.DECLARE) {
                // if nextToken is not an identifier throw an error
                if (scan.nextToken.subClassif != Token.IDENTIFIER) {
                    throw new ParserException(scan.nextToken.iSourceLineNr,
                        "Identifier expected after declare statement. " +
                        "Found : "+scan.currentToken.tokenStr
                        , scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr]);
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
                    STIdentifier entry = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
                    if (entry != null) {
                    	entry.value = "NO_VALUE";
                    } else {
	                    st.putSymbol(scan.currentToken.tokenStr,
	                            new STEntry(scan.currentToken.tokenStr,
	                                scan.currentToken.primClassif,
	                                scan.currentToken.subClassif));
	                    // set the type in the symbol table for the identifier
	                    st.getSymbol(scan.currentToken.tokenStr).type = type;
                    }
                    if (scan.nextToken.tokenStr.equals(";")) {
                    	scan.getNext();
                    	continue;
                    } else if (scan.nextToken.tokenStr.equals("=")) {
                        assign(scan.currentToken);
                    } else {
                    	error("Invalid assign. Found : " + scan.nextToken.tokenStr, scan.nextToken);
                    }
                }
            } else if (scan.currentToken.subClassif == Token.IDENTIFIER) { // if token is an identifier
                //check if curToken is in symbol table, if not throw an error
                STIdentifier entry = (STIdentifier)st.getSymbol(scan.currentToken.tokenStr);
                if (entry == null) {
                    error("Symbol '"+scan.currentToken.tokenStr+"' is not in Symbol Table.");
                }
                if (scan.nextToken.tokenStr.equals("=")) {
                    assign(scan.currentToken);
                } else {
                	error("Invalid assign. Expected '='. Found : "+scan.currentToken.tokenStr);
                }
            } else if (scan.currentToken.tokenStr.equals("endif")) {
            	scan.getNext();
            	if (! scan.currentToken.tokenStr.equals(";")) {
            		error("endif is not correctly terminated.\n");
            	}
            	if (!bCalled) {
            		error("Unmatched endif.\n");
            	}
            	return;
            }
            else if (scan.currentToken.tokenStr.equals("else")) {
            	if (!bCalled) {
            		error("Unmatched else.\n");
            	}
            	return;
            }
            else if (scan.currentToken.tokenStr.toLowerCase().equals("if")) {
            	scan.getNext(); // consume if
            	ifStmt();

            } else if (scan.currentToken.tokenStr.toLowerCase().equals("while")) {
                whileStmt();
            }
            scan.getNext();
            if (scan.currentToken.tokenStr.equals("endwhile")) {
            	if (!bCalled) {
            		error("Unmatched endwhile.\n");
            	}
            	return;
            }
            else if (scan.nextToken.tokenStr.isEmpty()) {
            	break;
            }
        }
    }

    @Override
	public String toString() {
		return "Parser [scan=" + scan + "]";
	}

    public void assign(Token curSymbol) throws Exception {
        Boolean show = debugger.assign;
        Boolean expr = debugger.expr;
        String value = "";
        Token curr = curSymbol.saveToken();

        int ltype = st.getSymbol(curSymbol.tokenStr).type;  // get type of left op
        scan.getNext(); // get equals sign
        scan.getNext(); // get val (right op)
        Token rToken = scan.currentToken;
        // check if rToken is an identifier or something else
        if (rToken.subClassif == Token.IDENTIFIER) {
            STIdentifier entryR = (STIdentifier)st.getSymbol(rToken.tokenStr);
            if (entryR == null) {
                error("Symbol '"+rToken.tokenStr+"' is not in Symbol Table.");
            }
            // get right op type
            int rtype = entryR.type;
            if (scan.nextToken.tokenStr.equals(";")) {  // only one identifier
                if (ltype != rtype) {
                    if ((ltype == Token.INTEGER && rtype == Token.FLOAT) || (ltype == Token.FLOAT && rtype == Token.INTEGER)) {
                        if (ltype == Token.INTEGER) {
                            String val = entryR.value; // get the float val from rside
                            int index = val.indexOf(".");
                            val = val.substring(0, index);
                            st.getSymbol(curSymbol.tokenStr).value = val;
                            int assignType = getLiteralType(val);
                            st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), assignType);
                            if (show) {
                            	System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                            }
                        } else { // left type is a float and right type is an integer
                            String val = entryR.value;   // get the int val from rside , must cast to a float 2 -> 2.0
                            val += ".00";
                            st.getSymbol(curSymbol.tokenStr).value = val;
                            int assignType = getLiteralType(val);
                            st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), assignType);
                            if (show) {
                                System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                            }
                        }

                    } else {
                        throw new ParserException(scan.currentToken.iSourceLineNr,
                            "Incompatible type for numeric expression. ",
                             scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr]);
                    }

                } else {
                    // set make left idents value equal to right idents value
                    String val = st.getSymbol(rToken.tokenStr).value;
                    st.getSymbol(curSymbol.tokenStr).value = val;
                    int assignType = getLiteralType(val);
                    st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), assignType);
                    if (show) {
                        System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                    }
                }
            } else {  // next token not a ';'  possible valid expression
                ResultValue resExpr = expr(false);
                int assignType = getLiteralType(resExpr.value);
                if (ltype != assignType) {
                    if ((ltype == Token.INTEGER && assignType == Token.FLOAT)
                        || (ltype == Token.FLOAT && assignType == Token.INTEGER)) {

                        if (ltype == Token.INTEGER) {
                            value = resExpr.value;
                            int index = value.indexOf(".");
                            value = value.substring(index);
                        } else {  // left side is Float
                            value = resExpr.value;
                            value += ".00";
                        }
                    }
                } else {  // they are the same type
                    value = resExpr.value;
                }
            	STIdentifier ident = (STIdentifier)st.getSymbol(curSymbol.tokenStr);
                ident.value = value;
                String val2 = ident.value;
            	ident.dataType = ltype;
                if (expr) {
                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val2);
                }
            }
        } else { // rToken is not an identifier
            if (scan.nextToken.tokenStr.equals(";")) {
                if (ltype != rToken.subClassif) {
                    if ((ltype == Token.INTEGER && rToken.subClassif == Token.FLOAT) || (ltype == Token.FLOAT && rToken.subClassif == Token.INTEGER)) {
                    	String val = "";
                        if (ltype == Token.INTEGER) {
                            int index = rToken.tokenStr.indexOf(".");
                            val = st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr.substring(0, index);
                        } else {
                        	st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr + ".00";
                        	val = st.getSymbol(curSymbol.tokenStr).value;
                        	st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), ltype);
                        }
                        if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                        }
                    } else {
                    	error("'" + scan.currentToken.tokenStr 
                    			+ "' is an incompatible type for numeric expression of type "
                    			+ Token.strSubClassifM[ltype] + ".");
                    }
                } else {
                    st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr;
                    st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), ltype);
                    String val = st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr;
                    if (show) {
                        System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                    }
                }
            } else {  // next token not a ';', possible expression
                ResultValue resExpr = expr(false);
                int assignType = getLiteralType(resExpr.value);
                STIdentifier ident = (STIdentifier)st.getSymbol(curSymbol.tokenStr);
                ident.value = resExpr.value;
                String val = ident.value;
                ident.dataType = assignType;
                if (expr) {
                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                }
            }
        }

    }

    // assumes currentToken is after an if.
    // when returned to, the value is on an else, or endif
    public void ifStmt() throws Exception {
    	ResultValue resCond = expr(false);
    	Token endingToken;
    	if (! scan.currentToken.tokenStr.equals(":")) {
    		error("Invalid terminating token on if.\n");
    	}
    	scan.getNext(); // consume separator
    	if (resCond.value.equals("true")) {
    		// the starting if is true execute the code
    		statements(true);
    		// back and the currentToken is after the endif;
    		// or on the else
    		if (scan.currentToken.tokenStr.equals("else")) {
            	scan.getNext(); // consume else
            	if (! scan.currentToken.tokenStr.equals(":")) {
            		error("Invalid separator for else: '"
            				+ scan.currentToken.tokenStr +"'.\n");
            	}
            	scan.getNext(); // consume separator
            	// run through the false statements
            	Stack<Token> stk = new Stack<Token>();
            	while (true) {
            		if (scan.currentToken.tokenStr.equals("if")) {
            			stk.push(scan.currentToken);
            		} else if (stk.isEmpty()) {
            			if (scan.currentToken.tokenStr.equals("else")
            					|| scan.currentToken.tokenStr.equals("endif")) {
            				break;
            			}
            		} else if (! stk.isEmpty()) {
            			if (scan.currentToken.tokenStr.equals("endif")) {
            				stk.pop();
            			}
            		}
            		scan.getNext();
            	}
            	// back and the currentToken can be an else or endif
            	if (scan.currentToken.tokenStr.equals("else")) {
            		// an else is an error
            		error("Unmatched nested else statement.\n");
            	} else if (scan.currentToken.tokenStr.equals("endif")) {
            		// this is the correct scenario
            		scan.getNext(); // consume endif
            		if (! scan.currentToken.tokenStr.equals(";")) {
            			error("Invalid separator for endif: '"
            					+ scan.currentToken.tokenStr + "'.\n");
            		}
            		// there isn't anything else to execute
            	} else {
            		error("If statement not terminated by endif.\n");
            	}
    		}
    		// else the previous token was an endif;
    		// so there isn't anything else to execute
    	} else {
    		// the starting if is false
        	// run through the false statements
        	Stack<Token> stk = new Stack<Token>();
        	while (true) {
        		if (scan.currentToken.tokenStr.equals("if")) {
        			stk.push(scan.currentToken);
        		} else if (stk.isEmpty()) {
        			if (scan.currentToken.tokenStr.equals("else")
        					|| scan.currentToken.tokenStr.equals("endif")) {
        				break;
        			}
        		} else if (! stk.isEmpty()) {
        			if (scan.currentToken.tokenStr.equals("endif")) {
        				stk.pop();
        			}
        		}
        		scan.getNext();
        	}
    		endingToken = scan.currentToken;
    		scan.getNext(); // consume the else or endif
    		if (! scan.currentToken.tokenStr.equals(":")
    				&& endingToken.tokenStr.equals("else")) {
    			error("Invalid separator for else: '"
        				+ scan.currentToken.tokenStr +"'.\n");
    		} else if (! scan.currentToken.tokenStr.equals(";")
    				&& endingToken.tokenStr.equals("endif")) {
    			error("Invalid separator for endif: '"
        				+ scan.currentToken.tokenStr +"'.\n");
    		}
    		if (endingToken.tokenStr.equals("else")) {
        		scan.getNext(); // consume the separator
    			// there was an else execute those statements
    			statements(true);
    		}
    		// else there was an endif
    		// done nothing to execute
    	}

    }


    public void whileStmt() throws Exception  {
    	Scanner old = this.scan.saveState();

        scan.getNext();
        ResultValue resCond = expr(false);

        if (resCond.value.equals("true")) {
        	while (resCond.value.equals("true")) {
	        	statements(true);
	        	if (! scan.currentToken.tokenStr.equals("endwhile")) {
	        		error("While not terminated by endwhile.");
	        	}
	        	scan.getNext();
            	if (! scan.currentToken.tokenStr.equals(";")) {
            		error("endwhile is not correctly terminated.");
            	}
	        	scan = old.saveState();
	        	scan.getNext();
	        	resCond = expr(false);
        	}
        	if (resCond.value.equals("false")) {
        		Stack<Token> stk = new Stack<Token>();
            	while (true) {
            		if (scan.currentToken.tokenStr.equals("while")) {
            			stk.push(scan.currentToken);
            		} else if (stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
            			scan.getNext(); // consume endwhile
            			return;
            		} else if (! stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
            			stk.pop();
            		}
            		scan.getNext();
            	}
        	}
        } else {
        	Stack<Token> stk = new Stack<Token>();
        	while (true) {
        		if (scan.currentToken.tokenStr.equals("while")) {
        			stk.push(scan.currentToken);
        		} else if (stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
        			scan.getNext(); // consume endwhile
        			return;
        		} else if (! stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
        			stk.pop();
        		}
        		scan.getNext();
        	}
        }
    }

    public int getLiteralType (String litToken)
    {

        char[] array = litToken.toCharArray();
        int i;
        Pattern p = Pattern.compile("[^0-9.]");
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

    // assumes that this is called when currentToken = to the first operand
    // stops at the next token after the expression
    public ResultValue expr(boolean funcCall) throws Exception {
    	Stack<Token> mainStack = new Stack<Token>();
    	ArrayList<Token> postAList = new ArrayList<Token>();
    	Token tok = new Token();
    	Token popped = new Token();

    	boolean bFound;

    	tok = scan.currentToken;
    	startOfExprToken = scan.currentToken.saveToken();

    	// go through the expr and end if there isn't a token
    	// that can be in a expr
    	while (tok.primClassif == Token.OPERAND
    			|| tok.primClassif == Token.OPERATOR
    			|| tok.tokenStr.equals("(")
    			|| tok.tokenStr.equals(")")) {
    		tok.setPrecedence();
    		switch (tok.primClassif) {
    			case Token.OPERAND:
    				postAList.add(tok);
    				break;
    			case Token.OPERATOR:
    				while (! mainStack.isEmpty()) {
    					// equal to or less than operators
    					if (tok.normPreced
    							> mainStack.peek().stkPreced)
    						break;
    					popped = mainStack.pop();
    					postAList.add(popped);
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
    							postAList.add(popped);
    						}
    						if (!bFound && funcCall) {   // no matching left paren but a func has been called ie print()
    							// TODO: call errors for missing "("
    							// TODO: add function implementations here
                                while (! mainStack.isEmpty()) {
                                    popped = mainStack.pop();
                                    if (popped.tokenStr.equals("("))
                                        error("Missing ')' separator");

                                    postAList.add(popped);
                                }
                                return evaluateExpr(postAList);
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
    	} // end while loop
    	while (! mainStack.isEmpty()) {
    		popped = mainStack.pop();
    		if (popped.tokenStr.equals("("))
    			error("Missing ')' separator");

    		postAList.add(popped);
    	}
        return evaluateExpr(postAList);
    }

    // TODO: Edit error messages so they're more useful to end users.
    // TODO: create a new error method that can deal with the currentToken (Not where the Scanner is at)
    public ResultValue evaluateExpr(ArrayList list) throws ParserException {
    	Stack<Token> stk = new Stack<Token>(); // a stack that will be used to do the math

    	Token currToken = new Token(); // Iterative token for the passed ArrayList
    	Token extraToken1 = new Token(); // Extra token used to add values to the expression stack
    	Token extraToken2 = new Token(); // Extra token used to check for leftover/single stack items
    	Token tokOp1 = new Token(); // contains the left operand
    	Token tokOp2 = new Token(); // contains the right operand

    	Numeric nOp1; // contains the left operand in Numeric form
    	Numeric nOp2; // contains the right operand in Numeric form

    	ResultValue resOp1; // contains the left operand in ResultValue form
    	ResultValue resOp2; // contains the right operand in ResultValue form
    	ResultValue resMain = new ResultValue(""); // the main returned resultValue
    	ResultValue resTemp = new ResultValue(""); // the temp resultValue used for holding calculated expressions

    	// Because we're doing postfix, we need to read through the expression
    	// left to right (stacks don't have that functionality so using an ArrayList
    	// is the best solution
    	for (int i = 0; i < list.size(); i++) {
    		currToken = (Token) list.get(i); // get the current Token from the ArrayList
    		switch (currToken.primClassif) {
	    		case Token.OPERAND:
	    			// TODO: add functionality to properly accept arrays and functions
	    			stk.push(currToken); // add the operand onto the stack for future use
	    			break;
	    		case Token.OPERATOR:
	    			// since we have an operator we need to evaluate the top two operands
	    			// and because we are using a stack, the righthand operand
	    			// appears before the lefthand operand on a stack

	    			if (currToken.tokenStr.equals("u-")) { // unary minus is handled differently
	    				try {
		    				tokOp2 = (Token) stk.pop(); // grab the right operand (the only operand)
		    			} catch (EmptyStackException a) {
		    				error("Missing right expression operand.");
		    			}
	    				if (tokOp2.subClassif == Token.IDENTIFIER) {
	    					STEntry stEnt2 = st.getSymbol(tokOp2.tokenStr);
	    					if (stEnt2 == null) {
	    	                    error("Symbol '"+tokOp2+"' is not in Symbol Table.");
	    	                }
	    					resOp2 = new ResultValue(stEnt2.value);
	    					resOp2.type = ((STIdentifier)stEnt2).dataType;
	    				} else {
	    					resOp2 = new ResultValue(tokOp2.tokenStr);
	    					resOp2.type = tokOp2.subClassif;
	    				}
	    				resOp2.structure.add(Token.strSubClassifM[tokOp2.subClassif]);

	    				nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "2nd Operand"); // must be a number
	    				resTemp = Utility.negative(this, nOp2);

	    				extraToken1 = tokOp2; // get the most accurate values for the line and column # as possible
		    			extraToken1.tokenStr = resTemp.value;
		    			extraToken1.primClassif = Token.OPERAND;
		    			extraToken1.subClassif = resTemp.type;
		    			stk.push(extraToken1);

		    			resMain.structure.add(currToken.tokenStr);
		    			for (int j = 0; j < resOp2.structure.size(); j++) {
		    				resMain.structure.add(resOp2.structure.get(j));
		    			}
	    			} else {
		    			try {
		    				tokOp2 = (Token) stk.pop(); // grab the right operand
		    			} catch (EmptyStackException a) {
		    				error("Missing expression operand. For operator: " + currToken.tokenStr);
		    			}
		    			try {
		    				tokOp1 = (Token) stk.pop(); // grab the left operand
		    			} catch (EmptyStackException b) {
		    				error("Missing expression operand. For operator: " + currToken.tokenStr);
		    			}

		    			// set the ResultValue objects of the operands

		    			// check if its a variable if so, we need its real value and dataType
		    			if (tokOp1.subClassif == Token.IDENTIFIER) {
		    				STEntry stEnt1 = st.getSymbol(tokOp1.tokenStr);
		    				if (stEnt1 == null) {
	    	                    error("Symbol '"+tokOp1.tokenStr+"' is not in Symbol Table.");
	    	                }
		    				resOp1 = new ResultValue(stEnt1.value);
		    				resOp1.type = ((STIdentifier)stEnt1).dataType;
		    			} else {
			    			resOp1 = new ResultValue(tokOp1.tokenStr);
			    			resOp1.type = tokOp1.subClassif;
		    			}
		    			// we however will keep it as an identifier in our structure
		    			resOp1.structure.add(Token.strSubClassifM[tokOp1.subClassif]);

		    			// do the same for the second possible variable
		    			if (tokOp2.subClassif == Token.IDENTIFIER) {
		    				STEntry stEnt2 = st.getSymbol(tokOp2.tokenStr);
		    				if (stEnt2 == null) {
	    	                    error("Symbol '"+tokOp2.tokenStr+"' is not in Symbol Table.");
	    	                }
		    				resOp2 = new ResultValue(stEnt2.value);
		    				resOp2.type = ((STIdentifier)stEnt2).dataType;
		    			} else {
			    			resOp2 = new ResultValue(tokOp2.tokenStr);
			    			resOp2.type = tokOp2.subClassif;
		    			}
		    			resOp2.structure.add(Token.strSubClassifM[tokOp2.subClassif]);

		    			// Numerical operands have different operators and need to have
		    			// new Numeric variables associated with them as well.
		    			// thus a fork in the code is made and a path is chosen based off of
		    			// the type of the first operand.
		    			switch (currToken.tokenStr) {
			    			case "+":
				    			nOp1 = new Numeric(this, resOp1, currToken.tokenStr, "1st Operand");
				    			nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "2nd Operand");
			    				resTemp = Utility.add(this, nOp1, nOp2);
			    				break;
			    			case "-":
				    			nOp1 = new Numeric(this, resOp1, currToken.tokenStr, "1st Operand");
				    			nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "2nd Operand");
			    				resTemp = Utility.subtract(this, nOp1, nOp2);
			    				break;
			    			case "*":
				    			nOp1 = new Numeric(this, resOp1, currToken.tokenStr, "1st Operand");
				    			nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "2nd Operand");
			    				resTemp = Utility.multiply(this, nOp1, nOp2);
			    				break;
			    			case "/":
				    			nOp1 = new Numeric(this, resOp1, currToken.tokenStr, "1st Operand");
				    			nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "2nd Operand");
			    				resTemp = Utility.divide(this, nOp1, nOp2);
			    				break;
			    			case "^":
				    			nOp1 = new Numeric(this, resOp1, currToken.tokenStr, "1st Operand");
				    			nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "2nd Operand");
			    				resTemp = Utility.expo(this, nOp1, nOp2);
			    				break;
			    			case "#":
			    				resTemp = Utility.concat(this, resOp1, resOp2);
			    				break;
			    			case "<":
			    				resTemp = Utility.lessThan(this, resOp1, resOp2, false);
			    				break;
			    			case ">":
			    				resTemp = Utility.greaterThan(this, resOp1, resOp2, false);
			    				break;
			    			case "<=":
			    				resTemp = Utility.lessThan(this, resOp1, resOp2, true);
			    				break;
			    			case ">=":
			    				resTemp = Utility.greaterThan(this, resOp1, resOp2, true);
			    				break;
			    			case "==":
			    				resTemp = Utility.equals(this, resOp1, resOp2, false);
			    				break;
			    			case "!=":
			    				resTemp = Utility.equals(this, resOp1, resOp2, true);
			    			case "in":
			    				// TODO: add string subscript functionality
			    				break;
			    			case "notin":
			    				// TODO: add string subscript functionality
			    				break;
			    			case "not":
			    				resTemp = Utility.booleanConditionals(this, resOp1, resOp2, "not");
			    				break;
			    			case "and":
			    				resTemp = Utility.booleanConditionals(this, resOp1, resOp2, "and");
			    				break;
			    			case "or":
			    				resTemp = Utility.booleanConditionals(this, resOp1, resOp2, "or");
			    				break;
							default:
								//error("Invalid operator in expression.");
					            throw new ParserException(scan.currentToken.iSourceLineNr,
					                    "Invalid operator in expression: '" + currToken.tokenStr + "'",
					                    scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr]);
		    			} // end of inner Operator switch

		    			// add our new value to the stack
		    			extraToken1 = tokOp1; // get the most accurate values for the line and column # as possible
		    			extraToken1.tokenStr = resTemp.value;
		    			extraToken1.primClassif = Token.OPERAND;
		    			extraToken1.subClassif = resTemp.type;
		    			stk.push(extraToken1);

		    			// building the main structure before returning
		    			for (int j = 0; j < resOp1.structure.size(); j++) {
		        			resMain.structure.add(resOp1.structure.get(j));
		        		}
		    			resMain.structure.add(currToken.tokenStr);
		    			for (int j = 0; j < resOp2.structure.size(); j++) {
		    				resMain.structure.add(resOp2.structure.get(j));
		    			}
	    			} // end of unary if
		    		break; // end of operator case
		    		default:
		    			error("Invalid operand in expression.");
    		} // end of primClassif switch

    	} // end of ArrayList loop

    	// check if the stack is empty or if it has one item.
    	// if it has more than one item, there is a bad expression
    	// that has a missing operand or operator.
    	// if it has none then there was an error. (shouldn't happen normally)

    	if (! stk.isEmpty()) { // everything went as planned
    		if (stk.size() == 1) { // see if the stack only has one value
    			// is has only one value (which is the desired amount)

    			extraToken2 = stk.pop(); // get the last value
    			if (extraToken2.subClassif == Token.IDENTIFIER) { // test to see if it's just a variable
    				STEntry stExtra = st.getSymbol(extraToken2.tokenStr);
    				if (stExtra == null) {
	                    error("Symbol '"+extraToken2.tokenStr+"' is not in Symbol Table.");
	                }
    				resMain.value = stExtra.value;
    				resMain.type = ((STIdentifier)stExtra).dataType;
    				// since the above algorithm doesn't allow for variables to exist in the stack
    				// resMain's structure is set here for the first time.
    				resMain.structure.add(Token.strSubClassifM[extraToken2.subClassif]);
    			} else {
        			resMain.value = extraToken2.tokenStr;
        			resMain.type = extraToken2.subClassif;
    			} // end of identifier check
    		} else {
    			error("Unbalanced Expression."); // this error should have been caught by all the other checks
    		} // end of size check
    	} else {
    		error("Invalid expression. Found : ''"); // this error should have been caught by all the other checks
    	} // end of empty check

    	return resMain; // return your brand new value!
    }

    public void error(String fmt) throws ParserException {
        throw new ParserException(scan.currentToken.iSourceLineNr
                , fmt, scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr - 1]);
    }
    
    public void error(String fmt, Token tok) throws ParserException {
        throw new ParserException(tok.iSourceLineNr
                , fmt, scan.sourceFileNm, scan.lines[tok.iSourceLineNr - 1]);
    }
}
