package es.iesjandula.reaktor_projector_server.entities;

import java.time.LocalDateTime;

import es.iesjandula.reaktor_projector_server.services.DefaultProjectorProvider;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreRemove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a server event that logs interactions between commands, projectors, and users.
 * <p>
 * This entity stores details about actions performed on projectors using specific commands.
 * It includes information such as the event ID, associated command, projector, user, 
 * timestamp, and action status
 *
 * This entity acts as a pseudo-queue, facilitating communication between the server 
 * and remote microcontrollers. It records pending actions, specifying what needs to be 
 * executed and by which device, ensuring efficient task processing and status tracking.
 * </p>
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerEvent
{
    /**
     * Unique identifier for the server event.
     */
	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private Long eventId;

    /**
     * The command associated with this event.
     * <p>
     * This is a many-to-one relationship with the {@link Command} entity.
     * It references multiple columns from the "Command" table.
     * </p>
     */
	@ManyToOne
	@JoinColumns(
	{ @JoinColumn(name = "commandModelName", referencedColumnName = "modelName",nullable = true),
			@JoinColumn(name = "commandActionName", referencedColumnName = "actionName",nullable = true),
			@JoinColumn(name = "commandInstruction", referencedColumnName = "command",nullable = true) })
	private Command command;

    /**
     * The projector involved in this event.
     * <p>
     * This is a many-to-one relationship with the {@link Projector} entity.
     * It references multiple columns from the "Projector" table.
     * </p>
     */
	@ManyToOne
	@JoinColumns(
	{ 
		@JoinColumn(name = "projectorModel", referencedColumnName = "modelName",nullable = true),
		@JoinColumn(name = "projectorClassroom", referencedColumnName = "classroom",nullable = true)
		})
	private Projector projector;

    /**
     * The username of the person who triggered this event.
     */
	private String user;

    /**
     * The timestamp indicating when the event occurred.
     */
	private LocalDateTime dateTime;
	
    /**
     * The status of the action associated with this event.
     * <p>
     * Possible values:
     * <ul>
     *   <li><b>PENDING</b>: The action has been created but not yet sent.</li>
     *   <li><b>SENT</b>: The action has been sent to the projector.</li>
     *   <li><b>COMPLETED</b>: The action has been executed successfully.</li>
     *   <li><b>ERROR</b>: An error occurred while executing the action.</li>
     * </ul>
     * </p>
     */
	private String actionStatus;
	
	@Override
	public String toString(){
		return new StringBuilder()
				.append("ServerEvent - eventId: ").append(this.eventId)
				.append(" | command:").append( this.command == null ? "N/A" : this.command.toString())
				.append(" | projector: ").append( this.projector == null ? "N/A" : this.projector.toString())
				.append(" | user: ").append(this.user.isBlank() ? "N/A" : this.user)
				.append(" | dateTime: ").append(this.dateTime == null ? "N/A" : this.dateTime.toString())
				.append(" | actionStatus: ").append( this.actionStatus.isBlank() ? "N/A" : this.actionStatus )
				.toString();
	}
}
