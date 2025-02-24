package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor_projector_server.dtos.ClassroomDto;
import es.iesjandula.reaktor_projector_server.entities.Classroom;

public interface IClassroomRepository extends JpaRepository<Classroom, String>
{

	@Query("""
		    SELECT new es.iesjandula.reaktor_projector_server.dtos.ClassroomDto(c.classroomName, c.floor.floorName)
		    FROM Classroom c
		    WHERE LOWER(c.floor.floorName) = LOWER(:floorname)
		""")
		public List<ClassroomDto> findDtoListByFloorName(@Param("floorname") String floorName);


}
