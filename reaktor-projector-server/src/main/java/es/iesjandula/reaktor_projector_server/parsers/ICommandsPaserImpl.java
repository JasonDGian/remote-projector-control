package es.iesjandula.reaktor_projector_server.parsers;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Action;
import es.iesjandula.reaktor_projector_server.entities.Command;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;
import es.iesjandula.reaktor_projector_server.repositories.IActionRepositories;
import es.iesjandula.reaktor_projector_server.repositories.ICommandRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorModelRepository;
import es.iesjandula.reaktor_projector_server.utils.Constants;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link ICommandsParser} that parses commands from a CSV file 
 * and updates the database with the parsed data (actions, commands, projector models).
 * 
 * The class reads a CSV file with fields in the format: action_name, command, model_name.
 * It checks if the parsed action, command, and projector model exist in the database.
 * If not, it creates and saves new records in the respective tables.
 * 
 * The class uses {@link IActionRepositories}, {@link ICommandRepository}, and 
 * {@link IProjectorModelRepository} to interact with the database for actions, 
 * commands, and projector models.
 * 
 * Logs are generated for each operation for debugging purposes.
 * 
 * @see ICommandsParser
 * 
 * @author David Jason Gianmoena [ https://github.com/JasonDGian ]
 * @version 1.0
 */
@Slf4j
@Service
public class ICommandsPaserImpl implements ICommandsParser
{
    @Autowired
    IActionRepositories ActionRepo;
    
    @Autowired
    ICommandRepository CommandRepo;
    
    @Autowired
    IProjectorModelRepository ProjectorModelRepo;

    /**
     * Parses commands from the provided {@link Scanner} input, which reads a CSV file.
     * For each line in the CSV, it extracts action, command, and projector model,
     * checks their existence in the database, and saves them if they don't exist.
     * 
     * @param scanner The {@link Scanner} instance to read the CSV file.
     * @throws ProjectorServerException If an error occurs during the parsing or database operations.
     * 
	 * @author David Jason Gianmoena [ https://github.com/JasonDGian ]
     * @version 1.0
     */
    @Override
    public void parseCommands(Scanner scanner) throws ProjectorServerException
    {
    	log.debug("Command parsing process initiated.");

        // Ignores the first line of the CSV file (column headers).
        scanner.nextLine();

        while (scanner.hasNextLine()) {
        	log.debug("-----------------------------------------------------------------------");
            // File fields should always be -> action_name, command, model_name
            String[] csvFields = scanner.nextLine().split(Constants.CSV_DELIMITER);
            
            String actionName = csvFields[0]; 
            String command = csvFields[1];
            String modelName = csvFields[2];
            
            Action currentAction = new Action();
            Command currentCommand = new Command();
            CommandId currentCommandId = new CommandId();
            ProjectorModel currentModel = new ProjectorModel();

            // Checks if the action exists in the database; if not, saves it.
            log.debug("Parsing action '{}'.", actionName);
            currentAction = getOrCreateAction(actionName);
                        
            // Checks if the model exists in the database; if not, saves it.
            log.debug("Parsing projector model '{}'.", modelName);
            currentModel = getOrCreateModel(modelName);

			// Associates action, model, and command to create a unique command ID. 
            currentCommandId.setAction(currentAction);
            currentCommandId.setModelName(currentModel);
            currentCommandId.setCommand(command);
            
			// Checks if the command exists in the database; if not, saves it.
            log.debug("Parsing command '{}'.", command);
            Optional<Command> currentCommandOptional = CommandRepo.findById(currentCommandId);
            
            if (currentCommandOptional.isEmpty()) {
                log.debug("Command with ID '{}' not present in DB, saving now.", currentCommandId.toString());
                currentCommand.setAction(currentAction);
                currentCommand.setModelName(currentModel);
                currentCommand.setCommand(command);
                CommandRepo.saveAndFlush(currentCommand);
            } else {
                log.debug("Command with ID '{}' already exists in the database. Skipping execution.", modelName);
            }
        }
    }
    
    /**
     * Retrieves or creates an Action if not already present in DB.
     */
    private Action getOrCreateAction(String actionName) {
    	// Fetch the object from DB.
        Optional<Action> actionOptional = ActionRepo.findById(actionName);
        
        /**
         * orElseGet() is a method that takes a supplier function and will invoke that 
         * function to generate a value if the Optional is empty. 
         */
        return actionOptional.orElseGet(() -> 
        {	
            log.debug("Action '{}' not present in DB, saving it now.", actionName);
            Action newAction = new Action();
            newAction.setActionName(actionName);
            return ActionRepo.save(newAction); // Save only once here
        	}
        );
    }
    
    /**
     * Retrieves or creates a ProjectorModel if not already present in DB.
     */
    private ProjectorModel getOrCreateModel(String modelName) {
    	// Fetch the object from DB.
        Optional<ProjectorModel> modelOptional = ProjectorModelRepo.findById(modelName);
        
        /**
         * orElseGet() is a method that takes a supplier function and will invoke that 
         * function to generate a value if the Optional is empty. 
         */
        return modelOptional.orElseGet(() -> {
            log.debug("Projector Model '{}' not present in DB, saving it now.", modelName);
            ProjectorModel newModel = new ProjectorModel();
            newModel.setModelName(modelName);
            return ProjectorModelRepo.save(newModel);  // Save only once here
        });
    }
}
