package es.iesjandula.reaktor_projector_server.parsers;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;
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
 * <p>This service processes each line in the CSV file, which contains the projector model name and the
 * classroom identifier. It ensures that the projector model exists in the database, creating a new model 
 * if necessary, and associates it with a new projector record.</p>
 * 
 * <h2>CSV File Format</h2>
 * <p>The expected format for the CSV file is:</p>
 * <pre>
 * model_name,classroom
 * Epson EB-S41,0.01
 * BenQ MW535,0.02
 * ViewSonic PX701-4K,0.03
 * </pre>
 * <p>The first line is ignored as it is assumed to be the header.</p>
 * 
 * <h2>Workflow</h2>
 * <ul>
 *     <li>Reads and skips the first line (header).</li>
 *     <li>Processes each subsequent line, extracting the projector model and classroom.</li>
 *     <li>Checks if the projector model exists in the database. If not, creates and saves it.</li>
 *     <li>Checks if the projector unit exists in the database. If not, creates and saves it.</li>
 *     <li>Logs and skips duplicate projector entries.</li>
 * </ul>
 * 
 * <h2>Error Handling</h2>
 * <p>If the input CSV file is empty or an error occurs during parsing, a {@link ProjectorServerException} is thrown.</p>
 * 
 * @see IProjectorParser
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.1
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
     * Parses the projectors from the provided {@link Scanner} input, which reads a CSV file.
     * 
     * <p>The method processes each line, ensuring that the projector model exists and creating a 
     * new projector record if necessary. If a projector already exists, it is skipped.</p>
     * 
     * @param scanner The {@link Scanner} instance that reads the CSV file.
     * @return A summary string indicating the number of records saved and skipped.
     * @throws ProjectorServerException If an error occurs during parsing or database operations.
     */
    @Override
    public String parseProjectors(Scanner scanner) throws ProjectorServerException
    {
    	
    	log.debug("Projector parsing process initiated.");
    	
        // Check if the CSV file is empty
        if (!scanner.hasNextLine())
        {
            log.error("The received file is empty. No projectors to parse.");
            throw new ProjectorServerException(492, "Empty CSV file received in parseProjectors() method.");
        }

        String message;
        
        int recordsSkipped = 0;
        int recordsSaved = 0;
        
        // Ignore the first line of the CSV file (column headers)
        scanner.nextLine();

        // Process each line in the CSV file
        while (scanner.hasNextLine())
        {
            log.debug("-----------------------------------------------------------------------");
            
            // Read and split the CSV line
            String[] csvFields = scanner.nextLine().split(Constants.CSV_DELIMITER);
            
            if (csvFields.length < 2) {
                log.error("Skipping malformed or empty CSV line.");
                recordsSkipped++;
                continue;
            }

            String modelName = csvFields[0].trim();
            String classroom = csvFields[1].trim();

            // Check if the projector model exists in the database; if not, create and save it
            Optional<ProjectorModel> modelOptional = projectorModelRepo.findById(modelName);

            // Retrieve existing model or create a new one if it doesn't exist
            ProjectorModel currentModel = modelOptional.orElseGet(() ->
            {
                log.debug("Projector Model '{}' not found in DB, saving it now.", modelName);
                ProjectorModel newModel = new ProjectorModel();
                newModel.setModelName(modelName);
                return projectorModelRepo.save(newModel);
            });

            // Construct a unique Projector ID
            ProjectorId projectorId = new ProjectorId();
            projectorId.setClassroom(classroom);
            projectorId.setModel(currentModel);

            // Check if the projector unit already exists in the database
            Optional<Projector> projectorOptional = projectorRepository.findById(projectorId);
            
            if (projectorOptional.isEmpty())
            {
                recordsSaved++;
                log.debug("Projector Unit with ID '{}' not found in DB, saving it now.", projectorId.toString());

                // Create and save a new projector entry
                Projector newProjector = new Projector();
                newProjector.setClassroom(classroom);
                newProjector.setModel(currentModel);

                projectorRepository.saveAndFlush(newProjector);
                log.debug("Projector for model '{}' in classroom '{}' successfully parsed and saved.", modelName, classroom);
            }
            else
            {
                log.debug("Projector '{}' in classroom '{}' already exists in DB, skipping.", modelName, classroom);
                recordsSkipped++;
            }
        }

        message = "Records saved: " + recordsSaved + " - Records skipped: " + recordsSkipped;
        //log.info(message);
		return message;
    }
}
