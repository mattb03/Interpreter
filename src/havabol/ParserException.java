package havabol;

@SuppressWarnings("serial")
public class ParserException extends Exception {
	
	public int iLineNr;
	public String diagnostic;
	public String sourceFileName;
	
	public ParserException(int iLineNr, String diagnostic, String sourceFileName) {
		this.iLineNr = iLineNr;
		this.diagnostic = diagnostic;
		this.sourceFileName = sourceFileName;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Line ");
		sb.append(Integer.toString(iLineNr));
		sb.append(" ");
		sb.append(diagnostic);
		sb.append(", File: ");
		sb.append(sourceFileName);
		
		return sb.toString();
	}
}
