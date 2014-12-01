# Simple Http Server Written in Clojure

## Requirements 
Leiningen 2.5 is required in order to run the server. Installation instructions can be found [here](http://leiningen.org/)

Cob Spec test suite is required to run the acceptance tests. The test suite and instructions can be found [here](https://github.com/8thlight/cob_spec)

## Initial Setup
1. Clone repository into directory of your choosing
2. cd into the clojure-http-server directory
3. run $lein deps to install dependencies

## Running the server
This server is configured to meet the specifications of the Cob Spec test suite, and all that is need to run it is the command:
```
$ lein run
```
## Cob Spec Setup
Cob spec will require a minimal amount of setup, and for this server it requires
that you set the public directory to the public directory in the
clojure-cob-spec server directory. Once cob spec is set up, you can run the
suite against the running server by clicking on the suite button that is on the fitnesse
page.
