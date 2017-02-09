package havabol;

import java.io.*;
import java.util.*;

public class STIdentifier extends STEntry
{
    public String symbol;
    public int primClassif;
    public String primClassifStr;

    public STIdentifier (String symbol, int primClassif)
    {
    	super(symbol, primClassif);
        this.symbol = symbol;
        
    }
}
