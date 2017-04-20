package havabol;

public class STIdentifier extends STEntry
{
    // type

    public STIdentifier (String symbol, int primClassif, int subClassif)
    {
        super(symbol, primClassif, subClassif);
        this.subClassifStr = "IDENTIFIER";

    }

    /*public void putValue (STIdentifier ident, String value)
    {
        //table.get(symbol).value = "TX";
        ident.value = value;
    }*/

    @Override
    public String toString()
    {
        return "STIdentifier [symbol= "+symbol+"  value= "+this.value+" primClassifStr= "+primClassifStr
        +", type= "+ subClassifStr+" dataType= " +type+"]";
    }
}
