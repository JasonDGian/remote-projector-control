package es.iesjandula.reaktor_projector_server.parsers;

import java.util.Scanner;

import es.iesjandula.reaktor_projector_server.utils.ProjectorServerException;

public interface ICommandsParser
{
	public void parseCommands( Scanner scanner) throws ProjectorServerException;
}
