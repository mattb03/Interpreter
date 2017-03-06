package havabol;

public class STIdentifier extends STEntry
{
    public STIdentifier (String symbol, int primClassif, int subClassif)
    {
    	super(symbol, primClassif, subClassif);
        this.subClassifStr = "IDENTIFIER";
    }


    @Override
    public String toString()
    {
        return "STIdentifier [symbol= "+symbol+"  value= "+this.value+" primClassifStr= "+primClassifStr
        +", type= "+ subClassifStr+"]";
    }
}
