import java.util.Arrays;
public class Traverse{
    public static int max(int[] numbers){
        Arrays.sort(numbers);
        int n = numbers[numbers.length - 1];
        return n;
    }
    public static void main(String[] args){
        int[]numbers = {100,89,99};
        System.out.println(max(numbers));
    }
}