package sample.clientapp;

import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import sample.config.ClientAppConfiguration;
import sample.jwt.WellKnownInfo;

public class WellKnownGetter {
	private ClientAppConfiguration clientConfig;
	private RestTemplate restTemplate;

	public WellKnownGetter(ClientAppConfiguration clientConfig, RestTemplate restTemplate) {
		this.clientConfig = clientConfig;
		this.restTemplate = restTemplate;
	}

	public WellKnownInfo getWellKnown() {
		RequestEntity<?> req = new RequestEntity<>(null, null, HttpMethod.GET,
				URI.create(clientConfig.getAuthserverUrl()));
		WellKnownInfo wellKnownInfo  = null;
		try {
			ResponseEntity<WellKnownInfo> res = restTemplate.exchange(req, WellKnownInfo.class);
			wellKnownInfo = res.getBody();
		} catch (HttpClientErrorException e) {
			System.out.println("!! response code=" + e.getStatusCode());
			System.out.println(e.getResponseBodyAsString());
		}
		return wellKnownInfo;
	}
}
