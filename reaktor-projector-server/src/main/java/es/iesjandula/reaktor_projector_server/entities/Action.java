package es.iesjandula.reaktor_projector_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an action that can be performed on a projector.
 *
 * <p>
 * Examples of actions include:
 * <ul>
 * <li>"Power On"</li>
 * <li>"Power Off"</li>
 * <li>"Switch Input"</li>
 * </ul>
 * </p>
 *
 * @author David Jason Gianmoena
 *         (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action
{
	/**
	 * The name of the action (e.g., "Power Off", "Volume Up").
	 */
	@Id
	@Column(name = "action_name")
	private String actionName;

	// Special to string method to prevent recursive calls and null pointers.
	@Override
	public String toString()
	{
		return new StringBuilder().append("Action - actionName: ")
				.append(this.actionName == null ? "N/A" : this.actionName).toString();
	}

}
