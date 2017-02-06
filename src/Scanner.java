package havabol;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Scanner {
    public String buffer;
    public Token currentToken;
    public Token nextToken;
    public int col = 1;
    public int line = 1;
    public boolean exit;
    //public boolean str;
    public boolean opCombine;
    public String lines[];
    public int lastLine;
    private final static String operators = "+-*/<>!=#^";
    private final static String separators = "():;[],";
    private final static String delimiters = " \t;:()\'\"=!<>+-*/[]#^\n,";
    private final static String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static String numbers = "0123456789";
    public static final String MALFORMED_NUM = "MALFORMED_NUM";
    public static final String NON_TERMINATED_STRING = "NON_TERMINATED_STRING";
    public static final String INVALID_OPERATOR = "INVALID_OPERATOR";

    public Scanner(String SourceFileNm, SymbolTable symbolTable) throws IOException, Exception
    {
        // create our buffer to iterate thru
        this.buffer = new String(Files.readAllBytes(Paths.get(SourceFileNm)));
        // create our String array of lines
        this.lines = this.buffer.split("\n");
        this.exit = false;
        this.lastLine = -1;
        this.opCombine = false;
        this.getNext();

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
        // first val of token a quote? then its a string
        if (value.charAt(0) == '"' || value.charAt(0) == '\'')
        {
            this.strEval(value);
        }
         // is first val a number?
        else if (this.numbers.indexOf(value.charAt(0)) != -1)
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
        if (this.nextToken.primClassif == this.nextToken.OPERATOR && this.currentToken.primClassif == this.currentToken.OPERATOR)
        {
            this.currentToken.tokenStr += this.nextToken.tokenStr;
            //this.opCombine = true;
            this.getNext();
            this.opCombine = true;
        }
        if (this.opCombine)
        {
            String str = this.currentToken.tokenStr;
            if ((str.length() == 2) && (str.equals("<=") || str.equals(">=") || str.equals("!=") ||str.equals("==")))
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

        this.nextToken.primClassif = this.nextToken.OPERAND;
        this.nextToken.subClassif = this.nextToken.INTEGER;
    }
   /**
    *This method sets the currentToken attributes to correspond to a float
    *@return void - nothing to be returned
    *<p>
    */
    public void floatEval()
    {
        this.nextToken.primClassif = this.nextToken.OPERAND;
        this.nextToken.subClassif = this.nextToken.FLOAT;
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
            this.nextToken.primClassif = this.nextToken.OPERATOR;
            this.nextToken.subClassif = this.nextToken.VOID;
        }
        // control flow/end tokens  if, endif, else,  while, endwhile, for, endfor
        else if (value.equals("if") || value.equals("while") || value.equals("for"))
        {
            this.nextToken.primClassif = this.nextToken.CONTROL;
            this.nextToken.subClassif = this.nextToken.FLOW;
        }
        else if (value.equals("endif") || value.equals("endwhile") || value.equals("endfor"))
        {
            this.nextToken.primClassif = this.nextToken.CONTROL;
            this.nextToken.subClassif = this.nextToken.END;
        }
        // recognize control declare tokens (Int, Float, String, Bool)
        else if (value.equals("Int") || value.equals("Float") || value.equals("String") || value.equals("Bool"))
        {
            this.nextToken.primClassif = this.nextToken.CONTROL;
            this.nextToken.subClassif = this.nextToken.DECLARE;
        }
        else if (value.equals("T") || value.equals("F"))
        {
            this.nextToken.primClassif = this.nextToken.OPERAND;
            this.nextToken.subClassif = this.nextToken.BOOLEAN;
        }
        else
        {
            this.nextToken.primClassif = this.nextToken.OPERAND;
            this.nextToken.subClassif = this.nextToken.IDENTIFIER;
        }
    }
   /**
    *This method sets the currentToken attributes to correspond to a String
    *@return void - nothing to be returned
    *<p>
    */
    public void strEval(String value)
    {

        char array[] = value.toCharArray();
        array[0] = 0x0;
        array[array.length-1] = 0x0;
        for (int i=0; i < array.length; i++)
        {
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
        this.nextToken.primClassif = this.nextToken.OPERAND;
        this.nextToken.subClassif = this.nextToken.STRING;
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
        if (this.operators.indexOf(value.charAt(0)) != -1)
        {
            this.nextToken.primClassif = this.nextToken.OPERATOR;
            this.nextToken.subClassif = this.nextToken.VOID;
        }
        else if (this.separators.indexOf(value.charAt(0)) != -1)
        {
            this.nextToken.primClassif = this.nextToken.SEPARATOR;
            if (value.charAt(0) == ')')
            {
                this.nextToken.subClassif = this.nextToken.RT_PAREN;
            }
            else
            {
                this.nextToken.subClassif = this.nextToken.VOID;
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
        System.out.println("********** ERROR **********");
        System.out.printf("%s  %s  at line %d, column %d\n", errVal, value, this.line, col);
        throw new Exception();
    }

    public String getNext() throws Exception
    {
        if (this.nextToken != null && !this.opCombine)
            this.currentToken = this.nextToken;
        //this.opCombine = false;
        String retVal = "";
        int i;
        // if global exit state is true? return empty string
        if (this.exit == true)
            return "";
        // if there are only line feeds left in our buffer, weve hit the end of file
        if (this.buffer.matches("[\\s]+") || this.buffer.isEmpty())
        {
            // set EOF param's in currentToken, set exit state to true, return " ", so we can come back one more time
            this.nextToken = new Token();
            this.nextToken.primClassif = this.nextToken.EOF;
            this.nextToken.subClassif = this.nextToken.VOID;
            this.exit = true;
            return " ";
        }
        // try to run thru entire file buffer
        for (i = 0; i < this.buffer.length(); i++)
        {
            char c = this.buffer.charAt(i);
            // if we hit a line feed, increment line num and set col num to one
            if (c == '\n')
            {
                this.line++;
                this.col = 1;
            }
            // else we hit space or tab, increment col num by one
            else if (c == ' ' || c == '\t')
            {
                this.col++;
            }
            // we hit some other char besides \n \t or space, stop running thru buffer
            else
            {
                break;
            }
        }
        // chop off part of buffer we just ran thru
        this.buffer = this.buffer.substring(i);
        // start running thru rest of buffer
        for (i = 0; i < this.buffer.length(); i++)
        {
            // we will look at first char of buffer
            char c = this.buffer.charAt(i);
            // add it to our return val (token)
            retVal += c;
            // if char is a delimiter
            if (this.delimiters.indexOf(c) != -1)
            {
                // if first char in our run thru the remaining buffer
                if (i == 0)
                {
                    // if this first char is a quote
                    if (retVal.charAt(0) == '"' || retVal.charAt(0) == '\'') // if a quote, add to retVal
                    {
                        // keep going thru buffer
                        continue;
                    }
                    // chop off one char from beginning of buffer
                    this.buffer = this.buffer.substring(1);
                }
                // if not the first char in our run thru buffer (aka first char in our token)
                else
                {
                    // if the first char is our return val(token) was a quote
                    if (retVal.charAt(0) == '"' || retVal.charAt(0) == '\'')
                    {
                        // and if the current char matches our beginning quote char
                        if (c == retVal.charAt(0))
                        {
                            // and if the char before it is NOT a backslash (escaping)
                            if (retVal.charAt(i - 1) != '\\')
                            {
                                // make this our return val (token)
                                retVal = retVal.substring(0, retVal.length());
                                // chop this piece off front of our buffer
                                this.buffer = this.buffer.substring(retVal.length());
                                // break out of this loop; stop iterating
                                break;
                            }
                        }
                        // not first char in our buffer(aka our token), first char starts with a quote
                        // and char is a line feed
                        else if (c == '\n')
                        {
                            // set col num to this token's length
                            this.col += retVal.length();
                            // throw appropriate exception
                            this.handleErrors(retVal, NON_TERMINATED_STRING);
                        }
                        // not first char, first char is a quote, keep going(building our token)
                        continue;
                    }
                    // not on first char in our current buffer, but we didnt hit any above conditions, we will
                    // blindly add this to our return val
                    retVal = retVal.substring(0, retVal.length() - 1);
                    // chop our built up return val off the front of the buffer
                    this.buffer = this.buffer.substring(retVal.length());
                }
                // when we finally get here we must break out because our string(token) is now built
                break;
            }
        }
        // our current line num is NOT equal to our last line num
        if (this.line != this.lastLine)
        {
            // print out the current line num and the line itself using String array of lines
            try
            {
                System.out.printf("%d %s\n", this.line, this.lines[this.line - 1]);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                ;
            }
            // set the lastLine num equal to the current line num
            this.lastLine = this.line;
        }
        // call the setCurrentToken method to evaulate and set the token or throw an exception
        this.setToken(retVal);

        //this.currentToken = this.nextToken;

        // update col num by adding the length of our current token to it.
        this.col += retVal.length();
        // return our current validated token
        return retVal;
    }
    private void setToken(String value) throws Exception
    {
        this.nextToken = new Token(value);
        this.nextToken.iSourceLineNr = this.line;
        this.nextToken.iColPos = this.col;
        this.process(value);
    }
}
