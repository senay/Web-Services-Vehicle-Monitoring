package it.polito.dp2.RNS.sol3.admClient;

import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.ParkingAreaType;;



public class ParkingAreaReader extends it.polito.dp2.RNS.sol3.admClient.PlaceReader implements it.polito.dp2.RNS.ParkingAreaReader{
    private ParkingAreaType pr = new ParkingAreaType();
	public ParkingAreaReader(Place place, ParkingAreaType pr) {
		super(place);
		this.pr = pr;
	}

	@Override
	public Set<String> getServices() {
		Set<String> services = new HashSet<>();
		System.out.println(".....list of services....."+pr.getService());
		services.addAll(pr.getService());
		return services;
	}
	
	

}
