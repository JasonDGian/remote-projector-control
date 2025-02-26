package es.iesjandula.reaktor_projector_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectorInfoDto
{
	private String model;
	private String classroom;
	private String floorname;
}
