package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor_projector_server.dtos.ProjectorDto;
import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;

public interface IProjectorRepository extends JpaRepository<Projector, ProjectorId>
{
	@Query("SELECT new es.iesjandula.reaktor_projector_server.dtos.ProjectorDto( pro.model.modelName, pro.classroom.classroomName) "
			+ "FROM Projector pro "
			+ "WHERE LOWER(pro.classroom) = LOWER(:classroom)")
	public List<ProjectorDto> getProjectorByClassroom( @Param("classroom") String classroom ); 
}
