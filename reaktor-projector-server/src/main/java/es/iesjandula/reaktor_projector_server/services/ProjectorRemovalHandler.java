package es.iesjandula.reaktor_projector_server.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ServerEvent;
import es.iesjandula.reaktor_projector_server.repositories.IProjectorRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectorRemovalHandler
{
	@Autowired
	private DefaultProjectorProvider defaultProjectorProvider;

	@Autowired
	private IProjectorRepository projectorRepository;

	/**
	 * This method will be responsible for handling the removal of a projector and
	 * updating all associated events to reference the default projector.
	 * 
	 * @param projector The projector to be removed
	 */
	
	// TODO: REVISIT THIS SERVICE
	
	
	public void handleProjectorRemoval(Projector projector)
	{
		log.debug("Projector removal handler call received");
		
		// Get the default projector
		Projector defaultProjector = defaultProjectorProvider.getDefaultProjector();

		// Update the server events to reference the default projector
		List<ServerEvent> serverEvents = projector.getServerEvents();
		for (ServerEvent event : serverEvents)
		{
			event.setProjector(defaultProjector);
		}

		// After the references are updated, we can save the updated events if necessary
		// You may want to handle saving the events after updating references (depends
		// on your requirements)
		projectorRepository.delete(projector); // Or any other removal process
	}
}