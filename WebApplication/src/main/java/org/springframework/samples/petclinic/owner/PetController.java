/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collection;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final OwnerRepository owners;

	public PetController(OwnerRepository owners) {
		this.owners = owners;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.owners.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {

		Owner owner = this.owners.findById(ownerId);
		if (owner == null) {
			throw new IllegalArgumentException("Owner ID not found: " + ownerId);
		}
		return owner;
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {

		Owner owner = this.owners.findById(ownerId);
		if (owner == null) {
			throw new IllegalArgumentException("Owner ID not found: " + ownerId);
		}
		return petId == null ? new Pet() : owner.getPet(petId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, ModelMap model) {
		Pet pet = new Pet();
		owner.addPet(pet);
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasText(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}

		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(Owner owner, @PathVariable("petId") int petId, ModelMap model) {
		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {

		String petName = pet.getName();

		// checking if the pet name already exist for the owner
		if (StringUtils.hasText(petName)) {
			Pet existingPet = owner.getPet(petName.toLowerCase(), false);
			if (existingPet != null && existingPet.getId() != pet.getId()) {
				result.rejectValue("name", "duplicate", "already exists");
			}
		}

		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		if (result.hasErrors()) {
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}



	@GetMapping("/pets/{petId}/uploadForm")
	public String showUploadForm(@PathVariable int ownerId, @PathVariable int petId, Model model) {
		// ... (add any necessary logic to prepare the model)
		return "pets/uploadForm"; // Create this Thymeleaf template
	}

	@PostMapping("/pets/{petId}/upload")
	public String handleFileUpload(@PathVariable int ownerId, @PathVariable int petId,
								   @RequestParam("file") MultipartFile file,
								   RedirectAttributes redirectAttributes) {
		if (file.isEmpty()) {
			// Handle empty file
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
			return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
		} else {
			Owner owner = this.owners.findById(ownerId);
			if (owner == null) {
				throw new IllegalArgumentException("Owner ID not found: " + ownerId);
			}
			Pet pet = owner.getPet(petId);
			if(pet.getPhotoPath()!=null ) {
				System.out.println("PHTOTO SAVED : " + pet.getPhotoPath());
			}
			String fileName = file.getOriginalFilename();
			Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
			Path filePath = tmpDir.resolve(fileName);
			System.out.println(filePath.toString());
			pet.setPhotoPath(filePath.toString());

			try {
				Files.copy(
					file.getInputStream(),
					filePath,
					StandardCopyOption.REPLACE_EXISTING);
				this.owners.save(owner);
			} catch (IOException e) {
				// Handle the IOException
				redirectAttributes.addFlashAttribute("message", "An error occurred while uploading the file.");
				return "redirect:/owners/{ownerId}/pets/{petId}/uploadForm";
			}

		}


		// Get the owner and pet objects (you might need to fetch them from the database)
		//Owner owner = ownerService.findOwnerById(ownerId);
		//	Pet pet = petService.findPetById(petId);

		// Save the file (e.g., to disk or a cloud storage service)
		// ... your file saving logic here ...

		// Update the pet object with the file information (if needed)
		// ...

		// Redirect back to the pet page with a success message
		redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
		return "redirect:/owners/{ownerId}/pets/{petId}";


	}

	@GetMapping("/pets/getPhotoByPath")
	public void showImageByPath(@RequestParam String photoPath, HttpServletResponse response) throws IOException {

		Path path = Paths.get(photoPath);
		byte[] imageBytes = Files.readAllBytes(path);

		URLConnection connection = path.toFile().toURL().openConnection();
		String mimeType = connection.getContentType();
		response.setContentType(mimeType);
		// Write the image bytes to the response output stream
		try (OutputStream os = response.getOutputStream()) {
			os.write(imageBytes);
			os.flush();
		}
	}


	@GetMapping("/pets/{petId}/image")
	public void showImage(@PathVariable int ownerId, @PathVariable int petId, HttpServletResponse response) throws IOException {
		// Get the owner and pet objects
		Owner owner = this.owners.findById(ownerId);
		Pet pet = owner.getPet(petId);

		// Get the image file path associated with the pet
		String imagePath = pet.getPhotoPath(); // Assuming you have a field in the Pet entity to store the image path
		if (imagePath != null) {
			// Load the image file
			Path path = Paths.get(imagePath);
			byte[] imageBytes = Files.readAllBytes(path);

			URLConnection connection = path.toFile().toURL().openConnection();
			String mimeType = connection.getContentType();
			response.setContentType(mimeType);

			// Write the image bytes to the response output stream
			try (OutputStream os = response.getOutputStream()) {
				os.write(imageBytes);
				os.flush();
			}
		}
	}

}
