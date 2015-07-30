package es.udc.fic.tic.nautilus.expcetion;

@SuppressWarnings("serial")
public class NotSaveException extends Exception {
	public NotSaveException() {
		super("The file can't save in the system");
	}
}