package com.mgrm.fidelius.decryption;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/decrypt")
public class DecryptionController {
	private final DecryptionService decryptionService;

	@Autowired
	public DecryptionController(DecryptionService decryptionService) {
		this.decryptionService = decryptionService;
	}

	@PostMapping
	public ResponseEntity<DecryptionResponse> decrypt(@RequestBody DecryptionRequest request) throws Exception {
		DecryptionResponse response = decryptionService.decrypt(request);
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
