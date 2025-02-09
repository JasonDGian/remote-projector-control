package es.iesjandula.reaktor_projector_server.entities.ids;

import es.iesjandula.reaktor_projector_server.entities.ProjectorModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectorId
{
	private ProjectorModel model;
	
	private String clasroom;
}
