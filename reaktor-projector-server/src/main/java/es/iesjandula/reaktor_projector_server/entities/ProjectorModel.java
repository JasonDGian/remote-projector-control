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
 * @author David Jason Gianmoena [ https://github.com/JasonDGian ]
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectorModel
{
    /**
     * The unique identifier for the projector model.
     * This field stores the model name (e.g., 'V11H979056').
     * The model name is used as the primary key.
     */
	@Id
	private String modelName;
	
    /**
     * The list of projectors associated with this projector model.
     * This is a one-to-many relationship where each model can be linked to 
     * multiple projectors.
     * <p>
     * The associated projectors are stored in the "Projector" table 
     * and are linked through the "model" property.
     * </p>
     */
	@OneToMany(mappedBy = "model")
	private List<Projector> associatedProjectors;
	
    /**
     * Constructs a new ProjectorModel with the specified model name.
     * <p>
     * This constructor is used when creating a projector model object with only 
     * the model name, without initializing the associated projectors list.
     * </p>
     *
     * @param modelName The unique identifier for the projector model (e.g., 'V11H979056').
     *                  This name serves as the primary key for the model.
     */
	public ProjectorModel ( String modelName ){
		this.modelName = modelName;
	}
}
