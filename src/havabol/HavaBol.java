/*
  This is a simple driver for the first programming assignment.
  Command Arguments:
      java HavaBol arg1
             arg1 is the havabol source file name.
  Output:
      Prints each token in a table.
  Notes:
      1. This creates a SymbolTable object which doesn't do anything
         for this first programming assignment.
      2. This uses the student's Scanner class to get each token from
         the input file.  It uses the getNext method until it returns
         an empty string.
      3. If the Scanner raises an exception, this driver prints
         information about the exception and terminates.
      4. The token is printed using the Token::printToken() method.
 */
package havabol;

public class HavaBol
{
    public static void main(String[] args)
    {
        try
        {
            // Print a column heading
            /*System.out.printf("%-11s %-12s %s\n"
                    , "primClassif"
                    , "subClassif"
                    , "tokenStr");*/
        	SymbolTable st = new SymbolTable();

            Parser parser = new Parser(args[0], st);
            while (! parser.scan.getNext().isEmpty())
            {
                parser.statements(true,true);
                /*
                try
                {
                  // call putSymbol(String symbol, STEntry entry)

                  scan.currentToken.printToken();
                  symbolTable.putSymbol(scan.currentToken.tokenStr, new STEntry(
                    scan.currentToken.tokenStr, scan.currentToken.primClassif,
                    scan.currentToken.subClassif));
                }
                catch (NullPointerException e)
                {
                  continue;
                }*/
            }
            // symbolTable.printTable();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
