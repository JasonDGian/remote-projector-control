package es.iesjandula.reaktor_projector_server.entities;

import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a command that can be sent to a projector to perform a specific
 * action. Each command is uniquely identified by the projector model, action,
 * and binary or hexadecimal instruction.
 *
 * <p>
 * Author: David Jason Gianmoena
 * (<a href="https://github.com/JasonDGian">GitHub</a>) Version: 1.0
 * </p>
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CommandId.class)
public class Command
{
	/**
	 * The projector model associated with the command.
	 */
	@Id
	@ManyToOne
	@JoinColumn(name = "modelName")
	private ProjectorModel modelName;

	/**
	 * The action that this command triggers.
	 */
	@Id
	@ManyToOne
	@JoinColumn(name = "actionName")
	private Action action;

	/**
	 * The binary or hexadecimal instruction sent to the projector.
	 */
	@Id
	private String command;

	// Special to string method to prevent recursive calls and null pointers.
	@Override
	public String toString()
	{
		return new StringBuilder().append("CommandID - action: ")
				.append(this.action == null ? "N/A" : this.action.getActionName()).append(" | modelName: ")
				.append(this.modelName == null ? "N/A" : this.modelName.getModelName()).append(" | command: ")
				.append(this.command == null ? "N/A" : this.command).toString();
	}
}
