package es.iesjandula.reaktor_projector_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;

public interface IProjectorModelRepository extends JpaRepository<ProjectorModel,String >
{

}
