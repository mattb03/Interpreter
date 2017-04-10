package havabol;

import java.util.ArrayList;

public class STIdentifier extends STEntry

{
    public static final int SCALAR = 100;
    public static final int ARRAY = -100;
    public int structure;
    public StorageManager array;
    public char [] charArray;

    public STIdentifier (String symbol, int primClassif, int subClassif)
    {
        super(symbol, primClassif, subClassif);
        this.subClassifStr = "IDENTIFIER";
        this.structure = SCALAR;
    }
    // array identifier creation which has a storage manager for array manipulation and storage
    public STIdentifier (Parser parser, String symbol, int primClassif, int subClassif, int size, int type)
    {
        super(symbol, primClassif, subClassif);
        this.subClassifStr = "IDENTIFIER";
        this.array = new StorageManager(parser, size, type);
        this.structure = ARRAY;
        this.array.init(size, type);
    }

    public void setValue(int index, String item) {
        this.charArray = this.value.toCharArray();
        this.charArray[index] = item.charAt(0);
        this.value  = new String(this.charArray);
    }

    public StorageManager getArray() {  // maybe not needed. can just access array directly
        return this.array;
    }

    @Override
    public String toString()
    {
        return "STIdentifier [symbol= "+symbol+"  value= "+this.value+" primClassifStr= "+primClassifStr
        +", type= "+ subClassifStr+" dataType= " +type+"]";
    }
}
