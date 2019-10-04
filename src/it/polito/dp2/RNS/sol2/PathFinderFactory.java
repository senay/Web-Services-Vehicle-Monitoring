package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RnsReaderException;
import it.polito.dp2.RNS.lab2.PathFinder;
import it.polito.dp2.RNS.lab2.PathFinderException;

public class PathFinderFactory extends it.polito.dp2.RNS.lab2.PathFinderFactory{

	RnsReader monitor;
	public PathFinderFactory(RnsReader monitor) {
		this.monitor = monitor;
	}
	
	@Override
	public PathFinder newPathFinder() throws PathFinderException {
		it.polito.dp2.RNS.sol2.PathFinder test=null;
		try {
			 test = new it.polito.dp2.RNS.sol2.PathFinder(monitor);
		} catch (RnsReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//throw new PathFinderException();
		}
		return test;
	}

}
