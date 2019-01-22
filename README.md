# txt2metadata connector

JAR library for getting metadata from infomedia articles. 

### Usage

In pom.xml add this dependency:

    <groupId>dk.dbc</groupId>
    <artifactId>txt2metadata-connector</artifactId>
    <version>1.0-SNAPSHOT</version>

In your EJB add the following inject:

    @Inject
    private Txt2MetadataConnectorFactory txt2MetadataConnectorFactory;

You must have the following environment variables in your deployment:

    TXT2METADATA_URL

To get Txt2MetaData for Infomedia article(s):
    
    Txt2MetadataConnector txt2MetadataConnector = txt2MetadataConnectorFactory.getInstance();

    List<Txt2MetaData> data = txt2MetadataConnector.getMetaDataForArticle(articleId);
    List<Txt2MetaData> data = txt2MetadataConnector.getMetaDataForArticles(articleIds);
    List<Txt2MetaData> data = txt2MetadataConnector.getMetaDataForText("text");

