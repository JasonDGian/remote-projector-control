package es.iesjandula.reaktor_projector_server.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Classroom;
import es.iesjandula.reaktor_projector_server.entities.Floor;
import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;
import es.iesjandula.reaktor_projector_server.repositories.IClassroomRepository;
import es.iesjandula.reaktor_projector_server.repositories.IFloorRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorModelRepository;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class responsible for providing a default Projector entity.
 * <p>
 * This class ensures that a default Projector, Classroom, and ProjectorModel
 * exist in the database and reuses them whenever needed, instead of creating
 * multiple instances. This prevents null references and maintains data
 * integrity.
 * </p>
 * 
 * <h2>How it Works:</h2>
 * <ol>
 * <li>First, it checks if the default Projector already exists.</li>
 * <li>If it exists, it retrieves and returns it.</li>
 * <li>If it does not exist, it creates the necessary default Floor, Classroom,
 * and ProjectorModel, persists them, and then creates the Projector.</li>
 * <li>Finally, the persisted default Projector is returned.</li>
 * </ol>
 * 
 * <p>
 * Only one default Projector instance is stored in the database.
 * </p>
 * 
 * @author David Jason Gianmoena
 *         (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Slf4j
@Service
public class DefaultProjectorProvider
{

	@Autowired
	private IProjectorRepository projectorRepository;

	@Autowired
	private IFloorRepository floorRepo;

	@Autowired
	private IClassroomRepository classRepo;

	@Autowired
	private IProjectorModelRepository modelRepo;

	/** Default model name for the Projector */
	private static final String DEFAULT_MODEL = "DEFAULT_MODEL";

	/** Default classroom name where the Projector is located */
	private static final String DEFAULT_CLASSROOM = "DEFAULT_CLASSROOM";

	/** Default floor name associated with the classroom */
	private static final String DEFAULT_FLOOR = "DEFAULT_FLOOR";

	/**
	 * Retrieves the default Projector from the database.
	 * <p>
	 * If the default Projector does not exist, it creates and persists the required
	 * entities.
	 * </p>
	 * 
	 * @return The default {@link Projector} instance.
	 */
	
	// TODO: REVISIT THIS SERVICE
	
	public Projector getDefaultProjector()
	{

		log.debug(" getDefaultProjector called.");

		// Create a composite key to check if the default projector exists
		ProjectorId defaultProjectorId = new ProjectorId(new ProjectorModel(DEFAULT_MODEL),
				new Classroom(DEFAULT_CLASSROOM, new Floor(DEFAULT_FLOOR)));

		// Try to find the default projector in the database
		Optional<Projector> defaultProjectorOpt = projectorRepository.findById(defaultProjectorId);

		Projector defaultProjector = new Projector();

		// If the default projector doesn't exist, create and persist it
		if (defaultProjectorOpt.isEmpty())
		{

			// Create and persist the default floor
			Floor defaultFloor = new Floor();
			defaultFloor.setFloorName(DEFAULT_FLOOR);
			floorRepo.saveAndFlush(defaultFloor);

			// Create and persist the default classroom
			Classroom defaultClassroom = new Classroom();
			defaultClassroom.setClassroomName(DEFAULT_CLASSROOM);
			defaultClassroom.setFloor(defaultFloor);
			classRepo.saveAndFlush(defaultClassroom);

			// Create and persist the default projector model
			ProjectorModel defaultModel = new ProjectorModel();
			defaultModel.setModelName(DEFAULT_MODEL);
			modelRepo.saveAndFlush(defaultModel);

			// Create and persist the default projector

			defaultProjector.setModel(defaultModel);
			defaultProjector.setClassroom(defaultClassroom);

			projectorRepository.saveAndFlush(defaultProjector);

			log.debug(" Default projector created: {}", defaultProjector.toString());

		} else
		{
			defaultProjector = defaultProjectorOpt.get();
			log.debug(" Default projector found: {}", defaultProjector.toString());

		}

		// Return the default projector
		return defaultProjector;

	}
}
