package es.iesjandula.reaktor_projector_server.rest;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor_projector_server.parsers.ICommandsParser;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProjectorController
{
	
	@Autowired
	ICommandsParser commandsParser;

	// metodo de parseo de acciones
	// Metodo de parseo de modelos
	// parseo comandos
	// parseo de proyectores (unidades)
	@Transactional
    @PostMapping("/parse-commands")
    public String parseCommands( MultipartFile file ){
				
		log.debug("Call to commands parser received.");

		Scanner scanner;
		
		try
		{
			scanner = new Scanner(file.getInputStream());
			commandsParser.parseCommands(scanner);
			
		} catch (IOException | ProjectorServerException e)
		{
			e.printStackTrace();
			return "Error encountered during parsing process.";
		}
		
		return "done";
    	
    	// introducir logica donde se recibe el fichero CSV y se llama al repositorio para parsear.

    }
	
}
