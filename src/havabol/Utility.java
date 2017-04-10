package havabol;

import java.util.Collections;

public class Utility {

	public static ResultValue add(Parser parser, Numeric nOp1, Numeric nOp2) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = nOp1.type;
		if (nOp1.type == Token.INTEGER)
			res.value = Integer.toString((nOp1.integerValue + nOp2.integerValue));
		else if (nOp1.type == Token.FLOAT)
			res.value = Double.toString((nOp1.doubleValue + nOp2.doubleValue));
		else {
			String error = nOp1.strValue + " and/or " + nOp2.strValue + " are/is not a numeric type\n";
			throw new ParserException(parser.scan.currentToken.iSourceLineNr, error
					, parser.scan.sourceFileNm, "");
		}

		return res;
	}

	public static ResultValue subtract(Parser parser, Numeric nOp1, Numeric nOp2) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = nOp1.type;
		if (nOp1.type == Token.INTEGER)
			res.value = Integer.toString((nOp1.integerValue - nOp2.integerValue));
		else if (nOp1.type == Token.FLOAT)
			res.value = Double.toString((nOp1.doubleValue - nOp2.doubleValue));
		else {
			String error = nOp1.strValue + " and/or " + nOp2.strValue + " are/is not a numeric type\n";
			throw new ParserException(parser.scan.currentToken.iSourceLineNr, error
					, parser.scan.sourceFileNm, "");
		}
		return res;
	}

	public static ResultValue multiply(Parser parser, Numeric nOp1, Numeric nOp2) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = nOp1.type;
		if (nOp1.type == Token.INTEGER)
			res.value = Integer.toString((nOp1.integerValue * nOp2.integerValue));
		else if (nOp1.type == Token.FLOAT)
			res.value = Double.toString((nOp1.doubleValue * nOp2.doubleValue));
		else {
			String error = nOp1.strValue + " and/or " + nOp2.strValue + " are/is not a numeric type\n";
			throw new ParserException(parser.scan.currentToken.iSourceLineNr, error
					, parser.scan.sourceFileNm, "");
		}
		return res;
	}

	public static ResultValue divide(Parser parser, Numeric nOp1, Numeric nOp2) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = nOp1.type;
		if (nOp1.type == Token.INTEGER)
			res.value = Integer.toString((nOp1.integerValue / nOp2.integerValue));
		else if (nOp1.type == Token.FLOAT)
			res.value = Double.toString((nOp1.doubleValue / nOp2.doubleValue));
		else {
			String error = nOp1.strValue + " and/or " + nOp2.strValue + " are/is not a numeric type\n";
			throw new ParserException(parser.scan.currentToken.iSourceLineNr, error
					, parser.scan.sourceFileNm, "");
		}
		return res;
	}

	public static ResultValue expo(Parser parser, Numeric nOp1, Numeric nOp2) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = nOp1.type;
		if (nOp1.type == Token.INTEGER) {
			int i = (int) Math.pow(nOp1.integerValue, nOp2.integerValue);
			res.value = Integer.toString(i);
		} else if (nOp1.type == Token.FLOAT)
			res.value = Double.toString(Math.pow(nOp1.doubleValue, nOp2.doubleValue));
		else {
			String error = nOp1.strValue + " and/or " + nOp2.strValue + " are/is not a numeric type\n";
			throw new ParserException(parser.scan.currentToken.iSourceLineNr, error
					, parser.scan.sourceFileNm, "");
		}
		return res;
	}

	public static ResultValue concat(Parser parser, ResultValue resOp1, ResultValue resOp2) {
		ResultValue res = new ResultValue("");
		res.type = resOp1.type;
		res.value = resOp1.value + resOp2.value;
		return res;
	}

	public static ResultValue negative(Parser parser, Numeric nOp2) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = nOp2.type;
		if (nOp2.type == Token.INTEGER)
			res.value = Integer.toString(-nOp2.integerValue);
		else if (nOp2.type == Token.FLOAT)
			res.value = Double.toString(-nOp2.doubleValue);
		else
			parser.error(nOp2.strValue + "is not a numeric type.");
		return res;
	}

	public static ResultValue lessThan(Parser parser, ResultValue resOp1, ResultValue resOp2, boolean equals) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = Token.BOOLEAN;
		if (resOp1.type == Token.INTEGER || resOp1.type == Token.FLOAT) {
			if (equals) {
				Numeric nOp1 = new Numeric(parser, resOp1, "<=", "1st Operand");
				Numeric nOp2 = new Numeric(parser, resOp2, "<=", "2nd Operand");
				if (resOp1.type == Token.INTEGER)
					res.value = Boolean.toString(nOp1.integerValue <= nOp2.integerValue);
				else
					res.value = Boolean.toString(nOp1.doubleValue <= nOp2.doubleValue);
			} else {
				Numeric nOp1 = new Numeric(parser, resOp1, "<", "1st Operand");
				Numeric nOp2 = new Numeric(parser, resOp2, "<", "2nd Operand");
				if (resOp1.type == Token.INTEGER)
					res.value = Boolean.toString(nOp1.integerValue < nOp2.integerValue);
				else
					res.value = Boolean.toString(nOp1.doubleValue < nOp2.doubleValue);
			}
			if (res.value.equals("true")) {
				res.value = "T";
			} else {
				res.value = "F";
			}
		} else {
			if (resOp1.value.compareTo(resOp2.value) < 0) {
				res.value = "T";
			} else if (resOp1.value.compareTo(resOp2.value) > 0) {
				res.value = "F";
			} else {
				if (equals)
					res.value = "T";
				else
					res.value = "F";
			}
		}
		return res;

	}

	public static ResultValue greaterThan(Parser parser, ResultValue resOp1, ResultValue resOp2, boolean equals) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = Token.BOOLEAN;
		if (resOp1.type == Token.INTEGER || resOp1.type == Token.FLOAT) {
			if (equals) {
				Numeric nOp1 = new Numeric(parser, resOp1, ">=", "1st Operand");
				Numeric nOp2 = new Numeric(parser, resOp2, ">=", "2nd Operand");
				if (resOp1.type == Token.INTEGER)
					res.value = Boolean.toString(nOp1.integerValue >= nOp2.integerValue);
				else
					res.value = Boolean.toString(nOp1.doubleValue >= nOp2.doubleValue);
			} else {
				Numeric nOp1 = new Numeric(parser, resOp1, ">", "1st Operand");
				Numeric nOp2 = new Numeric(parser, resOp2, ">", "2nd Operand");
				if (resOp1.type == Token.INTEGER)
					res.value = Boolean.toString(nOp1.integerValue > nOp2.integerValue);
				else
					res.value = Boolean.toString(nOp1.doubleValue > nOp2.doubleValue);
			}
			if (res.value.equals("true")) {
				res.value = "T";
			} else {
				res.value = "F";
			}
		} else {
			if (resOp1.value.compareTo(resOp2.value) < 0) {
				res.value = "F";
			} else if (resOp1.value.compareTo(resOp2.value) > 0) {
				res.value = "T";
			} else {
				if (equals)
					res.value = "T";
				else
					res.value = "F";
			}
		}
		return res;
	}

	public static ResultValue equals(Parser parser, ResultValue resOp1, ResultValue resOp2, boolean not) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = Token.BOOLEAN;
		if (resOp1.type == Token.INTEGER || resOp1.type == Token.FLOAT) {
			if (not) {
				Numeric nOp1 = new Numeric(parser, resOp1, "!=", "1st Operand");
				Numeric nOp2 = new Numeric(parser, resOp2, "!=", "2nd Operand");
				if (resOp1.type == Token.INTEGER)
					res.value = Boolean.toString(nOp1.integerValue != nOp2.integerValue);
				else
					res.value = Boolean.toString(nOp1.doubleValue != nOp2.doubleValue);
			} else {
				Numeric nOp1 = new Numeric(parser, resOp1, "==", "1st Operand");
				Numeric nOp2 = new Numeric(parser, resOp2, "==", "2nd Operand");
				if (resOp1.type == Token.INTEGER)
					res.value = Boolean.toString(nOp1.integerValue == nOp2.integerValue);
				else
					res.value = Boolean.toString(nOp1.doubleValue == nOp2.doubleValue);
			}
			if (res.value.equals("true")) {
				res.value = "T";
			} else {
				res.value = "F";
			}
		} else {
			if (resOp1.value.compareTo(resOp2.value) < 0) {
				if (not)
					res.value = "T";
				else
					res.value = "F";
			} else if (resOp1.value.compareTo(resOp2.value) > 0) {
				if (not)
					res.value = "T";
				else
					res.value = "F";
			} else {
				if (not)
					res.value = "F";
				else
					res.value = "T";
			}
		}
		return res;

	}

	public static ResultValue booleanConditionals(Parser parser, ResultValue resOp1, ResultValue resOp2, String op) throws ParserException {
		ResultValue res = new ResultValue("");
		res.type = Token.BOOLEAN;
		int type1, type2;
		type1 = Token.VOID;
		type2 = Token.VOID;
		if (resOp1.value.equals("T") || resOp1.value.equals("F")) {
			type1 = Token.BOOLEAN;
		}
		if (resOp2.value.equals("T") || resOp2.value.equals("F")) {
			type2 = Token.BOOLEAN;
		}

		if (type1 == Token.BOOLEAN && type2 == Token.BOOLEAN) {
			switch (op) {
				case "not":
					res.value = Boolean.toString(!(resOp1.value.equals(resOp2.value)));
					break;
				case "and":
					res.value = Boolean.toString(resOp1.value.equals(resOp2.value));
					break;
				case "or":
					if (resOp1.value.equals("T") || resOp2.value.equals("T"))
						res.value = "T";
					else
						res.value = "F";
					break;
				default:
					parser.error("Invalid boolean operator '" + op +"'.");
			}
			if (res.value.equals("true")) {
				res.value = "T";
			}else if (res.value.equals("false")) {
				res.value = "F";
			}
			return res;
		} else {
			if (type1 != Token.BOOLEAN)
				parser.error("1st Operand of '" + op + "' isn't a valid boolean variable.");
			else
				parser.error("2nd Operand of '" + op + "' isn't a valid boolean variable.");

			return null; // unreachable code
		}
	}
    
	// returns the length of the string
    public ResultValue LENGTH (Parser parser, String str) {
    	ResultValue resVal = new ResultValue(String.valueOf(str.length()));
    	resVal.type = 2;
    	resVal.structure.set(0, "primitive");
    	return resVal;
    }
    
    // returns T if the string is empty or nothing but spaces, F otherwise
    public ResultValue SPACES (Parser parser, String str) {
    	int i;
    	char array[] = str.toCharArray();    	
    	ResultValue resVal = new ResultValue(String.valueOf("F"));
    	resVal.type = 4;
    	resVal.structure.set(0, "primitive");
    	for (i = 0; i < array.length; i++) {
    		if (array[i] != ' ') {
    			return resVal;
    		}
    	}
    	resVal.value = String.valueOf("T");
    	return resVal;
    }
    
    // returns the number initialized elements
    public ResultValue ELEM (Parser parser, String array) {
    	STIdentifier arrayIdent = (STIdentifier) parser.st.getSymbol(array);
    	int i = 0;
    	int count = 0;
    	for (i = 0; i < arrayIdent.array.val.size(); i++) {
    		if (arrayIdent.array.val.get(i) != null) {
    			count++;
    		}
    	}
    	ResultValue resVal = new ResultValue(String.valueOf(count));
    	resVal.type = 2;
    	resVal.structure.set(0, "primitive");
    	return resVal;
    }
    
    // returns the number of elements in the array, whether initialized or not
    public Object MAXELEM (Parser parser, String array) {
    	STIdentifier arrayIdent = (STIdentifier) parser.st.getSymbol(array);
    	Collections.sort(arrayIdent.array.val);
    	ResultValue resVal = new ResultValue(String.valueOf(arrayIdent.array.val.size()));
    	resVal.type = 2;
    	resVal.structure.set(0, "primitive");
    	return resVal;
    }

}
