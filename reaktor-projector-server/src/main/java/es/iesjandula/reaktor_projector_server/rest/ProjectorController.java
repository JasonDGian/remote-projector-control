package es.iesjandula.reaktor_projector_server.rest;

import java.io.IOException;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor_projector_server.parsers.interfaces.ICommandParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorModelParser;
import es.iesjandula.reaktor_projector_server.parsers.interfaces.IProjectorParser;
import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProjectorController
{
	
	@Autowired
	ICommandParser commandsParser;
	
	@Autowired
	IProjectorParser projectorParser;
	
	@Autowired
	IProjectorModelParser projectorModelsParser;
	
	
	@Transactional
    @PostMapping("/parse-models")
    public String parseModels( MultipartFile file ){
				
		log.debug("Call to models parser received.");

		Scanner scanner;
		
		try
		{
			scanner = new Scanner(file.getInputStream());
			projectorModelsParser.parseProjectorModels(scanner);
			
		} catch (IOException | ProjectorServerException e)
		{
			e.printStackTrace();
			return "Error encountered during parsing process.";
		}
		
		return "done";
    	
    	// introducir logica donde se recibe el fichero CSV y se llama al repositorio para parsear.

    }

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
	

	
	
	
	@Transactional
    @PostMapping("/parse-projectors")
    public String parseProjectors( MultipartFile file ){
				
		log.debug("Call to projectors parser received.");

		Scanner scanner;
		
		try
		{
			scanner = new Scanner(file.getInputStream());
			projectorParser.parseProjectors(scanner);
			
		} catch (IOException | ProjectorServerException e)
		{
			e.printStackTrace();
			return "Error encountered during parsing process.";
		}
		
		return "done";
    	
    	// introducir logica donde se recibe el fichero CSV y se llama al repositorio para parsear.

    }
	
	
	// metodo POST que recibe por cuerpo un modelo de projector y un aula donde se encuentra y guarda el registro en base de deatos.
	
	// metodo DELETE que recibe por cuerpo un modelo de projector y un aula donde se encuentra y elimina el registro en base de deatos.
	
	
}
