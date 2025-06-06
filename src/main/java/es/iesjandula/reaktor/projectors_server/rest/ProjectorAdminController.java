package es.iesjandula.reaktor.projectors_server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.projectors_server.dtos.ActionDto;
import es.iesjandula.reaktor.projectors_server.dtos.CommandDto;
import es.iesjandula.reaktor.projectors_server.dtos.GeneralCountOverviewDto;
import es.iesjandula.reaktor.projectors_server.dtos.ProjectorInfoDto;
import es.iesjandula.reaktor.projectors_server.dtos.ResponseDto;
import es.iesjandula.reaktor.projectors_server.dtos.RichResponseDto;
import es.iesjandula.reaktor.projectors_server.dtos.ServerEventOverviewDto;
import es.iesjandula.reaktor.projectors_server.entities.Command;
import es.iesjandula.reaktor.projectors_server.entities.Projector;
import es.iesjandula.reaktor.projectors_server.entities.ServerEvent;
import es.iesjandula.reaktor.projectors_server.entities.ids.CommandId;
import es.iesjandula.reaktor.projectors_server.parsers.interfaces.ICommandParser;
import es.iesjandula.reaktor.projectors_server.parsers.interfaces.IProjectorParser;
import es.iesjandula.reaktor.projectors_server.repositories.ICommandRepository;
import es.iesjandula.reaktor.projectors_server.repositories.IProjectorRepository;
import es.iesjandula.reaktor.projectors_server.repositories.IServerEventHistoryRepository;
import es.iesjandula.reaktor.projectors_server.repositories.IServerEventRepository;
import es.iesjandula.reaktor.projectors_server.utils.Constants;
import es.iesjandula.reaktor.projectors_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/projectors")
public class ProjectorAdminController {

	@Autowired
	ICommandParser commandsParser;

	@Autowired
	IProjectorParser projectorParser;

	@Autowired
	IServerEventRepository serverEventRepository;

	@Autowired
	IProjectorRepository projectorRepository;

	@Autowired
	ICommandRepository commandRepository;
	
	@Autowired
	IServerEventHistoryRepository serverEventHistoryRepository;
	

	// -----------------------------------------------------------------------------
	// ------------------------------- CONTROL PANEL -------------------------------
	// -----------------------------------------------------------------------------3

	// ------------------------------ PARSING METHODS ------------------------------
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
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	//@PostMapping("/parse-multifile")
	@RequestMapping(method = RequestMethod.POST, value = "/parse-multifile", consumes = "multipart/form-data")
	public ResponseEntity<?> parseMultifile(
			@RequestBody( required = false) MultipartFile projectorsFile,
			@RequestBody( required = false) MultipartFile commandsFile) {
		try {
			// Log the incoming request to parse commands
			log.info("POST request for '/parse-multifile' received.");

			// Initialize message and response DTO for structured results.
			String message = "";
			RichResponseDto richResponseDto = new RichResponseDto();

			// Check if no files were received, and return an error message.
			if ((commandsFile == null || commandsFile.isEmpty())
					&& (projectorsFile == null || projectorsFile.isEmpty())) {
				message = "No files received for parse operation.";
				log.error(message);
				throw new ProjectorServerException(498, message); // Custom error for no files received.
			}

			// Process projectors file if provided.
			if (projectorsFile != null && !projectorsFile.isEmpty()) {
				log.info("Processing 'projectors.csv' file.");
				try (Scanner scanner = new Scanner(projectorsFile.getInputStream())) {
					this.validateFile(projectorsFile); // Validate the file before processing.
					richResponseDto.setMessage2(projectorParser.parseProjectors(scanner)); // Parse projectors.
					richResponseDto.setStatus2(Constants.RESPONSE_STATUS_SUCCESS);
				} catch (ProjectorServerException e) {
					richResponseDto.setMessage2(e.getMessage()); // Parse commands.
					richResponseDto.setStatus2(Constants.RESPONSE_STATUS_ERROR);
				}
			} else {
				log.info("No 'projectors.csv' file received.");
				richResponseDto.setMessage2("The request did not include a file for projectors.");
				richResponseDto.setStatus2(Constants.RESPONSE_STATUS_WARNING);
			}

			// Process commands file if provided.
			if (commandsFile != null && !commandsFile.isEmpty()) {
				log.info("Processing 'commands.csv' file.");
				try (Scanner scanner = new Scanner(commandsFile.getInputStream())) {
					this.validateFile(commandsFile); // Validate the file before processing.
					richResponseDto.setMessage1(this.commandsParser.parseCommands(scanner)); // Parse commands.
					richResponseDto.setStatus1(Constants.RESPONSE_STATUS_SUCCESS);
				} catch (ProjectorServerException e) {
					richResponseDto.setMessage1(e.getMessage()); // Parse commands.
					richResponseDto.setStatus1(Constants.RESPONSE_STATUS_ERROR);
				}
			} else {
				log.info("No 'commands.csv' file received.");
				richResponseDto.setMessage1("The request did not include a file for commands.");
				richResponseDto.setStatus1(Constants.RESPONSE_STATUS_WARNING);
			}

			// Log the final parsing results.
			log.info("Commands: {}\nProjectors: {}", richResponseDto.getMessage1(), richResponseDto.getMessage2());

			// Return the structured response DTO with success status.
			return ResponseEntity.ok().body(richResponseDto);

		} catch (ProjectorServerException e) {
			// Handle custom exceptions (e.g., invalid input, no files received).
			log.error("Projector server error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMapError());

		} catch (IOException e) {
			// Handle IO errors when reading the files.
			log.error("Error reading the file: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Error encountered while reading the file.");

		} catch (Exception e) {
			// Catch any unexpected errors.
			log.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR: Unexpected exception occurred.");
		}
	}

	
	// ---------------------------- PROJECTOR METHODS ------------------------------
	/**
	 * Retrieves a paginated list of projectors based on optional filters and
	 * sorting criteria.
	 * <p>
	 * This endpoint allows filtering by classroom, floor, or model. If a sorting
	 * criteria is provided, the list can be ordered by model name; otherwise, the
	 * default ordering is by floor and classroom.
	 * </p>
	 * 
	 * @param criteria  (Optional) Sorting criteria. If set to "modelname",
	 *                  projectors are ordered by model name. Any other value or
	 *                  null defaults to ordering by floor and classroom.
	 * @param classroom (Optional) Filter by classroom. If null, no classroom filter
	 *                  is applied.
	 * @param floor     (Optional) Filter by floor. If null, no floor filter is
	 *                  applied.
	 * @param model     (Optional) Filter by projector model. If null, no model
	 *                  filter is applied.
	 * @param pageable  Pagination and sorting parameters, including page number and
	 *                  page size.
	 * @return A paginated list of {@link ProjectorInfoDto} objects wrapped in
	 *         {@link ResponseEntity}. - **200 OK**: Successfully retrieved
	 *         projectors. - **204 No Content**: No projectors found matching the
	 *         filters. - **500 Internal Server Error**: An unexpected error
	 *         occurred.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@GetMapping("/projectors")
	public ResponseEntity<Page<ProjectorInfoDto>> getProjectorList(
			@RequestParam(value = "criteria", required = false) String criteria,
			@RequestParam(value = "classroom", required = false) String classroom,
			@RequestParam(value = "floor", required = false) String floor,
			@RequestParam(value = "model", required = false) String model,
			@RequestParam(value = "status", required = false) String status,
			@PageableDefault(page = 0, size = 15) Pageable pageable) {
		try {

			log.info("GET request received for '/projectors' with criteria: {}", criteria);

			Page<ProjectorInfoDto> projectors;

			boolean orderByModel = criteria != null && !criteria.isBlank()
					&& Constants.PROJECTORS_ORDER_CRITERIA_MODELNAME.equalsIgnoreCase(criteria.trim());

			// Determine ordering strategy based on criteria
			if (orderByModel) {
				log.debug("Applying ordering by model name.");
				projectors = projectorRepository.findProjectorsOrderedByModel(pageable, classroom, floor, model, status);
			} else {
				log.debug("Applying default ordering by floor and classroom.");
				projectors = projectorRepository.findProjectorsOrderedByFloorAndClassroom(pageable, classroom, floor,
						model, status);
			}

			log.info("Successfully retrieved {} projectors.", projectors.getTotalElements());
			return ResponseEntity.ok(projectors);

		} catch (Exception e) {
			log.error("Error while retrieving projectors: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body(Page.empty());
		}
	}

	/**
	 * Removes the selected projectors from the database.
	 * <p>
	 * This method handles the removal of one or more projectors from a classroom by
	 * performing the following checks: 1. Verifies that the provided classroom
	 * exists. 2. Verifies that the provided projector model exists. 3. Ensures that
	 * the projector assignment to the classroom exists before proceeding with
	 * removal. 4. If any error occurs during the process (e.g., classroom or
	 * projector model does not exist), appropriate exceptions are thrown.
	 * </p>
	 *
	 * @param projectorDto The Data Transfer Object (DTO) containing the classroom
	 *                     and projector model information to be removed.
	 *
	 * @return ResponseEntity The response entity containing the status of the
	 *         operation: - If the removal is successful, returns HTTP status 200
	 *         (OK) with a success message. - If any errors are encountered, returns
	 *         HTTP status 500 (Internal Server Error) with an error message.
	 *
	 * @throws ProjectorServerException If: - The classroom does not exist. - The
	 *                                  projector model does not exist. - The
	 *                                  projector assignment does not exist.
	 */
	@Transactional
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@DeleteMapping("/projectors")
	public ResponseEntity<?> deleteProjectors(@RequestBody List<ProjectorInfoDto> projectorDtoList) {
		try {
			log.info("DELETE request for '/projectors' received.");

			// Response DTO for returning status
			ResponseDto responseDto = new ResponseDto();
			String message;

			List<Projector> projectorEntitiesiList = new ArrayList<>();

			for (ProjectorInfoDto projectorDto : projectorDtoList) {
				String classroomName = projectorDto.getClassroom();
				String modelName = projectorDto.getModel();

				// Check if the projector assignment exists.
				Projector projectorEntity = projectorRepository.findById(classroomName).orElseThrow(() -> {
					String errorMessage = "Projector " + projectorDto + " does not exist.";
					log.error(errorMessage);
					return new ProjectorServerException(499, errorMessage);
				});

				// Log the projector assignment found.
				log.debug("Projector assignment found for model '{}' in classroom '{}'.", modelName, classroomName);

				// Add the current projector for later deletion order.
				projectorEntitiesiList.add(projectorEntity);

				// Log successful removal.
				message = "Projector " + modelName + " from classroom " + classroomName + "added to remove list.";
				log.info(message);

			}

			this.projectorRepository.deleteAll(projectorEntitiesiList);

			// Set the response DTO.
			message = String.format("Successfully removed %d projectors.", projectorEntitiesiList.size());
			log.info(message);
			responseDto.setStatus(Constants.RESPONSE_STATUS_SUCCESS);
			responseDto.setMessage(message);

			// Return the success response.
			return ResponseEntity.status(HttpStatus.OK).body(responseDto);

		} catch (DataIntegrityViolationException e) {
			log.error("Projector removal failed due to data integrity issues: {}", e.getMessage());
			return ResponseEntity.internalServerError().body(e.getMessage());
		}

		catch (ProjectorServerException e) {
			// Log the error for known exceptions (e.g., classroom or model not found).
			log.error("Projector removal failed: {}", e.getMessage());
			return ResponseEntity.internalServerError().body(e.getMapError());
		} catch (Exception e) {
			// Log the error for unexpected exceptions
			log.error("Unexpected error encountered while removing projector: {}", e.getMessage());
			return ResponseEntity.internalServerError().body("Unexpected error occurred: " + e.getMessage());
		}
	}
	
	
	@Transactional
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@DeleteMapping("/projectors-all")
	public ResponseEntity<?> deleteAllProjectors() {
		try {
			log.info("DELETE request for '/projectors-all' received.");

			// Response DTO for returning status
			ResponseDto responseDto = new ResponseDto();
			String message;
			
			List<Projector> projectorsList = this.projectorRepository.findAll();
			
			this.projectorRepository.deleteAll(projectorsList);
			
			int deletedRecords = projectorsList.size();
			
			// Set the response DTO.
			message = String.format("Successfully removed %d projectors.", deletedRecords);
			log.info(message);
			responseDto.setStatus(Constants.RESPONSE_STATUS_SUCCESS);
			responseDto.setMessage(message);

			// Return the success response.
			return ResponseEntity.status(HttpStatus.OK).body(responseDto);

		} catch (DataIntegrityViolationException e) {
			log.error("Projector removal failed due to data integrity issues: {}", e.getMessage());
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
		catch (Exception e) {
			// Log the error for unexpected exceptions
			log.error("Unexpected error encountered while removing projector: {}", e.getMessage());
			return ResponseEntity.internalServerError().body("Unexpected error occurred: " + e.getMessage());
		}
	}
	
	// ---------------------------- ACTIONS METHODS --------------------------------
	/**
	 * Handles HTTP DELETE requests to remove a list of actions from the system.
	 * <p>
	 * This method receives a list of actions (provided as DTOs), validates their
	 * existence, and deletes them from the database. If any action does not exist,
	 * an exception is thrown.
	 * </p>
	 * 
	 * @param actionsList The list of {@link ActionDto} objects representing the
	 *                    actions to be deleted.
	 * @return ResponseEntity containing a success message if actions are deleted
	 *         successfully, or an error message in case of failure.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@DeleteMapping(value = "/actions")
	// Ensures all repository interactions are wrapped in a transaction
	public ResponseEntity<?> deleteActions(@RequestBody List<ActionDto> actionsList) {
		try {
			log.info("DELETE request for '/actions' received with parameter '{}'.", actionsList);

			return deleteAction(actionsList);

		} catch (DataIntegrityViolationException e) {
			log.error("Data integrity violation: {}", e.getMessage());
			return ResponseEntity.internalServerError().body(
					"Deletion operation failed: the selected items are currently in use in server events and cannot be deleted.");

		} catch (ProjectorServerException e) {
			log.error(e.getMessage());
			return ResponseEntity.badRequest().body(e.getMapError());

		} catch (Exception e) {
			log.error("Unexpected error while deleting actions.", e);
			return ResponseEntity.internalServerError().body("Unexpected error while deleting actions.");
		}
	}

	/**
	 * Retrieves a paginated list of actions.
	 * 
	 * This endpoint returns a paginated list of actions using the provided
	 * pagination parameters. If no actions are found, a 204 No Content status is
	 * returned.
	 * 
	 * @param pageable The pagination information (page number, size).
	 * @return A paginated response with the list of actions.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
	@PostMapping(value = "/actions-page")
	public ResponseEntity<?> getActionsPage(@PageableDefault(page = 0, size = 15) Pageable pageable) {
		log.info("POST request for '/actions-page' received with pagination: page={}, size={}.",
				pageable.getPageNumber(), pageable.getPageSize());

		try {
			// Fetch paginated actions list
			Page<String> actionsPage = this.commandRepository.findActionsPage(pageable);

			// Log the successful retrieval of the actions page with pagination info
			log.info("Successfully retrieved page {} of actions, total pages: {}.", pageable.getPageNumber(),
					actionsPage.getTotalPages());

			// Return paginated actions with additional pagination metadata
			return ResponseEntity.ok().body(actionsPage);

		} catch (Exception e) {
			// Log any unexpected errors
			log.error("Error occurred while retrieving actions page: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("An error occurred while fetching actions data.");
		}
	}

	// ---------------------------- COMMANDS METHODS -------------------------------
	/**
	 * Retrieves a paginated list of commands based on the specified model and
	 * action.
	 * 
	 * This endpoint checks if the provided model and action exist, then fetches the
	 * commands for the given parameters with pagination. If no parameters are sent
	 * then it recovers all the commands unfiltered.
	 * 
	 * @param pageable  Pagination details (page number, size, etc.).
	 * @param modelName The model name to filter commands (optional).
	 * @param action    The action name to filter commands (optional).
	 * @return A response containing the paginated list of commands.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@PostMapping(value = "/commands-page")
	public ResponseEntity<?> getCommandsPage(@PageableDefault(page = 0, size = 15) Pageable pageable,
			@RequestParam(name = "modelName", required = false) String modelName,
			@RequestParam(name = "action", required = false) String action) {
		try {

			// Log the request.
			log.debug("POST request for '/commands-page' received with modelName: {}, action: {}", modelName, action);

			// Validate if the model exist.
			if (modelName != null && !this.commandRepository.existsByModelName(modelName)) {
				String errorMessage = String.format("There are no commands associated to model '%s'.", modelName);
				log.error("Model validation failed: {}", errorMessage);
				throw new ProjectorServerException(404, errorMessage);
			}

			// Validate if the action exist.
			if (action != null && !this.commandRepository.actionExists(action)) {
				String errorMessage = String.format("There are no commands associated to action '%s'.", action);
				log.error("Action validation failed: {}", errorMessage);
				throw new ProjectorServerException(404, errorMessage);
			}

			// Fetch the commands using pagination
			Page<CommandDto> commands = this.commandRepository.findAllCommandsPage(pageable, modelName, action);

			// Return the paginated response
			log.info("Successfully retrieved commands page.");
			return ResponseEntity.ok().body(commands);

		} catch (ProjectorServerException e) {
			// Log error and return a bad request response with the error details
			log.error("Error encountered during commands page retrieval: {}", e.getMapError());
			return ResponseEntity.badRequest().body(e.getMapError());

		} catch (Exception e) {
			// Log unexpected error and return a 500 internal server error response
			log.error("Unexpected error during commands page retrieval: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("An unexpected error occurred.");
		}
	}

	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@DeleteMapping(value = "/commands")
	@Transactional
	public ResponseEntity<?> deleteCommands(@RequestBody List<CommandDto> commandsList) {
		try {
			if (commandsList == null || commandsList.isEmpty()) {
				log.debug("No commands to delete.");
				return ResponseEntity.badRequest().body("No commands provided for deletion.");
			}

			log.debug("DELETE request for '/commands' received with parameter {}", commandsList);

			int recordsDeleted = 0;
			int serverEventsRecordsDeleted = 0;

			List<Command> commandListToDelete = new ArrayList<>();
			List<ServerEvent> serverEventListToDelete = new ArrayList<>();
			List<ServerEvent> serverEventIterationList = new ArrayList<>();

			for (CommandDto command : commandsList) {
				CommandId commandId = new CommandId();
				commandId.setAction(command.getAction());
				commandId.setModelName(command.getModelName());
				log.debug("Command Id generated for action '{}' and model '{}'", command.getAction(),
						command.getModelName());

				Command commandEntity = this.commandRepository.findById(commandId).orElseThrow(() -> {
					String message = String.format("No commands found for command ID with action '%s' and model '%s'",
							command.getAction(), command.getModelName());
					log.error(message);
					return new ProjectorServerException(499, message);
				});

				log.debug("Command retrieved for action '{}' and model '{}'.", command.getAction(),
						command.getModelName());

				// Fetch the associated server events to delete first
				serverEventIterationList = this.serverEventRepository.findByCommand(commandEntity.getModelName(),
						commandEntity.getAction());

				log.info("Recovered {} server event(s) for command '{}'.", serverEventIterationList.size(),
						commandEntity.toString());

				serverEventListToDelete.addAll(serverEventIterationList);
				serverEventsRecordsDeleted += serverEventIterationList.size();

				// Add command to delete list
				commandListToDelete.add(commandEntity);

				recordsDeleted++;

			}

			// Delete server events before commands
			this.serverEventRepository.deleteAllInBatch(serverEventListToDelete);

			// Delete the commands after server events
			this.commandRepository.deleteAllInBatch(commandListToDelete);

			String message = String.format("Deleted %d commands and %d associated server events.", recordsDeleted,
					serverEventsRecordsDeleted);

			log.info(message);

			ResponseDto response = new ResponseDto();
			response.setStatus(Constants.RESPONSE_STATUS_SUCCESS);
			response.setMessage(message);

			return ResponseEntity.ok().body(response);

		} catch (ProjectorServerException e) {
			log.error(e.getMessage());
			return ResponseEntity.badRequest().body(e.getMapError());

		} catch (Exception e) {
			log.error("Unexpected error while deleting actions.", e);
			return ResponseEntity.internalServerError().body("Unexpected error while deleting actions.");
		}
	}
	
	// ---------------------------- OVERVIEW METHODS -------------------------------
	/**
	 * Handles HTTP GET requests to retrieve an overview of server events.
	 * <p>
	 * This method fetches the count of server events categorized by their status
	 * (Canceled, Completed, Delivered, Error, and Pending) and returns this data
	 * encapsulated in a {@link ServerEventOverviewDto} object.
	 * </p>
	 * 
	 * @return ResponseEntity containing the event overview DTO if successful, or an
	 *         error message in case of failure.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@GetMapping("/events-overview")
	public ResponseEntity<?> getEventsOverview() {
		try {
			// Logging the request for monitoring and debugging
			log.info("GET request for '/events-overview' received.");

			// Creating a DTO object to hold event counts
			ServerEventOverviewDto serverEventOverviewDto = new ServerEventOverviewDto();

			// Populating the DTO with event counts based on their status
			serverEventOverviewDto.setCanceledEvents(
					this.serverEventHistoryRepository.countServerEventsByStatus(Constants.EVENT_STATUS_CANCELED));

			serverEventOverviewDto.setCompletedEvents(
					this.serverEventHistoryRepository.countServerEventsByStatus(Constants.EVENT_STATUS_EXECUTED));

			serverEventOverviewDto.setDeliveredEvents(
					this.serverEventHistoryRepository.countServerEventsByStatus(Constants.EVENT_STATUS_SERVED));

			serverEventOverviewDto
					.setErrorEvents(this.serverEventHistoryRepository.countServerEventsByStatus(Constants.EVENT_STATUS_ERROR));

			serverEventOverviewDto.setPendingEvents(
					this.serverEventHistoryRepository.countServerEventsByStatus(Constants.EVENT_STATUS_PENDING));

			
			log.info(serverEventOverviewDto.toString());
			
			// Returning the populated DTO with an HTTP 200 OK response
			return ResponseEntity.ok().body(serverEventOverviewDto);

		} catch (Exception e) {
			// Logging the error for debugging
			log.error("Error retrieving events overview", e);

			// Returning an HTTP 500 Internal Server Error response
			return ResponseEntity.internalServerError()
					.body("Unexpected error encountered while retrieving events overview.");
		}
	}

	/**
	 * Handles HTTP GET requests to retrieve a general overview of server-related
	 * entities.
	 * <p>
	 * This method gathers and returns a summary of various counts, including the
	 * number of projectors, actions, classrooms, commands, floors, and models. The
	 * data is encapsulated in a {@link GeneralCountOverviewDto} object.
	 * </p>
	 * 
	 * @return ResponseEntity containing the general overview DTO if successful.
	 */
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@GetMapping("/general-overview")
	public ResponseEntity<?> getServerOverview() {
		try {

			// Logging the request for monitoring and debugging
			log.info("GET request for '/general-overview' received.");

			// Creating a DTO object to hold count information
			GeneralCountOverviewDto projectorOverview = new GeneralCountOverviewDto();

			// Populating the DTO with counts of various entities
			projectorOverview.setNumberOfProjectors(this.projectorRepository.count());
			projectorOverview.setNumberOfActions(this.commandRepository.countDistinctActions());
			projectorOverview.setNumberOfClassrooms(this.projectorRepository.countClassrooms());
			projectorOverview.setNumberOfCommands(this.commandRepository.count());
			projectorOverview.setNumberOfFloors(this.projectorRepository.countFloors());
			projectorOverview.setNumberOfModels(this.commandRepository.countDistinctModels());

			// Returning the populated DTO with an HTTP 200 OK response
			return ResponseEntity.ok().body(projectorOverview);

		} catch (Exception e) {
			// Logging the error for debugging
			log.error("Error retrieving general overview", e);

			// Returning an HTTP 500 Internal Server Error response in case of failure
			return ResponseEntity.internalServerError()
					.body("Unexpected error encountered while retrieving general overview.");
		}
	}

	// ----------------------------- UTILITY METHODS -------------------------------
	@Transactional
	public ResponseEntity<?> deleteAction(List<ActionDto> actionsList) throws ProjectorServerException {
		// Validate that the received list is not null or empty
		if (actionsList == null || actionsList.isEmpty()) {
			String message = "Delete actions list is empty or null.";
			log.error(message);
			throw new ProjectorServerException(400, message);
		}

		// Extract action names from DTOs
		List<String> actionNamesList = actionsList.stream().map(ActionDto::getActionName).collect(Collectors.toList());

		// Check for non-existing actions
		List<String> existingActions = commandRepository.findExistingActions(actionNamesList);
		List<String> nonExistingActions = actionNamesList.stream().filter(action -> !existingActions.contains(action))
				.collect(Collectors.toList());

		if (!nonExistingActions.isEmpty()) {
			throw new ProjectorServerException(400, "Actions do not exist: " + nonExistingActions);
		}

		List<ServerEvent> serverEventsToDelete = new ArrayList<>();
		List<ServerEvent> serverEventsIteration = new ArrayList<>();
		int deletedServerEvents = 0;

		for (String actionName : actionNamesList) {

			serverEventsIteration = this.serverEventRepository.findAllByAction(actionName);
			log.debug("Adding server events to list for deletion. Found {} server events for action {}.",
					serverEventsIteration.size(), actionName);
			serverEventsToDelete.addAll(serverEventsIteration);
			deletedServerEvents += serverEventsIteration.size();
		}

		this.serverEventRepository.deleteAllInBatch(serverEventsToDelete);

		// Delete all commands associated with the actions
		int deletedCount = this.commandRepository.deleteCommandsByActions(actionNamesList);

		// Response for successful deletion
		ResponseDto response = new ResponseDto();

		String message = String.format(
				"%d actions deleted along with %d associated commands and related %d server events.",
				actionsList.size(), deletedCount, deletedServerEvents);
		log.info(message);
		response.setMessage(message);
		response.setStatus(Constants.RESPONSE_STATUS_SUCCESS);

		return ResponseEntity.ok().body(response);
	}

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
	private void validateFile(MultipartFile file) throws ProjectorServerException {
		log.info("Validation file method called for file {}", file.getName());

		String message;
		// Check if the file is empty
		if (file.isEmpty()) {
			message = "File validation failed: Received an empty CSV file.";
			log.error(message);
			throw new ProjectorServerException(490, message);
		}

		String contentType = file.getContentType();

		// Check if the file content type is valid for CSV files
		if (contentType == null || !contentType.equals("text/csv")) {
			message = "File validation failed: Unsupported file format received. Expected 'text/csv', but got: "
					+ contentType;
			log.error(message);
			throw new ProjectorServerException(498, message);
		}

		log.info("CSV file validation successful.");
	}

}
