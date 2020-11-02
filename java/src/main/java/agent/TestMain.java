package agent;

import java.util.Date;

/**
 * @auth caiguowei
 * @date 2020/10/31
 */
public class TestMain {
    public static void main(String[] args) {
        System.out.println("running main method:"+args[0]);
        System.out.println(say("Agent!"));
    }

    public static String say(String world){
        return world;
    }
}
