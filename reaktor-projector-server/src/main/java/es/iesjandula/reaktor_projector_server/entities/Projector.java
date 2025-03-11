package es.iesjandula.reaktor_projector_server.entities;

import java.util.List;

import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a projector entity that includes information about the projector
 * model and the classroom where the projector is located.
 * <p>
 * This entity is used to track individual projectors, linking each projector to
 * a specific model and physical location.
 * </p>
 * <p>
 * The combination of the model and classroom uniquely identifies each projector
 * and is used as a composite primary key via the {@link ProjectorId}.
 * </p>
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
@IdClass(ProjectorId.class)
public class Projector
{
	/**
	 * The model of the projector.
	 * <p>
	 * This is a many-to-one relationship with the {@link ProjectorModel} entity.
	 * The projector is associated with a specific model, and the model name is
	 * stored in the "modelName" field.
	 * </p>
	 */
	@Id
	@ManyToOne
	@JoinColumn(name = "modelName")
	private ProjectorModel model;

	/**
	 * The classroom where the projector is located.
	 * <p>
	 * This field, along with the projector model, forms the composite primary key
	 * for this entity.
	 * </p>
	 */
	@Id
	@ManyToOne
	@JoinColumn(name = "classroom")
	private Classroom classroom;

	/**
	 * List of server events associated with the projector.
	 */
	@OneToMany(mappedBy = "projector", cascade = CascadeType.REMOVE, orphanRemoval = false)
	private List<ServerEvent> serverEvents;

	// Special to string method to prevent recursive calls and null pointers.
	@Override
	public String toString()
	{
		return new StringBuilder().append("Projector - model: ")
				.append(this.model == null ? "N/A" : this.model.getModelName()).append(" | classroom: ")
				.append(this.classroom == null ? "N/A" : this.classroom.getClassroomName()).toString();
	}
}
