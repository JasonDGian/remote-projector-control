package es.iesjandula.reaktor_projector_server.parsers;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Action;
import es.iesjandula.reaktor_projector_server.entities.Command;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.ICommandParser;
import es.iesjandula.reaktor_projector_server.repositories.IActionRepository;
import es.iesjandula.reaktor_projector_server.repositories.ICommandRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorModelRepository;
import es.iesjandula.reaktor_projector_server.utils.Constants;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementation of the {@link ICommandParser} interface for parsing command data
 * from CSV files. This service is responsible for reading a CSV file containing
 * action names, commands, and projector models, checking for existing records
 * in the database, and saving new records.
 * 
 * @see ICommandParser
 * 
 * <p>
 * Author: David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>) 
 * Version: 1.1
 * </p>
 */
@Slf4j
@Service
public class ICommandPaserImpl implements ICommandParser
{
    @Autowired
    private IActionRepository actionRepo;
    
    @Autowired
    private ICommandRepository commandRepo;
    
    @Autowired
    private IProjectorModelRepository projectorModelRepo;


    /**
     * Parses commands from the given {@link Scanner} input and updates the database.
     *
     * <p>Processing steps:</p>
     * <ul>
     *     <li>Checks if the file is empty.</li>
     *     <li>Skips the first line (assumed to be the header).</li>
     *     <li>Reads and trims each subsequent line to extract the action name, command, and model name.</li>
     *     <li>Checks if they exist in the database.</li>
     *     <li>Saves new records if missing.</li>
     *     <li>Skips over malformed or already existing records</li>
     * </ul>
     *
     * @param scanner The {@link Scanner} instance reading the CSV file.
     * @return Summary of records saved and skipped.
     * @throws ProjectorServerException If the file is empty or an error occurs.
     */
    @Override
    public String parseCommands(Scanner scanner) throws ProjectorServerException
    {
        log.debug("Commands parsing process initiated.");
        
        // Check if the CSV file is empty
        if (!scanner.hasNextLine())
        {
            log.error("The received file is empty. No commands to parse.");
            throw new ProjectorServerException(493, "Empty CSV file received in parseCommands() method.");
        }
        String message;
        int recordLine = 0;
        int recordsSkipped = 0;
        int recordsSaved = 0;
        
        // Ignore the first line of the CSV file (column headers).
        scanner.nextLine();

        while (scanner.hasNextLine()) {
            log.debug("-----------------------------------------------------------------------");
            
            recordLine++;
            
            // Expected format: action_name, command, model_name
            String[] csvFields = scanner.nextLine().split(Constants.CSV_DELIMITER);
            
			if ( csvFields.length != 3 )
			{
				message = "ERROR: Missing value detected in the Commands CSV file in line " + recordLine + ".";
				log.error(message);
				throw new ProjectorServerException(499, message);
			}
            
            // Skips malformed lines.
            String actionName = csvFields[0].trim(); 
            String command = csvFields[1].trim();
            String modelName = csvFields[2].trim();
            
			// If the current line is not exactly 3 fields long, throw exception.
			if ( actionName.isBlank() || command.isBlank() || modelName.isBlank())
			{
				message = "ERROR: Blank or empty value detected in the Commands CSV file in line " + recordLine + ".";
				log.error(message);
				throw new ProjectorServerException(499, message);
			}
            
        	// Retrieve or create necessary action
            log.debug("Parsing action '{}'.", actionName);
            Action currentAction = getOrCreateAction(actionName);
                        
            // Retrieve or create necessary projector model
            log.debug("Parsing projector model '{}'.", modelName);
            ProjectorModel currentModel = getOrCreateModel(modelName);

            // Generate a unique command ID
            CommandId currentCommandId = new CommandId();
            currentCommandId.setAction(currentAction);
            currentCommandId.setModelName(currentModel);
            currentCommandId.setCommand(command);
            
            // Check if the command already exists...
            log.debug("Parsing command '{}'.", command);
            Optional<Command> currentCommandOptional = commandRepo.findById(currentCommandId);
            
            if (currentCommandOptional.isEmpty()) {
                log.debug("Command with ID '{}' not present in DB, saving now.", currentCommandId);
                Command currentCommand = new Command();
                currentCommand.setAction(currentAction);
                currentCommand.setModelName(currentModel);
                currentCommand.setCommand(command);
                commandRepo.saveAndFlush(currentCommand);
                recordsSaved++;
            } else {
                log.debug("Command with ID '{}' already exists in the database. Skipping it now.", currentCommandId);
                recordsSkipped++;
            }
        }
        
        message = "COMMANDS: Records saved: " + recordsSaved + " - Records skipped: " + recordsSkipped;
        log.info(message);
		return message;
    }
        
    /**
     * Retrieves an existing {@link Action} from the database or creates a new one if not found.
     *
     * @param actionName The name of the action.
     * @return The existing or newly created {@link Action} instance.
     */
    private Action getOrCreateAction(String actionName) {
        // Fetch the object from DB.
        Optional<Action> actionOptional = actionRepo.findById(actionName);
        
        // If the action is not found, create and save a new one.
        return actionOptional.orElseGet(() -> { 
            log.debug("Action '{}' not present in DB, saving it now.", actionName);
            Action newAction = new Action();
            newAction.setActionName(actionName);
            return actionRepo.save(newAction); // Save only once here
        });
    }
    
    /**
     * Retrieves an existing {@link ProjectorModel} from the database or creates a new one if not found.
     *
     * @param modelName The name of the projector model.
     * @return The existing or newly created {@link ProjectorModel} instance.
     */
    private ProjectorModel getOrCreateModel(String modelName) {
        // Fetch the object from DB.
        Optional<ProjectorModel> modelOptional = projectorModelRepo.findById(modelName);
        
        // If the projector model is not found, create and save a new one.
        return modelOptional.orElseGet(() -> {
            log.debug("Projector Model '{}' not present in DB, saving it now.", modelName);
            ProjectorModel newModel = new ProjectorModel();
            newModel.setModelName(modelName);
            return projectorModelRepo.save(newModel);  // Save only once here
        });
    }
}
