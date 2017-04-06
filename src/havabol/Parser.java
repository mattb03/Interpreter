package havabol;
import java.awt.event.FocusAdapter;
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
            if (scan.currentToken.tokenStr.equals("print")) {
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
                            if (scan.currentToken.subClassif == Token.STRING) {
                                arglist.add(scan.currentToken.tokenStr);
                            } else {
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
                    scan.getNext();

                    //array logic should start here or after symbol is put into symbol table

                    //put this token into symbol table!!!
                    Token tokk = scan.currentToken;
                    STIdentifier entry = (STIdentifier) st.getSymbol(tokk.tokenStr);
                    if (entry != null) {
                    	entry.value = "NO VALUE";
                    } else {
	                    st.putSymbol(tokk.tokenStr, new STEntry(tokk.tokenStr, 
                                        tokk.primClassif, tokk.subClassif));
	                    // set the type in the symbol table for the identifier
	                    st.getSymbol(tokk.tokenStr).type = type;
                    }
                    if (scan.nextToken.tokenStr.equals(";")) {
                    	scan.getNext();
                    	continue;
                    } else if (scan.nextToken.tokenStr.equals("=")) {
                        assign(scan.currentToken, true);
                    } else {
                    	error("Invalid assign. Found : " + scan.nextToken.tokenStr, scan.nextToken);
                    }
                    //scan.getNext();
                }
            } else if (scan.currentToken.subClassif == Token.IDENTIFIER) { // if token is an identifier
                //check if curToken is in symbol table, if not throw an error
                STIdentifier entry = (STIdentifier)st.getSymbol(scan.currentToken.tokenStr);
                if (entry == null) {
                    error("Symbol '"+scan.currentToken.tokenStr+"' is not in Symbol Table.");
                }
                if (scan.nextToken.tokenStr.equals("=")) {
                    assign(scan.currentToken, false);
                } else {
                	error("Invalid assign. Expected '='. Found : "+scan.currentToken.tokenStr);
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

    public void assign(Token curSymbol, boolean declare) throws Exception {
        Boolean show = debugger.assign;
        Boolean expr = debugger.expr;
        String value = "";
        Token curr = curSymbol.saveToken();
        STIdentifier entryL = (STIdentifier)st.getSymbol(curr.tokenStr);


        int ltype = st.getSymbol(curSymbol.tokenStr).type;  // get type of left op
        scan.getNext(); // get equals sign
        scan.getNext(); // get val (right op)
        Token rToken = scan.currentToken;
        // check if rToken is an identifier or something else
        if (rToken.subClassif == Token.IDENTIFIER) { //  case declare or not:   String s = i;
            STIdentifier entryR = (STIdentifier)st.getSymbol(rToken.tokenStr);
            if (entryR == null) {
                error("Symbol '"+rToken.tokenStr+"' is not in Symbol Table.");
            }
            // get right op type
            int rtype = entryR.type;
            if (scan.nextToken.tokenStr.equals(";")) {  // only one identifier
                if (ltype != rtype) {
                    if (ltype == Token.INTEGER && rtype == Token.FLOAT) {
                        String val = entryR.value; // get the float val from rside
                        int index = val.indexOf(".");
                        val = val.substring(0, index); // remove deciaml and other numbers
                        entryL.value = val; // set left sides val to this new val
                        if (show) {
                        	System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                		+ ": " + curr.tokenStr + " = "+val);
                        }
                    } else if (ltype == Token.FLOAT && rtype == Token.INTEGER) {
                        String val = entryR.value;   // get the int val from rside , must cast to a float 2 -> 2.0
                        val += ".00";
                        entryL.value = val;
                        if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                		+ ": " + curr.tokenStr + " = "+val);
                        }
                    } else if (ltype == Token.INTEGER && rtype == Token.STRING) {
                        int testVal;
                        try {
                            testVal = Integer.parseInt(entryR.value);
                            entryL.value = entryR.value;
                            String val = entryL.value;
                            if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                        + ": " + curr.tokenStr + " = "+val);
                            }
                        } catch (Exception e) {
                            error("Incompatible types. Cannot assign "+
                                scan.currentToken.strSubClassifM[rtype]+
                                " to "+curr.strSubClassifM[ltype]+
                                " when STRING is a non numeric value");
                        }
                    } else if ((ltype == Token.STRING && rtype == Token.INTEGER)
                        || (ltype == Token.STRING && rtype == Token.FLOAT)) {

                        entryL.value = entryR.value;
                        String val = entryL.value;
                        if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr+
                                ": "+curr.tokenStr+" = "+val);
                        }
                    } else if (ltype == Token.STRING && rtype == Token.BOOLEAN) {
                        entryL.value = entryR.value;
                        String val = entryL.value;
                        if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr+
                                ": "+curr.tokenStr+" = "+val);
                        }
                    } else if (ltype == Token.BOOLEAN && rtype == Token.STRING) {
                        if (entryR.value.equals("T") || entryR.value.equals("F")) {
                            entryL.value = entryR.value;
                            String val = entryL.value;
                            if (show) {
                                System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr+
                                    ": "+curr.tokenStr+" = "+val);
                            }
                        } else { // this code will never be executed as a boolean ident will always be T or F
                            error("Incompatible types. Cannot assign "+
                                Token.strSubClassifM[rtype]+" to "+
                                Token.strSubClassifM[ltype]+
                                " when STRING is not 'T' or 'F'");
                        }
                    } else {
                        error("Incompatible types. Cannot assign "+
                            scan.currentToken.strSubClassifM[rtype]+
                            " to "+curr.strSubClassifM[ltype]);
                    }


                } else {
                    // same type identifier assignment
                    String val = st.getSymbol(rToken.tokenStr).value;  // get side val
                    entryL.value = val;  //  set left side val to right sides val
                    if (show) {
                        System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                    }
                }
            } else {  // IDENTIFIER next token not a ';'  possible valid expression I.e  Int a = a + 2;
                ResultValue resExpr = expr(false);
                int assignType = resExpr.type;
                if (ltype != assignType) {
                    if ((ltype == Token.INTEGER && assignType == Token.FLOAT)
                        || (ltype == Token.FLOAT && assignType == Token.INTEGER)) {

                        if (ltype == Token.INTEGER) {
                            value = resExpr.value;
                            int index = value.indexOf(".");
                            value = value.substring(0, index);
                        } else {  // left side is Float
                            value = resExpr.value;
                            value += ".00";
                        }

                    } else if (ltype == Token.INTEGER && assignType == Token.STRING) {
                        value = resExpr.value;
                        int testVal;
                        try {
                            testVal = Integer.parseInt(value);
                            if (expr) {
                                System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                            + ": " + curr.tokenStr + " = "+value);
                            }
                        } catch (Exception e) {
                            error("Incompatible types. Cannot assign "+
                                Token.strSubClassifM[assignType]+
                                " to "+Token.strSubClassifM[ltype]+
                                " when STRING is a non numeric value");
                        }

                    } else if (ltype == Token.STRING) {
                        value = resExpr.value;
                        if (expr) {
                            System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                + ": " + curr.tokenStr + " = "+value);
                        }
                    } else if (ltype == Token.BOOLEAN && assignType == Token.STRING) {
                        value = resExpr.value;
                        if (value.equals("T") || value.equals("F")) {
                            ;
                        } else {
                            error("Incompatible types. Cannot assign "+
                                Token.strSubClassifM[assignType]+
                                " to "+Token.strSubClassifM[ltype]+
                                " when STRING is a not a 'T' or 'F'");
                        }
                        if (expr) {
                            System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                + ": " + curr.tokenStr + " = "+value);
                        }
                    } else {
                        error("Incompatible types. Cannot assign "+
                            Token.strSubClassifM[assignType]+
                            " to "+Token.strSubClassifM[ltype]);
                    }

                } else {  // they are the same type
                    value = resExpr.value;
                }
                entryL.value = value;
                String val2 = entryL.value;
                if (expr) {
                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val2);
                }
            }
        } else { // rToken is not an identifier
            if (scan.nextToken.tokenStr.equals(";")) {
                String val = "";
                if (ltype != rToken.subClassif) {
                    if (ltype == Token.INTEGER && rToken.subClassif == Token.FLOAT) {
                        int index = rToken.tokenStr.indexOf(".");
                        val = st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr.substring(0, index);
                    } else  if (ltype == Token.FLOAT && rToken.subClassif == Token.INTEGER) {
                    	st.getSymbol(curSymbol.tokenStr).value = rToken.tokenStr + ".00";
                    	val = st.getSymbol(curSymbol.tokenStr).value;
                    	//st.setDataType((STIdentifier)st.getSymbol(curSymbol.tokenStr), ltype);
                        if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                        + ": " + curr.tokenStr + " = "+val);
                        }
                    } else if (ltype == Token.INTEGER && rToken.subClassif == Token.STRING) {
                        int testVal;
                        try {
                            testVal = Integer.parseInt(rToken.tokenStr);
                            entryL.value = rToken.tokenStr;
                            val = rToken.tokenStr;
                            if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                        + ": " + curr.tokenStr + " = "+val);
                            }
                        } catch (Exception e) {
                            error("Incompatible types. Cannot assign "+
                                scan.currentToken.strSubClassifM[rToken.subClassif]+
                                " to "+curr.strSubClassifM[ltype]+
                                " when STRING is a non numeric value");
                        }
                    } else if ((ltype == Token.STRING && rToken.subClassif == Token.INTEGER)
                        || (ltype == Token.STRING && rToken.subClassif == Token.FLOAT)) {

                        entryL.value = rToken.tokenStr;
                        val = entryL.value;
                        if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr+
                                ": "+curr.tokenStr+" = "+val);
                        }

                    } else if (ltype == Token.STRING && rToken.subClassif == Token.BOOLEAN) {
                        entryL.value = rToken.tokenStr;
                        val = entryL.value;
                        if (show) {
                            System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr+
                                ": "+curr.tokenStr+" = "+val);
                        }
                    } else if (ltype == Token.BOOLEAN && rToken.subClassif == Token.STRING) {
                        if (rToken.tokenStr.equals("T") || rToken.tokenStr.equals("F")) {
                            entryL.value = rToken.tokenStr;
                            val = entryL.value;
                            if (show) {
                                System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr+
                                    ": "+curr.tokenStr+" = "+val);
                            }
                        } else {
                             error("Incompatible types. Cannot assign "+
                                Token.strSubClassifM[rToken.subClassif]+
                                " to "+Token.strSubClassifM[ltype]+
                                " when STRING is a not a 'T' or 'F'");
                        }
                    } else {
                    	error("Incompatible types. Cannot assign "+
                            Token.strSubClassifM[rToken.subClassif]+
                            " to "+Token.strSubClassifM[ltype]);
                    }
                } else {
                    entryL.value = rToken.tokenStr;
                    val = entryL.value;
                    if (show) {
                        System.out.println("==== ASSIGN  ==== :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                    }
                }
            } else {  // next token not a ';', expression: right side is NOT an identifier; ie   Int a = 3 + 4;
                ResultValue resExpr = expr(false);
                int assignType = resExpr.type; // get right type
                if (ltype != assignType) {
                    if ((ltype == Token.INTEGER && assignType == Token.FLOAT)
                        || (ltype == Token.FLOAT && assignType == Token.INTEGER)) {

                        if (ltype == Token.INTEGER) {
                            value = resExpr.value;
                            int index = value.indexOf(".");
                            value = value.substring(0, index);
                        } else if (ltype == Token.FLOAT) {  // left side is Float
                            value = resExpr.value;
                            value += ".00";
                        }
                    } else if (ltype == Token.INTEGER && assignType == Token.STRING) {
                        value = resExpr.value;
                        System.out.println("val is : "+value);
                        int testVal;
                        try {
                            testVal = Integer.parseInt(value);
                            if (expr) {
                                System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                            + ": " + curr.tokenStr + " = "+value);
                            }
                        } catch (Exception e) {
                            error("Incompatible types. Cannot assign "+
                                scan.currentToken.strSubClassifM[rToken.subClassif]+
                                " to "+curr.strSubClassifM[ltype]+
                                " when STRING is a non numeric value");
                        }

                    } else if (ltype == Token.STRING) {
                        value = resExpr.value;
                        if (expr) {
                            System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                + ": " + curr.tokenStr + " = "+value);
                        }
                    } else if (ltype == Token.BOOLEAN && assignType == Token.STRING) {
                        value = resExpr.value;
                        if (value.equals("T") || value.equals("F")) {
                            ;
                        } else {
                            error("Incompatible types. Cannot assign "+
                                Token.strSubClassifM[rToken.subClassif]+
                                " to "+Token.strSubClassifM[ltype]+
                                " when STRING is a not a 'T' or 'F'");
                        }
                        if (expr) {
                            System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                + ": " + curr.tokenStr + " = "+value);
                        }
                    } else {
                        error("Incompatible types. Cannot assign "+
                            Token.strSubClassifM[assignType]+
                            " to "+Token.strSubClassifM[ltype]);
                    }
                } else {  // same types
                    value = resExpr.value;
                }
                entryL.value = value;
                String val = entryL.value;
                if (expr) {
                    System.out.println("+++++ EXPRN +++++ :"+curr.iSourceLineNr
                                    		+ ": " + curr.tokenStr + " = "+val);
                }
            }
        }

    }

    // assumes currentToken is on an if.
    public void ifStmt() throws Exception {
    	// statementToken is used to track the start of a statement for error checking
    	Token beginningIf; // like statementToken this is used to save the start of the whole statement
    	Token statementToken = beginningIf = scan.currentToken;
    	scan.getNext(); // consume if
    	
    	ResultValue resCond = expr(false);
    	if (! scan.currentToken.tokenStr.equals(":")) {
    		error("Invalid terminating token for if: '"
    				+ scan.currentToken.tokenStr + "'", statementToken);
    	}
    	scan.getNext(); // consume separator
    	if (resCond.value.equals("T")) {
    		// the starting if is true execute the code
    		statements(true);
    		// currentToken is an endif or an else
    		if (scan.currentToken.tokenStr.equals("else")) {
    			statementToken = scan.currentToken; // save the starting else for error checking
            	scan.getNext(); // consume else
            	if (! scan.currentToken.tokenStr.equals(":")) {
            		error("Invalid terminating token for else: '"
            				+ scan.currentToken.tokenStr +"'", statementToken);
            	}
            	scan.getNext(); // consume separator
            	// run through the false statements
            	Stack<Token> stk = new Stack<Token>();
            	while (true) {
            		if (scan.currentToken.tokenStr.equals("if")) {
            			stk.push(scan.currentToken);
            			//statementToken = scan.currentToken; // update to nested if
            		} else if (stk.isEmpty()) {
            			if (scan.currentToken.tokenStr.equals("else")
            					|| scan.currentToken.tokenStr.equals("endif")) {
            				break;
            			}
            		} else if (! stk.isEmpty()) {
            			if (scan.currentToken.tokenStr.equals("else")) {
            				if (! scan.nextToken.tokenStr.equals(":")) {
            					error("Invalid terminating token for else: '"
            							+ scan.nextToken.tokenStr + "'");
            				}
            			}
            			if (scan.currentToken.tokenStr.equals("endif")) {
            				if (! scan.nextToken.tokenStr.equals(";")) {
            					error("Invalid terminating token for endif: '"
            							+ scan.nextToken.tokenStr + "'");
            				}
            				stk.pop();
            			}
            		}
            		if (scan.currentToken.primClassif == Token.EOF)
        				error("Else statement not terminated by endif.", statementToken);
            		
            		scan.getNext();
            	} // endwhile
            	
            	statementToken = scan.currentToken;
            	
            	// currentToken MUST be an else or endif
            	if (scan.currentToken.tokenStr.equals("else")) {
            		// an else is an error
            		error("Unmatched nested else statement.");
            	} else {
            		// this is the correct scenario
            		scan.getNext(); // consume endif
            		if (! scan.currentToken.tokenStr.equals(";")) {
            			error("Invalid terminating token for endif: '"
            					+ scan.currentToken.tokenStr + "'", statementToken);
            		}
            		// there isn't anything else to execute
            	}
    		} else { // currentToken is not an else, must be an endif, check
    			if (! scan.currentToken.tokenStr.equals("endif")) {
    				error("If statement not terminated by endif.", statementToken);
    			} else {
    				statementToken = scan.currentToken;
    				scan.getNext();
                	if (! scan.currentToken.tokenStr.equals(";")) {
                		error("Invalid terminating token for endif: '"
            					+ scan.currentToken.tokenStr + "'", statementToken);
                	}
    			}
    		}
    	// end of starting if true
    		
    	} else {
    		// the starting if is false
        	// run through the false statements
        	Stack<Token> stk = new Stack<Token>();
        	while (true) {
        		if (scan.currentToken.tokenStr.equals("if")) {
        			stk.push(scan.currentToken);
        			statementToken = scan.currentToken; // update to nested if
        		} else if (stk.isEmpty()) {
        			if (scan.currentToken.tokenStr.equals("else")
        					|| scan.currentToken.tokenStr.equals("endif")) {
        				break;
        			}
        		} else if (! stk.isEmpty()) {
        			if (scan.currentToken.tokenStr.equals("else")) {
        				if (! scan.nextToken.tokenStr.equals(":")) {
        					error("Invalid terminating token for else: '"
        							+ scan.nextToken.tokenStr + "'");
        				}
        			}
        			if (scan.currentToken.tokenStr.equals("endif")) {
        				if (! scan.nextToken.tokenStr.equals(";")) {
        					error("Invalid terminating token for endif: '"
        							+ scan.nextToken.tokenStr + "'");
        				}
        				stk.pop();
        				if (! stk.isEmpty())
        					statementToken = stk.peek();
        				else 
        					statementToken = beginningIf;
        			}
        		}
        		if (scan.currentToken.primClassif == Token.EOF)
    				error("If statement not terminated by endif.", statementToken);
        		
        		scan.getNext();
        	} // endwhile
        	
        	// currentToken MUST be an else or an endif
    		statementToken = scan.currentToken; // save for error checking
    		scan.getNext(); // consume else or endif
    		
    		if (statementToken.tokenStr.equals("else")) {
            	if (! scan.currentToken.tokenStr.equals(":")) {
            		error("Invalid terminating token for else: '"
            				+ scan.currentToken.tokenStr +"'", statementToken);
            	}
        		scan.getNext(); // consume the separator
        		
    			// execute the statements in else
    			statements(true);
    			
    			// currentToken MUST be an else or endif
            	if (scan.currentToken.tokenStr.equals("else")) {
            		// an else is an error
            		error("Unmatched else statement.");
            	} else { // currentToken is not an else, must be an endif, check
        			if (! scan.currentToken.tokenStr.equals("endif")) {
        				error("Else statement not terminated by endif.", statementToken);
        			} else {
        				statementToken = scan.currentToken;
        				scan.getNext();
                    	if (! scan.currentToken.tokenStr.equals(";")) {
                    		error("Invalid terminating token for endif: '"
                					+ scan.currentToken.tokenStr + "'", statementToken);
                    	}
        			}
            	}
    		} else {
        		if (! scan.currentToken.tokenStr.equals(";")) {
        			error("Invalid terminating token for endif: '"
        					+ scan.currentToken.tokenStr + "'", statementToken);
        		}
    		}
    	}

    }


    public void whileStmt() throws Exception  {
    	Scanner old = this.scan.saveState();

        scan.getNext();
        ResultValue resCond = expr(false);

        if (resCond.value.equals("T")) {
        	while (resCond.value.equals("T")) {
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
        	if (resCond.value.equals("F")) {
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

    public void forStmt() throws Exception {
		// 1. read in entire line
    	// 2. check if the word "in" is in the line
    	//    a. if true, then its a foreach
    	//    b. else, its a counting for
    	String forCond = getForCond();
    	System.out.println("wasssaaaaaaa");
    	// at this point we have the entire for loop condition, so we can begin evaluating
    	// we will check for errors in it along the way
    	
    	
	}
    
    public String getForCond() throws Exception {
    	String forCond = scan.currentToken.tokenStr + " " + scan.nextToken.tokenStr + " " + scan.getNext();
    	if (forCond.indexOf("=") != -1) {
    		// its a counter for loop
    		int i;
    		for (i = 0; i < 3; i++) {
    			forCond += " " + scan.getNext();
    		}
    		if (forCond.indexOf("to") == -1 ) {
    			// error, missing "to" keyword
				error("for loop missing \"to\" keyword: " + "\"" + forCond + "\"");
    		}
    		forCond += " " + scan.getNext();
    		if (forCond.indexOf("by") == -1 && forCond.indexOf(":") == -1) {
    			// error, missing "by" keyword and colon
				error("for loop missing terminating \":\" or \"by\" keyword: " + "\"" + forCond + "\"");
    		}
    		// if were here then its a valid counter for loop, so we can run it
    		if (forCond.indexOf("by") != -1) {
    			// theres an incr, so keep reading
    			forCond += " ";
    			forCond += scan.getNext() + " ";
    			scan.getNext();
    			// now we error check again
    			if (scan.nextToken.tokenStr.indexOf(":") == -1) {
    				error("for loop missing terminating \":\": " + "\"" + forCond + "\"");
    			}
    			else {
    				// if the next token is a colon then add it to the forCond and return forCond
    				forCond += scan.nextToken.tokenStr + " ";
    			}
    		}
    	}
    	
    	else if (forCond.indexOf("in") != -1) {
    		// its a foreach loop
    		return "for each loop";
    	}
    	else {
    		// error, missing "=" sign or "in" keyword
			error("for loop missing \"=\" or \"in\" keyword: " + "\"" + forCond + "\"");
    	}
    	return forCond;
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
    public ResultValue evaluateExpr(ArrayList<Token> list) throws ParserException {
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
	    					resOp2.type = ((STIdentifier)stEnt2).type;
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
	    	                    error("Symbol '"+tokOp2.tokenStr+"' is not in Symbol Table.");
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
    				resMain.type = ((STIdentifier)stExtra).type;
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
