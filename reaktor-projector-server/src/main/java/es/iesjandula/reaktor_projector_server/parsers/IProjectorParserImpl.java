package es.iesjandula.reaktor_projector_server.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Classroom;
import es.iesjandula.reaktor_projector_server.entities.Floor;
import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorParser;
import es.iesjandula.reaktor_projector_server.repositories.IClassroomRepository;
import es.iesjandula.reaktor_projector_server.repositories.IFloorRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorModelRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorRepository;
import es.iesjandula.reaktor_projector_server.utils.Constants;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link IProjectorParser} that processes projector data from a CSV file
 * and updates the database accordingly.
 * 
 * <p>This service ensures that projector models and classrooms exist in the database,
 * creating new entries when necessary.</p>
 * 
 * <h2>CSV File Format</h2>
 * <p>The expected CSV format:</p>
 * <pre>
 * model_name,classroom,floor
 * Epson EB-S41,0.01,1st Floor
 * BenQ MW535,0.02,2nd Floor
 * ViewSonic PX701-4K,0.03,3rd Floor
 * </pre>
 * 
 * <h2>Error Handling</h2>
 * <p>If the CSV file is empty or an error occurs during parsing, a
 * {@link ProjectorServerException} is thrown.</p>
 * 
 * @see IProjectorParser
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.2
 */
@Slf4j
@Service
public class IProjectorParserImpl implements IProjectorParser
{
	@Autowired
	private IProjectorModelRepository projectorModelRepo;

	@Autowired
	private IProjectorRepository projectorRepository;

	@Autowired
	private IFloorRepository floorRepository;

	@Autowired
	private IClassroomRepository classroomRepository;

    /**
     * Parses projector records from the provided {@link Scanner} input, reading a CSV file.
     * 
     * <p>Ensures that all necessary entities exist in the database before creating new projectors.</p>
     * 
	 * <p>Processing Steps</p>
	 * <ul>
	 *     <li>Reads and skips the first line (header).</li>
	 *     <li>Processes each line, extracting the projector model, classroom, and floor.</li>
	 *     <li>Ensures the floor exists, creating it if necessary.</li>
	 *     <li>Ensures the classroom exists, linking it to the correct floor.</li>
	 *     <li>Ensures the projector model exists.</li>
	 *     <li>Checks for an existing projector entry; creates and saves if absent.</li>
	 * </ul>
	 * 
     * @param scanner The {@link Scanner} instance reading the CSV file.
     * @return A summary indicating records saved and skipped.
     * @throws ProjectorServerException If an error occurs during parsing or database operations.
     */
	@Override
	@Transactional
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
		int recordLine = 0;

		// Skip the first line (assumed to be headers)
		scanner.nextLine();

		// Process each line in the CSV file
		while (scanner.hasNextLine())
		{
			log.debug("-----------------------------------------------------------------------");
			
			recordLine++;

			// Read and split the CSV line
			String[] csvFields = scanner.nextLine().split(Constants.CSV_DELIMITER);
			
			String modelName = csvFields[0].trim();
			String classroomName = csvFields[1].trim();
			String floorName = csvFields[2].trim();
						
			// If the current line is not exactly 3 fields long, throw exception.
			if ( modelName.isBlank() || classroomName.isBlank() || floorName.isBlank())
			{
				message = "ERROR: Blank or empty value detected in the Projectors CSV file in line " + recordLine + ".";
				log.error(message);
				throw new ProjectorServerException(499, message);
			}

			// Check if the floor exists in the database; if not, create and save it.
			Optional<Floor> floorOptional = this.floorRepository.findById(floorName);

			Floor floor = floorOptional.orElseGet(() ->
			{
				log.debug("Floor '{}' not found in DB, saving it now.", floorName);
				Floor newFloor = new Floor();
				newFloor.setFloorName(floorName);
				return this.floorRepository.save(newFloor);
			});
			
			
			// Check if the classroom exists; if not, create and save it. 
			Optional<Classroom> classroomOptional = this.classroomRepository.findById(classroomName);

			Classroom classroom = classroomOptional.orElseGet(() ->
			{
				log.debug("Classroom '{}' not found in DB, saving it now.", classroomName);
				Classroom newClassroom = new Classroom();
				newClassroom.setFloor(floor); // Link it to the floor
				newClassroom.setClassroomName(classroomName);
				return this.classroomRepository.save(newClassroom);
			});
			
			

			// Check if the model exists in the database; if not, create and save it.
			Optional<ProjectorModel> modelOptional = this.projectorModelRepo.findById(modelName);

			// Retrieve existing model or create a new one if it doesn't exist
			ProjectorModel currentModel = modelOptional.orElseGet(() ->
			{
				log.debug("Projector Model '{}' not found in DB, saving it now.", modelName);
				ProjectorModel newModel = new ProjectorModel();
				newModel.setModelName(modelName);
				return this.projectorModelRepo.save(newModel);
			});

			// Create unique projector ID
			ProjectorId projectorId = new ProjectorId();
			projectorId.setClassroom(classroom);
			projectorId.setModel(currentModel);

			// Check if projector already exists...
			Optional<Projector> projectorOptional = this.projectorRepository.findById(projectorId);

			if (projectorOptional.isEmpty())
			{
				recordsSaved++;
				log.debug("Projector Unit with ID '{}' not found in DB, saving it now.", projectorId.toString());

				// if doesn't exists: create and save a new projector entry
				Projector newProjector = new Projector();
				newProjector.setClassroom(classroom);
				newProjector.setModel(currentModel);

				this.projectorRepository.saveAndFlush(newProjector);
				log.debug("Projector for model '{}' in classroom '{}' successfully added to save list.", modelName,
						classroom);
			} else
			{	
				// if exists: skip current record.
				log.debug("Projector '{}' in classroom '{}' already exists in DB, skipping.", modelName, classroom);
				recordsSkipped++;
			}
		}

		message = "PROJECTORS: Records saved: " + recordsSaved + " - Records skipped: " + recordsSkipped;
		log.info(message);
		return message;
	}
}
