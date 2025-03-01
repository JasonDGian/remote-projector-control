package es.iesjandula.reaktor_projector_server.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableServerEventDto
{
	private Long eventId;
	private String action;
	private String model;
	private String classroom;
	private String floor;
	private String user;
	private LocalDateTime dateTime;
	private String actionStatus;
}
