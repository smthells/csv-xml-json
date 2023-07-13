import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String csvFileName = "data.csv";
        String xmlFileName = "data.xml";
        String firstJsonFileName = "data.json";
        String secondJsonFileName = "data2.json";

        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1, "John", "Smith", "USA", 25));
        employees.add(new Employee(2, "Ivan", "Petrov", "RU", 23));
        createCsvFile(employees, csvFileName);

        List<Employee> list = parseCSV(columnMapping, csvFileName);
        String firstJson = listToJson(list);
        writeString(firstJson, firstJsonFileName);

        List<Employee> list2 = parseXML(xmlFileName);
        String secondJson = listToJson(list2);
        writeString(secondJson, secondJsonFileName);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader).withMappingStrategy(strategy).build();
            list = csv.parse();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    private static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(list, listType);
    }

    private static void writeString(String json, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createCsvFile(List<Employee> employees, String csvFileName) {
        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(Employee.class);
        strategy.setColumnMapping("id", "firstName", "lastName", "country", "age");
        try (Writer writer = new FileWriter(csvFileName)) {
            StatefulBeanToCsv<Employee> sbc =
                    new StatefulBeanToCsvBuilder<Employee>(writer).withMappingStrategy(strategy)
                            .build();
            sbc.write(employees);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            System.out.println(e.getMessage());
        }
    }

    private static List<Employee> parseXML(String fileName) {
        List<Employee> list = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node_ = nodeList.item(i);
                if (Node.ELEMENT_NODE == node_.getNodeType()) {
                    Element employeeElement = (Element) node_;
                    int id = Integer.parseInt(getElementTextByTagName(employeeElement, "id"));
                    String firstName = getElementTextByTagName(employeeElement, "firstName");
                    String lastName = getElementTextByTagName(employeeElement, "lastName");
                    String country = getElementTextByTagName(employeeElement, "country");
                    int age = Integer.parseInt(getElementTextByTagName(employeeElement, "age"));
                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    list.add(employee);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    private static String getElementTextByTagName(Element employeeElement, String tag) {
        NodeList nodeList = employeeElement.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            Element tagElement = (Element) nodeList.item(0);
            return tagElement.getTextContent();
        }
        return "";
    }
}

