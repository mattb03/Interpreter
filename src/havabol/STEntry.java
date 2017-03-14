package havabol;

public class STEntry
{

    public static final int INTEGER = 2;
    public static final int FLOAT = 3;
    public static final int BOOLEAN = 4;
    public static final int STRING = 5;
    public static final int DATE = 6;
    public static final int VOID = 7;
    public String symbol;
    public int primClassif;
    public String primClassifStr = "??";
    public int subClassif;
    public String subClassifStr = "VOID";
    public String value = "NO VALUE";
    public int type = VOID;
    
    
    public STEntry(String symbol, int primClassif, int subClassif)
    {
        this.symbol = symbol;
        this.primClassif = primClassif;
        this.subClassif = subClassif;

        if (primClassif == Token.OPERAND)
            this.primClassifStr = "OPERAND";
        else if (primClassif == Token.OPERATOR)
            this.primClassifStr = "OPERATOR";
        else if (primClassif == Token.FUNCTION)
            this.primClassifStr = "FUNCTION";
        else if (primClassif == Token.CONTROL)
            this.primClassifStr = "CONTROL";
    }

    @Override
	public String toString() {
		return "STEntry [symbol= "+symbol+" primClassifStr= "+primClassifStr
        +", type= "+ subClassifStr+"]";
	}
}
