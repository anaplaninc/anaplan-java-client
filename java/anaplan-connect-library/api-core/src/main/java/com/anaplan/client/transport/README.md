Using custom transport-providers
================================

Provides mechanisms for communicating (normally via HTTP) with an Anaplan API service server.

The primary purpose of this interface is to cleanly abstract over third-party library dependencies, allowing alternatives to be developed if necessary.

An alternative can be provided by implementing the TransportProvider interface. An instance of a class implementing this interface can then be given to the Service object in the setup phase by calling setTransportProvider.

