package es.iesjandula.reaktor_projector_server.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a projector model that contains information about the model name
 * and the projectors associated with it.
 * <p>
 * This entity is used to store the details of a projector model and maintain
 * relationships with the actual projectors that belong to this model.
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
public class ProjectorModel
{
	/**
	 * The unique identifier for the projector model. This field stores the model
	 * name (e.g., 'V11H979056'). The model name is used as the primary key.
	 */
	@Id
	private String modelName;

	/**
	 * The list of projectors associated with this projector model.
	 */
//	@OneToMany(mappedBy = "model")
//	private List<Projector> associatedProjectors;

	/**
	 * The list of commands associated with this projector model.
	 */
	@OneToMany(mappedBy = "modelName")
	private List<Command> associatedCommands;

	/**
	 * Constructs a new ProjectorModel with the specified model name.
	 */
	public ProjectorModel(String modelName)
	{
		this.modelName = modelName;
	}

	// Special to string method to prevent recursive calls and null pointers.
	@Override
	public String toString()
	{
		return new StringBuilder().append("ProjectorModel - modelName: ")
				.append(this.modelName == null ? "N/A" : this.modelName).toString();
	}
}
