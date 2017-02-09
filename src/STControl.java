package havabol;

import java.io.*;
import java.util.*;

public class STControl extends STEntry 
{
    public String symbol;
    public int subClassif;
    public String subClassifStr;
    public String row;
    public int primClassif;
    public String primClassifStr;
    
    public STControl (String symbol, int primClassif, int subClassif) 
    {
    	super(symbol, primClassif);
    	this.symbol = symbol;
    	this.primClassifStr = "control";
    	this.subClassif = subClassif;
    	if (subClassif == 10)
    		this.subClassifStr = "flow";
    	else if (subClassif == 11)
    		this.subClassifStr = "end";
    	else if (subClassif == 12)
    		this.subClassifStr = "declare";
    	this.primClassif = primClassif;
    }

	@Override
	public String toString() {
		return "STControl [symbol=" + symbol + ", primClassifStr=" + primClassifStr + ", primClassif=" + primClassif
				+ ", subClassifStr=" + subClassifStr + ", subClassif=" + subClassif + "]";
	}
}
