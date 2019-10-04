package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.lab3.AdmClient;
import it.polito.dp2.RNS.lab3.AdmClientException;

public class AdmClientFactory  extends it.polito.dp2.RNS.lab3.AdmClientFactory{

	@Override
	public AdmClient newAdmClient() throws AdmClientException {
		return new it.polito.dp2.RNS.sol3.admClient.admClient();
	}
}
