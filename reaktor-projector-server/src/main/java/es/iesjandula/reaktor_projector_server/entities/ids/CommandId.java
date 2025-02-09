package es.iesjandula.reaktor_projector_server.entities.ids;

import es.iesjandula.reaktor_projector_server.entities.Action;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandId
{
	// Model for which the command is employed.
	private ProjectorModel modelName;
	
	// Action that the related command will perform.
	private Action action;
	
	// Binary instruction sent to the projector to perform an action.
	private String command;
}
