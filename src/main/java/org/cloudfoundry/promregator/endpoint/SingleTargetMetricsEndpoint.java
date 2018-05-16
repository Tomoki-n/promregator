package org.cloudfoundry.promregator.endpoint;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.promregator.config.Target;
import org.cloudfoundry.promregator.rewrite.CFMetricFamilySamplesEnricher;
import org.cloudfoundry.promregator.scanner.Instance;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.common.TextFormat;

@RestController
@Scope(value=WebApplicationContext.SCOPE_REQUEST) // see also https://github.com/promregator/promregator/issues/51
@RequestMapping(SingleTargetMetricsEndpoint.ENDPOINT_PATH+"/{applicationId}/{instanceNumber}")
public class SingleTargetMetricsEndpoint extends AbstractMetricsEndpoint {
	public static final String ENDPOINT_PATH = "/singleTargetMetrics";
	
	private Instance instance;
	
	@RequestMapping(method = RequestMethod.GET, produces=TextFormat.CONTENT_TYPE_004)
	public String getMetrics(
			@PathVariable String applicationId, 
			@PathVariable String instanceNumber
			) {
		
		String instanceId = String.format("%s:%s", applicationId, instanceNumber);
		
		return this.handleRequest( discoveredApplicationId -> applicationId.equals(discoveredApplicationId)
		, instance -> {
			if (instance.getInstanceId().equals(instanceId)) {
				return true;
			}
			
			return false;
		});
	}

	@Override
	protected boolean isIncludeGlobalMetrics() {
		// NB: This is done by PromregatorMetricsEndpoint in this scenario instead.
		return false;
	}

	@Override
	protected void handleScrapeDuration(CollectorRegistry requestRegistry, Duration duration) {
		Gauge scrape_duration = Gauge.build("promregator_scrape_duration_seconds", "Duration in seconds indicating how long scraping of all metrics took")
				.labelNames(CFMetricFamilySamplesEnricher.getEnrichingLabelNames())
				.register(requestRegistry);
		
		Target t = this.instance.getTarget();
		CFMetricFamilySamplesEnricher enricher = new CFMetricFamilySamplesEnricher(t.getOrgName(), t.getSpaceName(), t.getApplicationName(), this.instance.getInstanceId());
		
		List<String> labelValues = enricher.getEnrichedLabelValues(new ArrayList<String>(0));
		scrape_duration.labels(labelValues.toArray(new String[0])).set(duration.toMillis() / 1000.0);
	}
}
