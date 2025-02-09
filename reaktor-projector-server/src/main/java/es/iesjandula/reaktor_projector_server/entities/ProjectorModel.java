package es.iesjandula.reaktor_projector_server.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectorModel
{
	// Name of the model. 
	// E.g: 'V11H979056'
	@Id
	@Column( name = "model_name" )
	private String modelName;
	
	@OneToMany(mappedBy = "model")
	private List<Projector> associatedUnits;
}
