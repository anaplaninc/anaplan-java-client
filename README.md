![Anaplan Connect](img/anaplan-connect-logo.png)

Anaplan Connect is a command-line client and an SDK that makes it easy to communicate with the Anaplan API to execute Import, Export, Delete, and Process actions.
Please read the [NOTICE.txt](NOTICE.txt) and [LICENSE.txt](LICENSE.txt) files before using Anaplan Connect, particularly if you intend to redistribute it.


# Documentation

- The [**Anaplan Connect Guide**](https://community.anaplan.com/anapedia/data-integration/anaplan-connect) provides information on installing and using Anaplan Connect from the command-line on Windows, UNIX, GNU/Linux or MacOS environments.
- The [**Anaplan API Guide**](https://community.anaplan.com/anapedia/integrations/data-integration/anaplan-api-guide) provides information useful to developers who want to access the API directly without using Anaplan Connect.
- The [**Anaplan API Reference**](http://docs.anaplan.apiary.io/) covers all Anaplan APIs.
- The **Javadoc documentation** is useful for Java developers wanting to make use of Anaplan Connect as a library component from their own code. The content is available at ```doc/javadoc```, so open ```doc/javadoc/index.html``` in a browser.


# Developers


## SDK

The SDK provides the ```Service``` class, representing a connection to the Anaplan Connect server, along with associated classes representing accessible resources on the server.
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
