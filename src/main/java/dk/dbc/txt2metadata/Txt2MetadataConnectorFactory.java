/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.txt2metadata;

import dk.dbc.httpclient.HttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.client.Client;

@ApplicationScoped
public class Txt2MetadataConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(Txt2MetadataConnectorFactory.class);

    public static Txt2MetadataConnector create(String txt2metadataBaseUrl) {
        final Client client = HttpClient.newClient(new ClientConfig().register(new JacksonFeature()));
        LOGGER.info("Creating Txt2MetadataConnector for: {}", txt2metadataBaseUrl);
        return new Txt2MetadataConnector(client, txt2metadataBaseUrl);
    }

    public static Txt2MetadataConnector create(String txt2metadataBaseUrl, Txt2MetadataConnector.TimingLogLevel level) {
        final Client client = HttpClient.newClient(new ClientConfig().register(new JacksonFeature()));
        LOGGER.info("Creating Txt2MetadataConnector for: {}", txt2metadataBaseUrl);
        return new Txt2MetadataConnector(client, txt2metadataBaseUrl, level);
    }

    @Inject
    @ConfigProperty(name = "TXT2METADATA_URL")
    private String baseURL;

    @Inject
    @ConfigProperty(name = "TXT2METADATA_TIMING_LOG_LEVEL", defaultValue = "INFO")
    private String level;

    Txt2MetadataConnector connector;

    @PostConstruct
    public void initializeConnector() {
        connector = Txt2MetadataConnectorFactory.create(baseURL, Txt2MetadataConnector.TimingLogLevel.valueOf(level));
    }

    @Produces
    public Txt2MetadataConnector getInstance() {
        return connector;
    }

    @PreDestroy
    public void tearDownConnector() {
        connector.close();
    }
}
