package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor_projector_server.dtos.FloorDto;
import es.iesjandula.reaktor_projector_server.entities.Floor;

/**
 * Repository interface for managing {@link Floor} entities.
 * This interface extends JpaRepository to provide CRUD operations on Floor entities.
 * It also includes custom query methods for fetching Floor data.
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.1
 */
public interface IFloorRepository extends JpaRepository<Floor, String> {

    /**
     * Retrieves a list of {@link FloorDto} objects, each containing the name of a floor.
     * This custom query selects the floor name from all Floor entities and maps them to FloorDto.
     * 
     * @return a list of FloorDto objects, each representing a floor with its name
     */
    @Query("""
            SELECT new es.iesjandula.reaktor_projector_server.dtos.FloorDto(f.floorName)
            FROM Floor f
        """)
    public List<FloorDto> findAllFloorAsDtos();
}
