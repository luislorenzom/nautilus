package es.udc.fic.tic.nautilus.expcetion;

@SuppressWarnings("serial")
public class NotHaveDownloadLimitException extends Exception {
	public NotHaveDownloadLimitException() {
		super("Not have download limit");
	}

}
