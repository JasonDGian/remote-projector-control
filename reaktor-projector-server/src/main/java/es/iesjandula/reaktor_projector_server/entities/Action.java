package es.iesjandula.reaktor_projector_server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action
{
	// Name of the action to perform. 
	// E.g: "Power off"
	@Id
	@Column( name = "action_name" )
	private String actionName;
	
}
