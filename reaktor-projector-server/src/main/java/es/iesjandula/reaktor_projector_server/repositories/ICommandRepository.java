package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor_projector_server.dtos.CommandDto;
import es.iesjandula.reaktor_projector_server.entities.Action;
import es.iesjandula.reaktor_projector_server.entities.Command;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;

/**
 * Repository interface for managing {@link Command} entities.
 * Provides database operations and custom queries related to projector commands.
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.1
 */
@Repository
public interface ICommandRepository extends JpaRepository<Command, CommandId> {

    /**
     * Retrieves a list of commands associated with a specific projector model.
     *
     * @param modelName The name of the projector model.
     * @return A list of {@link CommandDto} containing model names, actions, and command details.
     */
    @Query("""
        SELECT new es.iesjandula.reaktor_projector_server.dtos.CommandDto( 
            c.modelName.modelName, c.action.actionName, c.command ) 
        FROM Command c 
        WHERE LOWER(c.modelName.modelName) = LOWER(:modelName)
    """)
    List<CommandDto> findCommandsByModelNameAsDto(@Param("modelName") String modelName);

    /**
     * Finds a specific command by its associated projector model and action.
     *
     * @param modelName The {@link ProjectorModel} entity.
     * @param action The {@link Action} entity.
     * @return An {@link Optional} containing the found command, if any.
     */
    Optional<Command> findByModelNameAndAction(ProjectorModel modelName, Action action);

    /**
     * Counts the number of commands associated with a specific projector model.
     *
     * @param modelName The name of the projector model (case-insensitive).
     * @return The total number of commands associated with the model.
     */
    @Query("""
        SELECT COUNT(*) 
        FROM Command cmd 
        WHERE LOWER(cmd.modelName.modelName) = LOWER(:modelName)
    """)
    Integer countCommandsByModelName(@Param("modelName") String modelName);

    /**
     * Retrieves a paginated list of commands based on optional filtering by model name and action.
     *
     * @param pageable Pagination and sorting parameters.
     * @param modelName Optional filter for projector model name.
     * @param action Optional filter for action name.
     * @return A {@link Page} of {@link CommandDto} containing filtered command details.
     */
    @Query("""
			SELECT new es.iesjandula.reaktor_projector_server.dtos.CommandDto(
			c.modelName.modelName, c.action.actionName, c.command )
			FROM Command c 
			WHERE ( :modelName = '' OR :modelName IS NULL OR c.modelName.modelName = :modelName) 
			AND ( :action = '' OR :action IS NULL OR c.action.actionName = :action)
			""")
    Page<CommandDto> findAllCommandsPage(
            Pageable pageable,
            @Param("modelName") String modelName,
            @Param("action") String action
    );
    
    /// TODO: Investigate about JPQL Clause -> COALESCE()
}
