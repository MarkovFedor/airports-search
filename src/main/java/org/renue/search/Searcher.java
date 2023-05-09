package org.renue.search;

import org.renue.config.Config;

import java.io.*;
import java.util.*;

public class Searcher {
    private final Config config;
    private RandomAccessFile fileReader;
    List<AirportSearchInformation> airports;

    public Searcher(Config config) throws FileNotFoundException {
        this.config = config;
        airports = new ArrayList<>();
        indexFile();
    }

    public void indexFile() throws FileNotFoundException {
        File file = new File(config.getFilePath());
        fileReader = new RandomAccessFile(file, "r");
        long pointer = 0;
        try {
            String currentLine = fileReader.readLine();
            while(currentLine != null) {
                currentLine = currentLine.replaceAll("\"", "");
                String[] lineSplited = currentLine.split(",");
                airports.add(new AirportSearchInformation(lineSplited[1], pointer));
                pointer = fileReader.getFilePointer();
                currentLine = fileReader.readLine();
            }
        } catch (IOException e) {
            System.out.println("error: не работает");
        }
        airports.sort(new Comparator<AirportSearchInformation>() {
            @Override
            public int compare(AirportSearchInformation o1, AirportSearchInformation o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }

    public ArrayList<String[]> search(String query) throws IOException {
        AirportSearchInformation searchAirport = new AirportSearchInformation(query);
        ArrayList<String[]> resultArrayList = new ArrayList<>();
        int start = Collections.binarySearch(airports, searchAirport, new Comparator<AirportSearchInformation>() {
            @Override
            public int compare(AirportSearchInformation o1, AirportSearchInformation o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        if (start < 0)
            start = -start - 1;
        int end = start;
        String currentLine = "";
        while (end < airports.size() && airports.get(end).getName().toLowerCase().startsWith(query)) {
            fileReader.seek(airports.get(end).getAddress());
            currentLine = fileReader.readLine().replaceAll("\"", "");
            resultArrayList.add(currentLine.split(","));
            end++;
        }
        return resultArrayList;
    }
}
