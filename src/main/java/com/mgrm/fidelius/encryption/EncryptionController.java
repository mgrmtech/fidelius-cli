package com.mgrm.fidelius.encryption;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/encrypt")
public class EncryptionController {
	private final EncryptionService encryptionService;

	@Autowired
	public EncryptionController(EncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	@PostMapping
	public ResponseEntity<EncryptionResponse> encrypt(@RequestBody EncryptionRequest request) throws Exception {
		EncryptionResponse response = encryptionService.encrypt(request);
		return ResponseEntity.ok(response);
	}

	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
