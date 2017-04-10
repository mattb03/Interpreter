package havabol;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Stack;

import com.sun.swing.internal.plaf.metal.resources.metal_zh_TW;


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
    // type
                            System.out.print(str);
                        }
                        System.out.println();
                    } else {
                    	error("Print function not terminated with ';'");

                    }
                } else {
                    error("Invalid print statement");
                }

            } else if (scan.currentToken.tokenStr.equals("LENGTH")) {
            	
            } else if (scan.currentToken.tokenStr.equals("SPACES")) {
            	
            } else if (scan.currentToken.tokenStr.equals("ELEM")) {
            	
            } else if (scan.currentToken.tokenStr.equals("MAXELEM")) {
            	
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
    // type
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


                    //put this token into symbol table!!!
                    Token tokk = scan.currentToken; //
                    if (!scan.nextToken.tokenStr.equals("[")) { // put in symbol table if not an array declaration
                        STIdentifier entry = (STIdentifier) st.getSymbol(tokk.tokenStr);
                        if (entry != null)
                    	    entry.value = "NO VALUE";
                        else {
                            st.putSymbol(tokk.tokenStr, new STIdentifier(tokk.tokenStr,
                                        tokk.primClassif, tokk.subClassif));
                            st.getSymbol(tokk.tokenStr).type = type;
                            if (scan.nextToken.tokenStr.equals(";")) {
                                scan.getNext();
                                continue;
                            } else if (scan.nextToken.tokenStr.equals("=")) {
                                assign(scan.currentToken);
                            } else {
                                error("Invalid assign. Found : " + scan.nextToken.tokenStr, scan.nextToken);
                            }

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
                                } else {
                                    ResultValue result = expr(true);
                                    int size = Integer.parseInt(result.value);  // check if its value is an int or not
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
                    if (scan.nextToken.subClassif != Token.IDENTIFIER && scan.nextToken.subClassif != Token.INTEGER)
                        error("Malformed array declaration");
                    scan.getNext(); // get index
                    ResultValue ind  = expr(true);  // pass in true to expr if using an array or inside a func
                    scan.getNext(); // got '='
                    if (!scan.currentToken.tokenStr.equals("="))
                        error("Invalid array statement. Expected '='");
                    scan.getNext();
                    ResultValue resVal = expr(false);
                    entry.array.set(Integer.parseInt(ind.value), resVal);
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
            } else if (scan.currentToken.tokenStr.equals("endfor")) {
            	return;
            }
            //if (!scan.nextToken.tokenStr.equals(";"))
                //errorNoTerm("Statement not terminated");
            
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

    public void assignArray(Token curSymbol) {
        //  this method will handle      array[10] = 100;

    }

    public void copyOrDefaultArray(STIdentifier lEntry) throws Exception {
        scan.getNext(); // got "="
        scan.getNext(); // got first val after
        if (scan.currentToken.subClassif == Token.IDENTIFIER) {
            STIdentifier rEntry = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
            if (rEntry == null) {
                error("Not in symbol table");
            } else {
                if (rEntry.structure == STIdentifier.ARRAY &&
                    !scan.nextToken.tokenStr.equals("[")) {
                        lEntry.array.copy(rEntry.array);
                        return;
                }
            }
        }
        // default case logic
        ResultValue resVal = expr(false);
        lEntry.array.defaultArray(resVal.value);
    }

    public void declareArray(Token curSymbol) throws Exception { //, int index, boolean single) {
        scan.getNext(); // currentToken is now "="
        scan.getNext(); // currentToken is now some val
        STIdentifier entry = (STIdentifier) st.getSymbol(curSymbol.tokenStr);
        /*if (single) {
            ResultValue resVal = expr(false);
            if (!scan.currentToken.tokenStr.equals(";")) {
                error("Array assignment statement is not terminated");
            }
            entry.array.set(index, resVal);
        }*/
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
                entry.array.set(index, resVal);
                index++;
            } else {
                error("Malformed array declaration");
            }
        }
    }

    public void assign(Token curSymbol) throws Exception {
        Boolean show = debugger.assign;
        Boolean expr = debugger.expr;
        String value = "";
        Token curr = curSymbol.saveToken();
        STIdentifier entryL = (STIdentifier)st.getSymbol(curr.tokenStr);


        int ltype = st.getSymbol(curSymbol.tokenStr).type;  // get type of left op
        scan.getNext(); // get equals sign
        scan.getNext(); // get val (right op)
        Token rToken = scan.currentToken;
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
    	Scanner old = scan.saveState();
    	Token beginningWhile;
    	Token statementToken = beginningWhile = scan.currentToken; // used for saving the statement token for errors

        scan.getNext(); // consume while
        ResultValue resCond = expr(false);

        if (! scan.currentToken.tokenStr.equals(":")) {
        	error("Invalid terminating token for while: '"
        			+ scan.currentToken.tokenStr + "'", beginningWhile);
        }
        scan.getNext(); // consume separator




        if (resCond.value.equals("T")) {
            while (resCond.value.equals("T")) {
                statements(true);
                // currentToken MUST be an endwhile
                statementToken = scan.currentToken;
                if (! statementToken.tokenStr.equals("endwhile")) {
                    error("While not terminated by endwhile.", beginningWhile);
                }
                scan.getNext(); // consume endwhile
                if (! scan.currentToken.tokenStr.equals(";")) {
                    error("Invalid terminating token for endwhile: '"
                            + scan.currentToken.tokenStr + "'", statementToken);
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
                        statementToken = scan.currentToken; // update to nested while
                    } else if (stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
                        scan.getNext(); // consume endwhile
                        return;
                    } else if (! stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
                        stk.pop();
        				if (! stk.isEmpty())
        					statementToken = stk.peek();
        				else
        					statementToken = beginningWhile;
                    }
                    if (scan.currentToken.primClassif == Token.EOF)
        				error("While statement not terminated by endwhile.", statementToken);
                    scan.getNext();
                }
            }
        } else {
            Stack<Token> stk = new Stack<Token>();
            while (true) {
                if (scan.currentToken.tokenStr.equals("while")) {
                    stk.push(scan.currentToken);
                    statementToken = scan.currentToken; // update to nested while
                } else if (stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
    				if (! scan.nextToken.tokenStr.equals(";")) {
    					error("Invalid terminating token for endwhile: '"
    							+ scan.nextToken.tokenStr + "'");
    				}
                    scan.getNext(); // consume endwhile
                    return;
                } else if (! stk.isEmpty() && scan.currentToken.tokenStr.equals("endwhile")) {
    				if (! scan.nextToken.tokenStr.equals(";")) {
    					error("Invalid terminating token for endwhile: '"
    							+ scan.nextToken.tokenStr + "'");
    				}
                    stk.pop();
    				if (! stk.isEmpty())
    					statementToken = stk.peek();
    				else
    					statementToken = beginningWhile;
                }
                if (scan.currentToken.primClassif == Token.EOF)
    				error("While statement not terminated by endwhile.", statementToken);
                scan.getNext();
            }
        }
    }



    public void forStmt() throws Exception {
    	// save the scanner state to revert back to original when done
    	Scanner savedScanner = this.scan.saveState();
    	Token startToken = scan.currentToken.saveToken();
    	String temp = scan.currentToken.tokenStr + " " + scan.nextToken.tokenStr + " " + scan.getNext();
    	int controlVar;
    	int i;
    	// default incr variable is 1 if there is none provided
    	int incr = 1;
    	// restore the state so we can use getNext()
    	this.scan = savedScanner;
    	scan.getNext();

		// begin first argument analysis
		// check if its an operand
		if (scan.currentToken.subClassif == 1) {
			// make sure its not in the symbol table
			STIdentifier startIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
			if (!scan.nextToken.tokenStr.equals("=") &&
					!scan.nextToken.tokenStr.equals("in") &&
					!scan.nextToken.tokenStr.equals("from")) {
				error("\"" + scan.nextToken.tokenStr + "\"" + " is not a valid for loop token");
			}
			if (scan.nextToken.tokenStr.equals("in") || scan.nextToken.tokenStr.equals("from")) {
				// should be a foreach loop
				/*if (startIdent != null) {
					error("\"" + scan.currentToken.tokenStr + "\"" + " has already been declared");
				}*/
				Token itemTok = scan.currentToken;
				String szItem = itemTok.tokenStr;
				scan.getNext();
				scan.getNext();
				// setIdent will be the set of indices that we will iterate over and assign item to each time
				STIdentifier setIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
				Token setTok = scan.currentToken;
				char array[] = null;
				i = 0;
				int type = 0;
				STIdentifier delimIdent = null;
				Token delimTok = null;
				String delim = "";
				if (scan.nextToken.tokenStr.equals("by")) {
					// should be a foreach loop with a split string delimiter
					scan.getNext();
					scan.getNext();
					// get the string delimiter to split the string on
					delimIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
					delimTok = scan.currentToken;
					delim = "";
					if (delimIdent == null) {
						// if its null, then it must be a string literal, otherwise error
						if (delimTok.subClassif != 5) {
							error("\"" + delimTok.tokenStr + "\"" + 
						        " must be a declared string identifier or string literal");
						}
						// if its a string literal, assign the value to delim
						delim = delimTok.tokenStr;
					}
					else {
						// it is in the symbol table, make sure it has a value and is a string
						if (delimIdent.value.equals("NO VALUE")) {
							error("\"" + delimTok + "\"" + " is not initialized");
						}
						if (delimIdent.type != 5) {
							error("\"" + delimTok + "\"" + " must be a string identifier");
						}
						// if its a string identifier, assign the value from the symbol table entry
						// to delim
						delim = delimIdent.value;
					}
				}
				// loop must have colon ":" terminating string
				if (!scan.nextToken.tokenStr.equals(":")) {
					error("No terminating colon " + ":" + " for for loop");
				}
				
				if (setIdent == null) {
					// if its an identifier and the symbol table entry is null, its undeclared
					if (setTok.subClassif == 1) {
						error("\"" + setTok.tokenStr + "\"" + " is an undeclared identifier");
					}
					// if its not an identifier and not a string literal, then its an invalid set
					else if (setTok.subClassif != 5) {
						error("\"" + setTok.tokenStr + "\"" + " must be an array or string");
					}
					// if we are at this line then it must be a string literal
					// so convert the string literal to a char array
					String szArray[] = null;
					int length = 0;
					if (delimTok != null) {
						// if the delimiter is a string literal, then split on the token
						if (setIdent == null) {
							szArray = setTok.tokenStr.split(delim);
						}
						// if the delimiter is an identifier for a string, then split on the ST value
						else {
							szArray = setIdent.value.split(delim);
						}
						length = szArray.length;
					}
					// if there is no delimiter, then its a simple foreach loop
					if (szArray == null) {
						array = setTok.tokenStr.toCharArray();
						length = array.length;
					}
					scan.getNext();
					scan.getNext();
					if (startIdent == null) {
						type = setIdent.array.type;
					}
					else {
						type = startIdent.type;
					}
					while (i < length) {
						//szItem = setIdent.array.val.get(i);
						savedScanner = this.scan.saveState();
						STIdentifier itemEntry = new STIdentifier(szItem, 1, 5); 
						itemEntry.type = type;
						if (szArray == null) {
							itemEntry.value = String.valueOf(array[i]);
						}
						else {
							itemEntry.value = String.valueOf(szArray[i]);
						}
						st.putSymbol(szItem, itemEntry);
						statements(true);
						itemEntry = null;
						i++;
						// only reset the buffer to top of loop if we are running the loop again
						if (i < length) {
							this.scan = savedScanner;
							st.table.remove(szItem);
						}
					}
				}
				else {
					// if it is a declared scalar identifier, the scalar must be a string
					if (setIdent.structure == 100) {
						if (setIdent.type != 5) {
							error("The identifier " + "\"" + setIdent.symbol + "\"" + " must be an array or string");
						}
						// if we are at this line, then it is a string identifier
						// so convert the string value to a char array
						String szArray[] = null;
						int length = 0;
						if (delimTok != null) {
							// if the set string is a string literal, then split on the token
							if (setIdent == null) {
								szArray = setTok.tokenStr.split(delim);
							}
							// if the delimiter is an identifier for a string, then split on the ST value
							else {
								szArray = setIdent.value.split(delim);
							}
							length = szArray.length;
						}
						// if there is no delimiter, then its a simple foreach loop
						if (szArray == null) {
							array = setTok.tokenStr.toCharArray();
							length = array.length;
						}
						scan.getNext();
						scan.getNext();
						if (startIdent == null) {
							type = setIdent.array.type;
						}
						else {
							type = startIdent.type;
						}
						while (i < length) {
							//szItem = setIdent.array.val.get(i);
							savedScanner = this.scan.saveState();
							STIdentifier itemEntry = new STIdentifier(szItem, 1, 5); 
							if (szArray == null) {
								itemEntry.value = String.valueOf(array[i]);
							}
							else {
								itemEntry.value = String.valueOf(szArray[i]);
							}
							st.putSymbol(szItem, itemEntry);
							statements(true);
							itemEntry = null;
							i++;
							// only reset the buffer to top of loop if we are running the loop again
							if (i < length) {
								this.scan = savedScanner;
								st.table.remove(szItem);
							}
						}
					}
					else {
						// if were here then it is a valid array identifier
						scan.getNext();
						scan.getNext();
						if (startIdent == null) {
							type = setIdent.array.type;
						}
						else {
							type = startIdent.type;
						}
						while (i < setIdent.array.val.size()) {
							//szItem = setIdent.array.val.get(i);
							
							if (setIdent.array.val.get(i) != null) { 
								savedScanner = this.scan.saveState();
								STIdentifier itemEntry = new STIdentifier(szItem, 1, 5); 
								itemEntry.type = type;
								itemEntry.value = setIdent.array.val.get(i);
								st.putSymbol(szItem, itemEntry);
								statements(true);
								itemEntry = null;
							}
							i++;
							// only reset the buffer to top of loop if we are running the loop again
							if (i < setIdent.array.val.size()) {
								this.scan = savedScanner;
								if (setIdent.array.val.get(i) != null) {
									st.table.remove(szItem);
								}
							}
						}
					}
				}
				
			}
			else {
				// should be a counting for loop
				/*if (startIdent == null) {
					error("Variable " + "\"" + scan.currentToken.tokenStr + "\"" + " has not been declared");
				}*/
				// begin second argument analysis
				ResultValue resVal = null;
				if (!scan.nextToken.tokenStr.equals("to")) {
					// if its not the word "to" it must be an operator
					if (scan.nextToken.primClassif == 2) {
						// only call expr(...) if its not an equal sign
						if (!scan.nextToken.tokenStr.equals("=")) {
							resVal = expr(false);
						}
						if (scan.nextToken.tokenStr.equals("=")) {
							if (startIdent == null) {
								int prim = scan.currentToken.primClassif;
								int sub = scan.currentToken.subClassif;
								startIdent = new STIdentifier(scan.currentToken.tokenStr, prim, sub);
								startIdent.type = 2;
								st.table.put(scan.currentToken.tokenStr, startIdent);
								//st.putSymbol(scan.currentToken.tokenStr, startIdent);
							}
							assign(scan.currentToken);
						}
					}
					// now get the limit
					if (!scan.currentToken.tokenStr.equals("to")) {
						error("Missing " + "\"" + "to" + "\"" + " keyword in for loop");
					}
					scan.getNext();
					// begin getting the limit in the for loop
					int end = 0;
					boolean funcFlag = false;
					STIdentifier limitIdent = null;
					if (scan.currentToken.tokenStr.equals("LENGTH") ||
							scan.currentToken.tokenStr.equals("ELEM") ||
							scan.currentToken.tokenStr.equals("MAXELEM")) {
						resVal = expr(false);
						funcFlag = false;
						end = Integer.parseInt(resVal.value);
					}
					else {
						limitIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
					}
					int start = Integer.parseInt(startIdent.value);
					if (limitIdent == null) {
						if (!scan.currentToken.tokenStr.equals(":")) {
							if (scan.currentToken.tokenStr.equals("by")) {
								scan.getNext();
							}
							// if its an identifier and not in the symbol table, then error
							if (scan.currentToken.subClassif == 1) {
								error("\"" + scan.currentToken.tokenStr + "\"" + " is an undeclared identifier");
							}
							// if the limit is not a integer or float constant, then error
							else if (scan.currentToken.subClassif != 2 && scan.currentToken.subClassif != 3) {
								error("\"" + scan.currentToken.tokenStr + "\"" + " is not a valid ending limit");
							}
							// if its a valid integer or float constant, assign the ending value to it
							else {
								if (funcFlag == false && end == 0) {
									end = Integer.parseInt(scan.currentToken.tokenStr);
								}
							}
						}
					}
					else {
						// we found the identifier in the symbol table, make sure it has a value
						if (limitIdent.value.equals("NO VALUE")) {
							// if the limitIdent is a scalar, ie. not an array, then error
							if (limitIdent.structure == 100) {
								error("The identifier " + "\"" + limitIdent.symbol + "\"" + " has no value");
							}
						}
						// if the look ahead is an operator or the limit is an array, then call expr()
						// and assign the value to end
						if (scan.nextToken.primClassif == 2 || limitIdent.structure == -100) {
							resVal = expr(false);
							end = Integer.parseInt(resVal.value);
						}
						else {
							// if the look ahead is not an operator, then it must be a colon, error if its neither
							if (!scan.currentToken.tokenStr.equals(":") && !scan.nextToken.tokenStr.equals(":")) {
								error("\"" + scan.nextToken.tokenStr + "\"" + " must be a colon " + "\"" + ":" + "\"" + " or an operator");
							}
							// if the look ahead is a colon, then we have reached the end of the for loop condition
							// so assign the symbol table entry to incr
							end = Integer.parseInt(limitIdent.value);
						}					}
					// end getting the limit in the for loop,

					// check if there is an incr variable
					if (scan.currentToken.tokenStr.equals("by") || scan.nextToken.tokenStr.equals("by") ||
							scan.nextToken.tokenStr.equals(":")) {
						if (!scan.nextToken.tokenStr.equals(":")) {
							scan.getNext();
						}
						if (scan.currentToken.tokenStr.equals("by")) {
							scan.getNext();
						}
						// begin getting the incr in the for loop
						funcFlag = false;
						STIdentifier incrIdent = null;
						if (scan.currentToken.tokenStr.equals("LENGTH") ||
								scan.currentToken.tokenStr.equals("ELEM") ||
								scan.currentToken.tokenStr.equals("MAXELEM")) {
							resVal = expr(false);
							funcFlag = true;
							incr = Integer.parseInt(resVal.value);
						}
						else {
							incrIdent = (STIdentifier) st.getSymbol(scan.currentToken.tokenStr);
						}
						// check if the incr is an identifier
						if (incrIdent == null) {
							// if its an identifier and not in the symbol table, then error
							if (scan.currentToken.subClassif == 1) {
								error("\"" + scan.currentToken.tokenStr + "\"" + " is an undeclared identifier");
							}
							// if the incr is not a integer or float constant, then error
							else if (scan.currentToken.subClassif != 2 && scan.currentToken.subClassif != 3) {
								error("\"" + scan.currentToken.tokenStr + "\"" + " is not a valid ending limit");
							}
							// if its a valid integer or float constant, assign the ending value to it
							else {
								if (funcFlag == false) {
									incr = Integer.parseInt(scan.currentToken.tokenStr);
								}							
							}
						}
						else {
							// we found the identifier in the symbol table, make sure it has a value
							if (incrIdent.value.equals("NO VALUE")) {
								// if the incrIdent is a scalar, ie. not an array, then error
								if (incrIdent.structure == 100) {
									error("The identifier " + "\"" + incrIdent.symbol + "\"" + " has no value");
								}

							}
							// if the look ahead is an operator or the incr is an array then call expr() and
							// assign the value to incr
							if (scan.nextToken.primClassif == 2 || incrIdent.structure == -100) {
								resVal = expr(false);
								incr = Integer.parseInt(resVal.value);
							}
							else {
								// if the look ahead is not an operator, then it must be a colon, error if its neither
								if (!scan.currentToken.tokenStr.equals(":") && !scan.nextToken.tokenStr.equals(":")) {
									error("\"" + scan.nextToken.tokenStr + "\"" + " must be a colon " + "\"" + ":" + "\"" + " or an operator");
								}
								// if the look ahead is a colon, then we have reached the end of the for loop condition
								// so assign the symbol table entry to incr
								incr = Integer.parseInt(incrIdent.value);
							}

						}
					}
						// start is the initial value for i. ie. "for i = 0 ..."
						controlVar = start;
						int difference = 0;
						//for (i = start; i < end; i++) {
						while (controlVar < end) {
							savedScanner = this.scan.saveState();
							statements(true);
							
							// was the control variable incremented by the programmer?
							if (Integer.parseInt(startIdent.value) != controlVar) {	
								// if true then update the control variable according to the symbol table entry
								difference = Integer.parseInt(startIdent.value) - controlVar;
								controlVar += difference;
							}
							
							// increment control variable by the incr
							controlVar += incr;
							
							// update the control variable symbol table entry
							startIdent.value = String.valueOf(controlVar);
							
							// only reset the buffer to top of loop if we are running the loop again
							if (controlVar < end) {
								this.scan = savedScanner;
							}
						}
						// end for loop execution
					}
					else {
						// there must be either a colon ":" or the "by" keyword, so throw error
						if (!scan.nextToken.tokenStr.equals("by")) {
							error("Missing " + "\"" + ":" + "\"" + " or " + "\"" + "by" + "\"" + " keyword in for loop");
						}
					}
				// if its not the keyword "to" or an operator then error
		    	/*else {
		    		error("\"" + scan.nextToken.tokenStr + "\"" + " is not a valid token");
		    	}*/
			}
		}
		// if the first argument is not an operand then error
		else {
			error("First argument is not a valid operand: " + "\"" + scan.currentToken.tokenStr + "\"");
		}
		// end first argument analysis
		// now we skip to the endfor if we are not already there
		while (!scan.currentToken.tokenStr.equals("endfor") &&
				!scan.currentToken.tokenStr.equals("for") &&
				scan.currentToken.primClassif != scan.currentToken.EOF) {
			scan.getNext();
		}
		// if the current token is a "for" or EOF, then error, we did not find a matching endfor
		if (scan.currentToken.tokenStr.equals("for")) {
			error("No terminating " + "\"" + "endfor" + "\"" + " for for loop");
		}
		else if(scan.currentToken.primClassif == scan.currentToken.EOF) {
			error("No terminating " + "\"" + "endfor" + "\"" + " for for loop", startToken);
		}
		// make sure there is a terminating semicolon after the endfor
		if (!scan.nextToken.tokenStr.equals(";")) {
			error("No terminating " + "\"" + ";" + "\"" + " after endfor");
		}
	}

    // assumes that this is called when currentToken = to the first operand
    // stops at the next token after the expression
    public ResultValue expr(boolean funcCall) throws Exception {
        Stack<Token> mainStack = new Stack<Token>();
        ArrayList<Token> postAList = new ArrayList<Token>();
        Token tok = new Token();
        Token popped = new Token();

        boolean bFound, bArrayFound;

        tok = scan.currentToken;
        startOfExprToken = scan.currentToken.saveToken();

    	// go through the expr and end if there isn't a token
    	// that can be in a expr
    	while ((! tok.tokenStr.equals("print")) && 
    			(tok.primClassif == Token.OPERAND
    			|| tok.primClassif == Token.OPERATOR
    			|| tok.primClassif == Token.FUNCTION
    			|| tok.tokenStr.equals("(")
    			|| tok.tokenStr.equals(")")
    			|| tok.tokenStr.equals("]"))) {
    		tok.setPrecedence();
    		switch (tok.primClassif) {
    			case Token.OPERAND:
    				if (tok.tokenStr.equals("to") || tok.tokenStr.equals("by")) {
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
    						postAList.add(tok);
    					}
    				} catch (Exception e) {
    					// operand is a Numeric add it to the postfix
    					postAList.add(tok);
    				}
    				
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
    	    		    		if (popped.isElemRef)
    	    		    			error("Missing ']' separator", popped);
    							postAList.add(popped);
    							if (popped.primClassif == Token.FUNCTION) {
    								bFound = true;
    								break;
    							}
    						}
    						if (!bFound && funcCall) {   // no matching left paren but a func has been called ie print()
    							// TODO: add function implementations here
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
    						break;
                        case "]":
	                        bArrayFound = false;
	                        
	                        while (! mainStack.isEmpty()) {
	                        	popped = mainStack.pop();
	                        	postAList.add(popped);
	                        	
	                        	if (popped.isArray) {
	                        		bArrayFound = true;
	                        		break;
	                        	}
	                        	
	                        }
	
	                        if (!bArrayFound && funcCall) {
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

    // TODO: Edit error messages so they're more useful to end users.
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
	    			// TODO: add functionality to properly accept arrays and functions
	    			if (currToken.isElemRef) {
	    				STEntry stArray = st.getSymbol(currToken.tokenStr);
	                    if (stArray == null) {
	                        error("Symbol '"+currToken.tokenStr+"' is not in Symbol Table.", currToken);
	                    }
	    				try {
		    				tokOp2 = (Token) stk.pop(); // grab the right operand (the only operand)
		    			} catch (EmptyStackException a) {
		    				error("Missing right expression operand for array element reference.", currToken);
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
	    				resOp2.structure.add("ARRAY ELEM REF");
	    				
	    				nOp2 = new Numeric(this, resOp2, currToken.tokenStr, "2nd Operand"); // must be a number
	    				resTemp = ((STIdentifier) stArray).getArray().get(nOp2.integerValue);
	    				
	    				extraToken1 = tokOp2.saveToken(); // get the most accurate values for the line and column # as possible
                        extraToken1.tokenStr = resTemp.value;
                        extraToken1.primClassif = Token.OPERAND;
                        extraToken1.subClassif = resTemp.type;
                        
                        stk.push(extraToken1);
                        
                        resMain.structure.add("ARRAY ELEM REF");
	    			} else {
	    				stk.push(currToken); // add the operand onto the stack for future use
	    			}
	    			break;
	    		case Token.OPERATOR:
	    			// since we have an operator we need to evaluate the top two operands
	    			// and because we are using a stack, the righthand operand
	    			// appears before the lefthand operand on a stack

	    			if (currToken.tokenStr.equals("u-")) { // unary minus is handled differently
	    				try {
		    				tokOp2 = (Token) stk.pop(); // grab the right operand (the only operand)
		    			} catch (EmptyStackException a) {
		    				error("Missing right expression operand for unary minus.", currToken);
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
		    				error("Missing expression operand or Invalid expression operand type. "
		    						+ "For operator: '" + currToken.tokenStr + "'", currToken);
		    			}
		    			try {
		    				tokOp1 = (Token) stk.pop(); // grab the left operand
		    			} catch (EmptyStackException b) {
		    				error("Missing expression operand or Invalid expression operand type. "
		    						+ "For operator: '" + currToken.tokenStr + "'", currToken);
		    			}

                        // set the ResultValue objects of the operands

		    			// check if its a variable if so, we need its real value and dataType
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
								error("Invalid operator in expression: '"
									+ currToken.tokenStr + "'", startOfExprToken);
		    			} // end of inner Operator switch

                        // add our new value to the stack
                        extraToken1 = tokOp1.saveToken(); // get the most accurate values for the line and column # as possible
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
    	                    error("Symbol '"+tokOp2+"' is not in Symbol Table.", tokOp2);
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
	    					resTemp = Utility.LENGTH(this, resOp2.value);
	    					break;
	    				case "SPACES":
	    					resTemp = Utility.SPACES(this, resOp2.value);
	    					break;
	    				case "ELEM":
	    					resTemp = Utility.ELEM(this, resOp2.value);
	    					break;
	    				case "MAXELEM":
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
                    	// return an array value
                    	resMain.value = ((STIdentifier) stExtra).array.val.toString();
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
            	extraToken2 = stk.pop(); // get array index
            	STIdentifier stArray = ((STIdentifier) st.getSymbol(poppedArray.tokenStr));
            	if (stArray == null) {
            		error("'" + poppedArray.tokenStr + "' is not in the SymbolTable", poppedArray);
            	}
            	resMain = stArray.array.get(Integer.parseInt(extraToken2.tokenStr));
            } else {
                error("Unbalanced Expression.", startOfExprToken); // this error should have been caught by all the other checks
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



