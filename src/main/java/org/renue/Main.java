package org.renue;

import org.renue.config.Config;
import org.renue.exceptions.FilterFormatException;
import org.renue.exceptions.IncorrectFilterQuery;
import org.renue.filter.Filtrator;
import org.renue.search.Searcher;

import javax.swing.plaf.basic.BasicTreeUI;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    private static String getSearchInput() {
        String searchString = scanner.nextLine();
        if(searchString.isEmpty()) {
            System.out.println("error: строка для поиска не может быть пустой.");
        }
        return searchString;
    }

    public static void main(String[] args) {
        System.out.println("Welcome!");
        Config config = new Config("src/main/resources/airports.csv");
        Scanner scanner = new Scanner(System.in);
        Filtrator filtrator = new Filtrator();
        Searcher searcher = null;
        try {
            searcher = new Searcher(config);
        } catch (FileNotFoundException e) {
            System.out.println("error: файл не найден");
        }
        String filtersString;
        String searchString;
        while(true) {
            System.out.println("Введите строку с фильтрами/enter(пропустить фильтры)/!quit(выход): ");
            filtersString = scanner.nextLine();
            if(!filtersString.isEmpty()) {
                try {
                    filtrator.parse(filtersString);
                } catch (FilterFormatException e) {
                    System.out.println(e);
                }
            }
            System.out.println("Введи строку для поиска: ");
            searchString = getSearchInput();

            ArrayList<String[]> searchResult = null;
            long startTime = System.currentTimeMillis();
            try {
                searchResult = searcher.search(searchString.toLowerCase());
            } catch(Exception e) {
                System.out.println(e);
            }
            if(searchResult.size() == 0) {
                System.out.println("По запросу поиска ничего не найдено");
                continue;
            }
            if(filtersString.isEmpty()) {
                long endTime = System.currentTimeMillis();
                for(String[] line: searchResult) {
                    System.out.println(line[1] + " " +Arrays.toString(line));
                }
                System.out.printf("Количество найденных строк: %d Время затраченное на поиск: %d мс\n",searchResult.size(), endTime - startTime);
            } else {
                try {
                    ArrayList<String[]> filteredResult = filtrator.filter(searchResult);
                    long endTime = System.currentTimeMillis();
                    for(String[] line: filteredResult) {
                        System.out.println(line[1] + " " +Arrays.toString(line));
                    }
                    System.out.printf("Количество найденных строк: %d Время затраченное на поиск: %d мс\n",filteredResult.size(), endTime - startTime);
                } catch (IncorrectFilterQuery e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}