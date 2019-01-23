/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.txt2metadata;

public class Txt2MetadataConnectorTestWireMockRecorder {

        /*
        Steps to reproduce wiremock recording:

        * Start standalone runner
            java -jar wiremock-standalone-{WIRE_MOCK_VERSION}.jar --proxy-all="{INFOMEDIA_BASE_URL}" --record-mappings --verbose

        * Run the main method of this class

        * Replace content of src/test/resources/{__files|mappings} with that produced by the standalone runner
     */

    public static void main(String[] args) throws Txt2MetadataConnectorException {
        Txt2MetadataConnectorTest.connector = new Txt2MetadataConnector(
                Txt2MetadataConnectorTest.CLIENT, "http://localhost:9090");
        final Txt2MetadataConnectorTest InfomediaConnectorTest = new Txt2MetadataConnectorTest();
        allTests(InfomediaConnectorTest);
    }

    private static void allTests(Txt2MetadataConnectorTest connectorTest)
            throws Txt2MetadataConnectorException {
        connectorTest.testGetMetadataForArticle();
        connectorTest.testGetMetadataForArticles();
        connectorTest.testGetMetadataForText();
    }

}
