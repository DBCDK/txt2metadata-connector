/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.txt2metadata;

public class Txt2MetaDataConnectorTestWireMockRecorder {

        /*
        Steps to reproduce wiremock recording:

        * Start standalone runner
            java -jar wiremock-standalone-{WIRE_MOCK_VERSION}.jar --proxy-all="{INFOMEDIA_BASE_URL}" --record-mappings --verbose

        * Run the main method of this class

        * Replace content of src/test/resources/{__files|mappings} with that produced by the standalone runner
     */

    public static void main(String[] args) throws Txt2MetaDataConnectorException {
        Txt2MetaDataConnectorTest.connector = new Txt2MetadataConnector(
                Txt2MetaDataConnectorTest.CLIENT, "http://localhost:9090");
        final Txt2MetaDataConnectorTest InfomediaConnectorTest = new Txt2MetaDataConnectorTest();
        allTests(InfomediaConnectorTest);
    }

    private static void allTests(Txt2MetaDataConnectorTest connectorTest)
            throws Txt2MetaDataConnectorException {
        connectorTest.testGetMetaDataForArticle();
        connectorTest.testGetMetaDataForArticles();
        connectorTest.testGetMetaDataForText();
    }

}
