package es.iesjandula.reaktor_projector_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor_projector_server.entities.Projector;
import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;

public interface IProjectorRepository extends JpaRepository<Projector, ProjectorId>
{

}
