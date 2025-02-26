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

	// ----------------------- HELPING METHODS ------------------------------------

	/**
	 * Validates the uploaded CSV file.
	 * <p>
	 * This method checks if the file is empty and if the content type is valid for
	 * CSV files. It throws a {@link ProjectorServerException} with appropriate
	 * error codes if the validation fails.
	 * </p>
	 *
	 * @param file The uploaded file to be validated.
	 * @throws ProjectorServerException if the file is empty or has an invalid
	 *                                  content type.
	 */
	private void validateFile(MultipartFile file) throws ProjectorServerException
	{
		// Check if the file is empty
		if (file.isEmpty())
		{
			throw new ProjectorServerException(490, "ERROR: Empty CSV file received.");
		}

		String contentType = file.getContentType();

		// Check if the file content type is valid for CSV files
		if (contentType == null || !contentType.startsWith("text/csv"))
		{
			throw new ProjectorServerException(498, "ERROR: Unsupported format. Expected format CSV.");
		}
	}

	// ----------------------- PARSING ENDPOINTS
	// -----------------------------------.

	/**
	 * Handles the parsing of multiple CSV files: projectors and commands. This
	 * endpoint accepts optional files and processes them accordingly. If no files
	 * are provided, it returns an error response.
	 * 
	 * <p>
	 * The method performs the following steps:
	 * <ul>
	 * <li>Validates the received files.</li>
	 * <li>Parses the projectors file if provided.</li>
	 * <li>Parses the commands file if provided.</li>
	 * <li>Returns a structured response with parsing results.</li>
	 * </ul>
	 * </p>
	 * 
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
			@RequestParam(value = "projectors.csv", required = false) MultipartFile projectorsFile,
			@RequestParam(value = "commands.csv", required = false) MultipartFile commandsFile)
	{
		try
		{

			log.info("Call to /parse-multifile received.");
			String message = ""; // Message to be returned in the response DTO.
			RichResponseDto richResponseDto = new RichResponseDto();

			// ERROR 498 - No files received, aborting operation.
			if ((projectorsFile == null || projectorsFile.isEmpty())
					&& (commandsFile == null || commandsFile.isEmpty()))
			{
				message = "No files received. Aborting parse operation.";
				log.error(message);
				throw new ProjectorServerException(498, message);
			}

			// Processing projectors file if provided.
			if (projectorsFile == null || projectorsFile.isEmpty())
			{
				log.info("No 'projectors.csv' file received.");
				richResponseDto.setMessage1("The request did not include a file for projectors.");

			} else
			{
				// Try with for automatic scanner closure.
				try (Scanner scanner = new Scanner(projectorsFile.getInputStream()))
				{
					this.validateFile(projectorsFile); // Validate file format before processing.
					richResponseDto.setMessage1(projectorParser.parseProjectors(scanner)); // Process projectors file.
				}
			}

			// Processing commands file if provided.
			if (commandsFile == null || commandsFile.isEmpty())
			{
				log.info("No 'commands.csv' file received.");
				richResponseDto.setMessage2("The request did not include a file for projector commands.");
			} else
			{
				log.info("Now calling Commands parser.");
				try (Scanner scanner = new Scanner(commandsFile.getInputStream()))
				{
					this.validateFile(commandsFile); // Validate file format before processing.
					richResponseDto.setMessage2(commandsParser.parseCommands(scanner)); // Process commands file.
				}
			}

			// Setting up the response object.
			richResponseDto.setStatus(Constants.RESPONSE_STATUS_SUCCESS);

			log.info("Projectors: " + richResponseDto.getMessage1() + "\nCommands: \" + "
					+ richResponseDto.getMessage2());

			return ResponseEntity.ok(richResponseDto);

		} catch (ProjectorServerException e)
		{
			// Custom exception.
			log.error("Projector server error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());
		} catch (IOException e)
		{
			log.error("Error reading the file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");
		} catch (Exception e)
		{
			// Por si las moscas.
			log.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	/**
	 * Handles the upload and parsing of a CSV file containing model data.
	 * <p>
	 * This endpoint receives a CSV file, reads its content, and processes the data
	 * using the {@code projectorModelsParser}. The file is validated to ensure it
	 * is not empty and that it is in CSV format. If an error occurs during parsing,
	 * an appropriate error message is returned to the client.
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

		// Log the incoming request for processing models
		log.info("Call to '/parse-models' received.");
		String message;

		// Use "try-with-resources" to automatically close the scanner when done with
		// the InputStream
		try (Scanner scanner = new Scanner(file.getInputStream()))
		{

			// Validate the file before processing
			this.validateFile(file);

			// Parse the models from the CSV file using the projectorModelsParser
			message = projectorModelsParser.parseProjectorModels(scanner);

			// Log the result of the parsing operation
			log.info("MODELS TABLE - " + message);

			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);

			// Return a success response with the result
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IOException e)
		{
			// Log and return an error response in case of an IO exception (e.g., reading
			// the file)
			log.error("Error reading the file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");
		} catch (ProjectorServerException e)
		{
			// Log and return an error response for custom exceptions
			log.error("Projector server error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());
		} catch (Exception e)
		{
			// Catch any unexpected exceptions and log the error
			log.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}

		// -----------------------------------

	}

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

			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);

			// Return a success response with the result
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
			return ResponseEntity.internalServerError().body(e.getMapError());
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

			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);

			// Return a success response with the result
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

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
			return ResponseEntity.internalServerError().body(e.getMapError());
		}
		// Por si las moscas.
		catch (Exception e)
		{
			log.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	// -------------------- UPLOAD AND DELETE RECORDS ENDPOINTS --------------------

	@GetMapping("/projectors")
	public ResponseEntity<?> getProjectorList(
			@RequestParam(value = "criteria", required = false) String criteria,
			@PageableDefault(page = 0, size = 10) Pageable pageable
			)
	{

		log.info("Call to /getProjectorList received with criteria: " + criteria);

		Page<ProjectorInfoDto> projectors;
		
		String message;

		String modelNameCriteria = "modelname";

		// If the criteria parameter is not null nor blank, then compare with criteria expected for modelname.
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

	@Transactional
	@PostMapping("/assign-projector")
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

	// ------------- !!!!! TO BE REVISED FROM HERE ON !!!!! -------------
	// From this line forwards endpoints are basically bare bones and if they work
	// it is just a temporary fix. Must be revised to consolidate and secure.
	// ------------- !!!!! TO BE REVISED FROM HERE ON !!!!! -------------

	// --------------------- SERVER EVENT ENDPOINTS --------------------------------

	/**
	 * Endpoint para que los usuarios envien las acciones al servidor. Este endpoint
	 * almacena en la tabla EVENTOS SERVIDOR una accion que luego el
	 * microcontrolador recuperara para saber que decirle al proyector que debe de
	 * hacer. Para guardar el evento es necesario: ID Evento - Automaticamente
	 * generado. Fecha evento - Generada mediante el metodo. Usuario Autor -
	 * Parametro de la petición. Comando - Comando que se quiere enviar al
	 * proyector. Proyector - Proyecto al que se quiere enviar la orden.
	 */
	@Transactional
	@PostMapping(value = "/server-events")
	public ResponseEntity<?> createServerEvent(@RequestBody(required = true) ServerEventDto serverEventDto)
	{
		try
		{

			String projectorModelName = serverEventDto.getProjectorDto().getModel();

			String projectorClassroom = serverEventDto.getProjectorDto().getClassroom();

			String commandModelName = serverEventDto.getCommandDto().getModelName();

			String commandActionName = serverEventDto.getCommandDto().getAction();

			String commandCommand = serverEventDto.getCommandDto().getCommand();

			log.info("Call to '/server-events' received with parameters: \n" + " - projector: "
					+ String.valueOf(projectorModelName) + " - " + String.valueOf(projectorClassroom) + "\n - command: "
					+ String.valueOf(commandModelName) + " - " + String.valueOf(commandActionName) + " - "
					+ String.valueOf(commandCommand));

			// Comprobar que el modelo del proyector exista
			Optional<ProjectorModel> projectorModelOpt = this.projectorModelRepository.findById(projectorModelName);
			ProjectorModel projectorModelEntity = projectorModelOpt.orElseThrow(() -> new ProjectorServerException(494,
					"The projector model '" + projectorModelName + "' does not exist."));
			log.debug("PROJECTOR MODEL RETRIEVED: " + projectorModelEntity.toString());

			// Comprobar que la clase exista.
			Optional<Classroom> classroomOpt = this.classroomRepository.findById(projectorClassroom);

			Classroom classroomEntity = classroomOpt.orElseThrow(() -> new ProjectorServerException(494,
					"The classroom " + projectorClassroom + " does not exist."));
			log.debug("CLASSROOM RETRIEVED: " + classroomEntity.toString());

			// Comprobar que el proyector exista.
			ProjectorId projectorId = new ProjectorId();
			projectorId.setClassroom(classroomEntity);
			projectorId.setModel(projectorModelEntity);

			Optional<Projector> projectorOpt = this.projectorRepository.findById(projectorId);
			Projector projectorEntity = projectorOpt
					.orElseThrow(() -> new ProjectorServerException(494, "The projector model '" + projectorModelName
							+ " in classroom " + projectorClassroom + "' does not exist."));
			log.debug("PROJECTOR UNIT RETRIEVED: " + projectorEntity.toString());

			// Comprobar que la acción exista.
			Optional<Action> actionOpt = this.actionRepositories.findById(commandActionName);
			Action actionEntity = actionOpt.orElseThrow(() -> new ProjectorServerException(494,
					"The given action '" + commandActionName + "' does not exist."));
			log.debug("COMMAND ACTION RETRIEVED: " + actionEntity.toString());

			// Comprobar que el modelo del comando exista.
			Optional<ProjectorModel> commandProjectorModelOpt = this.projectorModelRepository
					.findById(commandModelName);
			ProjectorModel commandProjectorModelEntity = commandProjectorModelOpt
					.orElseThrow(() -> new ProjectorServerException(494,
							"The projector model '" + commandModelName + "' does not exist."));
			log.debug("COMMAND MODEL RETRIEVED: " + commandProjectorModelEntity.toString());

			// comprobar que la orden exista.
			CommandId commandId = new CommandId();
			commandId.setAction(actionEntity);
			commandId.setModelName(commandProjectorModelEntity);
			commandId.setCommand(commandCommand);

			Optional<Command> commandOpt = this.commandRepository.findById(commandId);
			Command commandEntity = commandOpt.orElseThrow(
					() -> new ProjectorServerException(494, "The command '" + commandId + "' does not exist."));
			log.debug("COMMAND RETRIEVED: " + commandEntity.toString());

			// comprobar que la orden enviada corresponda al proyector enviado
			log.debug("Checking for models coincidence..");
			if (!projectorModelEntity.equals(commandProjectorModelEntity))
			{
				String message = "The model " + commandModelName + " and the model " + projectorModelName
						+ " are not the same.";
				log.error(message);
				throw new ProjectorServerException(495, message);
			}

			// Tomar fecha actual.
			LocalDateTime dateTime = LocalDateTime.now();

			// Tomar usuario.
			// TODO: Crear funcionamiento usuarios.
			String user = "TO DO";

			// Asignar estado por defecto.
			// TODO: Establecer los estados que puede tener un evento.

			// Crear nuevo objeto server event y asignar valores.
			ServerEvent serverEventEntity = new ServerEvent();

			serverEventEntity.setCommand(commandEntity);
			serverEventEntity.setProjector(projectorEntity);
			serverEventEntity.setActionStatus(Constants.EVENT_STATUS_PENDING);
			serverEventEntity.setDateTime(dateTime);
			serverEventEntity.setUser(user);

			// Guardar objeto en bbdd.
			this.serverEventRepository.saveAndFlush(serverEventEntity);

			String message = "Command sent successfully";

			ResponseDto response = new ResponseDto(Constants.RESPONSE_STATUS_SUCCESS, message);

			return ResponseEntity.ok().body(response);

		} catch (ProjectorServerException ex)
		{
			// do stuff
			log.error("Error during server event creation: " + ex.getMessage());
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
