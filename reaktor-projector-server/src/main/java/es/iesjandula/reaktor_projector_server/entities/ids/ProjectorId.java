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
 * <li>{@code model} - The model of the projector.</li>
 * <li>{@code classroom} - The classroom where the projector is located.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Author: David Jason Gianmoena
 * (<a href="https://github.com/JasonDGian">GitHub</a>) Version: 1.0
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectorId
{
	/**
	 * The model of the projector.
	 */
	private ProjectorModel model;

	/**
	 * The classroom where the projector is located.
	 */
	private Classroom classroom;

	@Override
	public String toString()
	{
		return new StringBuilder().append("ProjectorId - model: ")
				.append(this.model == null ? "N/A" : this.model.getModelName()).append(" | classroom: ")
				.append(this.classroom == null ? "N/A" : this.classroom.getClassroomName()).toString();
	}
}
