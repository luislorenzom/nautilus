package es.udc.fic.tic.nautilus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import es.udc.fic.tic.nautilus.server.ServerService;

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
	private ServerService serverService;
	
	private void start(String[] args) {
		try {
			serverService.keepTheFile("/home/file.java", 7, null, null, 28737, "hash17");
		} catch (Exception e) {
			//System.err.println("Error!");
			e.printStackTrace();
		}
	}
}
