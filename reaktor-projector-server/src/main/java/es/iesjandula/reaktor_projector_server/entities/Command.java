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
 * Represents a command that can be sent to a projector to perform a specific action.
 * Each command is uniquely identified by the projector model, action, and binary or 
 * hexadecimal instruction instruction.
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>{@link Entity} - Marks this class as a JPA entity.</li>
 *   <li>{@link IdClass} - Specifies a composite primary key.</li>
 *   <li>{@link ManyToOne} - Defines relationships with {@code ProjectorModel} and {@code Action}.</li>
 * </ul>
 *
 * @author David Jason Gianmoena [ https://github.com/JasonDGian ]
 * @version 1.0
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
	@JoinColumn( name = "modelName" )
	private ProjectorModel modelName;
	
	/** 
	 * The action that this command triggers. 
	 */
	@Id
	@ManyToOne
	@JoinColumn( name = "actionName" )
	private Action action;
	
	/** 
	 * The binary or hexadecimal instruction sent to the projector. 
	 */
	@Id
	private String command;
}
