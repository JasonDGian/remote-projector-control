package es.iesjandula.reaktor_projector_server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a classroom located in a specific floor.
 * The classroom is uniquely identified by its name and belongs to a floor.
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {

    /** Unique identifier for the classroom (using classroom name as the primary key) */
    @Id
    private String classroomName;

    /** The floor to which this classroom belongs */
    @ManyToOne
    @JoinColumn(name = "floor_name", referencedColumnName = "floorName", nullable = false)
    private Floor floor;
}
