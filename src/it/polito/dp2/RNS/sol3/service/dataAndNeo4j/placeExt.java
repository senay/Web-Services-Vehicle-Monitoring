package it.polito.dp2.RNS.sol3.service.dataAndNeo4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;

public class placeExt {
	private long id;
	private Place place;
	private Map<Long,Place> nextPlaces;

	public placeExt(Long id, Place place){
		this.id = id;
		this.place = place;
		nextPlaces = new ConcurrentHashMap<Long,Place>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public Map<Long, Place> getNextPlaces() {
		return nextPlaces;
	}

	public void setNextPlaces(Map<Long, Place> nextPlaces) {
		this.nextPlaces = nextPlaces;
	}
}
