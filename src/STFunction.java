package havabol;

import java.io.*;
import java.util.*;

public class STFunction extends STEntry
{
    public String symbol;
    public int primClassif;
    public String primClassifStr;
    public String subClassif;
    public String returnType; // type/subClassif ie. Int, void
    public String definedBy;	// structure/defined by ie. builtin, user
    public int numArgs;
    public String parmList;
    public SymbolTable symbolTable;
    public String row;

    public STFunction (String symbol, int primClassif, 
                int returnTypeNum, int definedByNum, int numArgs)
    {
        super(symbol, primClassif);
    	this.symbol = symbol;
    	this.primClassif = primClassif;
    	this.primClassifStr = "function";
    	if (returnTypeNum == 2)
    		this.returnType = "Int";
    	else if (returnTypeNum == 3)
    		this.returnType = "Float";
    	else if (returnTypeNum == 4)
    		this.returnType = "boolean";
    	else if (returnTypeNum == 5)
    		this.returnType = "string";
    	else if (returnTypeNum == 6)
    		this.returnType = "date";
    	else if (returnTypeNum == 7)
    		this.returnType = "void";
        this.returnType = returnType;
        if (definedByNum == 13)
        	this.definedBy = "builtin";
        else if(definedByNum == 14)
        	this.definedBy = "user";
        this.definedBy = definedBy;
        this.numArgs = numArgs;
        //System.out.println("symbol: " + this.symbol + "\tprimclass: " + this.primClassif + "\treturn type: " + this.returnType + 
        //		"\tdefinedby: " + this.definedBy + "\tnumargs: " + this.numArgs);

        
        
    }

	@Override
	public String toString() {
		return "STFunction [symbol=" + symbol + ", primClassifStr=" + primClassifStr + ", primClassif=" + primClassif
				+ ", returnType=" + returnType + ", definedBy=" + definedBy + ", numArgs=" + numArgs + ", parmList="
				+ parmList + "]";
	}
}
