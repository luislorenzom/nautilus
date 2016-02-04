package es.udc.fic.tic.nautilus;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.connection.Client;
import es.udc.fic.tic.nautilus.connection.Server;


@Component
public class Main {
	
	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		ApplicationContext context = 
				new ClassPathXmlApplicationContext("classpath:/spring-config.xml");
		
		Main p = context.getBean(Main.class);
		p.start(args);
	}
	
	@Autowired
	private Server server;
	
	@Autowired
	private Client client;
	
	private void start(String[] args) {
		//LogManager.getLogManager().reset();
		ConfigHandler configHandler = new ConfigHandler();
		if (!(new File("config.xml").exists())) {
			configHandler.initializeConfig();
		}
		
		/*TODO: comprobar que existe la carpeta para almacenar o tomar
		 * comprobar el espacio donado disponible contra la base de datos*/
		try {
			switch (args[0]) {
				case "-s":
					// Flag for launch the server
					lauchServer();
					break;
				
				case "-ck":
					// Check if the config file have some server
					if (configHandler.getConfig().getServerPreferences().size() == 0) {
						System.err.println("You Don't have any server in your config.xml!! Put one for save your file");
						System.exit(0);
					}
					
					// Client - Keep the file
					makeSavePetition(args);
					break;
	
				case "-cr":
					// Client - Retrieval the file
					makeRetrievalPetition(args);
					break;
	
				default:
					// Incorrect flag
					System.err.println("Invalid command");
					break;
			}
			
		} catch (Exception e) {
			//System.err.println("Error!");
			e.printStackTrace();
		}
	}
	
	
	private void lauchServer() throws Exception{
		System.out.println("\n");
		System.out.println("    _   __            __  _ __      ");
		System.out.println("   / | / /___ ___  __/ /_(_) / _ _______   ________  ______   _____  _____");
		System.out.println("  /  |/ / __ `/ / / / __/ / / / / / ___/  / ___/ _ \'/ ___/ | / / _ \'/ ___/");
		System.out.println(" / /|  / /_/ / /_/ / /_/ / / /_/ (__  )  (__  )  __/ /   | |/ /  __/ / ");
		System.out.println("/_/ |_/\'__,_/\'__,_/\'__/_/_/\'__,_/____/  /____/\'___/_/    |___/\'___/_/ v.0.1");

		server.startServer();
	}
	
	private void makeSavePetition(String[] args) throws Exception {
		if (args[1] != null) {
			int downloadLimit = 0;
			int index = 2;
			Calendar dateLimit = null;
			Calendar dateRelease = null;
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
			String dateRegex = "^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}$";
			
			// Get the download limit
			if (index < args.length) {
				if (args[index].equals("-dol")) { 
					if (args[index+1].matches("[0-9]+")) {
						downloadLimit = Integer.parseInt(args[index+1]);
						index = 4;
					} else {
						System.err.println("Error in the download limit specification");
						System.exit(0);
					}
				}
			}
			
			// Get the date limit
			if (index < args.length) { 
				if (args[index].equals("-dal")) { 
					if (args[index+1].matches(dateRegex)) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(df.parse(args[index+1]+" - 00:00:00"));
						dateLimit = cal;
						index = 6;
					} else {
						System.err.println("Error in the date limit specification");
						System.exit(0);
					}
				}
			}
			
			// Get the date release
			if (index < args.length) {
				if (args[index].equals("-dr")) { 
					if (args[index+1].matches(dateRegex)) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(df.parse(args[index+1]+" - 00:00:00"));
						dateRelease = cal;
					} else {
						System.err.println("Error in the date release specification");
						System.exit(0);
					}
				}
			}
			
			client.saveFileInNetwork(args[1], downloadLimit, dateLimit, dateRelease);	
		} else {
			System.err.println("Don't have selected a file for keep");
		}
	}
	
	private void makeRetrievalPetition(String[] args) throws Exception{
		if (args[1] != null) {
			client.getFileFromKey(args[1]);					
		} else {
			System.err.println("Don't have selected a key for retrieval a file");
		}
	}
	
}
