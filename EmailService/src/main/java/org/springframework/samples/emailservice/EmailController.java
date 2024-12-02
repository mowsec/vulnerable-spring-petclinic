package org.springframework.samples.emailservice;


import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.emailservice.model.Owner;
import org.springframework.samples.emailservice.model.OwnerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
public class EmailController {

	Logger logger = LogManager.getLogger(EmailController.class);

	@Autowired
	private OwnerRepository ownerRepository;

	@PostMapping("/registerEmail")
	public String registerEmail(@RequestBody OwnerDTO owner) throws IOException {
		logger.info("owner email : " +owner.getEmail() + " owner first name : "
			+owner.getFirstName()+" last name : " + owner.getLastName());
		return owner.getFirstName();
	}


	@GetMapping("/getOwners")
	public List<Owner> getOwners() {
		return ownerRepository.findAll();
	}

}
