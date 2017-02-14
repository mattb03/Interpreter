package havabol;

import java.io.*;
import java.util.*;

public class STControl extends STEntry
{
    public STControl (String symbol, int primClassif, int subClassif)
    {
    	super(symbol, primClassif, subClassif);
    	if (subClassif == 10)
    		this.subClassifStr = "FLOW";
    	else if (subClassif == 11)
    		this.subClassifStr = "END";
    	else if (subClassif == 12)
    		this.subClassifStr = "DECLARE";
    }

    @Override
    public String toString()
    {
        return "STControl [symbol= "+symbol+" primClassifStr= "+primClassifStr
        +", type= "+ subClassifStr+"]";
    }
}
