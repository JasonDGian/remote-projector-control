package es.iesjandula.reaktor_projector_server.rest;

import java.io.IOException;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor_projector_server.parsers.interfaces.ICommandParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorModelParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorParser;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProjectorController
{

	@Autowired
	ICommandParser commandsParser;

	@Autowired
	IProjectorParser projectorParser;

	@Autowired
	IProjectorModelParser projectorModelsParser;


	/**
	 * Handles the upload and parsing of a CSV file containing model data.
	 * <p>
	 * This endpoint receives a CSV file, reads its content, and processes the data
	 * using the {@code projectorModelsParser}. The file is validated to ensure it is not empty
	 * and that it is in CSV format. If an error occurs during parsing, an appropriate error message
	 * is returned to the client.
	 * </p>
	 *
	 * @param file The CSV file containing projector model data, expected as "models.csv".
	 * @return A {@link ResponseEntity} containing the result of the parsing operation or an error message.
	 */
	@Transactional
	@PostMapping("/parse-models")
	public ResponseEntity<?> parseModels(@RequestParam("models.csv") MultipartFile file) {

	    // Log the incoming request for processing models
	    log.info("Call to '/parse-models' received.");
	    String message;

	    // Use "try-with-resources" to automatically close the scanner when done with the InputStream
	    try (Scanner scanner = new Scanner(file.getInputStream())) {
	    	
            // Validate the file before processing
	    	this.validateFile(file);

	        // Parse the models from the CSV file using the projectorModelsParser
	        message = projectorModelsParser.parseProjectorModels(scanner);

	        // Log the result of the parsing operation
	        log.info("MODELS TABLE - " + message);

	        // Return a success response with the result
	        return ResponseEntity.status(HttpStatus.CREATED).body(message);

	    } catch (IOException e) {
	        // Log and return an error response in case of an IO exception (e.g., reading the file)
	        log.error("Error reading the file: {}", e.getMessage(), e);
	        return ResponseEntity.internalServerError().body("Error encountered while reading the file.");
	    } catch (ProjectorServerException e) {
	        // Log and return an error response for custom exceptions
	        log.error("Projector server error: {}", e.getMessage(), e);
	        return ResponseEntity.internalServerError().body("Error encountered during parsing process.");
	    } catch (Exception e) {
	        // Catch any unexpected exceptions and log the error
	        log.error("Unexpected error: {}", e.getMessage(), e);
	        return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
	    }
	}


	// metodo de parseo de acciones
	// Metodo de parseo de modelos
	// parseo comandos
	// parseo de proyectores (unidades)
	@Transactional
	@PostMapping("/parse-commands")
	public ResponseEntity<?> parseCommands(@RequestParam("commands.csv") MultipartFile file)
	{

		log.info("Call to commands parser received.");
		
		// Message that will be returned to the client.
		String message;
		
		try (Scanner scanner = new Scanner(file.getInputStream()))
		{
			

            // Validate the file before processing
			this.validateFile(file);
			
			message = commandsParser.parseCommands(scanner);
			
			log.info("COMMANDS TABLE - " + message);
			
			// Return a success response with the result
	        return ResponseEntity.status(HttpStatus.CREATED).body(message);
		} 
		// Input output exception catch.
		catch (IOException e)
		{
			log.error("Error reading the file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");
		}
		// Custom exception.
		catch (ProjectorServerException e)
		{
			log.error("Projector server error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered during parsing process.");
		}
		// Por si las moscas.
		catch (Exception e)
		{
			log.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	/**
	 * Handles the upload and parsing of a CSV file containing projector data.
	 * <p>
	 * This endpoint receives a CSV file, reads its contents, and processes the data
	 * using the {@code projectorParser}. In case of an error, an appropriate error
	 * message is returned.
	 * </p>
	 *
	 * @param file The CSV file containing projector data, expected as
	 *             "projectors.csv".
	 * @return A {@link ResponseEntity} with the parsing result or an error message.
	 */
	@Transactional
	@PostMapping("/parse-projectors")
	public ResponseEntity<?> parseProjectors(@RequestParam("projectors.csv") MultipartFile file)
	{

		log.info("Call to projectors parser received.");

		String message;
		try (Scanner scanner = new Scanner(file.getInputStream()))
		{
			
            // Validate the file before processing
			this.validateFile(file);

			// Parse the CSV file and obtain a result message
			message = projectorParser.parseProjectors(scanner);
			
			log.info("PROJECTORS TABLE - " + message);

			// Return a success response with the result
	        return ResponseEntity.status(HttpStatus.CREATED).body(message);

		}
		// Input output exception catch.
		catch (IOException e)
		{
			log.error("Error reading the file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");
		}
		// Custom exception.
		catch (ProjectorServerException e)
		{
			log.error("Projector server error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered during parsing process.");
		}
		// Por si las moscas.
		catch (Exception e)
		{
			log.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}
		
	/**
	 * Validates the uploaded CSV file.
	 * <p>
	 * This method checks if the file is empty and if the content type is valid for CSV files.
	 * It throws a {@link ProjectorServerException} with appropriate error codes if the validation fails.
	 * </p>
	 *
	 * @param file The uploaded file to be validated.
	 * @throws ProjectorServerException if the file is empty or has an invalid content type.
	 */
	private void validateFile(MultipartFile file) throws ProjectorServerException {
	    // Check if the file is empty
	    if (file.isEmpty()) {
	        throw new ProjectorServerException(490, "ERROR: Empty CSV file received.");
	    }

	    String contentType = file.getContentType();
	    
	    // Check if the file content type is valid for CSV files
	    if (contentType == null || !contentType.startsWith("text/csv")) {
	        throw new ProjectorServerException(498, "ERROR: Unsupported format. Expected format CSV.");
	    }
	}
}
