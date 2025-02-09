package es.iesjandula.reaktor_projector_server.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Command
{
	// Model for which the command is employed.
	@Id
	@ManyToOne
	@JoinColumn( name = "modelName" )
	private ProjectorModel modelName;
	
	// Action that performs the command.
	@Id
	@ManyToOne
	@JoinColumn( name = "actionName" )
	private Action action;
	
	// Binary instruction sent to the projector to perform an action.
	@Id
	private String command;
}
