package com.mgrm.fidelius.decryption;

public class DecryptionResponse {

	private String decryptedData;

	public DecryptionResponse(String decryptedData) {
		this.decryptedData = decryptedData;
	}

	public String getDecryptedData() {
		return decryptedData;
	}

	public void setDecryptedData(String decryptedData) {
		this.decryptedData = decryptedData;
	}
}
