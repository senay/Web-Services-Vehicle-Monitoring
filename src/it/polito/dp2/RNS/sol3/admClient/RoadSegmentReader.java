package it.polito.dp2.RNS.sol3.admClient;

import java.util.Set;

import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.RoadSegmentType;

public class RoadSegmentReader extends it.polito.dp2.RNS.sol3.admClient.PlaceReader implements it.polito.dp2.RNS.RoadSegmentReader {
	private RoadSegmentType rs = new RoadSegmentType();
	public RoadSegmentReader(Place place, RoadSegmentType rs){
		super(place);
		this.rs = rs;
		
	}

	@Override
	public String getName() {
		return rs.getName();
	}

	@Override
	public String getRoadName() {
		return rs.getRoadName();
	}

	

}
