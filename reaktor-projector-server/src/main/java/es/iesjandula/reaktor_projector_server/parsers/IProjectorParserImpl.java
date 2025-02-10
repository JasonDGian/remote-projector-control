package es.iesjandula.reaktor_projector_server.parsers;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorParser;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorModelRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorRepository;
import es.iesjandula.reaktor_projector_server.utils.Constants;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link IProjectorParser} that parses projector data from a CSV file
 * and updates the database with the parsed information (projector models and classrooms).
 * 
 * <p>This service parses each line in the CSV file, which contains the projector model and classroom.
 * The method checks if the projector model already exists in the database. If not, it creates a new model
 * and associates it with the new projector entry in the database.</p>
 * 
 * <p>Logging is included for debugging purposes, providing traceability of actions performed during parsing.</p>
 * 
 * @see IProjectorParser
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Slf4j
@Service
public class IProjectorParserImpl implements IProjectorParser
{
    @Autowired
    private IProjectorModelRepository projectorModelRepo;
    
    @Autowired
    private IProjectorRepository projectorRepository;

    /**
     * Parses the projectors from the provided {@link Scanner} input, which reads the CSV file.
     * Each line in the CSV should contain the model name and the classroom to which the projector belongs.
     * The method ensures that the projector model exists in the database, creating a new model if necessary,
     * and associates it with a new projector record.
     * 
     * @param scanner The {@link Scanner} instance that reads the CSV file.
     * @throws ProjectorServerException If an error occurs during the parsing or database operation.
     */
    @Override
    public void parseProjectors(Scanner scanner) throws ProjectorServerException
    {
        log.debug("Projector parsing process initiated.");

        // Ignores the first line of the CSV file (column headers).
        scanner.nextLine();

        // Loop through each line in the CSV file and process projector data
        while (scanner.hasNextLine())
        {
            log.debug("-----------------------------------------------------------------------");
            
            // Read the projector model and classroom from the CSV line
            String[] csvFields = scanner.nextLine().split(Constants.CSV_DELIMITER);
            String modelName = csvFields[0];
            String classroom = csvFields[1];

            // Check if the projector model exists in the database; if not, create and save it
            Optional<ProjectorModel> modelOptional = projectorModelRepo.findById(modelName);

            // Retrieves the existing model or creates a new one if it doesn't exist
            ProjectorModel currentModel = modelOptional.orElseGet(() ->
            {
                log.debug("Projector Model '{}' not present in DB, saving it now.", modelName);
                ProjectorModel newModel = new ProjectorModel();
                newModel.setModelName(modelName);
                return projectorModelRepo.save(newModel); // Save the new model only once
            });
            
            // Create a new projector entry and associate it with the existing or newly created model
            Projector newProjector = new Projector();
            newProjector.setClassroom(classroom);
            newProjector.setModel(currentModel);

            // Save the new projector record in the repository
            projectorRepository.saveAndFlush(newProjector);

            log.debug("Projector for model '{}' in classroom '{}' successfully parsed and saved.", modelName, classroom);
        }
    }
}
