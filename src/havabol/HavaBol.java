package havabol;

public class HavaBol {
    public static void main(String[] args) {
        try {
          SymbolTable st = new SymbolTable();
          Parser parser = new Parser(args[0], st);
          while (! parser.scan.getNext().isEmpty()) {
            parser.statements(false);
          }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
