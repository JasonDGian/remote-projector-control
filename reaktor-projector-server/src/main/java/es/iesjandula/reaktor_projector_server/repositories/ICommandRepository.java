package es.iesjandula.reaktor_projector_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor_projector_server.entities.Command;
import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;

public interface ICommandRepository extends JpaRepository<Command, CommandId >
{

}
