package Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class Logger {
	public enum Level {
		SEVR, WARN, INFO, FINE
	}

	public static String logs;
	public static String file;
	
	public static void log(Level level, String msg) {
		logs += "<font style='margin-right:5em' weight='bold' color='" + getColor(level) + "'>" + level + "</font>" + msg + "<br>";
		Logger.flush();
	}

	public static String getColor(Level level){
		switch(level){
		case SEVR: return "red";
		case WARN: return "orange";
		case INFO: return "black";
		case FINE: return "green";
		default: return "";
		}
	}
	
	public static void log(String msg) {
		log(Level.INFO, msg);
	}

	public static void init() {
		String timestamp = (new Date()).getTime() + "";
		Logger.file = timestamp + ".logs.html";
		Logger.logs = "<h1>Logs for Dragon Arena System at run: " + timestamp + "</h1>";
		
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Logger.file, true)));
		} catch (Exception e) {
			System.err.println("Could not create logfile.");
		}
	}

	private static void flush() {
		try {
			Files.write(Paths.get(Logger.file), logs.getBytes(), StandardOpenOption.APPEND);
			Logger.logs = "";
		} catch (IOException e) {
			System.err.println("Error flushing logs to logfile.");
		}
	}
}
