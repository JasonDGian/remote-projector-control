package es.iesjandula.reaktor_projector_server.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a classroom located on a specific floor. The classroom is uniquely
 * identified by its name and belongs to a floor.
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
public class Classroom
{

	/**
	 * Unique identifier for the classroom (using classroom name as the primary
	 * key).
	 */
	@Id
	private String classroomName;

	/**
	 * The floor to which this classroom belongs.
	 */
	@ManyToOne
	@JoinColumn(name = "floor_name", referencedColumnName = "floorName", nullable = false)
	private Floor floor;

	// Special to string method to prevent recursive calls and null pointers.
	@Override
	public String toString()
	{
		return "Classroom " + classroomName + ", Floor = " + floor.getFloorName();
	}
}
