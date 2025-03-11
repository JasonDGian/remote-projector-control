package es.iesjandula.reaktor_projector_server.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a floor in the building. The floor is uniquely identified by its
 * name (E.G: 'Planta 1', 'Planta 2'...).
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
public class Floor
{

	/**
	 * Unique identifier for the floor (using floor name as the primary key).
	 */
	@Id
	private String floorName;

	/**
	 * List of classrooms located on this floor.
	 */
	@OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Classroom> classrooms;

	/*
	 * Specialized constructor that ignores the classroom list for ease of use with
	 * the repositories.
	 */
	public Floor(String floorName)
	{
		this.floorName = floorName;
	}
}
