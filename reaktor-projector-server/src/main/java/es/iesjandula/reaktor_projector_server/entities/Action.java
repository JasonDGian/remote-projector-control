package es.iesjandula.reaktor_projector_server.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
	
	// When an action is removed all the related commands are deleted as well.
    @OneToMany(mappedBy = "action", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Command> commands;

	// Special to string method to prevent recursive calls and null pointers.
	@Override
	public String toString()
	{
		return new StringBuilder().append("Action - actionName: ")
				.append(this.actionName == null ? "N/A" : this.actionName).toString();
	}

}
