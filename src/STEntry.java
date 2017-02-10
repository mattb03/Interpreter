package havabol;

import java.io.*;
import java.util.*;

public class STEntry
{
    public String symbol;
    public int primClassif;
    public String primClassifStr = "??";
    public int subClassif;
    public String subClassifStr = "VOID";

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
        +" type= "+ subClassifStr+"]";
	}
}
