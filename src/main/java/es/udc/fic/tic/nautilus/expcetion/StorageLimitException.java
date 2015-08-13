package es.udc.fic.tic.nautilus.expcetion;

@SuppressWarnings("serial")
public class StorageLimitException extends Exception {
	public StorageLimitException() {
		super("Limit exceeded storage folder");
	}
}
