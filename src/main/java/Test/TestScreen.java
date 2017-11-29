package Test;

import httpServer.booter;
import nlogger.nlogger;

public class TestScreen {
	public static void main(String[] args) {
		booter booter = new booter();
		try {
			System.out.println("GrapeScreen1");
			System.setProperty("AppName", "GrapeScreen1");
			booter.start(1005);
		} catch (Exception e) {
			nlogger.logout(e);
		}
	}
}
