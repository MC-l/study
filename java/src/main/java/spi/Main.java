package spi;

/**
 * @auth caiguowei
 * @date 2020/9/3
 */
public class Main {

    private final static Integer[] arr = new Integer[10];
    private static Integer idx = 0;
    public static void main(String[] args){
        for (int i = 0; i < 100; i++) {
            arr[idx] = i;
            System.out.println(idx + " = " + arr[idx]);
            idx += 1;
            idx %= 10;
        }
    }

}
