package es.udc.fic.tic.nautilus;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
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
	
	private void start(String[] args) {
		if (!(new File("config.xml").exists())) {
			ConfigHandler configHandler = new ConfigHandler();
			configHandler.initializeConfig();
		}
		/*TODO: comprobar que existe la carpeta para almacenar o tomar
		 * comprobar el espacio donado disponible contra la base de datos*/
		try {
			server.startServer();
		} catch (Exception e) {
			//System.err.println("Error!");
			e.printStackTrace();
		}
	}
}
