# Consoles for virtual machines hosted on cloudspin will not work

### Cloudspin  

[Cloudspin](https://cloudspin.me/) is a free public cloud provider based on oVirt. The hosts on Cloudspin are in a private network.
Cloudspin only provides websocket proxy to get to them which is used by HTML5 clients.

### Consoles supported by moVirt :  

bVNC and aSPICE are the two consoles that moVirt supports at the moment barring exception to SPICE secured connections.   

### Why consoles won't work :

The web clients (noVNC and SPICE-HTML5) need websocket proxy to connect to the console.
The websocket proxy requires a specific ticket signed by the private key of the engine to establish the connection.
Generating the ticket on the frontend (e.g. moVirt) is not an issue, but to sign it we need support on the REST API.
This support for signing the ticket is not there in oVirt REST API 3.5 and lower.
Also, bVNC console does not support any proxy resolution to communicate with the hosts in private network.
SPICE, on the other hand, does support the SPICE PROXY but aSPICE does not expose it.

So in the current scenario, consoles for virtual machines hosted on cloudspin won’t work.
Similar issue can be observed if the hosts are on a different network than the one one which the moVirt is.
