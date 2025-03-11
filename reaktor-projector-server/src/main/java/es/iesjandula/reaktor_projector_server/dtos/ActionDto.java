package es.iesjandula.reaktor_projector_server.dtos;

import es.iesjandula.reaktor_projector_server.entities.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing an Action entity for use in the
 * frontend. This class provides a simplified representation of the Action
 * entity.
 * 
 * @author David Jason Gianmoena
 *         (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDto
{

	/**
	 * The name of the action.
	 */
	private String actionName;

	/**
	 * Constructs an ActionDto from the given Action entity.
	 * 
	 * @param actionEntity the Action entity to be converted into an ActionDto
	 */
	public ActionDto(Action actionEntity)
	{
		this.setActionName(actionEntity.getActionName());
	}

}
