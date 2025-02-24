package es.iesjandula.reaktor_projector_server.entities.ids;

import es.iesjandula.reaktor_projector_server.entities.Classroom;
import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the composite key for the Projector entity.
 * 
 * <p>
 * This class is used as an {@code @IdClass} to define a composite primary key
 * for the {@code Projector} entity.
 * </p>
 *
 * <p>
 * The composite key consists of:
 * <ul>
 * <li>{@code modelName} - The model name of the projector.</li>
 * <li>{@code classroom} - The classroom where the projector is located.</li>
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
 * <li>{@link Serializable} - Required for JPA composite keys.</li>
 * </ul>
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectorId
{
	/** 
	 * The model name of the projector. 
	 */
	private ProjectorModel model;

	/** 
	 * The classroom where the projector is located. 
	 */
	private Classroom classroom;
	
	@Override
	public String toString() {
	    return new StringBuilder()
	        .append("ProjectorId - model: ").append(this.model == null ? "N/A" : this.model.getModelName())
	        .append(" | classroom: ").append(this.classroom.getClassroomName() == null  ? "N/A" : this.classroom.getClassroomName())
	        .toString();
	}
}
