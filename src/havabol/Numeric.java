package havabol;

public class Numeric {

	public int integerValue;
	public double doubleValue;
	public String strValue;		// display value
	public int type;			// INTEGER, FLOAT

	public Numeric(Parser parser, ResultValue res, String op, String opPos) throws ParserException {
		if (res.value == null) {
			parser.error(opPos+" of '"+op+"' has not been initialized.", parser.startOfExprToken);
		}
		if (res.value.equals("NO VALUE")) {
			parser.error(opPos+" of '"+op+"' has not been initialized.", parser.startOfExprToken);
		}
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
				parser.error(opPos + " of " + op + " operator isn't a valid numeric type", parser.startOfExprToken);
			}
		}
	}
}
