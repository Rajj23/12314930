# Vehicle Scheduling Evidence

Save endpoint output screenshots for the assessment in this folder.

Run the service with a valid token:

```powershell
$env:LOG_AUTH_TOKEN="<provided-pre-test-token>"
cd maintence_scheduler
.\mvnw.cmd spring-boot:run
```

Then capture:

```text
GET http://localhost:8080/vehicle-scheduling/result/{depotId}
```
