# anaplan-java-client

## DOCUMENTATION

The Anaplan Java Client documentation consists of:

1. "Anaplan API Guide", which shows the workflow of making a series of API calls for import, export, delete, and process. See https://community.anaplan.com/anapedia/integrations/data-integration/anaplan-api-guide

2. For those who already understand the workflow, the API Reference at "Anaplan REST API for Integration" at https://anaplanbulkapi20.docs.apiary.io

# Developers

## SDK

The SDK provides the `Service` class, representing a connection to the Anaplan Connect server, along with associated classes representing accessible resources on the server.
Software using this library should conform to the following pattern:

- A new Service object is instantiated for the service endpoint.
- The Service has many necessary properties set. In particular, service credentials need to be provided.
- The Service object can be used to access Workspace objects, from which Model objects can be accessed, and so on. These are all tied to that Service object.
- When all interaction has been completed, the Service.close() method should be called to release any resources.

The SDK also provides:

- A mechanism to use custom [Transport-providers](src/main/java/com/anaplan/client/transport/README.md) (default: ApacheHttpProvider) for custom API communication.
- A mechanism to use custom [Serialization handlers](src/main/java/com/anaplan/client/serialization/README.md), to help serialize the data to and from Anaplan.

## Build from source

This is a standard Maven project, so to install, test and package do:

```
$ mvn clean install
$ mvn clean test
$ mvn clean package
```

## Deploy to Maven Repository (check with contributers)

To deploy to Maven Github repository at anaplaninc.github.io, do:

```
$ mvn clean deploy
```

Note: Requires a settings.xml file at location ~/.m2/ with contents as shown below and a Github access token. More info at: (https://github.com/settings/tokens):

```
<settings>
  <servers>
    <server>
      <id>github</id>
      <password><GITHUB PERSONAL ACCESS TOKEN></password>
    </server>
  </servers>
</settings>
```

## Update Javadoc Github-pages

To update the Github-pages (branch: gh-pages) site for this repository, with the latest Javadoc, do:

```
$ mvn javadoc:javadoc
$ cp -R target/site/apidocs/* doc/javadoc/.
$ git subtree push --prefix doc origin gh-pages
```

# Releases

## v1.4

Features:

- Use of the new and improved Anaplan v2.0 integration API
- Enabling customers to use certificates, obtained from public Certificate Authorities, for Anaplan authentication
- Enabling users to configure retry timeout and number of retries
- Configurable chunk sizes for imports, ranging from 1 to 50 MB
- Improved screen logging, including timestamp, classpath for debug lines, Linux process ID, and more.
- Better security and ease of querying using JDBCparameters moved to ”properties” file.

## v1.3.6

Features:

- Mavenized the Anaplan-Connect project, previously on Ant.
- Introduced Log4j.
- Removed cs/ C# code-base for Anaplan-Connect.
- doc/ folder cleanup.
- Fixed unit-tests using mocked API responses.
- Built using JDK 1.7.0_79
- Updated to use Github maven repo at anaplaninc.github.io

## v1.3.5

Bug Fixes as of 19-NOVEMBER-2015

- MOD-753: Anaplan Connect retrieves old or incomplete export file if the export fails to complete
- INTEGRA-699: When a value is blank/missing in the file, Anaplan Connect shifts the data to the left one column.
- BGTRK-10844: Anaplan Connect might hang waiting for task cancellation.
- INTEGRA-246: Remove doc and wadl from connect distribution build.
- RB-68: Change base url for UAT to fix naming convention.

NOTE: Use this for Informatica Connector v1.1.0
LICENSE See https://github.com/anaplaninc/anaplan-java-client/blob/master/java/LICENSE.txt
