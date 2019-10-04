package it.polito.dp2.RNS.sol3.vehClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.RNS.RnsReaderFactory;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;
import it.polito.dp2.RNS.sol2.jaxb.Path;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.VehicleStateType;
import it.polito.dp2.RNS.sol3.jaxb.VehicleTypeType;

public class vehicleClient implements it.polito.dp2.RNS.lab3.VehClient{
	private WebTarget target;
	private static String base_url=System.getProperty("it.polito.dp2.RNS.lab3.URL");
	private static String id;
	private Vehicle vehicle = new Vehicle();

	public vehicleClient() {
		Client c = ClientBuilder.newClient();
		if(base_url==null)
			base_url = "http://localhost:8080/RnsSystem/rest";
		target = c.target(base_url);
	}
	
	public XMLGregorianCalendar getXMLGregorianCalendarNow() throws DatatypeConfigurationException{
	        GregorianCalendar gregorianCalendar = new GregorianCalendar();
	        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
	        XMLGregorianCalendar now = 
	            datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
	        return now;
	}
	
	@Override
	public List<String> enter(String plateId, VehicleType type, String inGate, String destination)
			throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
		id = plateId;
		vehicle.setPosition(inGate);
		vehicle.setComesFrom(inGate);
		vehicle.setId(plateId);
		vehicle.setIsDirectedTo(destination);
		vehicle.setVehicleState(VehicleStateType.IN_TRANSIT);
		vehicle.setVehicleType(VehicleTypeType.valueOf(type.value()));
		try {
			vehicle.setEntryTime(getXMLGregorianCalendarNow());
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		Response res = target.path("/entity/vehicles")
				   .request("application/json")
				   .post(Entity.json(vehicle));
		int status = res.getStatus();
		if(status == 500)
			throw new ServiceException();
		if(status == 407)
			throw new EntranceRefusedException();
		if(status == 305)
			throw new WrongPlaceException();
		if(status == 304)
			throw new UnknownPlaceException();
		it.polito.dp2.RNS.sol2.jaxb.Path path = res.readEntity(it.polito.dp2.RNS.sol2.jaxb.Path.class);
		List<String> list = new ArrayList<>();
		list.addAll(path.getNodes());
		return list;
	}

	@Override
	public List<String> move(String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		vehicle.setPosition(newPlace);
		Response res = target.path("/entity/vehicles/"+vehicle.getId())
				   .request("application/json")
				   .put(Entity.json(vehicle));	
		int status = res.getStatus();
		
		if(status == 500)
			throw new ServiceException();		
		if(status == 309)
			return null;
		if(status == 305)
			throw new WrongPlaceException();
		if(status == 304)
			throw new UnknownPlaceException();
		
		it.polito.dp2.RNS.sol2.jaxb.Path path = res.readEntity(it.polito.dp2.RNS.sol2.jaxb.Path.class);
		List<String> list = new ArrayList<>();
		if(path!=null)
			list.addAll(path.getNodes());
		
		return list;
	}

	@Override
	public void changeState(VehicleState newState) throws ServiceException {
		vehicle.setVehicleState(VehicleStateType.valueOf(newState.name()));
		Response res = target.path("/entity/vehicles/id/state")
				   .request("application/json")
				   .put(Entity.json(vehicle));
		if(res.getStatus()==500)
			throw new ServiceException();
	}

	@Override
	public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		vehicle.setPosition(outGate);
		Response res = target.path("/entity/vehicles/"+vehicle.getId())
				   .queryParam("outGate", outGate)
				   .request("application/json")
				   .delete();
		
		int status = res.getStatus();
		if(status == 500)
			throw new ServiceException();		
		if(status == 305)
			throw new WrongPlaceException();
		if(status == 304)
			throw new UnknownPlaceException();
	}
}
