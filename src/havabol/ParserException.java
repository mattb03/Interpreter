package havabol;

@SuppressWarnings("serial")
public class ParserException extends Exception {

	public int iLineNr;
	public String diagnostic;
	public String sourceFileName;
	public String line;

	public ParserException(int iLineNr, String diagnostic, String sourceFileName, String line) {
		this.iLineNr = iLineNr;
		this.line = line;
		this.diagnostic = diagnostic;
		this.sourceFileName = sourceFileName;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
        sb.append("********** ERROR **********\n");
		sb.append("Line ");
		sb.append(Integer.toString(iLineNr));
		sb.append(" ");
		sb.append(line);
		sb.append("  ");
		sb.append(diagnostic);
		sb.append(" File: ");
		sb.append(sourceFileName);

		return sb.toString();
	}
}
