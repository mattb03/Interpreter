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
        // Create the SymbolTable
        SymbolTable symbolTable = new SymbolTable();

        try
        {
            // Print a column heading
            System.out.printf("%-11s %-12s %s\n"
                    , "primClassif"
                    , "subClassif"
                    , "tokenStr");

            Scanner scan = new Scanner(args[0], symbolTable);
            //scan.nextToken.printToken();
            while (! scan.getNext().isEmpty())
            {
                //System.out.print(scan.currentToken);
                scan.currentToken.printToken();
                scan.nextToken.printToken();

                System.out.println();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
