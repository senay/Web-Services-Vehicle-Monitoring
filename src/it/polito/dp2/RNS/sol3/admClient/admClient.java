package it.polito.dp2.RNS.sol3.admClient;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Places;
import it.polito.dp2.RNS.sol3.jaxb.TrackedVehicles;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;

public class admClient implements it.polito.dp2.RNS.lab3.AdmClient {
	Places placeEntity = new Places();
	private WebTarget target;
	private static String base_url=System.getProperty("it.polito.dp2.RNS.lab3.URL");
	Set<it.polito.dp2.RNS.PlaceReader> places = new HashSet<>();
	
	public admClient() {
		Client c = ClientBuilder.newClient();
		if(base_url==null)
			base_url = "http://localhost:8080/RnsSystem/rest";
		target = c.target(base_url);
		
		placeEntity = target.path("/entity/places")
				   .request("application/json")
				   .get(Places.class);
		places = createPlaces();
	}
	public Set<it.polito.dp2.RNS.PlaceReader> createPlaces(){		
		Set<it.polito.dp2.RNS.PlaceReader> places = new HashSet<>();
		for(Place p:placeEntity.getPlace()){
			if(p.getGate()!=null)
				places.add(new it.polito.dp2.RNS.sol3.admClient.GateReader(p, p));
			if(p.getRoadSegment()!=null)
				places.add(new it.polito.dp2.RNS.sol3.admClient.RoadSegmentReader(p, p.getRoadSegment()));
			if(p.getParkingArea()!=null)
				places.add(new it.polito.dp2.RNS.sol3.admClient.ParkingAreaReader(p, p.getParkingArea()));
		}
		
		for(it.polito.dp2.RNS.PlaceReader p:places)//for all PlaceReaders
		{
			for(Place pt:placeEntity.getPlace()){//for all PlaceTypes
				if(pt.getId().equals(p.getId())){//find the corresponding PlaceType
					for(String str :pt.getIsconnectedTo()){//iterate over IsConnectedTo set
						for(PlaceReader pr:places)// and find the corresponding 
						{
							if(str.equals(pr.getId())){//PlaceReader
								p.getNextPlaces().add(pr);
								break;
							}
						}
					}
					break;
				}
			}
		}
		return places;
	}
	
	@Override
	public Set<ConnectionReader> getConnections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<GateReader> getGates(GateType arg0) {
		Set<it.polito.dp2.RNS.GateReader> gReader = new HashSet<>();
		for(Place p:placeEntity.getPlace()){
			if(p.getGate() != null && p.getGate().equals(arg0)){
				for(PlaceReader pr:places){
					if(p.getId().equals(pr.getId())){
						gReader.add((GateReader) pr);
					}
				}
			}
			if(p.getGate() != null && (arg0==null)){
				for(PlaceReader pr:places){
					if(p.getId().equals(pr.getId())){
						gReader.add((GateReader) pr);
					}
				}
			}			
		}
				return gReader;
	}

	@Override
	public Set<ParkingAreaReader> getParkingAreas(Set<String> arg0) {
		Set<it.polito.dp2.RNS.ParkingAreaReader> paReader = new HashSet<>();
		for(Place p:placeEntity.getPlace()){
			if((arg0 != null)&&(p.getParkingArea() != null)&&(p.getParkingArea().getService().containsAll(arg0))){
				for(PlaceReader pr:places){
					if(p.getId().equals(pr.getId())){
						paReader.add((ParkingAreaReader) pr);
						System.out.println(".....list of services....."+p.getParkingArea().getService());
					}
				}
			}
			
			if((arg0==null)&&(p.getParkingArea() != null)){
				for(PlaceReader pr:places){
					if(p.getId().equals(pr.getId())){
						paReader.add((ParkingAreaReader) pr);
						System.out.println(".....list of services....."+p.getParkingArea().getService());
					}
				}
			}			
		}	
		return paReader;
	}

	@Override
	public PlaceReader getPlace(String arg0) {
		PlaceReader pr = null;
		for(PlaceReader p:places){
			if(p.getId().equals(arg0))
				pr = p;
		}
		return pr;
	}

	@Override
	public Set<PlaceReader> getPlaces(String arg0) {
		Set<PlaceReader> filteredprs = new HashSet<>();
		
		if(arg0 == null)
			filteredprs = places;
		
		if(arg0 != null){
			for(PlaceReader p:places){
				if(p.getId().startsWith(arg0))
					filteredprs.add(p);
			}
		}
		return filteredprs;
	}

	@Override
	public Set<RoadSegmentReader> getRoadSegments(String arg0) {
		Set<it.polito.dp2.RNS.RoadSegmentReader> rsReader = new HashSet<>();
		for(Place p:placeEntity.getPlace()){
			if(p.getRoadSegment() != null && p.getRoadSegment().getName().equals(arg0)){
				for(PlaceReader pr:places){
					if(p.getId().equals(pr.getId())){
						rsReader.add((RoadSegmentReader) pr);
					}
				}
			}
			if(p.getRoadSegment() != null && (arg0==null)){
				for(PlaceReader pr:places){
					if(p.getId().equals(pr.getId())){
						rsReader.add((RoadSegmentReader) pr);
					}
				}
			}			
		}
		return rsReader;
	}

	@Override
	public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException {
		Response res = target.path("/entity/vehicles/")
				   .queryParam("place", place)
				   .request("application/json")
				   .get();
		if(res.getStatus()==500)
			throw new ServiceException();
		TrackedVehicles vehicles = res.readEntity(TrackedVehicles.class);
		Set<VehicleReader> vr = new HashSet<>();
		for(Vehicle v:vehicles.getVehicle()){
			vr.add(new it.polito.dp2.RNS.sol3.admClient.VehicleReader(v, placeEntity));
		}
		return vr;
	}

	@Override
	public VehicleReader getUpdatedVehicle(String id) throws ServiceException {
		Vehicle vehicle = target.path("/entity/vehicles/"+id)
				   .request("application/json")
				   .get(Vehicle.class);
		if (vehicle==null)
			return null;
		it.polito.dp2.RNS.VehicleReader vr = null;
		vr = new it.polito.dp2.RNS.sol3.admClient.VehicleReader(vehicle, placeEntity);
		return vr;
	}
	
	@Override
	public VehicleReader getVehicle(String arg0) {//should return null
		return null;
	}

	@Override
	public Set<VehicleReader> getVehicles(Calendar arg0, Set<VehicleType> arg1, VehicleState arg2) {//should return null
		return null;
	}
}
