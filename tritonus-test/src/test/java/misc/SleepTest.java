package misc;

public class SleepTest {

    public static void main(String[] args) throws InterruptedException {
        long requestedSleepDuration = Long.parseLong(args[0]);
        for (int i = 0; i < 100; i++) {
            long timeBefore = System.currentTimeMillis();
            Thread.sleep(requestedSleepDuration, 0);
            long timeAfter = System.currentTimeMillis();
            System.out.println("actual sleep: " + (timeAfter - timeBefore));
        }
    }
}
