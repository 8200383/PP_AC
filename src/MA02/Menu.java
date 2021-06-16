package MA02;

import Core.*;
import SensorDataInput.ImportationReport;
import SensorDataInput.JsonImporter;
import edu.ma02.core.enumerations.AggregationOperator;
import edu.ma02.core.enumerations.Parameter;
import edu.ma02.core.exceptions.CityException;
import edu.ma02.core.interfaces.*;
import edu.ma02.io.interfaces.IOStatistics;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/*
 * Nome: Micael André Cunha Dias
 * Número: 8200383
 * Turma: LEI1T4
 *
 * Nome: Hugo Henrique Almeida Carvalho
 * Número: 8200590
 * Turma: LEI1T3
 */
public class Menu {

    private static City city = null;
    private static ImportationReport report = null;

    public Menu() {
    }

    public static void enterLoop() {
        try (Scanner scanner = new Scanner(System.in)) {
            int opt;
            do {
                System.out.print("""
                        ========Menu========
                        0. Create a new City
                        1. Import Data
                        2. View Imported Data
                        3. View Exceptions
                        4. View Measurements By Station
                        5. View Measurements By Sensor
                        6. View Measurements By Station Between Dates
                        7. View Measurements By Sensor Between Dates
                        8. Visualizar Relatório de Importação
                        9. Exit
                        >\040""");

                // \040 means trim whitespaces
                while (!scanner.hasNextInt()) {
                    System.out.print("""
                            Invalid Input!
                            >\040""");
                    scanner.next();
                }

                opt = scanner.nextInt();
                System.out.println("You entered: " + opt);

                switch (opt) {
                    case 0 -> city = (City) createCity(scanner, city);
                    case 1 -> report = importJsonData(scanner, city);
                    case 2 -> showAll(city);
                    case 3 -> showExceptions(report);
                    case 4, 6, 5, 7, 8 -> {
                        if (city == null) {
                            System.out.println("You should create a city first!");
                            break;
                        }

                        if (report == null) {
                            System.out.println("You should import data first!");
                            break;
                        }

                        IStatistics[] statistics = readMeasurements(scanner, city, opt);
                        for (IStatistics iStatistic : statistics) {
                            if (iStatistic instanceof Statistic statistic) {
                                System.out.println(statistic.getDescription() + " " + statistic.getValue());
                            }
                        }
                    }
                    case 9 -> System.out.println("Quiting...");
                }
            } while (opt != 9);
        }
    }

    private static String[] addString(String[] srcArray, String element) {
        String[] destArray = new String[srcArray.length + 1];
        System.arraycopy(srcArray, 0, destArray, 0, srcArray.length);

        destArray[destArray.length - 1] = element;
        return destArray;
    }

    private static void grow() {
        ChartInfo[] destArray = new ChartInfo[chartInfos.length * 2];
        System.arraycopy(chartInfos, 0, destArray, 0, chartInfoCount);
        chartInfos = destArray;
    }

    private static void addChartInfo(ChartInfo chartInfo) {
        if (chartInfos.length == chartInfoCount) {
            grow();
        }

        chartInfos[chartInfoCount++] = chartInfo;
    }

    private static AggregationOperator selectAggregationOperator(Scanner scanner) {
        AggregationOperator operator = null;

        int opt;
        do {
            System.out.println("========Chose an Aggregation Operator========");
            int n = 1;
            for (AggregationOperator ao : AggregationOperator.values()) {
                System.out.println(n + ". " + ao.toString());
                n++;
            }
            System.out.println("0. Back to main menu");

            while (!scanner.hasNextInt()) {
                System.out.print("""
                        Invalid Input!
                        >\040""");
                scanner.next();
            }

            opt = scanner.nextInt();
            System.out.println("You entered: " + opt);

            switch (opt) {
                case 1 -> operator = AggregationOperator.AVG;
                case 2 -> operator = AggregationOperator.COUNT;
                case 3 -> operator = AggregationOperator.MAX;
                case 4 -> operator = AggregationOperator.MIN;
                case 0 -> {
                    System.out.println("Back to Main Menu...");
                    enterLoop();
                }
            }
        } while (operator == null);

        return operator;
    }

    private static Parameter selectParameter(Scanner scanner) {
        Parameter parameter = null;

        int opt;
        do {
            System.out.println("========Chose a Parameter========");
            int n = 1;
            for (Parameter ao : Parameter.values()) {
                System.out.println(n + ". " + ao.toString());
                n++;
            }
            System.out.println("0. Back to main menu");

            while (!scanner.hasNextInt()) {
                System.out.print("""
                        Invalid Input!
                        >\040""");
                scanner.next();
            }

            opt = scanner.nextInt();
            System.out.println("You entered: " + opt);

            switch (opt) {
                case 1 -> parameter = Parameter.NO2;
                case 2 -> parameter = Parameter.O3;
                case 3 -> parameter = Parameter.PM2_5;
                case 4 -> parameter = Parameter.PM10;
                case 5 -> parameter = Parameter.SO2;
                case 6 -> parameter = Parameter.C6H6;
                case 7 -> parameter = Parameter.CO;
                case 8 -> parameter = Parameter.LAEQ;
                case 9 -> parameter = Parameter.PA;
                case 10 -> parameter = Parameter.TEMP;
                case 11 -> parameter = Parameter.RU;
                case 12 -> parameter = Parameter.VD;
                case 13 -> parameter = Parameter.VI;
                case 14 -> parameter = Parameter.HM;
                case 15 -> parameter = Parameter.PC;
                case 16 -> parameter = Parameter.RG;
                case 0 -> {
                    System.out.println("Back to Main Menu...");
                    enterLoop();
                }
            }
        } while (parameter == null);

        return parameter;
    }

    private static IStatistics[] readMeasurements(Scanner scanner, ICityStatistics city, int previousOption) {
        AggregationOperator operator = selectAggregationOperator(scanner);
        Parameter parameter = selectParameter(scanner);
        IStatistics[] statistics = null;

        switch (previousOption) {
            case 4 -> statistics = city.getMeasurementsByStation(operator, parameter);
            case 5 -> {
                System.out.print("Insert the name of the station: ");
                statistics = city.getMeasurementsBySensor(scanner.next(), operator, parameter);
            }
            case 6 -> statistics = city.getMeasurementsByStation(
                    operator,
                    parameter,
                    readDate(scanner, "Start Date"),
                    readDate(scanner, "End Date")
            );
            case 7 -> {
                System.out.print("Insert the name of the station: ");
                statistics = city.getMeasurementsBySensor(
                        scanner.next(),
                        operator,
                        parameter,
                        readDate(scanner, "Start Date"),
                        readDate(scanner, "End Date")
                );
            }
        }

        return statistics;
    }

    private static ICity createCity(Scanner scanner, ICity existingCity) {
        if (existingCity != null) {
            System.out.print("""
                    Caution! You already created a city
                    This operation will wipe out the city,
                    by creating a new one.
                                                        
                    Do you wanna continue?
                    1. Yes
                    0. No
                    >\040""");
            if (scanner.nextInt() == 0) {
                return existingCity;
            }
        }
        System.out.println("Enter the name of city");
        return new City(scanner.next());
    }

    private static ImportationReport importJsonData(Scanner scanner, ICity city) {
        ImportationReport report = new ImportationReport();
        JsonImporter jsonImporter = new JsonImporter();

        try {
            System.out.print("Enter the path of the Json file: ");
            report = (ImportationReport) jsonImporter.importData(city, scanner.next());
        } catch (CityException ce) {
            String cause = "You should create a city first";
            report.addException(ce.getStackTrace(), cause);
            System.out.println(cause);
        } catch (IOException e) {
            String cause = "Invalid file path";
            report.addException(e.getStackTrace(), cause);
            System.out.println(cause);
        }

        return report;
    }

    private static void showAll(ICity city) {
        if (city == null) {
            System.out.println("You should create a city first!");
            return;
        }
        for (IStation iStation : city.getStations()) {
            if (iStation instanceof Station station) {
                System.out.println(station);

                for (ISensor iSensor : station.getSensors()) {
                    if (iSensor instanceof Sensor sensor) {
                        System.out.println(sensor);

                        for (IMeasurement iMeasurement : sensor.getMeasurements()) {
                            if (iMeasurement instanceof Measurement measurement) {
                                System.out.println(measurement);
                            }
                        }

                        System.out.println();
                    }
                }
            }
        }
    }

    private static void showExceptions(IOStatistics ioStatistics) {
        if (ioStatistics == null) {
            System.out.println("You should import data first!");
            return;
        }

        for (String e : ioStatistics.getExceptions()) {
            if (e != null) {
                System.out.println(e);
            }
        }
    }

    private static LocalDateTime readDate(Scanner scanner, String hint) {
        System.out.println("Choose a " + hint + " in the following format yyyy-MM-dd HH:mm");
        String startDate = scanner.next();

        // TODO o que acontece se n for no formato que ele quer
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println("Choose a " + hint + " in the following format yyyy-MM-dd HH:mm");
        String startDate = scanner.next();
        return LocalDateTime.parse(startDate, formatter);
    }
}
