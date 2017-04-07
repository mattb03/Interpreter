package havabol;
import java.util.*;


public class SymbolTable
{
    public static final String reservedSymbols = "def enddef";
    public static final String functionSymbols = "print LENGTH";
    public Map<String, STEntry> table;

    public SymbolTable()
    {
        table = new HashMap<String, STEntry>();
        initGlobal();
        // printTable();
    }

    public void printTable() {

    	for(Map.Entry<String, STEntry> entry : table.entrySet()) {
            System.out.println(entry.getValue());
        }
    	STEntry obj = getSymbol("MAXLENGTH");
    	if (obj instanceof STControl) {
    		STControl controlObj = (STControl)obj;
    		obj = (STControl)obj;
    		System.out.println("Control object: " + controlObj);
    	}
    	else if (obj instanceof STFunction) {
    		STFunction funcObj = (STFunction)obj;
    		obj = (STFunction)obj;
    		System.out.println("Function object: " + funcObj);
    	}
    	else if (obj instanceof STIdentifier) {
    		STIdentifier identObj = (STIdentifier)obj;
    		obj = (STIdentifier)obj;
    		System.out.println("Identifier object: " + identObj);
    	}
    	else {
    		System.out.println("Entry object: " + obj);
    	}
    }

    STEntry getSymbol(String symbol)
    {
        return table.get(symbol);
    }

    public void putSymbol(String symbol, STEntry entry)
    {
        int prim = entry.primClassif;
        int sub = entry.subClassif;
        if (prim == Token.OPERAND && sub == Token.IDENTIFIER)
        {
            table.put(symbol, new STIdentifier(symbol, prim, sub));
        }
        else if (prim == Token.OPERAND && sub == Token.INTEGER)
        {
            entry.subClassifStr = "INTEGER";
            table.put(symbol, entry);
        }
        else if (prim == Token.OPERAND && sub == Token.FLOAT)
        {
            entry.subClassifStr = "FLOAT";
            table.put(symbol, entry);
        }
        else if (prim == Token.OPERAND && sub == Token.STRING)
        {
            entry.subClassifStr = "STRING";
            table.put(symbol, entry);
        }
        else if (prim == Token.CONTROL)
        {
            table.put(symbol, new STControl(symbol, prim, sub));
        }
        else if (prim == Token.OPERATOR)
        {
            table.put(symbol, entry);
        }
    }

    public void putArray(String symbol, STIdentifier entry)
    {
        table.put(symbol, entry);
    }

    /*public void setDataType (STIdentifier ident, int type)
    {
    	String symbol = ident.symbol;

    	ident.setDataType(type);
    }*/

    /*public void putValue (STIdentifier ident, String value)
    {
    	//table.get(symbol).value = "TX";
    	ident.putValue(ident, value);
    }*/

	private void initGlobal ()
    {
        int VAR_ARGS = 0;
        table.put("def", new STControl("def",Token.CONTROL,Token.FLOW));
        table.put("enddef", new STControl("enddef",Token.CONTROL,Token.END));
        table.put("if", new STControl("if",Token.CONTROL,Token.FLOW));
        table.put("endif", new STControl("endif",Token.CONTROL,Token.END));
        table.put("else", new STControl("else",Token.CONTROL,Token.END));
        table.put("for", new STControl("for",Token.CONTROL,Token.FLOW));
        table.put("endfor", new STControl("endfor",Token.CONTROL,Token.END));
        table.put("while", new STControl("while",Token.CONTROL,Token.FLOW));
        table.put("endwhile", new STControl("endwhile",Token.CONTROL,Token.END));

        table.put("print", new STFunction("print",Token.FUNCTION,Token.VOID
                , Token.BUILTIN, VAR_ARGS));

        table.put("Int", new STControl("Int",Token.CONTROL,Token.DECLARE));
        table.put("Float", new STControl("Float",Token.CONTROL,Token.DECLARE));
        table.put("String", new STControl("String",Token.CONTROL,Token.DECLARE));
        table.put("Bool", new STControl("Bool",Token.CONTROL,Token.DECLARE));
        table.put("Date", new STControl("Date",Token.CONTROL,Token.DECLARE));

        table.put("LENGTH", new STFunction("LENGTH",Token.FUNCTION,Token.INTEGER
                , Token.BUILTIN, VAR_ARGS));
        table.put("MAXLENGTH", new STFunction("MAXLENGTH",Token.FUNCTION,Token.INTEGER
        		, Token.BUILTIN, VAR_ARGS));
        table.put("SPACES", new STFunction("SPACES",Token.FUNCTION,Token.INTEGER
                , Token.BUILTIN, VAR_ARGS));
        table.put("ELEM", new STFunction("ELEM",Token.FUNCTION,Token.INTEGER
                , Token.BUILTIN, VAR_ARGS));
        table.put("MAXELEM", new STFunction("MAXELEM",Token.FUNCTION,Token.INTEGER
                , Token.BUILTIN, VAR_ARGS));

        table.put("and", new STEntry("and", Token.OPERATOR, Token.VOID));
        table.put("or", new STEntry("or", Token.OPERATOR, Token.VOID));
        table.put("not", new STEntry("not", Token.OPERATOR, Token.VOID));
        table.put("in", new STEntry("in", Token.OPERATOR, Token.VOID));
        table.put("notin", new STEntry("notin", Token.OPERATOR, Token.VOID));
    }
}
