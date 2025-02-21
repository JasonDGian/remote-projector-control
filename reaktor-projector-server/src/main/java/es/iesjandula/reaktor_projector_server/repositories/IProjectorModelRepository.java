package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor_projector_server.dtos.ProjectorModelDto;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;

public interface IProjectorModelRepository extends JpaRepository<ProjectorModel,String >
{
	@Query("""
			SELECT new es.iesjandula.reaktor_projector_server.dtos.ProjectorModelDto( pro.modelName ) 
			FROM ProjectorModel pro
			""")
	public List<ProjectorModelDto> getProjectorDtoList();
}
