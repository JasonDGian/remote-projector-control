package es.iesjandula.reaktor.projectors_server.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerEventHistory {

	/**
	 * Unique identifier for the server event.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	
	private Long eventId;
	
	private String modelName;
	
	private String action;
	
	private String command;
	
	private String classroom;
	
	private String floor;
	
	private String user;
	
	private LocalDateTime dateTime;
	
	private String actionStatus;
	

}
