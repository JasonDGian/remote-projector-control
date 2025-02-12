package es.iesjandula.reaktor_projector_server.entities.ids;

import es.iesjandula.reaktor_projector_server.entities.Action;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the composite key for a Command entity, uniquely identifying a
 * command by the projector model, the action it performs, and the binary
 * instruction.
 * 
 * <p>
 * This class is used in conjunction with JPA to define a composite key for the
 * corresponding Command entity.
 * </p>
 * 
 * <p>
 * The composite key consists of:
 * <ul>
 * <li>{@code modelName} - The projector model for which the command is
 * used.</li>
 * <li>{@code action} - The specific action that the command triggers.</li>
 * <li>{@code command} - The binary instruction sent to the projector.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Annotations:
 * </p>
 * <ul>
 * <li>{@link Data} - Generates boilerplate code like getters, setters, and
 * {@code toString()}.</li>
 * <li>{@link NoArgsConstructor} - Creates a no-argument constructor.</li>
 * <li>{@link AllArgsConstructor} - Creates a constructor with all fields as
 * parameters.</li>
 * </ul>
 * 
 * @author David Jason Gianmoena
 *         (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandId
{
	/**
	 * The projector model associated with the command.
	 */
	private ProjectorModel modelName;

	/**
	 * The action that the command is intended to perform.
	 */
	private Action action;

	/**
	 * The binary instruction sent to the projector to execute the action.
	 */
	private String command;

	@Override
	public String toString() {
	    return new StringBuilder()
	        .append("CommandId - modelName: ").append(this.modelName == null ? "N/A" : this.modelName.getModelName())
	        .append(" | action: ").append(this.action == null  ? "N/A" : this.action.getActionName())
	        .append(" | command: ").append(this.command == null ? "N/A" : this.command)
	        .toString();
	}

}
