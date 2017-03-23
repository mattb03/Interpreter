package havabol;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Scanner {
	public String sourceFileNm;
    public String buffer;
    public Token currentToken;
    public Token nextToken;
    public Debug debugger;
    public int col = 1;
    public int line = 1;
    public boolean exit;
    public boolean opCombine;
    public boolean aComment;
    public String lines[];
    public int lastLine;
    public int blankLines = -1;
    private final static String operators = "+-*/<>!=#^";
    private final static String separators = "():;[],";
    private final static String delimiters = " \t;:()\'\"=!<>+-*/[]#^\n,";
    private final static String escChars = "\"\'\\nat";
    private final static String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static String numbers = "0123456789";
    public static final String MALFORMED_NUM = "MALFORMED_NUM";
    public static final String NON_TERMINATED_STRING = "NON_TERMINATED_STRING";
    public static final String INVALID_OPERATOR = "INVALID_OPERATOR";
    public static final String INVALID_ESC = "ILLEGAL_ESCAPE_CHARACTER";

    public Scanner(){

    }

    public Scanner(String SourceFileNm, Debug debugger) throws IOException, Exception
    {
        this.debugger = debugger;
    	this.sourceFileNm = SourceFileNm;
        // create our buffer to iterate thru
        this.buffer = new String(Files.readAllBytes(Paths.get(SourceFileNm)));
        int i = this.buffer.length() - 1;
        boolean stopFlag = false;
        // this loop is to gather the number of blank lines at eof
        // to print later
        while (stopFlag == false)
        {
        	if (this.buffer.charAt(i) == '\n') {
        		this.blankLines++;
        	}
        	else
        		stopFlag = true;
        	i--;
        }
        // create our String array of lines
        this.lines = this.buffer.split("\n");
        this.exit = false;
        this.lastLine = -1;
        this.opCombine = false;
        this.aComment = false;
        this.getNext();

    }
    @Override
	public String toString() {
		return "Scanner [currentToken=" + currentToken + ", nextToken=" + nextToken + "]";
	}
	/**
    *This method provides the logic after a token is grabbed. The logic is for
    *deciding how to classify the token or to throw an exception if it is
    *malformed.  i.e.  23.4.6
    *@param value - this is the String the we will evaluate
    *@return void - Nothing will be returned as we will call other functions to set the currentToken
    *@throws Exception  - if a number is malformed, will throw an Exception via the handleErrors method
    *<p>
    */
    public void process(String value) throws Exception
    {
        if (this.currentToken != null) {
            String curVal = this.currentToken.tokenStr;
            if ( ( (operators.indexOf(curVal.charAt(0)) != -1) || curVal.equals("if") ||
                curVal.equals("while") || curVal.equals(",") || curVal.equals("(")) && value.equals("-") ) {
                this.nextToken.primClassif = Token.OPERATOR;
                this.nextToken.subClassif = Token.VOID;
                this.nextToken.tokenStr = "u-";
                return;
            }
        }
        // first val of token a quote? then its a string
        if (value.charAt(0) == '"' || value.charAt(0) == '\'')
        {
            this.strEval(value);
        }
         // is first val a number?
        else if (numbers.indexOf(value.charAt(0)) != -1)
        {
            try
            {   // try to parse as an integer, if so it is an int. if not...
                Integer.decode(value);
                this.intEval();
                return;
            }
            catch (NumberFormatException e)
            {
                try
                {
                    // ... try to parse as a double, if so it is a float. if not...
                    Double.parseDouble(value);
                    this.floatEval();
                    return;
                }
                catch (NumberFormatException e2)
                {
                    // ... throw exception as it is a malformed number
                    this.handleErrors(value, MALFORMED_NUM);
                }
            }
        }
        // if the token's len is 1 and its NOT a letter, eval as either a sep or op
        else if (value.length() == 1 && letters.indexOf(value.charAt(0)) == -1)
        {
            this.delEval(value);
        }
        else
        {
            // if it got this far it must be an identifier or and keyword operator
            this.idEval(value);
        }
        // logic to handle concatenation of operators ie  <=, >=, !=, ^, ==
        if (this.currentToken != null && this.nextToken.primClassif == Token.OPERATOR && this.currentToken.primClassif == Token.OPERATOR &&
                (this.nextToken.iColPos - this.currentToken.iColPos == 1))
        {
            this.currentToken.tokenStr += this.nextToken.tokenStr;
            this.opCombine = true;
            this.getNext();
        }
        if (this.opCombine)
        {
            String str = this.currentToken.tokenStr;
            if ((str.length() > 1 ) && (str.equals("<=") || str.equals(">=") ||
                str.equals("!=") || str.equals("==") || str.equals("-=") ||
                str.equals("+=") ))
            {
                this.opCombine = false;
            }
            else
            {
                this.handleErrors(str, INVALID_OPERATOR);
            }
        }
    }
   /**
    *This method sets the currentToken attributes to correspond to an integer
    *@return void - nothing to be returned
    *<p>
    */
    public void intEval()
    {

        this.nextToken.primClassif = Token.OPERAND;
        this.nextToken.subClassif = Token.INTEGER;
    }
   /**
    *This method sets the currentToken attributes to correspond to a float
    *@return void - nothing to be returned
    *<p>
    */
    public void floatEval()
    {
        this.nextToken.primClassif = Token.OPERAND;
        this.nextToken.subClassif = Token.FLOAT;
    }
   /**
    *This method sets the currentToken attributes to correspond to an identifier
    *@return void - nothing to be returned
    *<p>
    */
    public void idEval(String value)
    {
        //and", "or", "not", "in", "notin
        if (value.equals("and") || value.equals("or") || value.equals("not") || value.equals("in") || value.equals("notin"))
        {
            this.nextToken.primClassif = Token.OPERATOR;
            this.nextToken.subClassif = Token.VOID;
        }
        // control flow/end tokens  if, endif, else,  while, endwhile, for, endfor
        else if (value.equals("if") || value.equals("while") || value.equals("for"))
        {
            this.nextToken.primClassif = Token.CONTROL;
            this.nextToken.subClassif = Token.FLOW;
        }
        else if (value.equals("endif") || value.equals("else") || value.equals("endwhile") || value.equals("endfor"))
        {
            this.nextToken.primClassif = Token.CONTROL;
            this.nextToken.subClassif = Token.END;
        }
        // recognize control declare tokens (Int, Float, String, Bool)
        else if (value.equals("Int") || value.equals("Float") || value.equals("String") || value.equals("Bool"))
        {
            this.nextToken.primClassif = Token.CONTROL;
            this.nextToken.subClassif = Token.DECLARE;
        }
        else if (value.equals("print"))
        {
        	this.nextToken.primClassif = Token.FUNCTION;
        	this.nextToken.subClassif = Token.BUILTIN;
        }
        else if (value.equals("T") || value.equals("F"))
        {
            this.nextToken.primClassif = Token.OPERAND;
            this.nextToken.subClassif = Token.BOOLEAN;
        }
        else
        {
            this.nextToken.primClassif = Token.OPERAND;
            this.nextToken.subClassif = Token.IDENTIFIER;
        }
    }
   /**
    *This method sets the currentToken attributes to correspond to a String
    *@return void - nothing to be returned
    *<p>
    */
    public void strEval(String value) throws Exception
    {

        char array[] = value.toCharArray();
        array[0] = 0x0;
        array[array.length-1] = 0x0;
        for (int i=0; i < array.length; i++)
        {
            if (array[i] == '\\' && escChars.indexOf(array[i+1]) == -1)
            {
                this.handleErrors(value, INVALID_ESC);
            }
            if (array[i] =='\\' && array[i+1] == 't')
            {
                array[i] = 0x00;
                array[i+1] = 0x09;
                i++;
            }
            else if (array[i] == '\\' && array[i+1] == 'n')
            {
                array[i] = 0x00;
                array[i+1] = 0x0a;
                i++;
            }
            else if (array[i] == '\\' && array[i+1] == '\\')
            {
                array[i] = 0x00;
                i++;
            }
            else if (array[i] == '\\' && array[i+1] == 'a')
            {
                array[i] = 0x00;
                array[i+1] = 0x07;
                i++;
            }
            else if (array[i] == '\\' && array[i+1] == '\'')
            {
                array[i] = 0x00;
                i++;
            }
            else if (array[i] == '\\' && array[i+1] == '\"')
            {
                array[i] = 0x00;
                i++;
            }
        }
        this.nextToken.tokenStr = String.valueOf(array);
        this.nextToken.primClassif = Token.OPERAND;
        this.nextToken.subClassif = Token.STRING;
    }
   /**
    *This method sets the currentToken attributes to correspond to an operator
    *or a separator or right parenthesis
    *@param value  -  this is the String to be classified and used to set
    *@return void - nothing to be returned
    *<p>
    */
    public void delEval(String value)
    {
        if (operators.indexOf(value.charAt(0)) != -1)
        {
            this.nextToken.primClassif = Token.OPERATOR;
            this.nextToken.subClassif = Token.VOID;
        }
        else if (separators.indexOf(value.charAt(0)) != -1)
        {
            this.nextToken.primClassif = Token.SEPARATOR;
            if (value.charAt(0) == ')')
            {
                this.nextToken.subClassif = Token.RT_PAREN;
            }
            else
            {
                this.nextToken.subClassif = Token.VOID;
            }
        }
    }
    /**
    *This method throws exceptions for errors up to the process method and
    *prints out appropriate error message
    *@param value - String(token) used in error message
    *@param errVal - String(particular error Message) we will print out
    *@throws Exception - this exception is thrown on error up to process()
    */
    public void handleErrors(String value, String errVal) throws Exception
    {
        int col;
        if (errVal.equals(INVALID_OPERATOR))
        {
            col = this.currentToken.iColPos;
        }
        else
        {
            col = this.col;

        }
        System.out.println("\n********** ERROR **********");
        System.out.printf("%s  %s  at line %d, column %d\n", errVal, value, this.line, col);
        throw new Exception();
    }

    public String getNext() throws Exception
    {
        int i;
        String retVal = "";
        boolean finCom = false;
        if (this.nextToken != null && !this.opCombine)
            this.currentToken = this.nextToken;
        // if global exit state is true? return empty string
        if (this.exit == true) {
            return "";
        }
        // if there are only line feeds left in our buffer, weve hit the end of file
        if (this.buffer.matches("[\\s]+") || this.buffer.isEmpty())
        {
        	int lineNum = this.lines.length + 1;
        	// this loop is to print the blank lines at eof if any
    		for (i = 0; i <= this.blankLines; i++) {
	    		lineNum++;
	    	}
    		// print the last line only if its a comment
            // set EOF param's in currentToken, set exit state to true, return " ", so we can come back one more time
            this.nextToken = new Token();
            this.nextToken.primClassif = Token.EOF;
            this.nextToken.subClassif = Token.VOID;
            this.exit = true;
            return " ";
        }
        // try to run thru entire file buffer until we hit something that is a char we can use
        for (i = 0; i < this.buffer.length(); i++)
        {
            char c = this.buffer.charAt(i);
            if (c == '/' && this.buffer.charAt(i+1) == '/')
            {
                this.aComment = true;
                i++;
            }
            else if (c == '\n' && !this.aComment)
            {
                this.lastLine = this.line;
            	this.line++;

                this.col = 1;
            }
            else if (c == '\n' && this.aComment)
            {
            	this.aComment = false;
                finCom = true;

            	this.lastLine = this.line;
                this.line++;
            }
            else if (c == ' ' || c == '\t')
            {
                this.col++;
            }
            else if (this.aComment)
            {
                continue;
            }
            else
            {
                break;
            }
        }
        // resize buffer (chop off what we just ran thru)
        this.buffer = this.buffer.substring(i);
        // if we have a completed comment are there is just one char left in buffer set EOF
        if (finCom && this.buffer.length() < 2)
        {
        	int lineNum = this.lines.length + 1;

        	// this loop is to print blank lines at eof if any
    		for (i = 0; i <= this.blankLines; i++) {
	    		lineNum++;
	    	}

            this.nextToken = new Token();
            this.nextToken.primClassif = Token.EOF;
            this.nextToken.subClassif = Token.VOID;
            this.exit = true;
            return " ";
        }
        // start running thru rest of buffer
        for (i = 0; i < this.buffer.length(); i++)
        {
            // take care of carraige returns
            char c = this.buffer.charAt(i);
            if (c == '\r') {
                this.buffer = this.buffer.substring(1);
                return "";
            }
            retVal += c;

            // if char is a delimiter
            if (delimiters.indexOf(c) != -1)
            {
                // if first char in our run thru the remaining buffer
                if (i == 0)
                {
                    if (this.buffer.charAt(0) == '/' && this.buffer.charAt(1) == '/')
                    {
                        this.aComment = true;
                        continue;
                    }
                    // if this first char is a quote
                    if (retVal.charAt(0) == '"' || retVal.charAt(0) == '\'') // if a quote, add to retVal
                    {
                        continue;
                    }
                    // chop off one char from beginning of buffer
                    this.buffer = this.buffer.substring(1);
                    if (this.aComment)
                        this.buffer = this.buffer.substring(1);
                }
                // if not the first char in our run thru buffer (aka first char in our token)
                else
                {
                    // if the first char is our return val(token) was a quote
                    if (retVal.charAt(0) == '"' || retVal.charAt(0) == '\'')
                    {
                        if (c == retVal.charAt(0))
                        {
                            // and if the char before it is NOT a backslash (escaping)
                            if (retVal.charAt(i -1) != '\\' || (retVal.charAt(i-2) == '\\' && retVal.charAt(i-1) == '\\'))
                            {
                                // make this our return val (token)
                                retVal = retVal.substring(0, retVal.length());
                                // chop this piece off front of our buffer
                                this.buffer = this.buffer.substring(retVal.length());
                                break;
                            }
                        }
                        // not first char in our buffer(aka our token), first char starts with a quote
                        // and char is a line feed
                        else if (c == '\n')
                        {
                            // set col num to this token's length
                            this.col += retVal.length();
                            this.handleErrors(retVal, NON_TERMINATED_STRING);
                        }
                        continue;
                    }
                    // not on first char in our current buffer, but we didnt hit any above conditions, we will
                    // blindly add this to our return val
                    retVal = retVal.substring(0, retVal.length() - 1);
                    // chop our built up return val off the front of the buffer
                    this.buffer = this.buffer.substring(retVal.length());
                }
                if (!this.aComment)
                    break;
            }
        }
        // our current line num is NOT equal to our last line num
        if (this.line != this.lastLine)
        {
            this.lastLine = this.line;
        }
        if (!this.aComment)
            this.setToken(retVal);


        // update col num by adding the length of our current token to it.
        this.col += retVal.length();
        // return our current validated token
        return retVal;
    }

    public void setToken(String value) throws Exception
    {
        this.nextToken = new Token(value);
        this.nextToken.iSourceLineNr = this.line;
        this.nextToken.iColPos = this.col;
        if (this.currentToken != null && debugger.token) {
            System.out.print("***** TOKEN ***** :" + this.currentToken.iSourceLineNr + ":");
            this.currentToken.printToken();
        }
        this.process(value);
    }

    public Scanner saveState(){
        Scanner scan = new Scanner();
        scan.sourceFileNm = this.sourceFileNm;
        scan.buffer = this.buffer;
        scan.currentToken = this.currentToken;
        scan.nextToken = this.nextToken;
        scan.debugger = this.debugger;
        scan.col = this.col;
        scan.line = this.line;
        scan.exit = this.exit;
        scan.opCombine = this.opCombine;
        scan.aComment = this.aComment;
        scan.lines = this.lines;
        scan.lastLine = this.lastLine;
        scan.blankLines = this.blankLines;
        return scan;
    }

}



