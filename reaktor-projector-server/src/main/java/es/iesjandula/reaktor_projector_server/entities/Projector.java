package es.iesjandula.reaktor_projector_server.entities;

import es.iesjandula.reaktor_projector_server.entities.ids.ProjectorId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProjectorId.class)
public class Projector
{
	// Model of the projector.
	@Id
	@ManyToOne
	@JoinColumn( name = "modelName" )
	private ProjectorModel model;
	
	// Classroom in which the projector is located.
	@Id
	private String clasroom;
}
