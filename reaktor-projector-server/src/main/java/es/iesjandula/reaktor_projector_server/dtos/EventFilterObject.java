package es.iesjandula.reaktor_projector_server.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterObject
{
	Long eventId;
	String actionName;
	String modelName;
	String classroomName;
	String floorName;
	String user;
	LocalDateTime dateTime;
	String actionStatus;	
}
