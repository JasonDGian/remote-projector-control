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
 * <p>Examples of actions include:
 * <ul>
 *   <li>"Power On"</li>
 *   <li>"Power Off"</li>
 *   <li>"Switch Input"</li>
 * </ul>
 * </p>
 * 
 * <p>Annotations:</p>
 * <ul>
 *   <li>{@link Entity} - Marks this class as a JPA entity.</li>
 *   <li>{@link Data} - Generates boilerplate code like getters, setters, and {@code toString()}.</li>
 *   <li>{@link NoArgsConstructor} - Creates a no-argument constructor.</li>
 *   <li>{@link AllArgsConstructor} - Creates a constructor with all fields as parameters.</li>
 * </ul>
 *
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
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
	@Column( name = "action_name" )
	private String actionName;
	

//	@Column( name="description")
//	private String description;
	
	@Override
	public String toString(){
		return new StringBuilder()
		        .append("Action - actionName: ").append(this.actionName == null ? "N/A" : this.actionName)
		        .toString();
	}
	
}
