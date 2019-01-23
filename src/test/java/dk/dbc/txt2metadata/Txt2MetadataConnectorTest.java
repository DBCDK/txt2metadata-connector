/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.txt2metadata;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.httpclient.HttpClient;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Txt2MetadataConnectorTest {

    private static WireMockServer wireMockServer;
    private static String wireMockHost;

    final static Client CLIENT = HttpClient.newClient(new ClientConfig()
            .register(new JacksonFeature()));
    static Txt2MetadataConnector connector;

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(options().dynamicPort()
                .dynamicHttpsPort());
        wireMockServer.start();
        wireMockHost = "http://localhost:" + wireMockServer.port();
        configureFor("localhost", wireMockServer.port());
    }

    @BeforeAll
    static void setConnector() {
        connector = new Txt2MetadataConnector(CLIENT, wireMockHost, Txt2MetadataConnector.TimingLogLevel.INFO);
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void testGetMetadataForArticle() throws Txt2MetadataConnectorException {
        String articleId = "e70a69a1";

        List<Txt2Metadata> txt2MetadataList = connector.getMetadataForArticle(articleId);

        assertThat(txt2MetadataList.size(), is(2));
        assertThat(txt2MetadataList.get(0).getValue(), is("652*m97.8"));
        assertThat(txt2MetadataList.get(0).getScore(), is(2));
        assertThat(txt2MetadataList.get(0).getType(), is("dk5"));
        assertThat(txt2MetadataList.get(1).getValue(), is("610*aJydsk Boldspil-Union*2ARTB"));
        assertThat(txt2MetadataList.get(1).getScore(), is(1));
        assertThat(txt2MetadataList.get(1).getType(), is("emne"));
    }

    @Test
    void testGetMetadataForArticles() throws Txt2MetadataConnectorException {
        List<String> articleIds = Arrays.asList("e70a69a1", "e70a7341");

        List<Txt2Metadata> txt2MetadataList = connector.getMetadataForArticles(articleIds);

        assertThat(txt2MetadataList.size(), is(2));
        assertThat(txt2MetadataList.get(0).getValue(), is("652*m97.8"));
        assertThat(txt2MetadataList.get(0).getScore(), is(2));
        assertThat(txt2MetadataList.get(0).getType(), is("dk5"));
        assertThat(txt2MetadataList.get(1).getValue(), is("666*fvelfærdsstaten"));
        assertThat(txt2MetadataList.get(1).getScore(), is(1));
        assertThat(txt2MetadataList.get(1).getType(), is("emne"));
    }


    @Test
    void testGetMetadataForText() throws Txt2MetadataConnectorException {
        String text = "Det er en ældre herre, der i ' The Mule' har sat sig bag rattet i en lidt yngre, støvet pickup-truck af mærket Ford. Virkelighedens Leo Sharp var 88 år, da han blev narkokurér for Sinaloa-kartellet. Sharp er i filmversionen omdøbt til Earl Stone og spilles af Clint Eastwood, der også er blevet 88. Så det har ikke krævet den vilde method acting.";

        List<Txt2Metadata> txt2MetadataList = connector.getMetadataForText(text);

        assertThat(txt2MetadataList.size(), is(3));
        assertThat(txt2MetadataList.get(0).getValue(), is("630*ftrash metal*2ARTB"));
        assertThat(txt2MetadataList.get(0).getScore(), is(1));
        assertThat(txt2MetadataList.get(0).getType(), is("emne"));
        assertThat(txt2MetadataList.get(1).getValue(), is("630*aArtillery (rockgruppe)"));
        assertThat(txt2MetadataList.get(1).getScore(), is(1));
        assertThat(txt2MetadataList.get(1).getType(), is("emne"));
        assertThat(txt2MetadataList.get(2).getValue(), is("630*ferkendelsesteori*2ARTB"));
        assertThat(txt2MetadataList.get(2).getScore(), is(1));
        assertThat(txt2MetadataList.get(2).getType(), is("emne"));
    }

}
