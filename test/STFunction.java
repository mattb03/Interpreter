package havabol;

public class STFunction extends STEntry
{
    public String returnType; // type/subClassif ie. Int, void
    public String definedBy;	// structure/defined by ie. builtin, user
    public int numArgs = 0;
    public String parmList = "NULL";
    public String row;

    public STFunction (String symbol, int primClassif,
                int returnTypeNum, int definedByNum, int numArgs)
    {
        super(symbol, primClassif, definedByNum);
    	if (returnTypeNum == 2)
    		this.returnType = "Int";
    	else if (returnTypeNum == 3)
    		this.returnType = "Float";
    	else if (returnTypeNum == 4)
    		this.returnType = "Bool";
    	else if (returnTypeNum == 5)
    		this.returnType = "String";
    	else if (returnTypeNum == 6)
    		this.returnType = "Date";
    	else if (returnTypeNum == 7)
    		this.returnType = "Void";
    	
        if (definedByNum == 13)
        	this.definedBy = "BUILTIN";
        else if(definedByNum == 14)
        	this.definedBy = "USER";
        
        this.numArgs = numArgs;
    }
    //Symbol ,primClassif, type, structure, defined by, parm, nonlocal
	@Override
	public String toString() {
		return "STFunction [symbol= "+symbol+", primClassifStr= "+
         primClassifStr+", returnType= "+returnType+", definedBy= "+
         definedBy+", numArgs= "+numArgs+", parmList= "+parmList+"]";
	}
}
