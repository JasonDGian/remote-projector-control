package es.iesjandula.reaktor_projector_server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.iesjandula.reaktor_projector_server.dtos.FloorDto;
import es.iesjandula.reaktor_projector_server.entities.Floor;

public interface IFloorRepository extends JpaRepository<Floor, String>
{

	@Query("""
		    SELECT new es.iesjandula.reaktor_projector_server.dtos.FloorDto(f.floorName)
		    FROM Floor f
		""")
		public List<FloorDto> findAllDto();
}
