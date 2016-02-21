package es.udc.fic.tic.nautilus.client;

import es.udc.fic.tic.nautilus.util.ModelConstanst;
import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;

public class KeyContainer {
	
	private String key;
	private ModelConstanst.ENCRYPT_ALG encrypt_alg;
	
	public KeyContainer(String key, ENCRYPT_ALG encrypt_alg) {
		super();
		this.key = key;
		this.encrypt_alg = encrypt_alg;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ModelConstanst.ENCRYPT_ALG getEncrypt_alg() {
		return encrypt_alg;
	}

	public void setEncrypt_alg(ModelConstanst.ENCRYPT_ALG encrypt_alg) {
		this.encrypt_alg = encrypt_alg;
	}
}
