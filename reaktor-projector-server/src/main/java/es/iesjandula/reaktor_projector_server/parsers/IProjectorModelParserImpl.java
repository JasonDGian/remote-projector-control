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
 * Implementation of {@link IProjectorModelParser} responsible for parsing and storing projector models
 * from a CSV file into the database.
 *
 * <p>This service processes CSV data, where each line should contain a single projector model name.
 * If the model does not already exist in the database, it is added.</p>
 *
 * @see IProjectorModelParser
 *
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.1
 */
@Slf4j
@Service
public class IProjectorModelParserImpl implements IProjectorModelParser
{
    @Autowired
    private IProjectorModelRepository projectorModelRepo;

    /**
     * Parses projector models from the given {@link Scanner} input and updates the database.
     *
     * <p>Processing steps:</p>
     * <ul>
     *     <li>Checks if the file is empty.</li>
     *     <li>Skips the first line (assumed to be the header).</li>
     *     <li>Reads and trims each subsequent line to extract the projector model name.</li>
     *     <li>Checks if the model exists in the database.</li>
     *     <li>If not, saves the new model; otherwise, it is skipped.</li>
     * </ul>
     *
     * @param scanner The {@link Scanner} instance reading the CSV file.
     * @return A summary message indicating the number of records saved and skipped.
     * @throws ProjectorServerException If the input file is empty or an error occurs.
     */
    @Override
    public String parseProjectorModels(Scanner scanner) throws ProjectorServerException
    {
        log.debug("Projector Models parsing process initiated.");
       
        // Ensure the CSV file is not empty
        if (!scanner.hasNextLine())
        {
            log.error("The received file is empty. No projectors to parse.");
            throw new ProjectorServerException(491, "Empty CSV file received in parseProjectors() method.");
        }
        
        String message;
        
        int recordsSkipped = 0;
        int recordsSaved = 0;

        // Skip the first line (assumed to be headers)
        scanner.nextLine();

        // Loop through each line in the CSV file
        while (scanner.hasNextLine())
        {
            log.debug("-----------------------------------------------------------------------");
            
            // Read and trim the projector model name
            String modelName = scanner.nextLine().trim();
            
            if (modelName.isBlank()) {
                log.error("Skipping malformed or empty CSV line.");
                recordsSkipped++;
                continue;
            }

            // Check if the projector model already exists in the database
            Optional<ProjectorModel> modelOptional = projectorModelRepo.findById(modelName);

            // If the model exists in the database, log and skip current record.
            if (modelOptional.isPresent())
            {
                log.debug("Projector Model '{}' already present in DB, skipping to next record.", modelName);
                recordsSkipped++;
            }
            else
            {
                // If the model doesn't exist, create and save a new record.
                log.debug("Projector Model '{}' not present in DB, saving it now.", modelName);
                ProjectorModel newModel = new ProjectorModel();
                newModel.setModelName(modelName);
                projectorModelRepo.save(newModel); // Save the new model
                recordsSaved++;
            }
        }
        
        message = "MODELS: Records saved: " + recordsSaved + " - Records skipped: " + recordsSkipped;
        log.info(message);
		return message;
    }
}
