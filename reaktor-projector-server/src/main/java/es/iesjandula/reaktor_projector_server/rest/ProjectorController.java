package es.iesjandula.reaktor_projector_server.rest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor_projector_server.dtos.ClassroomDto;
import es.iesjandula.reaktor_projector_server.dtos.CommandDto;
import es.iesjandula.reaktor_projector_server.dtos.FloorDto;
import es.iesjandula.reaktor_projector_server.dtos.ProjectorDto;
import es.iesjandula.reaktor_projector_server.dtos.ProjectorInfoDto;
import es.iesjandula.reaktor_projector_server.dtos.ProjectorModelDto;
import es.iesjandula.reaktor_projector_server.dtos.ResponseDto;
import es.iesjandula.reaktor_projector_server.dtos.RichResponseDto;
import es.iesjandula.reaktor_projector_server.dtos.ServerEventBatchDto;
import es.iesjandula.reaktor_projector_server.dtos.ServerEventDto;
import es.iesjandula.reaktor_projector_server.dtos.SimplifiedServerEventDto;
import es.iesjandula.reaktor_projector_server.dtos.TableServerEventDto;
import es.iesjandula.reaktor_projector_server.entities.Action;
import es.iesjandula.reaktor_projector_server.entities.Classroom;
import es.iesjandula.reaktor_projector_server.entities.Command;
import es.iesjandula.reaktor_projector_server.entities.Floor;
import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ServerEvent;
import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;
import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IClassroomParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.ICommandParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorModelParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorParser;
import es.iesjandula.reaktor_projector_server.repositories.IActionRepository;
import es.iesjandula.reaktor_projector_server.repositories.IClassroomRepository;
import es.iesjandula.reaktor_projector_server.repositories.ICommandRepository;
import es.iesjandula.reaktor_projector_server.repositories.IFloorRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorModelRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorRepository;
import es.iesjandula.reaktor_projector_server.repositories.IServerEventRepository;
import es.iesjandula.reaktor_projector_server.utils.Constants;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProjectorController
{

	@Autowired
	IClassroomParser classroomParser;

	@Autowired
	ICommandParser commandsParser;

	@Autowired
	IProjectorParser projectorParser;

	@Autowired
	IProjectorModelParser projectorModelsParser;

	@Autowired
	IServerEventRepository serverEventRepository;

	@Autowired
	IProjectorRepository projectorRepository;

	@Autowired
	IProjectorModelRepository projectorModelRepository;

	@Autowired
	ICommandRepository commandRepository;

	@Autowired
	IActionRepository actionRepositories;

	@Autowired
	IClassroomRepository classroomRepository;

	@Autowired
	IFloorRepository floorRepository;

	// ----------------------------- UTILITY METHODS -------------------------------

	/**
	 * Validates the uploaded CSV file.
	 * <p>
	 * Ensures the uploaded file is not empty and has a valid content type (CSV). If
	 * validation fails, a {@link ProjectorServerException} is thrown with an
	 * appropriate error code and message.
	 * </p>
	 *
	 * @param file The uploaded {@link MultipartFile} to be validated.
	 * @throws ProjectorServerException if the file is empty or has an invalid
	 *                                  content type.
	 */
	private void validateFile(MultipartFile file) throws ProjectorServerException
	{
		// Check if the file is empty
		if (file.isEmpty())
		{
			log.warn("File validation failed: Received an empty CSV file.");
			throw new ProjectorServerException(490, "ERROR: Empty CSV file received.");
		}

		String contentType = file.getContentType();

		// Check if the file content type is valid for CSV files
		if (contentType == null || !contentType.equals("text/csv"))
		{
			log.warn("File validation failed: Unsupported file format received. Expected 'text/csv', but got: {}",
					contentType);
			throw new ProjectorServerException(498, "ERROR: Unsupported format. Expected format CSV.");
		}

		log.info("File validation successful: Received a valid CSV file.");
	}

	// --------------------------- END UTILITY METHODS -----------------------------

	// ----------------------------- PARSING METHODS -------------------------------

	/**
	 * Endpoint to parse classrooms from a CSV file.
	 * <p>
	 * This method receives a CSV file containing classroom data, validates it, and
	 * parses the content. It returns a response indicating success or failure.
	 * </p>
	 *
	 * @param classroomsFile The uploaded CSV file containing classroom data.
	 * @return A {@link ResponseEntity} containing a success or error message.
	 */
	@Transactional
	@PostMapping(value = "/parse-classrooms")
	public ResponseEntity<?> parseClassrooms(@RequestParam(value = "classrooms.csv") MultipartFile classroomsFile)
	{

		// Log the incoming request with the file name
		log.info("Received request to parse classrooms.");

		String message;

		// Use "try-with-resources" to automatically close the scanner when done.
		try (Scanner scanner = new Scanner(classroomsFile.getInputStream()))
		{

			this.validateFile(classroomsFile);

			// Log the successful file validation
			log.info("Projectors file successfully validated.");

			// Parse the classrooms from the CSV file (parser returns a string with a
			// result.)
			message = classroomParser.parseClassroom(scanner);

			// Wrap the result in a ResponseDto
			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);

			// Return a success response
			log.debug("Returning success response with status: {}", HttpStatus.CREATED);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IOException e)
		{
			// Log file reading errors
			log.error("IO error while reading classrooms file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");

		} catch (ProjectorServerException e)
		{
			// Log custom projector server exceptions
			log.error("Projector server error while processing classrooms file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());

		} catch (Exception e)
		{
			// Log unexpected errors
			log.error("Unexpected error while processing classrooms file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	/**
	 * Handles the upload and parsing of a CSV file containing projector model data.
	 * <p>
	 * This endpoint receives a CSV file (expected to be named "models.csv"), reads
	 * its content, and processes the data using the {@code projectorModelsParser}.
	 * The file is validated to ensure it is not empty and is in CSV format. If an
	 * error occurs during parsing, an appropriate error message is returned to the
	 * client.
	 * </p>
	 *
	 * @param file The CSV file containing projector model data, expected as
	 *             "models.csv".
	 * @return A {@link ResponseEntity} containing the result of the parsing
	 *         operation or an error message.
	 */
	@Transactional
	@PostMapping("/parse-models")
	public ResponseEntity<?> parseModels(@RequestParam("models.csv") MultipartFile file)
	{

		// Log the incoming request for processing models with a unique request ID for
		// traceability.
		log.info("Received request to parse projector models.");

		String message;

		// Use "try-with-resources" to automatically close the scanner when done with
		// the InputStream
		try (Scanner scanner = new Scanner(file.getInputStream()))
		{

			// Validate the file (empty check, CSV format check)
			this.validateFile(file);

			// Log validation success
			log.info("Models file successfully validated.");

			// Parse the models from the CSV file using the projectorModelsParser
			message = projectorModelsParser.parseProjectorModels(scanner);

			// Create and return a success response with the parsed message
			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IOException e)
		{
			// Log and return an error response in case of an IO exception (e.g., reading
			// the file)
			log.error("Error reading models file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");
		} catch (ProjectorServerException e)
		{
			// Log and return an error response for custom exceptions (e.g., parsing issues)
			log.error("Projector server error processing models file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());
		} catch (Exception e)
		{
			// Log and return an error response for unexpected exceptions
			log.error("Unexpected error occurred while processing models file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	/**
	 * Handles the upload and parsing of a CSV file containing projector data.
	 * <p>
	 * This endpoint receives a CSV file (expected to be named "projectors.csv"),
	 * reads its contents, and processes the data using the {@code projectorParser}.
	 * In case of an error during the processing or parsing, an appropriate error
	 * message is returned to the client.
	 * </p>
	 *
	 * @param file The CSV file containing projector data, expected as
	 *             "projectors.csv".
	 * @return A {@link ResponseEntity} containing the parsing result or an error
	 *         message.
	 */
	@Transactional
	@PostMapping("/parse-projectors")
	public ResponseEntity<?> parseProjectors(@RequestParam("projectors.csv") MultipartFile file)
	{

		// Log the incoming request to parse the projectors data file.
		log.info("Received request to parse projectors.");

		String message;
		try (Scanner scanner = new Scanner(file.getInputStream()))
		{

			// Validate the file before processing (check if it is non-empty and is in CSV
			// format).
			this.validateFile(file);

			// Log the successful file validation
			log.info("Projectors file successfully validated.");

			// Parse the CSV file using the projectorParser and obtain a result message
			message = projectorParser.parseProjectors(scanner);

			// Create and return a success response with the parsing message
			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IOException e)
		{
			// Log and return an error response in case of an IO exception (e.g., issue
			// reading the file)
			log.error("Error reading file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");
		} catch (ProjectorServerException e)
		{
			// Log and return an error response for custom exceptions (e.g., parsing or
			// business logic errors)
			log.error("Projector server error processing file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());
		} catch (Exception e)
		{
			// Log and return an error response for unexpected exceptions (fallback)
			log.error("Unexpected error occurred while processing file '{}': {}", file.getOriginalFilename(),
					e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	/**
	 * Endpoint to parse commands from a CSV file.
	 * <p>
	 * This method receives a CSV file containing command data, validates the file,
	 * and parses the content. It returns a response indicating success or failure.
	 * </p>
	 *
	 * @param file The uploaded CSV file containing command data.
	 * @return A {@link ResponseEntity} containing a success or error message.
	 */
	@Transactional
	@PostMapping("/parse-commands")
	public ResponseEntity<?> parseCommands(@RequestParam("commands.csv") MultipartFile file)
	{

		// Log the incoming request, including the file name for traceability
		log.info("Received request to parse commands.");

		// Message that will be returned to the client after processing
		String message;

		// Use "try-with-resources" to automatically close the scanner after use
		try (Scanner scanner = new Scanner(file.getInputStream()))
		{

			// Validate the file (checks for format, empty file, etc.)
			this.validateFile(file);

			// Log the successful file validation
			log.info("Commands file successfully validated.");

			// Parse the commands from the CSV file using commandsParser
			message = commandsParser.parseCommands(scanner);

			// Create and return a success response with the parsing message
			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IOException e)
		{
			// Log the IO error when reading the file
			log.error("IO error while reading file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");

		} catch (ProjectorServerException e)
		{
			// Log and return custom errors related to the projector server (parsing or
			// business logic errors)
			log.error("Projector server error while processing commands file '{}': {}", file.getOriginalFilename(),
					e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());

		} catch (Exception e)
		{
			// Log unexpected errors to capture any other issues
			log.error("Unexpected error while processing commands file '{}': {}", file.getOriginalFilename(),
					e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	/**
	 * Handles the parsing of multiple CSV files: projectors, commands, and
	 * classrooms. This endpoint accepts optional files and processes them
	 * accordingly. If no files are provided, it returns an error response.
	 * <p>
	 * The method performs the following steps:
	 * <ul>
	 * <li>Validates the received files.</li>
	 * <li>Parses the projectors file if provided.</li>
	 * <li>Parses the commands file if provided.</li>
	 * <li>Parses the classrooms file if provided.</li>
	 * <li>Returns a structured response with parsing results.</li>
	 * </ul>
	 * </p>
	 *
	 * @param classroomsFile Multipart file containing classroom data (Optional).
	 * @param projectorsFile Multipart file containing projector data (Optional).
	 * @param commandsFile   Multipart file containing command data (Optional).
	 * @return ResponseEntity containing a success message or error response.
	 * @throws ProjectorServerException If an issue occurs related to the server
	 *                                  logic.
	 * @throws IOException              If there is an error reading the provided
	 *                                  files.
	 * @throws Exception                If an unexpected error occurs.
	 */
	@Transactional
	@PostMapping("/parse-multifile")
	public ResponseEntity<?> parseMultifile(
			@RequestParam(value = "classrooms.csv", required = false) MultipartFile classroomsFile,
			@RequestParam(value = "projectors.csv", required = false) MultipartFile projectorsFile,
			@RequestParam(value = "commands.csv", required = false) MultipartFile commandsFile)
	{

		try
		{
			log.info("Call to parse multiple files received.");

			// Initialize message and response DTO for structured results.
			String message = "";
			RichResponseDto richResponseDto = new RichResponseDto();

			// Check if no files were received, and return an error message.
			if ((classroomsFile == null || classroomsFile.isEmpty()) && (commandsFile == null || commandsFile.isEmpty())
					&& (projectorsFile == null || projectorsFile.isEmpty()))
			{
				message = "No files received for parse operation.";
				log.error(message);
				throw new ProjectorServerException(498, message); // Custom error for no files received.
			}

			// NOTE: IF YOU ARE MODIFYING THIS KEEP IN MIND THE PARSING ORDER MATTERS TO
			// KEEP A CONSISTENT RESULT MESSAGE. 
			// BEST ORDER IS: CLASSROOMS > PROJECTORS > COMMANDS.

			// Process classrooms file if provided.
			if (classroomsFile != null && !classroomsFile.isEmpty())
			{
				log.info("Processing 'classrooms.csv' file.");
				try (Scanner scanner = new Scanner(classroomsFile.getInputStream()))
				{
					this.validateFile(classroomsFile); // Validate the file before processing.
					richResponseDto.setMessage3(this.classroomParser.parseClassroom(scanner)); // Parse classrooms.
				}
			} else
			{
				log.info("No 'classrooms.csv' file received.");
				richResponseDto.setMessage3("The request did not include a file for classrooms.");
			}

			// Process projectors file if provided.
			if (projectorsFile != null && !projectorsFile.isEmpty())
			{
				log.info("Processing 'projectors.csv' file.");
				try (Scanner scanner = new Scanner(projectorsFile.getInputStream()))
				{
					this.validateFile(projectorsFile); // Validate the file before processing.
					richResponseDto.setMessage2(projectorParser.parseProjectors(scanner)); // Parse projectors.
				}
			} else
			{
				log.info("No 'projectors.csv' file received.");
				richResponseDto.setMessage2("The request did not include a file for projectors.");
			}

			// Process commands file if provided.
			if (commandsFile != null && !commandsFile.isEmpty())
			{
				log.info("Processing 'commands.csv' file.");
				try (Scanner scanner = new Scanner(commandsFile.getInputStream()))
				{
					this.validateFile(commandsFile); // Validate the file before processing.
					richResponseDto.setMessage1(this.commandsParser.parseCommands(scanner)); // Parse commands.
				}
			} else
			{
				log.info("No 'commands.csv' file received.");
				richResponseDto.setMessage1("The request did not include a file for commands.");
			}

			// Set response status to success.
			richResponseDto.setStatus(Constants.RESPONSE_STATUS_SUCCESS);

			// Log the final parsing results.
			log.info("Commands: {}\nProjectors: {}\nClassrooms: {}", richResponseDto.getMessage1(),
					richResponseDto.getMessage2(), richResponseDto.getMessage3());

			// Return the structured response DTO with success status.
			return ResponseEntity.ok(richResponseDto);

		} catch (ProjectorServerException e)
		{
			// Handle custom exceptions (e.g., invalid input, no files received).
			log.error("Projector server error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());

		} catch (IOException e)
		{
			// Handle IO errors when reading the files.
			log.error("Error reading the file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");

		} catch (Exception e)
		{
			// Catch any unexpected errors.
			log.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	// --------------------------- END PARSING METHODS -----------------------------

	
	// -------------------------- SERVER EVENT METHODS -----------------------------
	// ------------------------ END SERVER EVENT METHODS ---------------------------
	// -----------------------------------------------------------------------------
	// ---------------------------- PROJECTOR METHODS ------------------------------
	// -------------------------- END PROJECTOR METHODS ----------------------------
	// -----------------------------------------------------------------------------
	// ---------------------------- PROJECTOR METHODS ------------------------------
	// -------------------------- END PROJECTOR METHODS ----------------------------
	// -----------------------------------------------------------------------------
	// ---------------------------- PROJECTOR METHODS ------------------------------
	// -------------------------- END PROJECTOR METHODS ----------------------------
	// -----------------------------------------------------------------------------

	// ----------------------- HELPING METHODS ------------------------------------

	// ----------------------- PARSING ENDPOINTS ----------------------- .

	// -------------------- UPLOAD AND DELETE RECORDS ENDPOINTS --------------------

	/**
	 * Creates a new projector model in the system.
	 * <p>
	 * This method handles the creation of a new projector model. It first checks if
	 * the model name is provided, and then verifies whether the model already
	 * exists in the database. If the model does not exist, it is created and saved.
	 * If the model already exists or if the model name is blank, appropriate
	 * exceptions are thrown.
	 * </p>
	 * 
	 * @param projectorDto The Data Transfer Object (DTO) containing the projector
	 *                     model details, including the model name.
	 * @return ResponseEntity A response entity containing the status of the
	 *         operation. If the projector model is created successfully, a message
	 *         confirming the upload is returned with HTTP status 201 (Created). In
	 *         case of errors, a custom error message is returned with the
	 *         corresponding error code and HTTP status.
	 * @throws ProjectorServerException if the projector model already exists in the
	 *                                  database or if the model name is blank.
	 */
	@PostMapping("/projector-models")
	public ResponseEntity<?> createNewModel(@RequestBody() ProjectorModelDto projectorDto)
	{
		try
		{

			// Log the incoming request for processing models
			log.info("POST call to '/projector-models' received.");

			String message; // Store the message returned in responses and logged.

			// Name of the projector to be stored.
			String modelName = projectorDto.getModelname();

			log.info("Projector model name: '" + modelName + "'.");

			if (modelName == null || modelName.isBlank() || modelName.isEmpty())
			{
				// .. if the dto has no string for the model name.
				message = "No projector model name given. Projector model needs a name.";
				log.error(message);
				// ERROR 496 - "No projector model name given.
				throw new ProjectorServerException(496, message);
			}

			// Recover optinal projector object from DB.
			Optional<ProjectorModel> existingModel = projectorModelRepository.findById(modelName);

			// Check if the projector exists ..
			if (existingModel.isPresent())
			{
				// .. if the projector exists throw exception.
				message = "The projector already exists in the database.";
				log.error(message);
				// ERROR 496 - Thrown when a user tries to upload a projector that is already
				// stored in the data base.
				throw new ProjectorServerException(496, message);
			}

			// If the projector does NOT exist yet, create it.
			ProjectorModel projectorModel = new ProjectorModel();
			projectorModel.setModelName(modelName);

			this.projectorModelRepository.saveAndFlush(projectorModel);

			message = "Projector with model name " + modelName + " succesffully stored in database.";

			log.debug(message);

			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (ProjectorServerException e)
		{
			log.error("{}", e.getMessage());
			return ResponseEntity.internalServerError().body(e.getMapError());
		} catch (Exception e)
		{
			String message = "Error encountered during projector model upload: ";
			log.error(message + " {}", e.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(message + e.getMessage());
		}

	}

	/**
	 * Deletes a projector model from the database.
	 * <p>
	 * This method handles the deletion of a projector model by performing the
	 * following checks: 1. Verifies that the model name is provided and is valid.
	 * 2. Checks if there are any projectors associated with the given model. 3. If
	 * no projectors are associated, attempts to delete the model from the database.
	 * 4. If any error occurs during the process (e.g., model not found, or
	 * projectors are still associated with the model), appropriate exceptions are
	 * thrown.
	 * </p>
	 *
	 * @param projectorDto The Data Transfer Object (DTO) containing the projector
	 *                     model name to be deleted. The model name must be valid
	 *                     and non-blank.
	 * 
	 * @return ResponseEntity A response entity containing the status of the
	 *         operation: - If the model is successfully deleted, returns HTTP
	 *         status 200 (OK) with a success message. - If any errors are
	 *         encountered, returns an HTTP status 500 (Internal Server Error) with
	 *         an error message.
	 * 
	 * @throws ProjectorServerException if: - The projector model name is blank or
	 *                                  null. - The model does not exist in the
	 *                                  database. - There are projectors still
	 *                                  associated with the model.
	 */
	@DeleteMapping("/projector-models")
	public ResponseEntity<?> deleteModel(@RequestBody ProjectorModelDto projectorDto)
	{
		try
		{
			// Log the incoming request for model deletion
			log.info("Received DELETE request for '/projector-models'.");

			// Validate the input
			if (projectorDto == null || projectorDto.getModelname() == null || projectorDto.getModelname().isBlank())
			{
				String message = "Projector model name is required for deletion.";
				log.warn(message); // Log as warning, as the request cannot proceed without a model name
				// Custom error code for missing model name
				throw new ProjectorServerException(497, message);
			}

			// Extract the model name to delete
			String modelName = projectorDto.getModelname();
			log.debug("Attempting to delete projector model: {}", modelName); // Log the specific model name

			// Check if there are any projectors associated with the given model
			Integer associatedProjectorCount = projectorRepository.countProjectorAssociatedModel(modelName);
			if (associatedProjectorCount > 0)
			{

				String sChar = associatedProjectorCount > 1 ? "s are" : " is";

				String message = "Deletion failed: " + associatedProjectorCount + " projector" + sChar
						+ " still associated with model " + modelName + ".";

				log.warn(message);

				// Custom error code for associated projectors
				throw new ProjectorServerException(591, message);
			}

			// Attempt to find the projector model in the database
			Optional<ProjectorModel> existingModel = projectorModelRepository.findById(modelName);
			if (existingModel.isEmpty())
			{
				// Log error if the model is not found and throw an exception
				String message = "Projector model " + modelName + " not found in the database for deletion.";
				log.error(message); // Log as error since it's an unexpected condition (model not found)
				throw new ProjectorServerException(497, message); // Custom error code for model not found
			}

			// Proceed to delete the projector model
			projectorModelRepository.deleteById(modelName);
			String successMessage = "Projector model " + modelName + " successfully deleted from the database.";
			log.info(successMessage); // Log successful deletion at info level

			// Response for client side.
			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, successMessage);
			return ResponseEntity.status(HttpStatus.OK).body(response); // Return success response

		} catch (ProjectorServerException e)
		{
			// Custom exceptions.
			log.error("Projector deletion failed: {}", e.getMessage());
			return ResponseEntity.internalServerError().body(e.getMapError());
		} catch (Exception e)
		{
			// Unexpected errors
			String message = "Unexpected error encountered during projector model deletion.";
			log.error("{} Error: {}", message, e.getLocalizedMessage());
			return ResponseEntity.internalServerError().body(message + e.getMessage());
		}
	}

	/**
	 * Retrieves a list of all projector models from the database.
	 * 
	 * This endpoint queries the database to fetch all the available projector
	 * models in the form of a list of {@link ProjectorModelDto}. If no models are
	 * found, it returns a 404 Not Found response with a relevant message. In case
	 * of a server error during the retrieval process, a 500 Internal Server Error
	 * response is returned with a message indicating an unexpected error.
	 * 
	 * @return ResponseEntity<?> - The response entity containing the status code
	 *         and either the list of projector models (HTTP 200 OK) or an error
	 *         message (HTTP 404 Not Found or HTTP 500 Internal Server Error).
	 * 
	 * @throws Exception If an error occurs while retrieving the projector models
	 *                   from the database.
	 */
	@GetMapping("/projector-models")
	public ResponseEntity<?> getModelsList()
	{
		try
		{
			// Log the start of the retrieval process
			log.info("GET call to '/projector-models' received.");

			// Retrieve the list of projector models from the database
			List<ProjectorModelDto> projectorModelList = this.projectorModelRepository.getProjectorDtoList();

			// Check if the list is empty
			if (projectorModelList.size() < 1)
			{
				// Log if no models are found
				log.warn("No projector models found in the database.");

				// Return a 404 Not Found response with a message
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						"No projector models found in the database. Please ensure models are added before retrieving the list.");
			}

			// Log the successful retrieval of the models
			log.info("Successfully retrieved {} projector models from the database.", projectorModelList.size());

			// Return a 200 OK response with the list of projector models
			return ResponseEntity.status(HttpStatus.OK).body(projectorModelList);

		} catch (Exception e)
		{
			// Define an error message
			String message = "An unexpected error occurred while retrieving the projector model list.";

			// Log the error in case of failure
			log.error(message, e);

			// Return a 500 Internal Server Error response with an error message
			return ResponseEntity.internalServerError().body(message);
		}
	}

	// --------------------- PROJECTORS ENDPOINTS --------------------------------

	@GetMapping("/projectors")
	public ResponseEntity<?> getProjectorList(@RequestParam(value = "criteria", required = false) String criteria,
			@PageableDefault(page = 0, size = 10) Pageable pageable)
	{

		log.info("Call to /getProjectorList received with criteria: " + criteria);

		Page<ProjectorInfoDto> projectors;

		String message;

		String modelNameCriteria = "modelname";

		// If the criteria parameter is not null nor blank, then compare with criteria
		// expected for modelname.
		if (criteria != null && !criteria.isBlank() && modelNameCriteria.equals(criteria.toLowerCase().trim()))
		{
			projectors = this.projectorRepository.getProjectorOrderByModelName(pageable);
			message = "Projectors list ordered by model name.";
		}
		// If no criteria specified, return list ordered by floor and classroom.
		else
		{
			projectors = this.projectorRepository.getProjectorsOrderByFloorAndClassroom(pageable);
			message = "Projectors list ordered by floor and classroom.";
		}

		log.info(message);

		// Return a success response with the result
		return ResponseEntity.status(HttpStatus.OK).body(projectors);
	}

	@Transactional
	@PostMapping("/projectors")
	public ResponseEntity<?> asignProjectoToClassroom(@RequestBody ProjectorDto projectorDto)
	{
		try
		{

			log.info("Call to /assign-projector received.");

			ResponseDto responseDto = new ResponseDto();
			String message;

			String modelName = projectorDto.getModel();
			String classroomName = projectorDto.getClassroom();

			// Check if the classroom exists.
			Optional<Classroom> classroomOpt = this.classroomRepository.findById(classroomName);

			Classroom classroomEntity = classroomOpt
					.orElseThrow(() -> new ProjectorServerException(499, "Classroom does not exist."));

			log.debug("Classroom successfully retreived...");

			// Check if the projector model exists.
			Optional<ProjectorModel> projectorModelOpt = this.projectorModelRepository.findById(modelName);

			ProjectorModel projectorModelEntity = projectorModelOpt
					.orElseThrow(() -> new ProjectorServerException(499, "Projector Model does not exist."));

			log.debug("Projector model successfully retreived...");

			// Si la ejecución alcanza este punto es que ambas entidades existen.

			// Bloque de asignación.
			Projector projectorEntity = new Projector();

			projectorEntity.setClassroom(classroomEntity);
			projectorEntity.setModel(projectorModelEntity);

			this.projectorRepository.save(projectorEntity);

			message = "Projector " + modelName + " successfully assigned to classroom " + classroomName + ".";

			log.debug(message);

			responseDto.setStatus(Constants.RESPONSE_STATUS_SUCCESS);
			responseDto.setMessage(message);

			return ResponseEntity.status(HttpStatus.OK).body(responseDto);

		} catch (ProjectorServerException e)
		{
			log.error(e.getMessage());
			return ResponseEntity.internalServerError().body(e.getMapError());
		}
	}

	@Transactional
	@DeleteMapping("/projectors")
	public ResponseEntity<?> removeProjectorFromClassroom(@RequestBody ProjectorDto projectorDto)
	{
		try
		{
			log.info("Call to /remove-projector received.");

			ResponseDto responseDto = new ResponseDto();
			String message;

			String modelName = projectorDto.getModel();
			String classroomName = projectorDto.getClassroom();

			// Find the classroom
			Classroom classroomEntity = classroomRepository.findById(classroomName)
					.orElseThrow(() -> new ProjectorServerException(499, "Classroom does not exist."));

			// Find the projector model
			ProjectorModel projectorModelEntity = projectorModelRepository.findById(modelName)
					.orElseThrow(() -> new ProjectorServerException(499, "Projector Model does not exist."));

			log.debug("Classroom and projector model found.");

			ProjectorId projectorId = new ProjectorId();

			projectorId.setClassroom(classroomEntity);
			projectorId.setModel(projectorModelEntity);

			// Find the assigned projector
			Optional<Projector> projectorOpt = projectorRepository.findById(projectorId);

			Projector projectorEntity = projectorOpt
					.orElseThrow(() -> new ProjectorServerException(499, "Projector assignment does not exist."));

			// Remove the projector assignment
			projectorRepository.delete(projectorEntity);

			message = "Projector " + modelName + " successfully removed from classroom " + classroomName + ".";

			log.debug(message);

			responseDto.setStatus(Constants.RESPONSE_STATUS_SUCCESS);
			responseDto.setMessage(message);

			return ResponseEntity.status(HttpStatus.OK).body(responseDto);

		} catch (ProjectorServerException e)
		{
			log.error(e.getMessage());
			return ResponseEntity.internalServerError().body(e.getMapError());
		}
	}

	// ------------- !!!!! TO BE REVISED FROM HERE ON !!!!! -------------
	// From this line forwards endpoints are basically bare bones and if they work
	// it is just a temporary fix. Must be revised to consolidate and secure.
	// ------------- !!!!! TO BE REVISED FROM HERE ON !!!!! -------------

	// --------------------- SERVER EVENT ENDPOINTS --------------------------------

	public ServerEvent createServerEventEntity(String projectorModelName, String projectorClassroom,
			String commandActionName) throws ProjectorServerException
	{

		// Check if any of the parameters is null or empty/blank string.
		if (projectorModelName == null || projectorModelName.isBlank() || projectorClassroom == null
				|| projectorClassroom.isBlank() || commandActionName == null || commandActionName.isBlank())
		{
			// if blank or null throw exception.
			throw new ProjectorServerException(505, "Null parameter received while during server event creation.");
		}

		// log call.
		log.info("Creating '" + commandActionName + "' event for projector '" + projectorModelName + "' in classroom "
				+ projectorClassroom);

		/* -------------- FORMING PROJECTOR ENTITY -------------- */
		// This block contains the logic to retreive the entities related to the
		// projector entiy.

		// Check if the model name exists.
		Optional<ProjectorModel> projectorModelOpt = this.projectorModelRepository.findById(projectorModelName);

		// If the model exists, stores in entity, otherwise throws new exception and
		// logs error.
		ProjectorModel projectorModelEntity = projectorModelOpt.orElseThrow(() ->
		{
			String message = "The projector model '" + projectorModelName + "' does not exist.";
			log.error(message);
			return new ProjectorServerException(494, message);
		});

		log.debug("PROJECTOR MODEL RETRIEVED: " + projectorModelEntity.toString());

		// Check if the classroom exists.
		Optional<Classroom> classroomOpt = this.classroomRepository.findById(projectorClassroom);

		// If the classroom exists, stores in entity, otherwise throws new exception and
		// logs error.
		Classroom classroomEntity = classroomOpt.orElseThrow(() ->
		{
			String message = "The classroom " + projectorClassroom + " does not exist.";
			log.error(message);
			return new ProjectorServerException(494, message);
		});

		log.debug("CLASSROOM RETRIEVED: " + classroomEntity.toString());

		// Create composite projector ID.
		ProjectorId projectorId = new ProjectorId();
		projectorId.setClassroom(classroomEntity);
		projectorId.setModel(projectorModelEntity);

		// Check if the projector exists.
		Optional<Projector> projectorOpt = this.projectorRepository.findById(projectorId);
		Projector projectorEntity = projectorOpt.orElseThrow(() ->
		{
			String message = "The projector model '" + projectorModelName + " in classroom " + projectorClassroom
					+ "' does not exist.";
			log.error(message);
			return new ProjectorServerException(494, message);
		});

		log.debug("PROJECTOR UNIT RETRIEVED: " + projectorEntity.toString());

		/* -------------------- END FORMING PROJECTOR ENTITY -------------------- */

		/* -------------------- RETREIVE COMMAND ENTITY -------------------- */

		// Check if the action exists.
		Optional<Action> actionOpt = this.actionRepositories.findById(commandActionName);

		Action actionEntity = actionOpt.orElseThrow(() ->
		{
			String message = "The given action '" + commandActionName + "' does not exist.";
			log.error(message);
			return new ProjectorServerException(494, message);
		});

		log.debug("COMMAND ACTION RETRIEVED: " + actionEntity.toString());

		// Search for a command for the given model and action.
		Optional<Command> commandOpt = this.commandRepository.findByModelNameAndAction(projectorModelEntity,
				actionEntity);

		Command commandEntity = commandOpt.orElseThrow(() ->
		{
			String message = "No command found in DB for model " + projectorModelName + " to perform action "
					+ commandActionName;
			log.error(message);
			return new ProjectorServerException(494, message);
		});

		log.debug("COMMAND RETRIEVED: " + commandEntity.toString());

		/* -------------- END RETREIVE COMMAND ENTITY -------------- */

		/* -------------- SET OTHER PARAMETERS FOR THE EVENT -------------- */

		// Tomar fecha actual.
		LocalDateTime dateTime = LocalDateTime.now();

		// Tomar usuario.
		String user = "TO DO";

		// Asignar estado por defecto.
		String defaultStatus = Constants.EVENT_STATUS_PENDING;

		// Crear nuevo objeto server event y asignar valores.
		ServerEvent serverEventEntity = new ServerEvent();

		serverEventEntity.setCommand(commandEntity);
		serverEventEntity.setProjector(projectorEntity);
		serverEventEntity.setActionStatus(defaultStatus);
		serverEventEntity.setDateTime(dateTime);
		serverEventEntity.setUser(user);

		// Guardar objeto en bbdd.
		return serverEventEntity;
	}

	@Transactional
	@PostMapping(value = "/server-events-batch")
	public ResponseEntity<?> createServerEventBatch(
			@RequestBody(required = true) ServerEventBatchDto serverEventBatchDto)
	{
		try
		{
			String commandActionName = serverEventBatchDto.getAction();
			List<ProjectorDto> projectorList = serverEventBatchDto.getProjectorList();
			List<ServerEvent> serverEventList = new ArrayList<>();

			// For each projector, create an event.
			for (ProjectorDto projectorDto : projectorList)
			{
				serverEventList.add(this.createServerEventEntity(projectorDto.getModel(), projectorDto.getClassroom(),
						commandActionName));
			}

			log.info("Saving {} server events to the database.", serverEventList.size());

			// Save all the events in a single transaction.
			this.serverEventRepository.saveAllAndFlush(serverEventList);

			// Prepare response object
			ResponseDto response = new ResponseDto();
			response.setMessage(serverEventList.size() + " events successfully created.");
			response.setStatus(Constants.RESPONSE_STATUS_SUCCESS);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (ProjectorServerException ex)
		{
			return ResponseEntity.badRequest().body(ex.getMapError());
		}
	}

	/**
	 * Endpoint que recibe una peticion por parte de un microcontrolador y devuelve
	 * una acción a realizar.
	 * 
	 * Este endpoint espera parametros utilizados para identificar el proyector con
	 * el que el micro está asociado. Esta identificacion es necesaria para poder
	 * saber que orden debe de servir el endpoint al microcontrolador para que este
	 * la re-envie al proyector.
	 * 
	 * Enviar al micro: ID accion + Orden
	 * 
	 */
	@GetMapping(value = "/server-events")
	public String serveCommandToController(@RequestParam(required = true) String projectorModel,
			@RequestParam(required = true) String projectorClassroom)
	{

		// TODO ESTO PROVISIONAL.
		Floor floor = new Floor();
		floor.setFloorName("Planta baja");

		Classroom classroom = new Classroom();
		classroom.setFloor(floor);
		classroom.setClassroomName("0.01");

		Projector projector = new Projector();
		projector.setClassroom(classroom);
		projector.setModel(this.projectorModelRepository.findById("Epson EB-S41").get());

		String actionStatus = Constants.EVENT_STATUS_PENDING;

		// recupear el ultimo comando para el modelo y aula especificado
		List<SimplifiedServerEventDto> simpleEvent = this.serverEventRepository.findMostRecentCommandOpen(projector,
				actionStatus);

		log.debug(simpleEvent.toString());

		if (simpleEvent.size() > 0)
		{
			return simpleEvent.get(0).toString();
		}

		return null;
	}

	@GetMapping(value = "/server-events-table")
	public ResponseEntity<?> serveCommandsTable()
	{

		List<TableServerEventDto> commandsList = this.serverEventRepository.getTableServerEventDtoList();

		return ResponseEntity.ok().body(commandsList);
	}

	/**
	 * Endpoint que recibe una peticion por parte de un microcontrolador y actualiza
	 * el estado de una accion.
	 * 
	 * Para saber que orden actualizar, recibir por parametro ID Accion a actualizar
	 * y codigo de estado.
	 */
	public void updateServerEventStatus()
	{

	}

	@GetMapping(value = "/micro-greeting")
	public ResponseEntity<?> acknowledgeMicro()
	{
		log.info("Call received on /micro-greeting");

		// CommandDto cdto = new CommandDto("1","2","3");

		return ResponseEntity.ok().body("turn-on");
	}

	// testing stuff
	@GetMapping(value = "/floors")
	public ResponseEntity<?> getFloorList()
	{
		log.info("Call to /floors received.");
		List<FloorDto> floors = this.floorRepository.findAllDto();

		log.info("Retrieved floors: \n" + floors);
		return ResponseEntity.ok().body(floors);
	}

	@GetMapping(value = "/classrooms")
	public ResponseEntity<?> getClassroomList(@RequestParam(required = true) String floor)
	{
		log.info("Call to /classrooms received with param : " + floor);
		List<ClassroomDto> classroom = this.classroomRepository.findDtoListByFloorName(floor);
		log.info("Retrieved classrooms: \n" + classroom);
		return ResponseEntity.ok().body(classroom);
	}

	@GetMapping(value = "/classroom-projectors")
	public ResponseEntity<?> getClassroomProjectors(@RequestParam(required = true) String classroom)
	{

		List<ProjectorDto> projectors = this.projectorRepository.getProjectorByClassroom(classroom);

		return ResponseEntity.ok().body(projectors);

	}

	@GetMapping(value = "/commands")
	public ResponseEntity<?> getProjectorModelCommands(@RequestParam(required = true) String modelname)
	{
		// Controlar que el modelo exista.
		// si no existe devolver 404

		// Si no existen comandos devolver 204 (no content)

		// si existen devolver los comandos

		List<CommandDto> commands = this.commandRepository.findCommandsByModel(modelname);

		if (commands.size() < 1)
		{
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok().body(commands);
	}

}
