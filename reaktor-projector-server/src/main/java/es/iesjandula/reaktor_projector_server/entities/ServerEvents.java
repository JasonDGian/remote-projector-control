package es.iesjandula.reaktor_projector_server.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerEvents
{
	@Id
	private long eventId;

	// commando
	@ManyToOne
	@JoinColumns(
	{ @JoinColumn(name = "commandModelName", referencedColumnName = "modelName"),
			@JoinColumn(name = "commandActionName", referencedColumnName = "actionName"),
			@JoinColumn(name = "commandInstruction", referencedColumnName = "command") })
	private Command command;

	@ManyToOne
	@JoinColumns(
	{ 
		@JoinColumn(name = "projectorModel", referencedColumnName = "modelName"),
		@JoinColumn(name = "projectorClasroom", referencedColumnName = "clasroom")
		})
	private Projector projector;

	// usuario
	private String user;

	// fecha
	private LocalDateTime dateTime;
}
