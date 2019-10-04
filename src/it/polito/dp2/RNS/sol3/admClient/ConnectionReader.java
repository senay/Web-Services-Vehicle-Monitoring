package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.sol3.jaxb.Connection;

public class ConnectionReader implements it.polito.dp2.RNS.ConnectionReader{
	private Connection conn = new Connection();
	public ConnectionReader(Connection conn) {
		this.conn=conn;
	}
	@Override
	public PlaceReader getFrom() {
		return null;//conn.getFrom();
	}

	@Override
	public PlaceReader getTo() {
		return null;//conn.getTo();
	}

}
