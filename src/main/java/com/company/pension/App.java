package com.company.pension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        List<Employee> employees = seedData();

        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list":
                printAllEmployees(employees);
                break;
            case "upcoming":
                printQuarterlyUpcomingEnrollees(employees);
                break;
            default:
                printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar employee-pension-planner.jar [list|upcoming]");
    }

    private static void printAllEmployees(List<Employee> employees) {
        List<Employee> sorted = employees.stream()
                .sorted(Comparator
                        .comparing(Employee::getYearlySalary, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                        .thenComparing(Employee::getLastName, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        writeAsJson(sorted);
    }

    private static void printQuarterlyUpcomingEnrollees(List<Employee> employees) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayNextQuarter = firstDayOfNextQuarter(today);
        LocalDate lastDayNextQuarter = lastDayOfQuarter(firstDayNextQuarter);

        List<Employee> filtered = employees.stream()
                .filter(e -> e.getPensionPlan() == null)
                .filter(e -> {
                    LocalDate threeYearAnniversary = e.getEmploymentDate().plusYears(3);
                    return (threeYearAnniversary.isEqual(firstDayNextQuarter) || threeYearAnniversary.isAfter(firstDayNextQuarter))
                            && (threeYearAnniversary.isEqual(lastDayNextQuarter) || threeYearAnniversary.isBefore(lastDayNextQuarter));
                })
                .sorted(Comparator.comparing(Employee::getEmploymentDate).reversed())
                .collect(Collectors.toList());
        writeAsJson(filtered);
    }

    private static LocalDate firstDayOfNextQuarter(LocalDate date) {
        int currentQuarter = (date.getMonthValue() - 1) / 3 + 1;
        int nextQuarter = currentQuarter == 4 ? 1 : currentQuarter + 1;
        int year = currentQuarter == 4 ? date.getYear() + 1 : date.getYear();
        Month startMonth = Month.of((nextQuarter - 1) * 3 + 1);
        return LocalDate.of(year, startMonth, 1);
    }

    private static LocalDate lastDayOfQuarter(LocalDate firstDayOfQuarter) {
        Month start = firstDayOfQuarter.getMonth();
        Month end = Month.of(start.getValue() + 2);
        YearMonth ym = YearMonth.of(firstDayOfQuarter.getYear(), end);
        return ym.atEndOfMonth();
    }

    private static void writeAsJson(Object value) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        try {
            String json = mapper.writeValueAsString(value);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize to JSON: " + e.getMessage());
        }
    }

    private static List<Employee> seedData() {
        List<Employee> list = new ArrayList<>();
        // 1: Daniel Agar, yearly 105,945.50, Employment 2023-01-17, Plan EX1089, no enrollment date, $100.00
        list.add(new Employee(
                1L, "Daniel", "Agar", LocalDate.parse("2023-01-17"), new BigDecimal("105945.50"),
                new PensionPlan("EX1089", null, new BigDecimal("100.00"))
        ));

        // 2: Benard Shaw, 197,750.00, Employment 2022-09-03, Enrollment 2025-09-03, no plan ref, contribution null
        // Interpreting row: enrolled (has enrollment date) but missing plan ref? To respect constraints, a plan must exist if enrolled.
        list.add(new Employee(
                2L, "Benard", "Shaw", LocalDate.parse("2022-09-03"), new BigDecimal("197750.00"),
                new PensionPlan("BN0001", LocalDate.parse("2025-09-03"), null)
        ));

        // 3: Carly Agar, 842,000.75, Employment 2014-05-16, Enrollment 2017-05-17, Plan SM2307, $1,555.50
        list.add(new Employee(
                3L, "Carly", "Agar", LocalDate.parse("2014-05-16"), new BigDecimal("842000.75"),
                new PensionPlan("SM2307", LocalDate.parse("2017-05-17"), new BigDecimal("1555.50"))
        ));

        // 4: Wesley Schneider, 74,500.00, Employment 2023-07-21, no plan
        list.add(new Employee(
                4L, "Wesley", "Schneider", LocalDate.parse("2023-07-21"), new BigDecimal("74500.00"),
                null
        ));

        // 5: Anna Wiltord, 85,750.00, Employment 2023-03-15, no plan
        list.add(new Employee(
                5L, "Anna", "Wiltord", LocalDate.parse("2020-03-15"), new BigDecimal("85750.00"),
                null
        ));

        // 6: Yosef Tesfalem, 100,000.00, Employment 2024-10-31, no plan
        list.add(new Employee(
                6L, "Yosef", "Tesfalem", LocalDate.parse("2020-10-31"), new BigDecimal("100000.00"),
                null
        ));

        // 7: New employee in 2022 Q4 to be eligible next quarter (3-year anniversary in Q4 2025)
        list.add(new Employee(
                7L, "Quentin", "Reeves", LocalDate.parse("2022-11-15"), new BigDecimal("92000.00"),
                null
        ));

        // Additional employees eligible next quarter (employment dates in 2022 Q4), all without plans
        list.add(new Employee(
                8L, "Lana", "Khan", LocalDate.parse("2022-10-05"), new BigDecimal("97000.00"),
                null
        ));
        list.add(new Employee(
                9L, "Marco", "Diaz", LocalDate.parse("2022-10-20"), new BigDecimal("88000.00"),
                null
        ));
        list.add(new Employee(
                10L, "Priya", "Singh", LocalDate.parse("2022-11-30"), new BigDecimal("102500.00"),
                null
        ));
        list.add(new Employee(
                11L, "Ethan", "Brown", LocalDate.parse("2022-12-01"), new BigDecimal("76000.00"),
                null
        ));
        list.add(new Employee(
                12L, "Zara", "Lopez", LocalDate.parse("2022-12-31"), new BigDecimal("93500.00"),
                null
        ));

        return list;
    }
}
