package havabol;

public class STIdentifier extends STEntry
{
	public int dataType = 0;
	
    public STIdentifier (String symbol, int primClassif, int subClassif)
    {
    	super(symbol, primClassif, subClassif);
        this.subClassifStr = "IDENTIFIER";

    }

    // takes an integer that represents the data type
    public void setDataType (int type)
    {
    	dataType = type;
    }

    public void putValue (STIdentifier ident, String value)
    {
    	//table.get(symbol).value = "TX";
    	ident.value = value;
    }
    
    @Override
    public String toString()
    {
        return "STIdentifier [symbol= "+symbol+"  value= "+this.value+" primClassifStr= "+primClassifStr
        +", type= "+ subClassifStr+" dataType= " +dataType+"]";
    }
}
