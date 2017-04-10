import java.util.ArrayList;


public class Test {

    public static void main(String[] args) {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i=0; i < 10; i++)
            arr.add(null);

        arr.add(5, 1000);
        System.out.println(arr);
    }



}
