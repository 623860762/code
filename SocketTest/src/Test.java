import java.util.Timer;
import java.util.TimerTask;



public class Test {

	/**
	 * @param args
	 */

	 private class ReadJson extends java.util.TimerTask{
			@Override
			public void run() {
				System.out.println("隔2s输出.......");
			}
		}
	 
		public static void main(String[] args) {
			Timer timer = new Timer();
			timer.schedule(new Test().new ReadJson(), 1000, 2000);
		}

}
