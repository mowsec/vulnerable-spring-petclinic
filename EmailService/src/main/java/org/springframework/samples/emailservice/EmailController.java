package org.springframework.samples.emailservice;


import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.emailservice.model.EmailData;
import org.springframework.samples.emailservice.model.Owner;
import org.springframework.samples.emailservice.model.OwnerRepository;
import org.springframework.util.SerializationUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.text.Normalizer;

@RestController
public class EmailController {

	Logger logger = LogManager.getLogger(EmailController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private OwnerRepository ownerRepository;

	@PostMapping("/registerEmail")
	public String registerEmail(@RequestBody OwnerDTO owner) throws IOException {
		logger.info("owner email : " + owner.getEmail() + " owner first name : "
			+ owner.getFirstName() + " last name : " + owner.getLastName());
		return owner.getFirstName();
	}


	@GetMapping("/getOwners")
	public List<Owner> getOwners() {
		return ownerRepository.findAll();
	}


	@GetMapping("/ping")
	public String ping(@RequestParam String ip) throws IOException {
		StringBuilder result = new StringBuilder();
		// Execute the command
		String[] cmd  = {"/bin/sh", "-c", "ping -c 3 " + ip};
		Process process = Runtime.getRuntime().exec(cmd);

		// Get the input stream from the process
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		// Read and print the output
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line).append("\n");
			System.out.println(line);
		}
		// Wait for the process to complete
		try {
			int exitCode = process.waitFor();
			System.out.println("Process exited with code: " + exitCode);
			return result.toString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ip;
	}

	@GetMapping("/cmd")
	public String getCMD(@RequestParam String arg) throws IOException {
		StringBuilder result = new StringBuilder();
		// Execute the command
		Process process = Runtime.getRuntime().exec(arg);

		// Get the input stream from the process
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		// Read and print the output
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line).append("\n");
			System.out.println(line);
		}
		// Wait for the process to complete
		try {
			int exitCode = process.waitFor();
			System.out.println("Process exited with code: " + exitCode);
			return result.toString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return arg;
	}

	@PostMapping("/postcmd")
	public String postCMD(@RequestParam String arg) throws IOException {
		String normalizedString = Normalizer.normalize(arg, Normalizer.Form.NFKD);
		StringBuilder result = new StringBuilder();
		Process process = Runtime.getRuntime().exec(normalizedString);

		// Get the input stream from the process
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		// Read and print the output
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line).append("\n");
			System.out.println(line);
		}
		// Wait for the process to complete
		try {
			int exitCode = process.waitFor();
			System.out.println("Process exited with code: " + exitCode);
			return result.toString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return normalizedString;
	}

	@PostMapping("/postjsoncmd")
	public String postjsonCMD(@RequestBody String jsonString) throws IOException {
		JsonNode rootNode = objectMapper.readTree(jsonString);
		String arg = rootNode.get("arg").asText();
		StringBuilder result = new StringBuilder();
		Process process = Runtime.getRuntime().exec(arg);

		// Get the input stream from the process
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		// Read and print the output
		String line;
		while ((line = reader.readLine()) != null) {
			result.append(line).append("\n");
			System.out.println(line);
		}
		// Wait for the process to complete
		try {
			int exitCode = process.waitFor();
			System.out.println("Process exited with code: " + exitCode);
			return result.toString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	@GetMapping("/deserialize")
	public String deserialize(@RequestParam String base64) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(base64);
		Object obj = SerializationUtils.deserialize(data);
		return obj.toString();
	}

	@GetMapping("/getPayload")
	public String getPayload(@RequestParam String cmd) throws IOException {
		EmailData data = new EmailData();
		data.setBody(cmd);
		data.setSubject("test");
		data.setEmailAddress("test@example.com");
		return Base64.getEncoder().encodeToString(SerializationUtils.serialize(data));
	}
}
