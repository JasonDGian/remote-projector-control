package es.iesjandula.reaktor_projector_server.rest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor_projector_server.dtos.CommandDto;
import es.iesjandula.reaktor_projector_server.dtos.ProjectorDto;
import es.iesjandula.reaktor_projector_server.dtos.ServerEventDto;
import es.iesjandula.reaktor_projector_server.dtos.SimplifiedServerEventDto;
import es.iesjandula.reaktor_projector_server.dtos.TableServerEventDto;
import es.iesjandula.reaktor_projector_server.entities.Action;
import es.iesjandula.reaktor_projector_server.entities.Command;
import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ServerEvent;
import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;
import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.ICommandParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorModelParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorParser;
import es.iesjandula.reaktor_projector_server.repositories.IActionRepositories;
import es.iesjandula.reaktor_projector_server.repositories.ICommandRepository;
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
	IActionRepositories actionRepositories;
	
	// -----------------------  HELPING METHODS ------------------------------------
	
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

	// -----------------------  PARSING ENDPOINTS ------------------------------------
	
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
		

	// -----------------------  SERVER EVENT ENDPOINTS ------------------------------------
		
	/**
	 * Endpoint para que los usuarios envien las acciones al servidor.
	 * Este endpoint almacena en la tabla EVENTOS SERVIDOR una accion que luego el microcontrolador 
	 * recuperara para saber que decirle al proyector que debe de hacer.
	 * Para guardar el evento es necesario:
	 * ID Evento - Automaticamente generado.
	 * Fecha evento - Generada mediante el metodo.
	 * Usuario Autor - Parametro de la petición.
	 * Comando - Comando que se quiere enviar al proyector.
	 * Proyector - Proyecto al que se quiere enviar la orden.
	 */ 
	@PostMapping( value = "/server-events" )
	public ResponseEntity<?> createServerEvent( @RequestBody(required = true) ServerEventDto serverEventDto ){
		try {
			
		String projectorModelName = serverEventDto.getProjectorDto().getModel();
		
		String projectorClassroom = serverEventDto.getProjectorDto().getClassroom();
		
		String commandModelName = serverEventDto.getCommandDto().getModelName();
		
		String commandActionName = serverEventDto.getCommandDto().getAction();
		
		String commandCommand = serverEventDto.getCommandDto().getCommand();
		
		log.info("Call to '/server-events' received with parameters: \n"
				+ " - projector: " + projectorModelName.toString() + " - " + projectorClassroom.toString() 
				+ "\n - command: " + commandModelName.toString() + " - " + commandActionName.toString() + " - " + commandCommand.toString());
			
		// Comprobar que el modelo del proyector exista
		Optional<ProjectorModel> projectorModelOpt = this.projectorModelRepository.findById(projectorModelName);
		ProjectorModel projectorModelEntity = projectorModelOpt.orElseThrow( () -> new ProjectorServerException(494, "The projector model '" + projectorModelName +"' does not exist." ) );
		log.debug("PROJECTOR MODEL RETRIEVED: " + projectorModelEntity.toString() );
		
		// Comprobar que el proyector exista.
		ProjectorId projectorId = new ProjectorId();
		projectorId.setClassroom(projectorClassroom);
		projectorId.setModel(projectorModelEntity);
		
		Optional<Projector> projectorOpt = this.projectorRepository.findById(projectorId);
		Projector projectorEntity = projectorOpt.orElseThrow( ()-> new ProjectorServerException(494, "The projector model '" + projectorModelName + " in classroom " + projectorClassroom + "' does not exist." ));
		log.debug("PROJECTOR UNIT RETRIEVED: " + projectorEntity.toString());	
		
		// Comprobar que la acción exista.
		Optional<Action> actionOpt = this.actionRepositories.findById(commandActionName);
		Action actionEntity = actionOpt.orElseThrow( ()-> new ProjectorServerException(494, "The given action '" + commandActionName + "' does not exist." ) );
		log.debug("COMMAND ACTION RETRIEVED: " + actionEntity.toString());
		
		
		// Comprobar que el modelo del comando exista.
		Optional<ProjectorModel> commandProjectorModelOpt = this.projectorModelRepository.findById(commandModelName);
		ProjectorModel commandProjectorModelEntity = commandProjectorModelOpt.orElseThrow( () -> new ProjectorServerException(494, "The projector model '" + commandModelName +"' does not exist." ) );
		log.debug("COMMAND MODEL RETRIEVED: " + commandProjectorModelEntity.toString());
		
		// comprobar que la orden exista.
		CommandId commandId = new CommandId();
		commandId.setAction(actionEntity);
		commandId.setModelName(commandProjectorModelEntity);
		commandId.setCommand(commandCommand);
		
		Optional<Command> commandOpt = this.commandRepository.findById(commandId);
		Command commandEntity = commandOpt.orElseThrow( () -> new ProjectorServerException(494, "The command '" + commandId + "' does not exist." ) ); 
		log.debug("COMMAND RETRIEVED: " + commandEntity.toString());
		
		// comprobar que la orden enviada corresponda al proyector enviado
		log.debug("Checking for models coincidence..");
		if ( !projectorModelEntity.equals(commandProjectorModelEntity ) ){
			String message = "The model " + commandModelName + " and the model " + projectorModelName + " are not the same.";
			log.error(message);
			throw new ProjectorServerException(495, message );
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
		
		return ResponseEntity.ok().body("All done and okay");
		
		} 
		catch ( ProjectorServerException ex ){
			// do stuff
			log.error( "Error during server event creation: " + ex.getMessage() );
			return ResponseEntity.badRequest().body(ex.getMapError());
		}
	}

	/**
	 * Endpoint que recibe una peticion por parte de un microcontrolador y devuelve una acción a realizar.
	 * 
	 * Este endpoint espera parametros utilizados para identificar el proyector con el que el micro está asociado.
	 * Esta identificacion es necesaria para poder saber que orden debe de servir el endpoint al microcontrolador
	 * para que este la re-envie al proyector.
	 * 
	 * Enviar al micro: ID accion + Orden
	 * 
	 */
	@GetMapping( value = "/server-events")
	public String serveCommandToController( @RequestParam(required = true) String projectorModel, @RequestParam(required = true) String projectorClassroom  ){
		
		// TODO ESTO PROVISIONAL.
		
		Projector projector = new Projector();
		projector.setClassroom("0.01");
		projector.setModel( this.projectorModelRepository.findById("Epson EB-S41").get() );
		
		String actionStatus = Constants.EVENT_STATUS_PENDING;
		
		// recupear el ultimo comando para el modelo y aula especificado
		List<SimplifiedServerEventDto> simpleEvent = this.serverEventRepository.findMostRecentCommandOpen(projector, actionStatus);
		
		log.debug(simpleEvent.toString());
		
		if ( simpleEvent.size() > 0 ){
			return simpleEvent.get(0).toString();
		}
		
		return null;
	}
	
	@GetMapping( value = "/server-events-table")
	public ResponseEntity<?> serveCommandsTable(){
		
		List<TableServerEventDto> commandsList = this.serverEventRepository.getTableServerEventDtoList();
		
		return ResponseEntity.ok().body(commandsList);
	}
	
	/**
	 * Endpoint que recibe una peticion por parte de un microcontrolador y actualiza el estado de una accion.
	 * 
	 * Para saber que orden actualizar, recibir por parametro ID Accion a actualizar y codigo de estado.
	 */
	public void updateServerEventStatus(){
		
	}
	
	@GetMapping( value = "/micro-greeting")
	public ResponseEntity<?> acknowledgeMicro(){
		log.info("Call received on /micro-greeting");
		
		//CommandDto cdto = new CommandDto("1","2","3");
		
		return ResponseEntity.ok().body("turn-on");
	}
	
	
	// testing stuff
	@GetMapping( value = "/floors")
	public ResponseEntity<?> getFloorList(){
		
		String [] floors = {"Planta 0", "Planta 1", "Planta 2", "Planta 3"};

		return ResponseEntity.ok().body(floors);
	}
	
	@GetMapping( value = "/classrooms")
	public ResponseEntity<?> getClassroomList( @RequestParam(required = true) String floor   ){
	
		List<String> classroom = new ArrayList();
		
		if ( "Planta 0".equals(floor)) {
			classroom.add("0.01");
			classroom.add("0.02");
			classroom.add("0.03");
			 
		}
		
		if ( "Planta 1".equals(floor)) {
			classroom.add("1.01");
			classroom.add("1.02");
			classroom.add("1.03");
			 
		}
		if ( "Planta 2".equals(floor)) {
			classroom.add("2.01");
			classroom.add("2.02");
			classroom.add("2.03");
			
		}
		if ( "Planta 3".equals(floor)) {
			classroom.add("3.01");
			classroom.add("3.02");
			classroom.add("3.03");
			
		}
		
		return ResponseEntity.ok().body(classroom);
	}
	
	@GetMapping( value = "/classroom-projectors")
	public ResponseEntity<?> getClassroomProjectors ( @RequestParam(required = true) String classroom ){
		
		List<ProjectorDto> projectors = this.projectorRepository.getProjectorByClassroom(classroom);
		
		return ResponseEntity.ok().body(projectors);
		
	}
	
	@GetMapping( value = "/commands")
	public ResponseEntity<?> getProjectorModelCommands ( @RequestParam(required = true) String modelname ){
		
		List<CommandDto> commands = this.commandRepository.findCommandsByModel(modelname);
		
		return ResponseEntity.ok().body(commands);
		
	}
	


}
