package es.iesjandula.reaktor_projector_server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichResponseDto
{
	String status;
	String message1;
	String message2;
	String message3;
}
