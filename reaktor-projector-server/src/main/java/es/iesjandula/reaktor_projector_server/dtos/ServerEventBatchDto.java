package es.iesjandula.reaktor_projector_server.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerEventBatchDto
{
	// Action to be performed.
	String action;
	// Projectors to send the action to.
	List<ProjectorDto> projectorList;
}
