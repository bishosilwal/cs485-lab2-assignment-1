## Employee Pension Planner (CLI)

### Build

```bash
mvn -B -DskipTests package
```

### Run

```bash
java -jar target/employee-pension-planner-1.0-SNAPSHOT.jar list
java -jar target/employee-pension-planner-1.0-SNAPSHOT.jar upcoming
```

### Features

- Prints all employees in JSON including `PensionPlan` if present, sorted by yearly salary (desc) then last name (asc).
- Prints Quarterly Upcoming Enrollees in JSON: employees without a plan whose 3-year anniversary falls within next quarter; sorted by employment date (desc).

### Tech

- Java 21, Maven, Jackson (databind + jsr310), Shade plugin.
- GitHub Actions CI builds on push and PR.


