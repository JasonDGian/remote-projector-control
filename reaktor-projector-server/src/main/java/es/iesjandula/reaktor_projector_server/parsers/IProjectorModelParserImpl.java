package es.iesjandula.reaktor_projector_server.parsers;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorModelParser;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorModelRepository;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link IProjectorModelParser} that handles the parsing of projector models
 * from a CSV file and updates the database accordingly.
 * 
 * <p>This service parses each line in the CSV file, which should contain the projector model name, 
 * and checks if that model is already present in the database. If not, it creates and saves a new
 * {@link ProjectorModel} in the repository.</p>
 * 
 * <p>Logging is provided at each stage of the process for debugging and traceability.</p>
 * 
 * @see IProjectorModelParser
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Slf4j
@Service
public class IProjectorModelParserImpl implements IProjectorModelParser
{
    @Autowired
    private IProjectorModelRepository projectorModelRepo;

    /**
     * Parses the projector models from the provided {@link Scanner} input.
     * Each line in the CSV should contain a projector model name only.
     * The method checks if each model exists in the database and saves it if it does not exist.
     * 
     * @param scanner The {@link Scanner} instance that reads the CSV file.
     * @throws ProjectorServerException If an error occurs during the parsing or database operation.
     */
    @Override
    public void parseProjectorModels(Scanner scanner) throws ProjectorServerException
    {
        log.debug("Projector Models parsing process initiated.");

        // Ignores the first line of the CSV file (column headers).
        scanner.nextLine();

        // Loop through each line in the CSV file
        while (scanner.hasNextLine())
        {
            log.debug("-----------------------------------------------------------------------");
            
            // Read the model name from the CSV line
            String modelName = scanner.nextLine().trim();

            // Check if the projector model already exists in the database
            Optional<ProjectorModel> modelOptional = projectorModelRepo.findById(modelName);

            // If the model exists in the database, log and skip it
            if (modelOptional.isPresent())
            {
                log.debug("Projector Model '{}' already present in DB, skipping to next record.", modelName);
            }
            else
            {
                // If the model doesn't exist, create and save a new one
                log.debug("Projector Model '{}' not present in DB, saving it now.", modelName);
                ProjectorModel newModel = new ProjectorModel();
                newModel.setModelName(modelName);
                projectorModelRepo.save(newModel); // Save the new model
            }
        }
    }
}
