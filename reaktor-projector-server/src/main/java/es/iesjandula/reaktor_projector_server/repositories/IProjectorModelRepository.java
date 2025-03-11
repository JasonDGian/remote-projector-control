package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor_projector_server.dtos.ProjectorModelDto;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;

/**
 * Repository interface for managing {@link ProjectorModel} entities.
 * Provides CRUD operations on ProjectorModel entities as well as custom queries.
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
public interface IProjectorModelRepository extends JpaRepository<ProjectorModel, String> {

    /**
     * Retrieves a list of {@link ProjectorModelDto} objects, each containing the model name.
     * This query selects the model name from all ProjectorModel entities and maps them to ProjectorModelDto.
     * 
     * @return a list of ProjectorModelDto objects, each representing a projector model with its name
     */
    @Query("""
            SELECT new es.iesjandula.reaktor_projector_server.dtos.ProjectorModelDto( pro.modelName ) 
            FROM ProjectorModel pro
        """)
    public List<ProjectorModelDto> findAllProjectorModelsAsDto();
}
