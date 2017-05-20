package havabol;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;



public class Parser {

    public Scanner scan;
    public Debug debugger;
    public SymbolTable st;
    public ParserException error;
    public Token startOfExprToken;
    public Utility util;

    public Parser(String SourceFileNm, SymbolTable st) throws Exception {
        scan = new Scanner(SourceFileNm, debugger);
        debugger = new Debug(scan);
        this.st = st;
        util = new Utility();
    }

    public void statements(boolean bCalled) throws Exception, ParserException {
        Boolean expr = debugger.expr;


        while (true) {
            expr = debugger.expr;
            if (scan.currentToken.tokenStr.equals("do")) {
            	doWhileStmt();
            } else if (scan.currentToken.tokenStr.equals(":") && 
            			scan.nextToken.tokenStr.equals("while")) {
            	return;
            }
            else if (scan.currentToken.tokenStr.equals("print")) {
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
                                    		+ ": " + curr.tokenStr + " = "+res.value);
                                }
                                continue;
                            }
                        } else if (scan.currentToken.tokenStr.equals(",") && arglist.isEmpty()) {
                            error("Did not expect a comma. Found: "+scan.currentToken.tokenStr);
                        } else if (scan.currentToken.tokenStr.equals(",")) {  //  handle expr logic
                            scan.getNext(); // consume comma and call expr

                            Token curr = scan.currentToken.saveToken();
                            ResultValue res = expr(true);
                            arglist.add(res.value);
                            if (expr) {
                            	if (curr.subClassif == Token.IDENTIFIER) {
                                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+res.value);
                            	} else {
                            		System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": "+res.value);
                            	}
                            }
                            continue;

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
                        scan.getNext();//////
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
                } else {
                    error("Invalid print statement");
                }

            }
            else if (scan.currentToken.tokenStr.equals("debug")) {
                scan.getNext();
                String type = scan.currentToken.tokenStr; // got type
                scan.getNext();
                String state = scan.currentToken.tokenStr;  // got state; // 'on' or 'off'
                if (state.equals("on")) {
                    debugger.turnOn(type);
                } else if (state.equals("off")) {
                    debugger.turnOff(type);
                } else {
                    error("Invalid debug state. Found: "+scan.currentToken.tokenStr);
                }
                scan.getNext(); // consume semicolon
                if (scan.currentToken.equals(";")) {
                    error("Expected ';'. Found: "+scan.currentToken.tokenStr);
                }
            // if we come across a declare statement. i.e. Int, String, Bool ...
            } else if (scan.currentToken.subClassif == Token.DECLARE) {
                // if nextToken is not an identifier throw an error
                if (scan.nextToken.subClassif != Token.IDENTIFIER) {
                    error("Identifier expected after declare statement. " +
                        "Found: '"+scan.nextToken.tokenStr+"' of type "
                        +scan.currentToken.strPrimClassifM[scan.nextToken.primClassif]);
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
                    scan.getNext(); // get var ... i.e. Int var

                    //non array logic
                    Token tokk = scan.currentToken; //
                    if (!scan.nextToken.tokenStr.equals("[")) { // put in symbol table if not an array declaration
                        st.putSymbol(tokk.tokenStr, new STIdentifier(tokk.tokenStr,
                                    tokk.primClassif, tokk.subClassif));
                        st.getSymbol(tokk.tokenStr).type = type;
                        if (scan.nextToken.tokenStr.equals(";")) {
                            scan.getNext();
                            continue;
                        } else if (scan.nextToken.tokenStr.equals("=")) {
                            assign(scan.currentToken);
                            if (scan.currentToken.tokenStr.equals(","))
                                error("Malformed array declaration"+scan.nextToken.tokenStr);
                            else if (!scan.currentToken.tokenStr.equals(";")) {
                                errorNoTerm("Assign statement not terminated. Expected ';'");
                            }

                        } else {
                            error("Invalid non-array assignment. Token '=' or ';' expected. Found '"+scan.nextToken.tokenStr+"'");
                        }

                    }

                    if (scan.nextToken.tokenStr.equals("[")) {   //start of array logic
                        scan.getNext(); // got '['
                        scan.getNext(); // got size of array or ']'
                        if (scan.currentToken.tokenStr.equals("]")) {
                            STIdentifier array = new STIdentifier(this,
                                tokk.tokenStr, tokk.primClassif, tokk.subClassif, -100, type);
                            st.putArray(tokk.tokenStr, array);
                            if (scan.nextToken.tokenStr.equals(";"))
                                error("Malformed array declaration");
                        } else {  // next token is NOT ']', its either a literal or a variable
                            try {
                                if (scan.currentToken.tokenStr.equals("unbound")) {
                                    STIdentifier array = new STIdentifier(this,
                                        tokk.tokenStr, tokk.primClassif, tokk.subClassif, -1, type);
                                    st.putArray(tokk.tokenStr, array);
                                    scan.getNext(); // get the ']'
                                } else { //
                                    ResultValue result = expr(true);
                                    int size = Integer.parseInt(result.value);  // check if its value is an int or not
                                    if (size == 0)
                                        error("Cannot declare an array of size 0. Must be of size 1 or greater");
                                    STIdentifier array = new STIdentifier(this,
                                        tokk.tokenStr, tokk.primClassif, tokk.subClassif, size, type);
                                    st.putArray(tokk.tokenStr, array);

                                }
                            } catch (NumberFormatException e) { // if a float or other we get here
                                error("'"+this.startOfExprToken.tokenStr+"' is not a valid size parameter");
                            }
                        }

                        if (scan.nextToken.tokenStr.equals("="))
                            declareArray(tokk);
                        else if (scan.nextToken.tokenStr.equals(";"))
                            scan.getNext();
                    }
                }
            } else if (scan.currentToken.subClassif == Token.IDENTIFIER) { // if token is an identifier
                //check if curToken is in symbol table, if not throw an error
                STIdentifier entry = (STIdentifier)st.getSymbol(scan.currentToken.tokenStr);
                if (entry == null) {
                    error("Symbol '"+scan.currentToken.tokenStr+"' is not in Symbol Table.");
                }
                if (scan.nextToken.tokenStr.equals("=")) {  // default or array copy.  ie  array = 10; array1 = array2;
                    if (entry.structure == STIdentifier.ARRAY) {
                        copyOrDefaultArray(entry);
                    } else {
                        assign(scan.currentToken);
                        continue;
                    }
                } else if (scan.nextToken.tokenStr.equals("[")) { // array logic!!!!!!!!!!!!!!!!!!!!!!!
                    scan.getNext(); // get "["
                    scan.getNext();  // get

                    if (entry.structure == STIdentifier.SCALAR) {
                        ResultValue resVal = expr(true); // expr ends on right bracket
                        int ind = Integer.parseInt(resVal.value);
                        scan.getNext(); // get '='
                        scan.getNext(); // get str token
                        Token item = scan.currentToken;
                        //scan.getNext();

                        if (!scan.nextToken.tokenStr.equals(";")) {
                            errorNoTerm("Statement is not terminated. Expected ';'");
                        }
                        if (item.subClassif != Token.STRING) {
                            error(item.tokenStr+" must be of type STRING");
                        }
                        entry.setValue(ind, item.tokenStr);

                    } else if (entry.structure == STIdentifier.ARRAY) { // array logic
                        ResultValue uInd = null;
                        if (scan.currentToken.subClassif != Token.IDENTIFIER) {
                            if (scan.currentToken.subClassif != Token.INTEGER && !scan.currentToken.tokenStr.equals("u-")) {
                                error("Malformed array declaration");
                            }
                        }
                        ResultValue ind  = expr(true);  // pass in true to expr if using an array or inside a func; and expr get me the index if its an expression or not
                        scan.getNext(); // got '='
                        if (!scan.currentToken.tokenStr.equals("="))
                            error("Invalid array statement. Expected '='");
                        scan.getNext(); // got token
                        ResultValue resVal = expr(true);
                        entry.array.set(Integer.parseInt(ind.value), resVal, "");
                    }
                } else {
                    error("Invalid assignment syntax");
                }
            } else if (scan.currentToken.tokenStr.equals("endif")) {
                if (!bCalled) {
                    error("Endif is missing corresponding if.");
                }
                return;
            } else if (scan.currentToken.tokenStr.equals("else")) {
                if (!bCalled) {
                    error("Else is missing corresponding if.");
                }
                return;
            } else if (scan.currentToken.tokenStr.toLowerCase().equals("if")) {
                ifStmt();

            } else if (scan.currentToken.tokenStr.toLowerCase().equals("while")) {
                whileStmt();
            } else if (scan.currentToken.tokenStr.equals("endwhile")) {
                if (!bCalled) {
                    error("Unmatched endwhile.");
                }
                return;
            } else if (scan.currentToken.tokenStr.equals("for")) {
                forStmt();
            } else if (scan.currentToken.tokenStr.equals("endfor")) {
            	return;
            } 
            

            scan.getNext();
            if (scan.nextToken.primClassif == Token.EOF) {
                break;
            }
        }
    }

    @Override
	public String toString() {
		return "Parser [scan=" + scan + "]";
	}

    public void copyOrDefaultArray(STIdentifier lEntry) throws Exception {
        String token = scan.currentToken.tokenStr;
        token = "'"+token+"'";
        scan.getNext(); // got "="
        scan.getNext(); // got first val after
        if (scan.currentToken.subClassif == Token.IDENTIFIER) {
            STIdentifier rEntry = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
            if (rEntry == null) {
                error("Not in symbol table");
            } else {
                if (rEntry.structure == STIdentifier.ARRAY &&
                    !scan.nextToken.tokenStr.equals("[")) {
                        lEntry.array.copy(rEntry.array, rEntry.symbol);
                        return;
                }
            }
        }
        ResultValue resVal = null;
        // default case logic
        if (scan.currentToken.subClassif == Token.STRING) {
        	resVal = new ResultValue(scan.currentToken.tokenStr);
        	resVal.type = Token.STRING;
        	resVal.structure.add("STRING");
        }
        else {
        	resVal = expr(false);
        }

        lEntry.array.defaultArray(resVal, token);
    }

    public void declareArray(Token curSymbol) throws Exception { //, int index, boolean single) {
        scan.getNext(); // currentToken is now "="
        scan.getNext(); // currentToken is now some val
        STIdentifier entry = (STIdentifier) st.getSymbol(curSymbol.tokenStr);
        int index = 0;
        while (true) {
            if (scan.currentToken.tokenStr.equals(";")) {
                break;
            } else if (scan.currentToken.tokenStr.equals(",")) {
                scan.getNext();
            } else if (scan.currentToken.primClassif == Token.EOF) {
                error("EOF reached");
            } else if (scan.currentToken.primClassif == Token.OPERAND) {
                ResultValue resVal = expr(false);
                if (entry.array.sizeUnknown) {
                    entry.array.add(index, resVal, curSymbol.tokenStr);
                } else {
                    entry.array.set(index, resVal, curSymbol.tokenStr);
                }
                index++;
            } else if (scan.currentToken.subClassif == Token.DECLARE) {
                errorNoTerm("Array declaration not terminated. Expected ';'");
            } else {
                error("Malformed array declaration");
            }
        }
    }

    public void assign(Token curSymbol) throws Exception {  // current token is 'a'   a = val
        Boolean show = debugger.assign;
        Boolean expr = debugger.expr;
        String value = "";
        Token curr = curSymbol.saveToken();
        STIdentifier entryL = (STIdentifier)st.getSymbol(curr.tokenStr);
        int ltype = st.getSymbol(curSymbol.tokenStr).type;  // get type of left op
        scan.getNext(); // get equals sign
        scan.getNext(); // get val (right op), could possibly be unary minus
        Token rToken = scan.currentToken;
        ResultValue resExpr = expr(false);
        int assignType = resExpr.type;
        if (ltype != assignType) {
            if (ltype == Token.BOOLEAN) {
                if (assignType == Token.STRING) {
                    if (resExpr.value.equals("T") || resExpr.value.equals("F")) {
                        st.getSymbol(curr.tokenStr).value = resExpr.value;
                        if (expr) {
                            System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                            + ": " + curr.tokenStr + " = "+resExpr.value);
                        }
                    } else {
                        error("Found '"+resExpr.value+"'. STRING must be either 'T' or 'F'");
                    }
                } else {
                    error("Incompatible types. Cannot assign "+
                        Token.strSubClassifM[assignType]+
                        " to "+Token.strSubClassifM[ltype]+
                        " when STRING is a not a 'T' or 'F'");
                }
            } else {
                if (ltype == Token.STRING && assignType == Token.BOOLEAN) {
                    st.getSymbol(curr.tokenStr).value = resExpr.value;
                    return;
                }
                Numeric num = new Numeric(this, resExpr, "=", "'"+resExpr.value+"'");
                if (ltype == Token.INTEGER) {
                    resExpr.value = String.valueOf(num.integerValue);
                } else if (ltype == Token.FLOAT) {
                    resExpr.value = String.valueOf(num.doubleValue);
                    String last;
                    last = resExpr.value.substring(resExpr.value.length() - 2, resExpr.value.length());
                    if (last.charAt(0) == '.')
                        resExpr.value += "0";
                } else if (ltype == Token.STRING) {
                    if (assignType == Token.BOOLEAN) {

                    } else {
                        resExpr.value = num.strValue;
                    }
                }
                value = resExpr.value;
                st.getSymbol(curr.tokenStr).value = value;
                String val2 = value;

                if (expr) {
                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    + ": " + curr.tokenStr + " = "+val2);
                }
            }
        } else {
            if (ltype == Token.INTEGER || ltype == Token.FLOAT) {
                Numeric num = new Numeric(this, resExpr, "=", "'"+resExpr.value+"'");
                if (ltype == Token.INTEGER) {
                    st.getSymbol(curr.tokenStr).value = Integer.toString(num.integerValue);
                } else {
                    String last;
                    last = num.strValue.substring(num.strValue.length() - 2, num.strValue.length());
                    if (last.charAt(0) == '.')
                        num.strValue += "0";
                    st.getSymbol(curr.tokenStr).value = num.strValue;
                }
            } else  {
                st.getSymbol(curr.tokenStr).value = resExpr.value;
            }
            if (expr) {
                System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    + ": " + curr.tokenStr + " = "+resExpr.value);
            }
        }
    }

    // assumes currentToken is on an if.
    public void ifStmt() throws Exception {
    	// just in case
    	//Scanner savedScanner = this.scan.saveState();
    	ResultValue resVal;
    	Token beginningIf = scan.currentToken;
    	scan.getNext();
    	boolean flag;
    	// currentToken is the start of the expression
    	if (scan.nextToken.tokenStr.equals("LENGTH") ||
    			scan.nextToken.tokenStr.equals("ELEM") ||
    			scan.nextToken.tokenStr.equals("MAXELEM")) {
    		resVal = expr(true);
    	}
    	else {
    		resVal = expr(false);
    	}
    	
    	// currentToken is a colon ":"
    	if (!scan.currentToken.tokenStr.equals(":")) {
    		error("Missing terminating colon \":\" ", beginningIf);
    	}
    	if (resVal.value.equals("T")) {
    		flag = true;
    		statements(true);
    		
    		if (scan.currentToken.tokenStr.equals("else")) {
        		skipIf(true);
    		}
    	}
    	else {
    		flag = false;
    		// if cond was false. skip to else or endif, whichever comes first
    		// currentToken is a colon
    		if (!scan.currentToken.tokenStr.equals(":")) {
        		error("Missing terminating colon \":\" ", beginningIf);
    		}
    		//Stack<Token> stk = new Stack<Token>();
    		skipIf(false);
    		if (!scan.currentToken.tokenStr.equals(":")) {
        		error("Missing terminating colon \":\" ", beginningIf);
    		}
    		statements(true);
    	}
    	
    	scan.getNext();
    	if (!scan.currentToken.tokenStr.equals(";")) {
    		error("Missing terminating semicolon \";\" after \"endif\"");
    	}
    }
    
    public void skipIf(boolean flag) throws Exception {
    	int ifCount = 1;
    	int elseCount;
    	int endifCount = 0;
    	scan.getNext();
    	if (flag == true) { // if the condition was true we want to skip to the endif
    		elseCount = -100000;
    	}
    	else { // if the condition was false we want to skip to the else
    		elseCount = 0;
    	}
    	while (ifCount != endifCount && elseCount != ifCount) {
    		if (scan.currentToken.tokenStr.equals("if")) {
    			ifCount++;
    		}
    		else if (scan.currentToken.tokenStr.equals("endif")) {
    			//ifCount--;
    			endifCount++;
    		}
    		else if (scan.currentToken.tokenStr.equals("else")) {
    			elseCount++;
    		}
    		if (ifCount != endifCount) {
    			scan.getNext();
    		}
    	}
    }

    public void whileStmt() throws Exception  {
    	// currentToken is "while"
    	Token beginningWhile = scan.currentToken;
    	scan.getNext();
    	// on beginning of condition/expr
    	Scanner savedScanner = this.scan.saveState();
    	boolean isFunc;
    	ResultValue resVal;
    	if (scan.currentToken.tokenStr.equals("LENGTH") ||
    			scan.currentToken.tokenStr.equals("ELEM") ||
    			scan.currentToken.tokenStr.equals("MAXELEM")) {
    		isFunc = true;
    		resVal = expr(isFunc);
    	}
    	else {
    		isFunc = false;
    		resVal = expr(isFunc);
    	}
    	// on the colon ":"
    	if (!scan.currentToken.tokenStr.equals(":")) {
    		error("Missing terminating colon \":\" after while loop condition", beginningWhile);
    	}
    	while (resVal.value.equals("T")) {
    		statements(true);
    		if (!scan.nextToken.tokenStr.equals(";")) {
    			error("Missing terminating \";\" after \"endwhile\"", beginningWhile);
    		}
    		this.scan = savedScanner;
    		savedScanner = this.scan.saveState();
    		resVal = expr(isFunc);
    	}
    	int whileCount = 1;
    	while (whileCount != 0 && scan.currentToken.primClassif != Token.EOF) {
    		if (scan.currentToken.tokenStr.equals("while")) {
    			whileCount++;
    		}
    		else if (scan.currentToken.tokenStr.equals("endwhile")) {
    			whileCount--;
    		}
    		scan.getNext();
    	}
    	if (scan.currentToken.primClassif == Token.EOF) {
    		error("Missing terminating \"endwhile\" after while loop", beginningWhile);
    	}
    	else if (!scan.currentToken.tokenStr.equals(";")) {
    		error("Missing terminating \";\" after \"endwhile\"", beginningWhile);
    	}
    }
    
    public void doWhileStmt() throws Exception {
    	// currentToken is "do"
    	Token beginningDo = scan.currentToken;
    	scan.getNext();
    	// on colon ":"
    	if (!scan.currentToken.tokenStr.equals(":")) {
    		error("Missing terminating colon \":\" after \"do\" keyword");
    	}
    	Scanner savedScanner = this.scan.saveState();
    	scan.getNext();
    	ResultValue resVal = new ResultValue("T");
    	boolean done = false;
		boolean isFunc = false;;
    	while (resVal.value.equals("T")) {
    		statements(true);
    		if (done == false) {
    			if (!scan.currentToken.tokenStr.equals(":") || 
    					!scan.nextToken.tokenStr.equals("while")) {
    				error("Missing terminating colon \":\" or \"while\" keyword after do while loop", beginningDo);
    			}
    			scan.getNext();
    			scan.getNext();
    			if (scan.currentToken.tokenStr.equals("LENGTH") ||
    					scan.currentToken.tokenStr.equals("ELEM") ||
    					scan.currentToken.tokenStr.equals("MAXELEM")) {
    				isFunc = true;
    			}
    		}
    		resVal = expr(isFunc);
    		if (!scan.currentToken.tokenStr.equals(";")) {
    			error("Missing terminating \";\" after do while loop condition", beginningDo);
    		}
    		this.scan = savedScanner;
    		savedScanner = this.scan.saveState();
    		
    	}
    	int doCount = 1;
    	while (doCount != 0) {
    		if (scan.currentToken.tokenStr.equals("do")) {
    			doCount++;
    		}
    		else if (scan.currentToken.tokenStr.equals("while")) {
    			doCount--;
    		}
    		scan.getNext();
    	}
    	Thread.sleep(100);
    	resVal = expr(false);
    }

    public void forStmt() throws Exception {
        // save the scanner state to revert back to original when done
        Scanner savedScanner = this.scan.saveState();
        Token startToken = scan.currentToken.saveToken();
        String temp = scan.currentToken.tokenStr + " " + scan.nextToken.tokenStr + " " + scan.getNext();
        int controlVar = 0;
        STIdentifier controlIdent;
        STIdentifier setIdent;
    	ArrayList<String> setList = new ArrayList<String>();
    	String delim = "";
        int i, start = 0, end = 0;
        STIdentifier endIdent;
        STIdentifier incrIdent;
        ResultValue resVal = new ResultValue("");
        // default incr variable is 1 if there is none provided
        int incr = 1;
        // restore the state so we can use getNext()
        this.scan = savedScanner;
        Numeric num;
        Token beginningFor = scan.currentToken;
        startOfExprToken = beginningFor;
        scan.getNext();
        controlIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
        STIdentifier startIdent = null;
        if (scan.nextToken.tokenStr.equals("=")) {
            if (controlIdent == null) {
            	controlIdent = new STIdentifier(scan.currentToken.tokenStr, 1, 1);
            	controlIdent.value = String.valueOf(0);
            	st.putSymbol(controlIdent.symbol, controlIdent);
            	st.getSymbol(controlIdent.symbol).value = String.valueOf(0);
            	st.getSymbol(controlIdent.symbol).type = 2;
            }
        	if (scan.currentToken.subClassif != 1) {
        		error(scan.currentToken.tokenStr + " must be an identifier");
	        }
	        savedScanner = this.scan.saveState();
	        scan.getNext();
	        scan.getNext();

	        if (scan.currentToken.subClassif != 1 &&
	        		scan.currentToken.subClassif != 2) {
	        	if (!scan.currentToken.tokenStr.equals("ELEM") &&
	        			!scan.currentToken.tokenStr.equals("MAXELEM") &&
	        			!scan.currentToken.tokenStr.equals("LENGTH")) {
	        		error("\"" + scan.currentToken.tokenStr + "\"" +
	        			" must be an integer");
	        	}
	        }
	        resVal = expr(false);
	        if (resVal.value == null) {
	        	error("\"" + controlIdent.symbol + "\"" + " has not been initialized");
	        }
	        st.getSymbol(controlIdent.symbol).value = String.valueOf(resVal.value);
	        num = new Numeric(this, resVal, "=", "RIGHT OPERAND");
	        //controlVar = Integer.parseInt(controlIdent.value);
	        controlVar = num.integerValue;
	        if (!scan.currentToken.tokenStr.equals("to")) {
	        	error(scan.currentToken.tokenStr + " is not a valid for loop token");
	        }
	        scan.getNext();
	        // on ending value
	        if (scan.currentToken.subClassif == 1) {
	        	endIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
	        	if (endIdent == null) {
	        		error("\"" + scan.currentToken.tokenStr + "\"" + " is undeclared");
	        	}
	        	resVal = expr(false);
	        	if (resVal.value == null) {
	        		error("\"" + endIdent.symbol + "\"" + " is uninitialized");
	        	}
	        	if (resVal.value.charAt(0) == '[') {
	        		error("\"" + resVal.value + "\"" + " array must be indexed");
	        	}
	        	num = new Numeric(this, resVal, "limit", "OPERAND");
	        	end = num.integerValue;
	        }
	        else if (scan.currentToken.subClassif == 2) {
	        	end = Integer.parseInt(scan.currentToken.tokenStr);
	        }
	        else if (scan.currentToken.tokenStr.equals("ELEM") ||
	        		scan.currentToken.tokenStr.equals("MAXELEM") ||
	        		scan.currentToken.tokenStr.equals("LENGTH")) {
	        	resVal = expr(true);
		        num = new Numeric(this, resVal, "=", "RIGHT OPERAND");
	        	end = num.integerValue;
	        }
	        else {
	        	error(scan.currentToken.tokenStr + " must be an integer");
	        }
	        if (!scan.currentToken.tokenStr.equals(":") && !scan.currentToken.tokenStr.equals("by")) {
	        	scan.getNext();
	        }
	        // on ":" or "by"
	        if (scan.currentToken.tokenStr.equals("by")) {
	        	scan.getNext();
	        if (scan.currentToken.subClassif == 1) {
	        	incrIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
	        	if (incrIdent == null) {
	        		error("\"" + scan.currentToken.tokenStr + "\"" + " is undeclared");
	        	}
	        	resVal = expr(false);

	        	if (resVal.value == null) {
	        		error("\"" + incrIdent.symbol + "\"" + " is uninitialized");
	        	}
	        	if (resVal.value.charAt(0) == '[') {
	        		error("\"" + resVal.value + "\"" + " array must be indexed");
	        	}
	        	num = new Numeric(this, resVal, "increment", "OPERAND");
	        	incr = num.integerValue;
	        }
	        else if (scan.currentToken.subClassif == 2) {
	        	incr = Integer.parseInt(scan.currentToken.tokenStr);
	        }
	        else if (scan.currentToken.tokenStr.equals("ELEM") ||
	        		scan.currentToken.tokenStr.equals("MAXELEM") ||
	        		scan.currentToken.tokenStr.equals("LENGTH")) {
	        	resVal = expr(true);
	        	incr = Integer.parseInt(resVal.value);
	        }
	        else {
	        	error(scan.currentToken.tokenStr + " must be an integer");
	        }
	        if (!scan.currentToken.tokenStr.equals(":")) {
	        	scan.getNext();
	        }
	       }
	       if (!scan.currentToken.tokenStr.equals(":")) {
	    	   if (scan.currentToken.primClassif == 1) {
	    		   error("Missing " + "\"" + "by" + "\"" + " keyword in for loop");
	    	   }
	    	   else {
	    		   error("Missing terminating " + "\"" + ":" + "\"" + " in for loop", beginningFor);
	    	   }
	       }
	       while (controlVar < end) {
	    	   savedScanner = this.scan.saveState();
	           statements(true);
	           controlVar += incr;
	           st.getSymbol(controlIdent.symbol).value = String.valueOf(controlVar);
		       if (!scan.currentToken.tokenStr.equals("endfor") || !scan.nextToken.tokenStr.equals(";")) {
		    	   if (!scan.currentToken.tokenStr.equals("endfor")) {
		    		   error("Missing terminating " + "\"" + "endfor" + "\"" + " after for loop", beginningFor);
		    	   }
		    	   else {
		    		   error("Missing terminating " + "\"" + ";" + " after " + "\"" + "endfor" + "\"", beginningFor);
		    	   }
		       }

	           if (controlVar < end) {
	        	   this.scan = savedScanner;
	           }
	        }

	       if (!scan.currentToken.equals("endfor")) {
	    	   Stack<Token> stk = new Stack<Token>();
	    	   Token currentFor = beginningFor;
	    	   while (true) {
	 	    	   if (scan.currentToken.tokenStr.equals("for")) {
	 	    		   currentFor = scan.currentToken;
		    		   stk.push(scan.currentToken);
		    	   }
	 	    	   else if (scan.currentToken.tokenStr.equals("endfor")) {
	 	    		   if (!stk.isEmpty()) {
	 	    			   stk.pop();
	 	    			   if (stk.isEmpty()) {
	 	    				   currentFor = beginningFor;
	 	    			   }
	 	    			   else {
	 	    				   currentFor = stk.peek();
	 	    			   }
	 	    		   }
	 	    		   else {
	 	    			   break;
	 	    		   }
	 	    	   }
	 	    	   else if (scan.currentToken.primClassif == Token.EOF) {
	 	    		   error("Missing " + "\"" + "endfor" + "\"" + " after for loop", currentFor);
	 	    	   }
	 	    	   else {
	 	    		   scan.getNext();
	 	    	   }
	    	   }
	       }
 	       if (!scan.nextToken.tokenStr.equals(";")) {
 	    	   error("Missing " + "\"" + ";" + "\"" + " after endfor");
 	       }
        }
        else if (scan.nextToken.tokenStr.equals("in") || scan.nextToken.tokenStr.equals("from")) {
        	if (scan.currentToken.subClassif != 1) {
        		error(scan.currentToken.tokenStr + " must be an identifier");
        	}
        	String setStr = "";
        	controlIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
        	if (controlIdent == null) {
        		controlIdent = new STIdentifier(scan.currentToken.tokenStr, 1, 1);
        		st.putSymbol(controlIdent.symbol, controlIdent);
        		st.getSymbol(controlIdent.symbol).value = String.valueOf(0);
        		if (scan.nextToken.tokenStr.equals("from")) {
        			controlIdent.type = 5;
        			st.getSymbol(controlIdent.symbol).type = 5;
        		}
        	}
        	if (scan.nextToken.tokenStr.equals("from")) {
        		if (controlIdent.type != 5) {
        			error("\"" + scan.currentToken.tokenStr + "\"" + " must be a string identifier");
        		}
        	}
        	scan.getNext();
        	boolean fromFlag = false;
        	if (scan.currentToken.tokenStr.equals("from")) {
        		fromFlag = true;
        	}
        	scan.getNext();
        	// on array/set string
        	setIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
        	int setType = 0;
        	if (scan.currentToken.subClassif == 1) {
        		if (setIdent == null) {
        			error("\"" + scan.currentToken.tokenStr + "\"" + " is undeclared");
        		}
        		setType = setIdent.type;
        		resVal = expr(false);
        		if (resVal.value == null) {
        			error("\"" + setIdent.symbol + "\"" + " is uninitialized");
        		}
        		if (fromFlag == true) {
        			if (setIdent.type != 5) {
        				error("\"" + setIdent.symbol + "\"" + " must be a string");
        			}
        		}
        		if (resVal.structure.get(0).matches("ARRAY")) {
                	setType = setIdent.array.type;
                	if (setType != controlIdent.type) {
                		if (controlIdent.type == 4) {
                			if (setType != 5) {
                				error("\"" + controlIdent.symbol + "\"" + " must be of type string or boolean");
                			}
                		}
                		if (setType == 4) {
                			if (controlIdent.type != 5) {
                				error("\"" + scan.currentToken.tokenStr + "\"" + " must be of type string or boolean");
                			}
                		}
                	}
        			resVal.value = resVal.value.substring(1, resVal.value.length()-1);
        			setStr = resVal.value;
        			String[] items = resVal.value.split(",");
        			for (i = 0; i < items.length; i++) {
        				if (!items[i].equals("null")) {
        					items[i] = items[i].trim();
        					setList.add(items[i]);
        				}
        			}
        		}
        	}
        	if (resVal.value.equals("") || !resVal.structure.get(0).matches("ARRAY")) {
        		if (fromFlag == true) {
        			if (scan.currentToken.subClassif != 5 && !scan.currentToken.tokenStr.equals("by")) {
        				error("\"" + scan.currentToken.tokenStr + "\"" + " must be a string");
        			}
            		st.getSymbol(controlIdent.symbol).type = 5;
        		}
        		// if its a string literal or string identifier
        		// convert the value to the setList
        		char [] chArray;
        		if (resVal.value.equals("")) {
        			// if resVal.value is empty then its a string literal
        			chArray = scan.currentToken.tokenStr.toCharArray();
        			setStr = scan.currentToken.tokenStr;
        			scan.getNext();
        		}
        		else {
        			chArray = resVal.value.toCharArray();
        			setStr = resVal.value;
        			setType = 5;
        		}
        		for (i = 0; i < chArray.length; i++) {
        			setList.add(String.valueOf(chArray[i]));
        		}

        	}
        	// now we have the value of the array/set string stored in resVal
        	// should be on ":" or "by"
        	if (scan.currentToken.tokenStr.equals("by")) {
        		scan.getNext();
    			delim = scan.currentToken.tokenStr;
        		if (scan.currentToken.subClassif == 5) {
        			scan.getNext();
        		}
        		else if (scan.currentToken.subClassif == 1) {
        			if (fromFlag == true) {
        				if (st.getSymbol(scan.currentToken.tokenStr) == null) {
        					error("\"" + scan.currentToken.tokenStr + "\"" + " is undeclared");
        				}
        				if (st.getSymbol(scan.currentToken.tokenStr).type != 5) {
        					error("\"" + scan.currentToken.tokenStr + "\"" + " must be a string");
        				}
        			}
        			resVal = expr(false);
        			if (resVal.value == null) {
        				error("\"" + delim + "\"" + " is uninitialized");
        			}
        			if (resVal.value == null) {

        			}
        			delim = resVal.value;
        		}
        		else {
        			error("\"" + scan.currentToken.tokenStr + "\"" + " must be a string");
        		}
        		String[] items = setStr.split(delim);
        		setList.clear();
        		for (i = 0; i < items.length; i++) {
        			setList.add(items[i]);
        		}
        	}
        	// should be on ":"
        	if (!scan.currentToken.tokenStr.equals(":")) {
        		errorNoTerm("Missing terminating " + "\"" + ":" + "\"" + " in for loop");
        	}
        	// if it was an undeclared identifier
        	if (controlIdent.type == 7) {
        		controlIdent.type = setType;
        		st.getSymbol(controlIdent.symbol).type = setType;
        	}
        	for (i = 0; i < setList.size(); i++) {
        		savedScanner = this.scan.saveState();
        		if (controlIdent.type != setType) {
	        		if (controlIdent.type == 4) {
	        			if (!setList.get(i).equals("T") && !setList.get(i).equals("F")) {
	        				error("\"" + setList.get(i) + "\"" + " must be T or F");
	        			}
	        		}
	        		else {
	        			if (controlIdent.type == 5) {
	        				st.getSymbol(controlIdent.symbol).value = setList.get(i);
	        			}
	        			else {
		        			resVal = new ResultValue(String.valueOf(setList.get(i)));
		        			num = new Numeric(this, resVal, "iterated variable", "index " + String.valueOf(i));
		        			if (controlIdent.type == 2) {
		        				st.getSymbol(controlIdent.symbol).value = String.valueOf(num.integerValue);
		        			}
		        			else if (controlIdent.type == 3) {
		        				st.getSymbol(controlIdent.symbol).value = String.valueOf(num.doubleValue);
		        			}
	        			}
	        		}
        		}
        		else {
        			st.getSymbol(controlIdent.symbol).value = setList.get(i);
        		}
        		statements(true);
 		        if (!scan.currentToken.tokenStr.equals("endfor") || !scan.nextToken.tokenStr.equals(";")) {
		    	   if (!scan.currentToken.tokenStr.equals("endfor")) {
		    		   error("Missing terminating " + "\"" + "endfor" + "\"" + " after for loop", beginningFor);
		    	   }
		    	   else {
		    		   error("Missing terminating " + "\"" + ";" + " after " + "\"" + "endfor" + "\"", beginningFor);
		    	   }
		        }
        		if (i+1 < setList.size()) {
        			this.scan = savedScanner;
        		}
        	}
 	       if (!scan.currentToken.equals("endfor")) {
	    	   Stack<Token> stk = new Stack<Token>();
	    	   Token currentFor = beginningFor;
	    	   while (true) {
	 	    	   if (scan.currentToken.tokenStr.equals("for")) {
	 	    		   currentFor = scan.currentToken;
		    		   stk.push(scan.currentToken);
		    	   }
	 	    	   else if (scan.currentToken.tokenStr.equals("endfor")) {
	 	    		   if (!stk.isEmpty()) {
	 	    			   stk.pop();
	 	    			   if (stk.isEmpty()) {
	 	    				   currentFor = beginningFor;
	 	    			   }
	 	    			   else {
	 	    				   currentFor = stk.peek();
	 	    			   }
	 	    		   }
	 	    		   else {
	 	    			   break;
	 	    		   }
	 	    	   }
	 	    	   else if (scan.currentToken.primClassif == Token.EOF) {
	 	    		   error("Missing " + "\"" + "endfor" + "\"" + " after for loop", currentFor);
	 	    	   }
	 	    	   else {
	 	    		   scan.getNext();
	 	    	   }
	    	   }
	       }
 	       if (!scan.nextToken.tokenStr.equals(";")) {
 	    	   error("Missing " + "\"" + ";" + "\"" + " after endfor");
 	       }
        }
        else {
        	error(scan.nextToken.tokenStr + " is not a valid for loop token");
        }
        if (scan.currentToken.tokenStr.equals("for")) {
        	error("Missing endfor at the end of for loop");
        }
        scan.getNext();
    }

    // assumes that this is called when currentToken = to the first operand
    // stops at the next token after the expression
    public ResultValue expr(boolean funcCall) throws Exception {
        Stack<Token> mainStack = new Stack<Token>();
        ArrayList<Token> postAList = new ArrayList<Token>();
        Token tok = new Token();
        Token popped = new Token();

        boolean bFound, bRefFound;

        tok = scan.currentToken;
        startOfExprToken = scan.currentToken.saveToken();

    	// go through the expr and end if there isn't a token
    	// that can be in a expr
    	while ((! tok.tokenStr.equals("print") && ! tok.tokenStr.equals("=")) &&
    			(tok.primClassif == Token.OPERAND
    			|| tok.primClassif == Token.OPERATOR
    			|| tok.primClassif == Token.FUNCTION
    			|| tok.tokenStr.equals("(")
    			|| tok.tokenStr.equals(")")
    			|| tok.tokenStr.equals("]"))) {
    		tok.setPrecedence();
    		switch (tok.primClassif) {
    			case Token.OPERAND:
    				if (tok.tokenStr.equals("to") || (tok.tokenStr.equals("by") && tok.subClassif != Token.STRING)) {
    					while (! mainStack.isEmpty()) {
    						popped = mainStack.pop();
	    		    		if (popped.tokenStr.equals("("))
	    		    			error("Missing ')' separator", popped);
	    		    		if (popped.isElemRef)
	    		    			error("Missing ']' separator", popped);
	    		    		if (popped.primClassif == Token.FUNCTION)
	    		    			error("Missing ')' separator", popped);
	    		    		postAList.add(popped);
    					}
    			        return evaluateExpr(postAList);
    				}
    				try {
    					if (((STIdentifier) st.getSymbol(tok.tokenStr)).structure
    							== STIdentifier.ARRAY) {
    						tok.isArray = true;
    						tok.setPrecedence();
    						mainStack.push(tok);
    						if (scan.nextToken.tokenStr.equals("[")) {
    							scan.getNext(); // consume the '[', we don't want it in the list
    							tok.isElemRef = true; // denote that this is an array element reference
    						}
    					} else {
    						// operand is a Scalar Identifier
    						// test for string reference
    						if (((STIdentifier) st.getSymbol(tok.tokenStr)).type == Token.STRING) {
    							if (scan.nextToken.tokenStr.equals("[")) {
        				    		tok.normPreced = 16;
        				    		tok.stkPreced = 0;
        				    		mainStack.push(tok);
        							scan.getNext(); // consume the '[', we don't want it in the list
        							tok.isElemRef = true; // denote that this is an array element reference
        						} else {
        							postAList.add(tok);
        						}
    						} else {
    							postAList.add(tok);
    						}
    					}
    				} catch (Exception e) {
    					// operand is a Numeric add it to the postfix
    					postAList.add(tok);
    				}

    				break;
    			case Token.OPERATOR:
    				if (tok.tokenStr.equals("in")) {
    					while (! mainStack.isEmpty()) {
    						popped = mainStack.pop();
	    		    		if (popped.tokenStr.equals("("))
	    		    			error("Missing ')' separator", popped);
	    		    		if (popped.isElemRef)
	    		    			error("Missing ']' separator", popped);
	    		    		if (popped.primClassif == Token.FUNCTION)
	    		    			error("Missing ')' separator", popped);
	    		    		postAList.add(popped);
    					}
    			        return evaluateExpr(postAList);
    				}
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
    	    		    		if (popped.isElemRef)
    	    		    			error("Missing ']' separator", popped);
    							postAList.add(popped);
    							if (popped.primClassif == Token.FUNCTION) {
    								bFound = true;
    								break;
    							}
    						}
    						if (!bFound && funcCall) {   // no matching left paren but a func has been called ie print()
                                while (! mainStack.isEmpty()) {
                                    popped = mainStack.pop();
                                    if (popped.tokenStr.equals("("))
                                        error("Missing ')' separator", popped);
        	    		    		if (popped.isElemRef)
        	    		    			error("Missing ']' separator", popped);
        	    		    		if (popped.primClassif == Token.FUNCTION)
        	    		    			error("Missing ')' separator", popped);

                                    postAList.add(popped);
                                }
                                return evaluateExpr(postAList);
    					    } else if (!bFound){
	                        	error("Missing left paren in expression", startOfExprToken);
	                        }
    						break;
                        case "]":
	                        bRefFound = false;

	                        while (! mainStack.isEmpty()) {
	                        	popped = mainStack.pop();
	                        	postAList.add(popped);

	                        	if (popped.isElemRef) {
	                        		bRefFound = true;
	                        		break;
	                        	}

	                        }

	                        if (!bRefFound && funcCall) {
	                            while (!mainStack.isEmpty()) {
	                                popped = mainStack.pop();
	                        		if (popped.tokenStr.equals("("))
	                        			error("Missing ')' separator", popped);
	                                if (popped.tokenStr.equals("["))
	                                    error("Missing ']' separator", popped);
	    	    		    		if (popped.primClassif == Token.FUNCTION)
	    	    		    			error("Missing ')' separator", popped);
	                                postAList.add(popped);
	                            }
	                            return evaluateExpr(postAList);
	                        } else if (!bRefFound){
	                        	error("Missing left bracket in expression", startOfExprToken);
	                        }
	                        break;
    					default:
    						error("Invalid separator in expression", startOfExprToken);
    						break;
    				}
    				break;
    			case Token.FUNCTION:
					mainStack.push(tok);
					if (scan.nextToken.tokenStr.equals("(")) {
						scan.getNext(); // consume the '[', we don't want it in the list
					} else {
						error("FUNCTION '" + tok.tokenStr + "' missing '(' separator", tok);
					}
    				break;
    			default:
    				error("Invalid operator/operand in expression", startOfExprToken);
    		}
    		scan.getNext();
    		tok = scan.currentToken;
    	} // end while loop
    	while (! mainStack.isEmpty()) {
    		popped = mainStack.pop();
    		if (popped.tokenStr.equals("("))
    			error("Missing ')' separator", popped);
    		if (popped.isElemRef)
    			error("Missing ']' separator", popped);
    		if (popped.primClassif == Token.FUNCTION)
    			error("Missing ')' separator", popped);
    		postAList.add(popped);
    	}


        return evaluateExpr(postAList);
    }

    public ResultValue evaluateExpr(ArrayList<Token> list) throws ParserException, Exception {
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
    		currToken = list.get(i); // get the current Token from the ArrayList
    		switch (currToken.primClassif) {
	    		case Token.OPERAND:
	    			if (currToken.isElemRef) {
	    				STEntry stArray = st.getSymbol(currToken.tokenStr);
	                    if (stArray == null) {
	                        error("Symbol '"+currToken.tokenStr+"' is not in Symbol Table.", currToken);
	                    }
	    				try {
		    				tokOp2 = (Token) stk.pop(); // grab the right operand (the only operand)
		    			} catch (EmptyStackException a) {
		    				error("Missing index expression operand for array '"+currToken.tokenStr+"'.", currToken);
		    			}
	    				if (tokOp2.subClassif == Token.IDENTIFIER) {
	    					STEntry stEnt2 = st.getSymbol(tokOp2.tokenStr);
	    					if (stEnt2 == null) {
	    	                    error("Symbol '"+tokOp2+"' is not in Symbol Table.", tokOp2);
	    	                }
	    					resOp2 = new ResultValue(stEnt2.value);
	    					resOp2.type = ((STIdentifier)stEnt2).type;
	    				} else {
	    					resOp2 = new ResultValue(tokOp2.tokenStr);
	    					resOp2.type = tokOp2.subClassif;
	    				}
	    				if (currToken.isArray) {
		    				resOp2.structure.add("ARRAY ELEM REF");

		    				nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "Index Operand"); // must be a number
		    				if (nOp2.integerValue == -1) {
		    					resTemp = ((STIdentifier) stArray).array.get(((STIdentifier) stArray).array.val.size() - 1);
		    				} else {
		    					resTemp = ((STIdentifier) stArray).array.get(nOp2.integerValue);
		    				}
	    				} else { // we have a string index reference
	    					resOp2.structure.add("STRING ELEM REF");

	    					nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "Index Operand"); // must be a number
	    					if (nOp2.integerValue == -1) {
	    						resTemp.value = ((STIdentifier) stArray).getValue(((STIdentifier) stArray).value.length(), this);
	    						resTemp.type = Token.STRING;
	    					} else {
	    						resTemp.value = ((STIdentifier) stArray).getValue(nOp2.integerValue, this);
	    						resTemp.type = Token.STRING;
	    					}
	    				}

	    				extraToken1 = tokOp2.saveToken(); // get the most accurate values for the line and column # as possible
	    				extraToken1.isArray = false;
                        extraToken1.tokenStr = resTemp.value;
                        extraToken1.primClassif = Token.OPERAND;
                        extraToken1.subClassif = resTemp.type;

                        stk.push(extraToken1);

                        resMain.structure.add(resOp2.structure.get(0));
	    			} else {
	    				stk.push(currToken); // add the operand onto the stack for future use
	    			}
	    			break;
	    		case Token.OPERATOR:
	    			// since we have an operator we need to evaluate the top two operands
	    			// and because we are using a stack, the righthand operand
	    			// appears before the lefthand operand on a stack

	    			if (currToken.tokenStr.equals("u-") || currToken.tokenStr.equals("not")) { // unary minus and not are handled differently
	    				try {
		    				tokOp2 = (Token) stk.pop(); // grab the right operand (the only operand)
		    			} catch (EmptyStackException a) {
		    				error("Missing right expression operand for unary operator.", currToken);
		    			}
	    				if (tokOp2.subClassif == Token.IDENTIFIER) {
	    					STEntry stEnt2 = st.getSymbol(tokOp2.tokenStr);
	    					if (stEnt2 == null) {
	    	                    error("Symbol '"+tokOp2+"' is not in Symbol Table.", tokOp2);
	    	                }
	    					resOp2 = new ResultValue(stEnt2.value);
	    					resOp2.type = ((STIdentifier)stEnt2).type;
	    				} else {
	    					resOp2 = new ResultValue(tokOp2.tokenStr);
	    					resOp2.type = tokOp2.subClassif;
	    				}
	    				resOp2.structure.add(Token.strSubClassifM[tokOp2.subClassif]);

	    				switch (currToken.tokenStr) {
	    				case "u-":
	                        nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "Operand"); // must be a number
	                        resTemp = Utility.negative(this, nOp2);
	    					break;
	    				case "not":
	    					if (resOp2.type != Token.BOOLEAN && resOp2.type != Token.STRING)
	    						error("Operand for 'not' is not of type BOOLEAN", tokOp2);

	    					resTemp = Utility.booleanConditionals(this, null, resOp2, "not");
	    					break;
	    				}

                        extraToken1 = tokOp2; // get the most accurate values for the line and column # as possible
                        extraToken1.isArray = false;
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
		    				error("Missing expression operand "
		    						+ "for operator: '" + currToken.tokenStr + "'", currToken);
		    			}
		    			try {
		    				tokOp1 = (Token) stk.pop(); // grab the left operand
		    			} catch (EmptyStackException b) {
		    				error("Missing expression operand "
		    						+ "for operator: '" + currToken.tokenStr + "'", currToken);
		    			}

                        // set the ResultValue objects of the operands

		    			// check if its a variable if so, we need its real value and dataType
		    			// cannot operate on array tokens
		    			if (tokOp1.isArray)
		    				error("1st Operand of " + currToken.tokenStr + " operator cannot be of type ARRAY", currToken);
		    			if (tokOp2.isArray)
		    				error("2nd Operand of " + currToken.tokenStr + " operator cannot be of type ARRAY", currToken);

		    			if (tokOp1.subClassif == Token.IDENTIFIER) {
		    				STEntry stEnt1 = st.getSymbol(tokOp1.tokenStr);
		    				if (stEnt1 == null) {
	    	                    error("Symbol '"+tokOp1.tokenStr+"' is not in Symbol Table.", tokOp1);
	    	                }
		    				resOp1 = new ResultValue(stEnt1.value);
		    				resOp1.type = ((STIdentifier)stEnt1).type;
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
	    	                    error("Symbol '"+tokOp2.tokenStr+"' is not in Symbol Table.", tokOp2);
	    	                }
		    				resOp2 = new ResultValue(stEnt2.value);
		    				resOp2.type = ((STIdentifier)stEnt2).type;
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
			    			case "and":
			    				resTemp = Utility.booleanConditionals(this, resOp1, resOp2, "and");
			    				break;
			    			case "or":
			    				resTemp = Utility.booleanConditionals(this, resOp1, resOp2, "or");
			    				break;
							default:
								error("Invalid operator in expression: '"
									+ currToken.tokenStr + "'", startOfExprToken);
		    			} // end of inner Operator switch

                        // add our new value to the stack
                        extraToken1 = tokOp1.saveToken(); // get the most accurate values for the line and column # as possible
                        extraToken1.isArray = false;
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
	    		case Token.FUNCTION:
	    			// all functions need one operand
    				try {
	    				tokOp2 = (Token) stk.pop(); // grab the right operand (the only operand)
	    			} catch (EmptyStackException a) {
	    				error("Missing operand for '" + currToken.tokenStr + "' function.", currToken);
	    			}
    				if (tokOp2.subClassif == Token.IDENTIFIER) {
    					STEntry stEnt2 = st.getSymbol(tokOp2.tokenStr);
    					if (stEnt2 == null) {
    	                    error("Symbol '"+tokOp2.tokenStr+"' is not in Symbol Table.", tokOp2);
    	                }
    					if (tokOp2.isArray) {
	    					resOp2 = new ResultValue(stEnt2.symbol);
	    					resOp2.type = ((STIdentifier)stEnt2).array.type;
    					} else {
	    					resOp2 = new ResultValue(stEnt2.value);
	    					resOp2.type = ((STIdentifier)stEnt2).type;
    					}
    				} else {
    					resOp2 = new ResultValue(tokOp2.tokenStr);
    					resOp2.type = tokOp2.subClassif;
    				}

    				switch (currToken.tokenStr) {
	    				case "LENGTH":
	    					if (tokOp2.isArray)
	    						error("'" + tokOp2.tokenStr + "' type ARRAY is not a valid type for function LENGTH", tokOp2);
	    					resTemp = Utility.LENGTH(this, resOp2.value);
	    					break;
	    				case "SPACES":
	    					if (tokOp2.isArray)
	    						error("'" + tokOp2.tokenStr + "' type ARRAY is not a valid type for function SPACES", tokOp2);
	    					resTemp = Utility.SPACES(this, resOp2.value);
	    					break;
	    				case "ELEM":
	    					if (! tokOp2.isArray)
	    						error("'" + tokOp2.tokenStr + "' is not type ARRAY for function ELEM", tokOp2);
	    					resTemp = Utility.ELEM(this, resOp2.value);
	    					break;
	    				case "MAXELEM":
	    					if (! tokOp2.isArray)
	    						error("'" + tokOp2.tokenStr + "' is not type ARRAY for function ELEM", tokOp2);
	    					resTemp = Utility.MAXELEM(this, resOp2.value);
	    					break;
    				}
    				// add our new value to the stack
                    extraToken1 = tokOp2.saveToken(); // get the most accurate values for the line and column # as possible
                    extraToken1.tokenStr = resTemp.value;
                    extraToken1.primClassif = Token.OPERAND;
                    extraToken1.subClassif = resTemp.type;
                    stk.push(extraToken1);

                    resMain.structure.add(resTemp.structure.get(0));
	    			break;
	    		default:
	    			error("Invalid operand in expression: '"
							+ currToken.tokenStr + "'", startOfExprToken);
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
                        error("Symbol '"+extraToken2.tokenStr+"' is not in Symbol Table.", extraToken2);
                    }
                    if (extraToken2.isArray) {
                    	// return the string of the array as the value
                    	resMain.value = "[" + ((STIdentifier) stExtra).array.val.get(0);
                    	for (int i = 1; i < Integer.parseInt((Utility.ELEM(this, extraToken2.tokenStr).value)); i++) {
                    		resMain.value = resMain.value + ", " + ((STIdentifier) stExtra).array.val.get(i);
                    	}
                    	resMain.value = resMain.value + "]";
                    	resMain.type = ((STIdentifier) stExtra).type;
                    	resMain.structure.add("ARRAY");
                    } else {
	                    resMain.value = stExtra.value;
	                    resMain.type = ((STIdentifier)stExtra).type;
	                    // since the above algorithm doesn't allow for variables to exist in the stack
	                    // resMain's structure is set here for the first time.
	                    resMain.structure.add(Token.strSubClassifM[extraToken2.subClassif]);
                    }
                } else {
                    resMain.value = extraToken2.tokenStr;
                    resMain.type = extraToken2.subClassif;
                } // end of identifier check
            } else if (stk.peek().isArray && stk.size() == 2) {
            	// deal with array
            	Token poppedArray = stk.pop(); // get array
            	if (poppedArray.isElemRef) {
	            	extraToken2 = stk.pop(); // get array index
	            	STIdentifier stArray = ((STIdentifier) st.getSymbol(poppedArray.tokenStr));
	            	if (stArray == null) {
	            		error("'" + poppedArray.tokenStr + "' is not in the SymbolTable", poppedArray);
	            	}
	            	resMain = stArray.array.get(Integer.parseInt(extraToken2.tokenStr));
            	} else {
            		error("Array '" + poppedArray.tokenStr + "' is missing an index reference", poppedArray);
            	}
            } else {
                error("Unbalanced Expression. Possibly Missing Operator", startOfExprToken); // this error should have been caught by all the other checks
            } // end of size check
        } else {
            error("'"+startOfExprToken.tokenStr+"' is an invalid expression."); // this error should have been caught by all the other checks
        } // end of empty check
        return resMain; // return your brand new value!
    }

    public void error(String fmt) throws ParserException {
        throw new ParserException(scan.currentToken.iSourceLineNr
                , fmt, scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr - 1]);
    }

    public void errorNoTerm(String fmt) throws ParserException {
        throw new ParserException(scan.currentToken.iSourceLineNr -1
                , fmt, scan.sourceFileNm, scan.lines[scan.currentToken.iSourceLineNr - 2]);
    }

    public void error(String fmt, Token tok) throws ParserException {
        throw new ParserException(tok.iSourceLineNr
                , fmt, scan.sourceFileNm, scan.lines[tok.iSourceLineNr - 1]);
    }

}



