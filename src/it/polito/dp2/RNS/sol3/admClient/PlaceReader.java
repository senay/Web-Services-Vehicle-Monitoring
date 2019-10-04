package it.polito.dp2.RNS.sol3.admClient;



import java.util.HashSet;
import java.util.Set;

import javax.print.attribute.HashAttributeSet;

import it.polito.dp2.RNS.sol3.jaxb.*;

public class PlaceReader implements it.polito.dp2.RNS.PlaceReader {
	private Place place = new Place();
	private Set<it.polito.dp2.RNS.PlaceReader> nextPlaces = new HashSet<>();
	public PlaceReader(Place place) {
		this.place = place;
	}
	@Override
	public String getId() {
		return place.getId();
	}

	@Override
	public int getCapacity() {
		return place.getCapacity();
	}

	@Override
	public Set<it.polito.dp2.RNS.PlaceReader> getNextPlaces() {
		return nextPlaces;
	}

}
