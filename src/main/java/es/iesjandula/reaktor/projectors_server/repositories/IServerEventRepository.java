package es.iesjandula.reaktor.projectors_server.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.projectors_server.dtos.SimplifiedServerEventDto;
import es.iesjandula.reaktor.projectors_server.dtos.TableServerEventDto;
import es.iesjandula.reaktor.projectors_server.entities.Projector;
import es.iesjandula.reaktor.projectors_server.entities.ServerEvent;

/**
 * Repository interface for managing ServerEvent entities. Provides CRUD
 * operations on ServerEvent entities and custom queries to retrieve event data.
 * 
 * @author David Jason Gianmoena
 *         (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.1
 */
public interface IServerEventRepository extends JpaRepository<ServerEvent, Long>
{

	@Query("""
			    SELECT se
			    FROM ServerEvent se
			    WHERE se.command.action = :action
			""")
	List<ServerEvent> findAllByAction(@Param("action") String action);
	
    List<ServerEvent> findByProjector(Projector projector);

	// Find all server events by a specific command (using modelName and action)
	@Query("SELECT s FROM ServerEvent s WHERE s.command.modelName = :modelName AND s.command.action = :action")
	List<ServerEvent> findByCommand(@Param("modelName") String modelName, @Param("action") String action);

}
