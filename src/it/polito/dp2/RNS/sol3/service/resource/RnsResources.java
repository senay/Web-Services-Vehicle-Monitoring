package it.polito.dp2.RNS.sol3.service.resource;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.Api;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.PathFinderException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;
import it.polito.dp2.RNS.sol3.jaxb.Entity;
import it.polito.dp2.RNS.sol3.jaxb.Places;
import it.polito.dp2.RNS.sol3.jaxb.TrackedVehicles;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.service.rnsService.serviceLogic;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/entity")
@Api(value = "/entity")
public class RnsResources {
	public UriInfo uriInfo;
	serviceLogic service = new serviceLogic();
	
	public RnsResources(@Context UriInfo uriInfo){
		this.uriInfo = uriInfo;
	}	
	@GET
    @ApiOperation(value = "getEntity", notes = "reads main resource"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Entity getEntity() {
		Entity entity = new Entity();
		UriBuilder root = uriInfo.getAbsolutePathBuilder();
		entity.setSelf(root.toTemplate());		
		UriBuilder places = root.clone().path("places");
		entity.setPlaces(places.toTemplate());
		entity.setVehicles((root.clone().path("vehicles").toTemplate()));
		
		return entity;		
	}
	//1.
	@GET
	@Path("/places")
    @ApiOperation(value = "getPlaces", notes = "searches places"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getPlaces() {
		Places places = service.getPlaces();
		return places;
	}
	
	//2.
	@POST
	@Path("/vehicles")
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response addVehicle(Vehicle vehicle) {
		String id = vehicle.getId();
    	UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(id);
    	URI self = builder.build();
    	vehicle.setSelf(self.toString());
    	it.polito.dp2.RNS.sol2.jaxb.Path nodesList = new it.polito.dp2.RNS.sol2.jaxb.Path();
    	List<String> created = new ArrayList<>();
    	try {
			created = service.addVehicle(id, vehicle);
		} catch (UnknownPlaceException e) {
			return Response.status(304).entity("Uknown Place").build();
		}catch(WrongPlaceException wpe){
			return Response.status(305).entity("Wrong Place").build();
		}catch(BadStateException | ServiceException e){
			return Response.status(500).entity("Service Exception").build();
		} catch (UnknownIdException e) {
			e.printStackTrace();
		} catch (EntranceRefusedException e) {
			return Response.status(407).entity("Service Exception").build();
		}
		nodesList.getNodes().addAll(created);
		return Response.created(self).entity(nodesList).build();
	}
	
	//3.
	@GET
	@Path("/vehicles")
    @ApiOperation(value = "getVehicles", notes = "searches vehicles"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public TrackedVehicles getVehicles(@QueryParam("place") String place) {
		return service.getVehicles(place);	
	}
	
	//4.
	@GET
	@Path("/vehicles/{id}")
    @ApiOperation(value = "getVehicle", notes = "finds a vehicle"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicle getVehicle(@PathParam("id") String id) {
				return service.getVehicle(id);
	}
	//5.
	@PUT
	@Path("/vehicles/{id}")
    @ApiOperation(value = "updateSuggestedPath", notes = "update vehicles suggested Path"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response updateSuggestedPath(@PathParam("id") String id, Vehicle V) throws UnknownIdException, BadStateException, ServiceException, PathFinderException {
		it.polito.dp2.RNS.sol2.jaxb.Path nodesList = new it.polito.dp2.RNS.sol2.jaxb.Path();
    	List<String> newPath = new ArrayList<>();
		try {
			newPath = service.updateSuggestedPath(id, V.getPosition());
		} catch (UnknownPlaceException upe) {
			return Response.status(304).entity("Uknown Place").build();
		}catch(WrongPlaceException wpe) {
			return Response.status(305).entity("Wrong Place").build();
		}catch(UnknownIdException uie){
			return Response.status(309).entity("Path Not Changed").build();
		}
		nodesList.getNodes().addAll(newPath);
		
		return Response.accepted().entity(nodesList).build();
	}
	
	//6.
	@PUT
	@Path("/vehicles/{id}/state")
    @ApiOperation(value = "changeState", notes = "update vehicles state"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicle changeState(@PathParam("id") String id, Vehicle V) {
		Vehicle updated = service.changeState(id,V.getVehicleState());
			return updated; 
	}
	
	//7.
	@DELETE
	@Path("/vehicles/{id}")
    @ApiOperation(value = "removeVehicle", notes = "removes a single vehicle"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No content"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	public Response removeVehicle(@PathParam("id") String id, @QueryParam("outGate") String outGate) {
		boolean removed = false;
		
		try {
			removed = service.removeVehicle(id, outGate);
		} catch (UnknownPlaceException upe) {
			return Response.status(304).entity("Uknown Place").build();
		}catch(WrongPlaceException wpe) {
			return Response.status(305).entity("Wrong Place").build();	
		}
		
		if (removed==true){
			return Response.status(200).entity("Ok").build(); 
			
		}
		else{
			return Response.status(400).entity("Not Ok").build();
		}
	}
}
