package havabol;

public class Numeric {

	public int integerValue;
	public double doubleValue;
	public String strValue;		// display value
	public int type;			// INTEGER, FLOAT
	
	public Numeric(Parser parser, ResultValue res, String op, String opPos) throws ParserException {
		try {
			integerValue = Integer.parseInt(res.value);
			doubleValue = (double) integerValue;
			strValue = Integer.toString(integerValue);
			type = Token.INTEGER;
		} catch (Exception e) {
			try {
				doubleValue = Double.parseDouble(res.value);
				integerValue = (int) doubleValue;
				strValue = Double.toString(doubleValue);
				type = Token.FLOAT;
			} catch (Exception f) {
				String error = opPos + " of " + op + " operand isn't a valid numeric type\n";
				throw new ParserException(parser.scan.currentToken.iSourceLineNr,  error
						, parser.scan.sourceFileNm, "");
			}
		}
	}
}
