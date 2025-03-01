package es.iesjandula.reaktor_projector_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplifiedServerEventDto
{
	private Long eventId;
	private String commandInstruction;
	private String actionStatus;
}

