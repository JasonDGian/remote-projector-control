package es.iesjandula.reaktor_projector_server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Represents a floor in a building.
 * The floor is uniquely identified by its name.
 * 
 * @author David Jason Gianmoena (<a href="https://github.com/JasonDGian">GitHub</a>)
 * @version 1.0
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Floor {

    /** Unique identifier for the floor (using floor name as the primary key) */
    @Id
    private String floorName;

    /** List of classrooms located on this floor */
    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Classroom> classrooms;
}
