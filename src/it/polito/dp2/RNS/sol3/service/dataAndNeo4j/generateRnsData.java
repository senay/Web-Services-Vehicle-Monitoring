package it.polito.dp2.RNS.sol3.service.dataAndNeo4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.text.WordUtils;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.RnsReaderException;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ModelException;
import it.polito.dp2.RNS.lab2.PathFinderException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Places;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.VehicleStateType;

public class generateRnsData {
	public UriInfo uriInfo;
	private static generateRnsData data;
	static { 
		try {
			data = new generateRnsData();
		}catch(ModelException m){
			;
		}catch(ServiceException s){
			;
		}catch(PathFinderException p){
			;
		}
	}
	it.polito.dp2.RNS.sol2.PathFinderFactory pathFinder;
	private static it.polito.dp2.RNS.RnsReader monitor;
	private static long lastIdPlace=0;
	private static long lastIdVehicle=0;
	private ConcurrentHashMap<String,List<String>> vehiclePath;
	private ConcurrentHashMap<Long,placeExt> placesById;
	private static ConcurrentHashMap<String,vehicleExt> vehiclesById;
	
	Set<it.polito.dp2.RNS.sol3.jaxb.Place> places = new HashSet<>();
	
	private generateRnsData() throws PathFinderException, ServiceException, ModelException{
		placesById = new ConcurrentHashMap<Long,placeExt>();
		it.polito.dp2.RNS.RnsReaderFactory factory = it.polito.dp2.RNS.RnsReaderFactory.newInstance();
		try {
			monitor = factory.newRnsReader();
		} catch (RnsReaderException e) {
			e.printStackTrace();
		}
		vehiclesById = new ConcurrentHashMap<String, vehicleExt>();
		places = createPlaces();
	    
		pathFinder = new it.polito.dp2.RNS.sol2.PathFinderFactory(monitor);
		pathFinder.newPathFinder().reloadModel();
	}
	
	public Set<it.polito.dp2.RNS.sol3.jaxb.Place> createPlaces(){
		Set<it.polito.dp2.RNS.sol3.jaxb.Place> places = new HashSet<>();			
		long id;
		for(RoadSegmentReader rr:monitor.getRoadSegments(null)){	
			it.polito.dp2.RNS.sol3.jaxb.Place place = new it.polito.dp2.RNS.sol3.jaxb.Place();
			place.setId(rr.getId());
			place.setCapacity(rr.getCapacity());
			it.polito.dp2.RNS.sol3.jaxb.RoadSegmentType roadSegment = new it.polito.dp2.RNS.sol3.jaxb.RoadSegmentType();
			roadSegment.setRoadName(rr.getRoadName());
			roadSegment.setName(rr.getName());
			place.setRoadSegment(roadSegment);
			id = generateRnsData.getNextIdVehicle();
	    	place.setSelf("localhost:8080/RnsSystem/rest/entity/places/"+id);
			places.add(place);
		}
		
		for(GateReader gr:monitor.getGates(null)){
			it.polito.dp2.RNS.sol3.jaxb.Place place = new it.polito.dp2.RNS.sol3.jaxb.Place();
			place.setId(gr.getId());
			place.setCapacity(gr.getCapacity());
			place.setGate(gr.getType().name());
			id = generateRnsData.getNextIdVehicle();
	    	place.setSelf("localhost:8080/RnsSystem/rest/entity/places/"+id);
			places.add(place);
		}
		
		for(ParkingAreaReader pr:monitor.getParkingAreas(null)){
			it.polito.dp2.RNS.sol3.jaxb.Place place = new it.polito.dp2.RNS.sol3.jaxb.Place();
			place.setId(pr.getId());
			place.setCapacity(pr.getCapacity());
			it.polito.dp2.RNS.sol3.jaxb.ParkingAreaType parkingArea = new it.polito.dp2.RNS.sol3.jaxb.ParkingAreaType();
			parkingArea.getService().addAll(pr.getServices());
			place.setParkingArea(parkingArea);	
			id = generateRnsData.getNextIdVehicle();
	    	place.setSelf("localhost:8080/RnsSystem/rest/entity/places/"+id);
			places.add(place);
		}
		
		for(Place p:places){
			for(ConnectionReader cr:monitor.getConnections()){
				if(p.getId().equals(cr.getFrom().getId())){
					for(Place n:places){
						if(cr.getTo().getId().equals(n.getId()))
							p.getIsconnectedTo().add(n.getId());
					}
				}
			}
			Long idp = Long.parseLong(p.getSelf().substring(p.getSelf().lastIndexOf("/") + 1));
			placeExt plExt = new placeExt(idp, p);
			placesById.put(idp, plExt);
			
		}
		return places;
	}
	
	public static synchronized long getNextIdPlace() {
		return ++lastIdPlace;
	}
	
	public static synchronized long getNextIdVehicle() {
		return ++lastIdVehicle;
	}	
	
	public static generateRnsData getData() {
		return data;
	}
	//1.
	public Collection<Place> getPlaces() {
		Collection<Place> col = new HashSet<>();
		for (Map.Entry<Long, placeExt> entry : placesById.entrySet())
		{
		   col.add(entry.getValue().getPlace());
		}
		return col;
	}
	
	//2.
	public List<String> addVehicle(String id, Vehicle vehicle) throws WrongPlaceException, BadStateException,ServiceException, UnknownPlaceException, EntranceRefusedException {
		String origin = null;
		String destination = null;
		Place pOrigin = null;
		for(Place p:places){
			if(p.getId().equals(vehicle.getIsDirectedTo()))
				destination = vehicle.getIsDirectedTo();
			if(p.getId().equals(vehicle.getComesFrom())){
					origin = vehicle.getComesFrom();
					pOrigin = p;
			}			
		}
		if((origin==null)||(destination==null)){
			throw new UnknownPlaceException();
		}		
		
		if(pOrigin.getGate()==null){
			throw new WrongPlaceException();
		}
		else if((pOrigin.getGate()==null)||!((pOrigin.getGate().equals("IN"))||(pOrigin.getGate().equals("INOUT")))){			
			throw new WrongPlaceException();
		}
		else{
			try {
				Set<List<String>> paths = pathFinder.newPathFinder().findShortestPaths(vehicle.getComesFrom(), vehicle.getIsDirectedTo(), 20);
				
				if(paths.isEmpty()){
					throw new EntranceRefusedException();
				}
				for(List<String> path:paths){
					vehicleExt vExt = new vehicleExt(id, vehicle,path);
					vehiclesById.putIfAbsent(id, vExt);
					return path;
				}
			} catch (BadStateException | ServiceException | PathFinderException e){	
			} catch (UnknownIdException e) {
				e.printStackTrace();
			}			
		}				
		return null;
	}
	
	//3.
	public Collection<Vehicle> getVehicles(String place) {
		Collection<Vehicle> col = new HashSet<>();
		Vehicle v = new Vehicle();
		if(place == null){
			for (Map.Entry<String, vehicleExt> entry : vehiclesById.entrySet())
			{
			   col.add(entry.getValue().getVehicle());
			}
		}
		else{
			for (Map.Entry<String, vehicleExt> entry : vehiclesById.entrySet())
			{
				v =entry.getValue().getVehicle();
				if(v.getPosition().equals(place))
					col.add(v);
			}
		}			
		return col;
	}	
	
	//4.
	public Vehicle getVehicle(String lon) {
		for (Map.Entry<String, vehicleExt> entry : vehiclesById.entrySet())
		{
			if(entry.getValue().getVehicle().getId().equals(lon)){
				return entry.getValue().getVehicle();
			}
		}
		return null;
		
	}	
		
	//5.
	public List<String> updateSuggestedPath( String id, String newPlace) throws UnknownPlaceException, WrongPlaceException, UnknownIdException {
		List<String> pathR = null;
		Vehicle V = vehiclesById.get(id).getVehicle();
		Set<List<String>> paths = null;
		
		if(vehiclesById.get(id).getSuggestedPath().contains(newPlace)){
			vehiclesById.get(id).getVehicle().setPosition(newPlace);
			throw new UnknownIdException();
		}
		boolean placeInRns = false;
		for(Place p:places){
			if(p.getId().equals(newPlace)){
				placeInRns = true;
			}
		}
		
		if(placeInRns){
			try {
				paths = pathFinder.newPathFinder().findShortestPaths(V.getPosition(), newPlace, 20);
			}catch (Exception e) {}
			if(paths == null)
				throw new WrongPlaceException();
			try {
				Set<List<String>> newPaths = pathFinder.newPathFinder().findShortestPaths(newPlace, V.getIsDirectedTo(), 20);
				if (newPaths != null){							
					for(List<String> path:newPaths){
						vehiclesById.get(id).setSuggestedPath(path);
						vehiclesById.get(id).getVehicle().setPosition(newPlace);
						pathR = path;
					}
				} 
			}catch (BadStateException | ServiceException | PathFinderException e){
				
			} catch (UnknownIdException e) {
				e.printStackTrace();
			}
			return pathR;
		}
		else{
			throw new UnknownPlaceException();
		}
	}
	
	//6.
	public Vehicle changeState(String id, VehicleStateType vst2) {
		Vehicle v = new Vehicle();
		v = vehiclesById.get(id).getVehicle();
		v.setVehicleState(vst2);
		return v;
	}	
	//7.
	public boolean deleteVehicle(String id, String currPos) throws WrongPlaceException, UnknownPlaceException {
		Set<List<String>> paths = null;
		Vehicle v = null;
		v = vehiclesById.get(id).getVehicle();
		if(v!=null){			
			Place position = null;
				
			for(Place p:places){
				if(p.getId().equals(currPos)){
					position = p;					
				}
			}
			if(position==null){
				throw new UnknownPlaceException();
			}			
			if(((position.getGate().equals("OUT"))||(position.getGate().equals("INOUT")))){
				try {
					paths = pathFinder.newPathFinder().findShortestPaths(v.getPosition(), currPos, 20);
				}catch (Exception e) {}
				if(paths == null)
					throw new WrongPlaceException();
				else{
					vehiclesById.remove(id);
					return true;
				}					
			}
			else
				throw new WrongPlaceException();
		}
		else 
			return false;
	}	
}