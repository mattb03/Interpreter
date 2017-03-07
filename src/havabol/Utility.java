package havabol;

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
					, parser.scan.sourceFileNm);
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
					, parser.scan.sourceFileNm);
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
					, parser.scan.sourceFileNm);
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
					, parser.scan.sourceFileNm);
		}
		return res;
	}
	
	public static ResultValue concatNum(Parser parser, Numeric nOp1, Numeric nOp2) {
		ResultValue res = new ResultValue("");
		res.type = nOp1.type;
		res.value = nOp1.strValue + nOp2.strValue;
		return res;
	}
	
	public static ResultValue concatStr(Parser parser, String str1, String str2) {
		ResultValue res = new ResultValue("");
		res.type = Token.STRING;
		res.value = str1 + str2;
		return res;
	}
}
