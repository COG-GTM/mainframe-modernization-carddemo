# COACTUPC account update service

Spring Boot 2.7 service translating the CardDemo CAUP (`COACTUPC`) account update flow.

```bash
mvn -q test
mvn -q package -DskipTests
java -jar target/coactupc-service-1.0.0.jar
```

Endpoints:

* `GET /api/v1/accounts/{acctId}` fetches the account/customer/card context.
* `POST /api/v1/accounts/{acctId}/validate` performs the ENTER-key validation.
* `PUT /api/v1/accounts/{acctId}` performs the PF5 validation and update.

PF3 and PF12 are screen navigation actions in the original CICS program. PF12 is represented by a client re-issue of the GET request and is not a separate endpoint.
