package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor_projector_server.dtos.ClassroomDto;
import es.iesjandula.reaktor_projector_server.entities.Classroom;

/**
 * Repository interface for managing {@link Classroom} entities.
 * Provides database operations and custom queries related to classrooms.
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.2
 */
@Repository
public interface IClassroomRepository extends JpaRepository<Classroom, String>
{
    /**
     * Retrieves a list of classrooms on a specific floor as {@link ClassroomDto} objects.
     *
     * @param floorName The name of the floor (case-insensitive).
     * @return A list of {@link ClassroomDto} containing classroom names and floor names.
     */
    @Query("""
        SELECT new es.iesjandula.reaktor_projector_server.dtos.ClassroomDto(c.classroomName, c.floor.floorName)
        FROM Classroom c
        WHERE LOWER(c.floor.floorName) = LOWER(:floorName)
    """)
    List<ClassroomDto> findClassroomsByFloorNameAsDto(@Param("floorName") String floorName);
}
