package org.cloudfoundry.promregator.cfaccessor;

import org.cloudfoundry.promregator.internalmetrics.InternalMetrics;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class CFAccessorCacheSpringApplication {

	@Bean
	public InternalMetrics internalMetrics() {
		return Mockito.mock(InternalMetrics.class);
	}
	
	@Bean
	public CFAccessor parentMock() {
		CFAccessor mock = Mockito.mock(CFAccessor.class);
		Mockito.when(mock.retrieveOrgId("dummy")).thenReturn(Mockito.mock(Mono.class));
		Mockito.when(mock.retrieveSpaceId("dummy1", "dummy2")).thenReturn(Mockito.mock(Mono.class));
		Mockito.when(mock.retrieveAllApplicationIdsInSpace("dummy1", "dummy2")).thenReturn(Mockito.mock(Mono.class));
		Mockito.when(mock.retrieveSpaceSummary("dummy")).thenReturn(Mockito.mock(Mono.class));
		return mock;
	}
	
	@Bean
	public CFAccessorCache subject(@Qualifier("parentMock") CFAccessor parentMock) {
		return new CFAccessorCache(parentMock);
	}
	
}
