package org.example;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class RepresentationCalculator {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar XESLogParser.jar <path_to_xes_file>");
            return;
        }

        String filePath = args[0];
        try {
            Document doc = parseDocument(filePath);
            int[] traceInfo = processTraces(doc);
            double[] results = calculateRepresentation(traceInfo);
            printResults(traceInfo, results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Document parseDocument(String filePath) throws Exception {
        File inputFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static int[] processTraces(Document doc) {
        NodeList traceList = doc.getElementsByTagName("trace");
        int totalTraces = traceList.getLength();
        int totalEvents = 0;
        int totalUniqueEvents = 0;

        for (int i = 0; i < totalTraces; i++) {
            Node traceNode = traceList.item(i);
            if (traceNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList eventList = ((Element) traceNode).getElementsByTagName("event");
                totalEvents += eventList.getLength();

                Set<String> uniqueEventNames = new HashSet<>();
                for (int j = 0; j < eventList.getLength(); j++) {
                    Node eventNode = eventList.item(j);
                    if (eventNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eventElement = (Element) eventNode;
                        NodeList stringList = eventElement.getElementsByTagName("string");
                        for (int k = 0; k < stringList.getLength(); k++) {
                            Node stringNode = stringList.item(k);
                            if (stringNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element stringElement = (Element) stringNode;
                                String key = stringElement.getAttribute("key");
                                if ("concept:name".equals(key)) {
                                    uniqueEventNames.add(stringElement.getAttribute("value"));
                                }
                            }
                        }
                    }
                }
                totalUniqueEvents += uniqueEventNames.size();
            }
        }
        return new int[]{totalTraces, totalEvents, totalUniqueEvents};
    }

    private static double[] calculateRepresentation(int[] traceInfo) {
        int totalTraces = traceInfo[0];
        int totalEvents = traceInfo[1];
        int totalUniqueEvents = traceInfo[2];

        double averageEventsPerTrace = totalTraces > 0 ? (double) totalEvents / totalTraces : 0;
        double averageUniqueEventsPerTrace = totalTraces > 0 ? (double) totalUniqueEvents / totalTraces : 0;
        double representation = averageEventsPerTrace > 0 ? averageUniqueEventsPerTrace / averageEventsPerTrace : 0;

        return new double[]{averageEventsPerTrace, averageUniqueEventsPerTrace, representation};
    }

    private static void printResults(int[] traceInfo, double[] results) {
        System.out.println("Total Traces: " + traceInfo[0]);
        System.out.println("Total Events: " + traceInfo[1]);
        System.out.println("Average Number of Events per Trace: " + results[0]);
        System.out.println("Total Unique Events Across All Traces: " + traceInfo[2]);
        System.out.println("Average Number of Event Classes per Trace: " + results[1]);
        System.out.println("Representation: " + results[2]);
    }
}
