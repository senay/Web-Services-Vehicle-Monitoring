package it.polito.dp2.RNS.sol3.service.rnsService;

import it.polito.dp2.RNS.sol3.jaxb.Places;
import it.polito.dp2.RNS.sol3.jaxb.TrackedVehicles;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.PathFinderException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.VehicleStateType;
import it.polito.dp2.RNS.sol3.service.dataAndNeo4j.generateRnsData;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public class serviceLogic {	
	private generateRnsData data = generateRnsData.getData();
	
	public long getNextId() {
		return generateRnsData.getNextIdVehicle();
	}

	
	//1.
	public Places getPlaces() {
		Places places = new Places();
		places.getPlace().addAll(data.getPlaces());
		return places;
	}
	
	//2.
	public List<String> addVehicle(String id, Vehicle vehicle) throws UnknownIdException, WrongPlaceException, BadStateException, ServiceException, UnknownPlaceException, EntranceRefusedException {
		return data.addVehicle(id, vehicle);
	}
	
	//3.
	public TrackedVehicles getVehicles(String place) {
		TrackedVehicles vehicles = new TrackedVehicles();
		vehicles.getVehicle().addAll(data.getVehicles(place));
		return vehicles;
	
	}
	
	//4.
	public Vehicle getVehicle(String id) {
		Vehicle vehicle = new Vehicle();
		vehicle = data.getVehicle(id);
		return vehicle;
	
	}

	//5.
	public List<String> updateSuggestedPath(String id, String newPlace) throws UnknownPlaceException, WrongPlaceException, UnknownIdException{
		return data.updateSuggestedPath(id, newPlace);
	}
	
	//6.
	public Vehicle changeState(String id, VehicleStateType vst) {
		return data.changeState(id,vst);
	}

	//7.	
	public boolean removeVehicle(String id, String position) throws WrongPlaceException, UnknownPlaceException {
		return data.deleteVehicle(id, position);
	}
}
