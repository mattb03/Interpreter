
package havabol;

//import java.util.*;
public class Token
{
    public String tokenStr = "";
    public int primClassif = 0;
    public int subClassif = 0;
    public int iSourceLineNr = 0;
    public int iColPos = 0;
    public boolean isArray = false;

    @Override
	public String toString() {
		return "Token [tokenStr=" + tokenStr + "]";
	}

	public int normPreced = 0;
    public int stkPreced = 0;
    // Constants for primClassif
    public static final int OPERAND = 1;    // constants, identifier
    public static final int OPERATOR = 2;   // + - * / < > = !
    public static final int SEPARATOR = 3;  // ( ) , : ; [ ]
    public static final int FUNCTION = 4;   // TBD
    public static final int CONTROL = 5;    // TBD
    public static final int EOF = 6;        // EOF encountered
    public static final int RT_PAREN = 7;   // TBD
    // Constants for OPERAND's subClassif
    public static final int IDENTIFIER = 1;
    public static final int INTEGER    = 2; // integer constant
    public static final int FLOAT      = 3; // float constant
    public static final int BOOLEAN    = 4; // boolean constant
    public static final int STRING     = 5; // string constant
    public static final int DATE       = 6; // date constant
    public static final int VOID       = 7; // void
    // Constants for CONTROL's subClassif  (after Pgm 1)
    public static final int FLOW       = 10;// flow statement (e.g., if)
    public static final int END        = 11;// end statement (e.g., endif)
    public static final int DECLARE    = 12;// declare statement (e.g., Int)
    // Constants for FUNCTION's subClassif (definedby)
    public static final int BUILTIN    = 13;// builtin function (e.g., print)
    public static final int USER       = 14;// user defined

    // array of primClassif string values for the constants
    public static final String[] strPrimClassifM =
        {"Undefined"
            , "OPERAND"     // 1
            , "OPERATOR"    // 2
            , "SEPARATOR"   // 3
            , "FUNCTION"    // 4
            , "CONTROL"     // 5
            , "EOF"         // 6
        };
    public static final int PRIM_CLASS_MAX = 6;
    // array of subClassif string values for the constants
    public static final String[] strSubClassifM =
        {"Undefined"
            , "IDENTFIER"   // 1
            , "INTEGER"     // 2
            , "FLOAT"       // 3
            , "BOOLEAN"     // 4
            , "STRING"      // 5
            , "DATE"        // 6
            , "Void"        // 7
            , "**not used**"// 8
            , "**not used**"// 9
            , "FLOW"        //10
            , "END"         //11
            , "DECLARE"     //12
        };
    public static final int OPERAND_SUB_CLASS_MIN = 1;
    public static final int OPERAND_SUB_CLASS_MAX = 7;
    public static final int CONTROL_SUB_CLASS_MIN = 10;
    public static final int CONTROL_SUB_CLASS_MAX = 12;

    public Token(String value)
    {
        this.tokenStr = value;
        // ??
    }
    public Token()
    {
        this("");   // invoke the other constructor
    }

    public void printToken()
    {
        String primClassifStr;
        String subClassifStr;
        // convert the primClassif to a string
        if (primClassif >= 0
            && primClassif <= PRIM_CLASS_MAX)
            primClassifStr = strPrimClassifM[primClassif];
        else
            primClassifStr = "**garbage**";

        // convert the subClassif to a string
        switch(primClassif)
        {
            case Token.OPERAND:
                if (subClassif >= OPERAND_SUB_CLASS_MIN
                        && subClassif <= OPERAND_SUB_CLASS_MAX)
                    subClassifStr = strSubClassifM[subClassif];
                else
                    subClassifStr = "**garbage**";
                break;
            case Token.CONTROL:
                if (subClassif >= CONTROL_SUB_CLASS_MIN
                        && subClassif <= CONTROL_SUB_CLASS_MAX)
                    subClassifStr = strSubClassifM[subClassif];
                else
                    subClassifStr = "**garbage**";
                break;
            case Token.FUNCTION:
                if (subClassif == BUILTIN)
                    subClassifStr = "BUILTIN";
                else if (subClassif == USER)
                    subClassifStr = "USER";
                else
                    subClassifStr = "**garbage**";
                break;
            default:
                subClassifStr = "-";
        }

        System.out.printf("%-11s %-12s %s\n"
            , primClassifStr
            , subClassifStr
            , this.parseString(this));
    }

    public String hexPrint(int indent, String str)
    {
        StringBuilder retStr = new StringBuilder();
        int len = str.length();
        char[] charray = str.toCharArray();
        char ch;
        // print each character string
        for (int i = 0; i < len; i++)
        {
            ch = charray[i];
            if (ch == 0x00) // ignore nulls
                ;
            else if (ch > 31 && ch < 127) // ASCII printable charcters
                retStr.append(ch);
            else
               retStr.append(". ");
        }
        retStr.append("\n");
        // indent the second line to the number of specified spaces
        for (int i = 0; i < indent; i++)
        {
            retStr.append(" ");
        }
        // print the second line. Non-printable characters will be shown as
        // their hex value. Printable will simply be a space
        for (int i = 0; i < len; i++)
        {
            ch = charray[i];
            // only deal with the printable characters
            if (ch > 31 && ch < 127) // ASCII printable characters
                retStr.append(" ");
            else if (ch == 0x00)  // ignore nulls
                ;
            else
                retStr.append(String.format("%02X", (int)ch));
        }
        return retStr.toString();
    }


    public ResultValue toResult() {
    	return null;
    }

    public void setPrecedence() {
    	if (isArray) {
    		normPreced = 16;
    		stkPreced = 0;
    	} else if (primClassif != Token.OPERAND) {
    		String conditionals = "<><=>===!=";
	    	if (primClassif == SEPARATOR) {
	    		normPreced = 15;
	    		stkPreced = 2;
	    	} else if (tokenStr.equals("u-")) {
	    		normPreced = stkPreced = 12;
	    		// TODO: unary minus symbol in scanner "u-"
	    	} else if (tokenStr.equals("^")) {
	    		normPreced = 11;
	    		stkPreced = 10;
	    	} else if (tokenStr.equals("*")
	    			|| tokenStr.equals("/")) {
	    		normPreced = stkPreced = 9;
	    	} else if (tokenStr.equals("+")
	    			|| tokenStr.equals("-")) {
	    		normPreced = stkPreced = 8;
	    	} else if (tokenStr.equals("#")) {
	    		normPreced = stkPreced = 7;
	    	} else if (conditionals.contains(tokenStr)
	    			|| tokenStr.equals("in")
	    			|| tokenStr.equals("notin")) {
	    		normPreced = stkPreced = 6;
	    	} else if (tokenStr.equals("not")) {
	    		normPreced = stkPreced = 5;
	    	} else if (tokenStr.equals("and")
	    			|| tokenStr.equals("or")) {
	    		normPreced = stkPreced = 4;
	    	}
    	}
    }
    
    public Token saveToken() {
    	Token newToken = new Token("");
    	
    	newToken.tokenStr = this.tokenStr;
    	newToken.primClassif = this.primClassif;
        newToken.subClassif = this.subClassif;
        newToken.iSourceLineNr = this.iSourceLineNr;
        newToken.iColPos = this.iColPos;
        return newToken;
    }

    private String parseString(Token tok)
    {
        if (tok.subClassif == STRING)
        {
            return this.hexPrint(47, this.tokenStr);
        }
        else
        {
            return tok.tokenStr;
        }
    }
}
