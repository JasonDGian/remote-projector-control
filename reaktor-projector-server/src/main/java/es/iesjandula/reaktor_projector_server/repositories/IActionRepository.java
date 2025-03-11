package es.iesjandula.reaktor_projector_server.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor_projector_server.dtos.ActionDto;
import es.iesjandula.reaktor_projector_server.entities.Action;

/**
 * Repository interface for managing {@link Action} entities.
 * Provides database operations and custom queries related to actions.
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.2
 */
@Repository
public interface IActionRepository extends JpaRepository<Action, String>
{
    /**
     * Retrieves a paginated list of actions as {@link ActionDto} objects.
     *
     * @param pageable Pagination information.
     * @return A page of {@link ActionDto} objects.
     */
    @Query("""
        SELECT new es.iesjandula.reaktor_projector_server.dtos.ActionDto(ac.actionName) 
        FROM Action ac
    """)
    Page<ActionDto> findAllActionsAsDto(Pageable pageable);
}
