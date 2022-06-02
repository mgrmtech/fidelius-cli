package com.mgrm.fidelius.decryption;

public class DecryptionRequest {

	private String encryptedData;
	private String requesterNonce;
	private String senderNonce;
	private String requesterPrivateKey;
	private String senderPublicKey;

	public DecryptionRequest(
		String encryptedData,
		String requesterNonce,
		String senderNonce,
		String requesterPrivateKey,
		String senderPublicKey
	) {
		this.encryptedData = encryptedData;
		this.requesterNonce = requesterNonce;
		this.senderNonce = senderNonce;
		this.requesterPrivateKey = requesterPrivateKey;
		this.senderPublicKey = senderPublicKey;
	}

	public String getEncryptedData() {
		return encryptedData;
	}

	public void setEncryptedData(String encryptedData) {
		this.encryptedData = encryptedData;
	}

	public String getRequesterNonce() {
		return requesterNonce;
	}

	public void setRequesterNonce(String requesterNonce) {
		this.requesterNonce = requesterNonce;
	}

	public String getSenderNonce() {
		return senderNonce;
	}

	public void setSenderNonce(String senderNonce) {
		this.senderNonce = senderNonce;
	}

	public String getRequesterPrivateKey() {
		return requesterPrivateKey;
	}

	public void setRequesterPrivateKey(String requesterPrivateKey) {
		this.requesterPrivateKey = requesterPrivateKey;
	}

	public String getSenderPublicKey() {
		return senderPublicKey;
	}

	public void setSenderPublicKey(String senderPublicKey) {
		this.senderPublicKey = senderPublicKey;
	}
}
