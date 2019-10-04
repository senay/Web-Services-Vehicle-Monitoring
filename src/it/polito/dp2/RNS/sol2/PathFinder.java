package it.polito.dp2.RNS.sol2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.google.gson.Gson;
import com.sun.jersey.core.header.Token;

import javax.ws.rs.client.WebTarget;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RnsReaderException;
import it.polito.dp2.RNS.RnsReaderFactory;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ModelException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.sol2.jaxb.Path;
//import it.polito.dp2.RNS.sol2.jaxb.Nodes;
import it.polito.dp2.RNS.sol2.jaxb.PathsTo;
import it.polito.dp2.RNS.sol2.jaxb.Relationships;
import it.polito.dp2.RNS.sol2.jaxb.Place;
import it.polito.dp2.RNS.sol2.jaxb.Rship;


public class PathFinder implements it.polito.dp2.RNS.lab2.PathFinder{
	private WebTarget target;
	private boolean reloaded = false;
	Set<String> nodesAddress = new HashSet<String>();
	
	Map<String, String> placesMap = new HashMap<String, String>();
	Map<String, String> uriMap = new HashMap<String, String>();
	private static String base_url=System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL");
	
	private static RnsReader monitor;
	
	public PathFinder(RnsReader monitor) throws RnsReaderException {
		Client c = ClientBuilder.newClient();

		RnsReaderFactory factory = RnsReaderFactory.newInstance();
		this.monitor = monitor;
		
		if(base_url==null)
			base_url = "http://localhost:7474/db";
		target = c.target(base_url);

	}		
	
	@Override
	public boolean isModelLoaded() {
		return reloaded;
	}

	@Override
	public void reloadModel() throws ServiceException, ModelException {
		try
		  { 
			CreateNodes();
			createRelations();
			reloaded = true;
		  }
		  catch (Exception e)
		  {
			 System.out.println ("Fatal error: " + e);
		  }
	}

	private void CreateNodes() throws ServiceException, ModelException{
		Set<PlaceReader> places = monitor.getPlaces(null);
		
		if(places == null){
			throw new ModelException("Model can-t be read");
		}
		
		Place pl = new Place();
		
		for (PlaceReader gate: places) {
			pl.setId(gate.getId());
			javax.ws.rs.core.Response res = target.path("/data/node/")
					   .request("application/json")
					   .post(Entity.entity(pl, MediaType.APPLICATION_JSON));
			
			if(res.getStatus() == 500){
				throw new ServiceException("Server Error");
			}
			
			if(res.getStatus() == 201){
				placesMap.put(gate.getId(),res.getLocation().toString().substring(res.getLocation().toString().lastIndexOf("/") + 1) );
				uriMap.put(res.getLocation().toString(), gate.getId());
				
				nodesAddress.add(res.getLocation().toString());	
			}
		}
	}
	
	public void createRelations() throws ServiceException, ModelException {
		Set<ConnectionReader> set = monitor.getConnections();	
		if(set == null){
			throw new ModelException("Model can-t be read");
		}
		Rship rship = new Rship();
		String neo4jIdNode = null;
		String destinationURI = null;
		String sourceURI = null;
		for (ConnectionReader con:set){
			String src = placesMap.get(con.getFrom().getId());
			String dst = placesMap.get(con.getTo().getId());
			
			for(String node:nodesAddress){
				if(node.contains(src)){
					sourceURI =node;
				}
				if(node.contains(dst)){
					destinationURI =node;
				}
			}
			
			rship.setType("isConnectedTo");	
			rship.setTo(destinationURI);
			neo4jIdNode = sourceURI.substring(sourceURI.lastIndexOf("/") + 1);
			Response res = target.path("/data/node/"+ neo4jIdNode +"/relationships/")
					   .request("application/json")
					   .post(Entity.entity(rship, MediaType.APPLICATION_JSON));
			
			if(res.getStatus() == 500){
				throw new ServiceException("Server Error");
			}
		}
	}
	
	@Override
	public Set<List<String>> findShortestPaths(String source, String destination, int maxlength)
			throws UnknownIdException, BadStateException, ServiceException {
		//if(!reloaded){
			try {
				CreateNodes();
				createRelations();
			} catch (Exception e) {
		
			}
		//}
	
		String src = placesMap.get(source);
		String dst = placesMap.get(destination);
	
		PathsTo pathsto = new PathsTo();
		
		for(String node:nodesAddress){
			if(node.contains(dst)){
				pathsto.setTo(node);
				break;
			}
		}
		pathsto.setMaxDepth(maxlength);
		pathsto.setAlgorithm("shortestPath");
		Relationships r = new Relationships();
		r.setDirection("out");
		r.setType("isConnectedTo");
		pathsto.setRelationships(r);
		
		
					
		Response res = target.path("/data/node/"+ src +"/paths/")
		   .request("application/json").post(Entity.entity(pathsto, MediaType.APPLICATION_JSON),Response.class);
		
		if(res.getStatus()==500)
			throw new ServiceException();
		
		Path[] path = res.readEntity(Path[].class);
			
		List<String> shortestPath = new ArrayList<>();
		Set<List<String>> shortestPaths = new HashSet<>();
	

		for(int i=0;i<path.length;i++){
			for(String str:path[i].getNodes()){
				shortestPath.add(uriMap.get(str));				
			}				
			shortestPaths.add(shortestPath);
		}
		return shortestPaths;
	}
}