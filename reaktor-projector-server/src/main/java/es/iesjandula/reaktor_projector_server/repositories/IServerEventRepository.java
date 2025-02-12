package es.iesjandula.reaktor_projector_server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor_projector_server.entities.ServerEvent;

public interface IServerEventRepository extends JpaRepository<ServerEvent, Long>
{

}



