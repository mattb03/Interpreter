package havabol;

import java.io.*;
import java.util.*;


public class STEntry 
{
    public String symbol;
    public int primClassif;
    public String primClassifStr;
    public String row;

    public STEntry(String symbol, int primClassif)
    {
        this.symbol = symbol;
        
        this.primClassifStr = "operator";

    }


    
    @Override
	public String toString() {
		return "STEntry [symbol=" + symbol + ", primClassifStr=" + primClassifStr + ", primClassif=" + primClassif
				+ "]";
	}
    
    
}
