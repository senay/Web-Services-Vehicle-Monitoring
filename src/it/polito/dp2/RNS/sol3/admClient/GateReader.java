package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.sol3.jaxb.Place;

public class GateReader extends it.polito.dp2.RNS.sol3.admClient.PlaceReader implements it.polito.dp2.RNS.GateReader{
	private Place gate = new Place();
	public GateReader(Place place, Place gate) {
		super(place);
		this.gate = gate;
		
	}
	@Override
	public GateType getType() {
		return GateType.valueOf(gate.getGate());
	}
	

}
