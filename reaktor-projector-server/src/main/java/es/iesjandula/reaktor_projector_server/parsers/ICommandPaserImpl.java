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
 * Implementation of {@link ICommandParser} responsible for parsing commands from a CSV file
 * and updating the database accordingly.
 * 
 * <p>This class reads a CSV file containing fields in the format:
 * <code>action_name, command, model_name</code>. It checks if the extracted 
 * action, command, and projector model exist in the database and saves them 
 * if they do not exist.</p>
 * 
 * <p>It interacts with the database via:
 * <ul>
 *     <li>{@link IActionRepository} - to manage action records.</li>
 *     <li>{@link ICommandRepository} - to store and retrieve commands.</li>
 *     <li>{@link IProjectorModelRepository} - to manage projector models.</li>
 * </ul>
 * </p>
 * 
 * <p>Logs are generated at each step for debugging and traceability.</p>
 * 
 * @see ICommandParser
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
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
     * Parses commands from the provided {@link Scanner} input, extracting and storing 
     * actions, commands, and projector models in the database.
     * 
     * <p>For each line in the CSV file, this method:
     * <ul>
     *     <li>Extracts action, command, and model data.</li>
     *     <li>Verifies if they exist in the database.</li>
     *     <li>Saves new records if they are not found.</li>
     * </ul>
     * </p>
     * 
     * @param scanner The {@link Scanner} instance reading the CSV file.
     * @throws ProjectorServerException If an error occurs during parsing or database operations.
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

        int recordsSkipped = 0;
        int recordsSaved = 0;
        
        // Ignore the first line of the CSV file (column headers).
        scanner.nextLine();

        while (scanner.hasNextLine()) {
            log.debug("-----------------------------------------------------------------------");
            
            // File fields should always be -> action_name, command, model_name
            String[] csvFields = scanner.nextLine().split(Constants.CSV_DELIMITER);
            
            if (csvFields.length < 3) {
                log.error("Skipping malformed or empty CSV line.");
                recordsSkipped++;
                continue;
            }
            
            String actionName = csvFields[0]; 
            String command = csvFields[1];
            String modelName = csvFields[2];
            
            // Retrieve or create an action
            log.debug("Parsing action '{}'.", actionName);
            Action currentAction = getOrCreateAction(actionName);
                        
            // Retrieve or create a projector model
            log.debug("Parsing projector model '{}'.", modelName);
            ProjectorModel currentModel = getOrCreateModel(modelName);

            // Create a unique command ID
            CommandId currentCommandId = new CommandId();
            currentCommandId.setAction(currentAction);
            currentCommandId.setModelName(currentModel);
            currentCommandId.setCommand(command);
            
            // Check if the command exists in the database; if not, save it.
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
        String message = "Records saved: " + recordsSaved + " - Records skipped: " + recordsSkipped;
        //log.info(message);
		return message;
    }
    
    /**
     * Retrieves an existing {@link Action} from the database or creates a new one if not found.
     * 
     * @param actionName The name of the action.
     * @return The existing or newly created {@link Action}.
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
     * @return The existing or newly created {@link ProjectorModel}.
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
