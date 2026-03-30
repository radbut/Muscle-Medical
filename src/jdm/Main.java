package jdm;

import java.io.BufferedReader;
import java.util.Scanner;


public class Main {
public static void main(String[] args) {
    String file = "data\\Measurement.csv";
    BufferedReader reader = null;
    String line = "";

    Scanner scanner = new Scanner(System.in);
    boolean running = true; // otherwise the program stops after making the first choice

    // Defining all option strings so you only have to change their text in one place if necessary
String optionA = "See All patients with their status";
String optionP = "See all Patients with red status";
String optionS = "Search for Specific patient";
String optionF = "See Full summary report";
String optionN = "Add New patient";
String optionR = "Add new lab or CMAS Result for existing patient";
String optionX = "EXIT program";


while (running) {
    System.out.println("Please find the menu below:");

    System.out.println("A: " + optionA);
    System.out.println("P: " + optionP);
    System.out.println("S: " + optionS);
    System.out.println("F: " + optionF);
    System.out.println("N: " + optionN);
    System.out.println("R: " + optionR);
    System.out.println("X: " + optionX);

// == is for integers, equals is for strings
// TODO: add option activities for each option below
    String choice = scanner.nextLine();    
    if (choice.equalsIgnoreCase("A")) {
    System.out.println(optionA);
    } else if (choice.equalsIgnoreCase("P")) {
    System.out.println(optionP);
    } else if (choice.equalsIgnoreCase("S")) {
    System.out.println(optionS);
    } else if (choice.equalsIgnoreCase("F")) {
    System.out.println(optionF);
    } else if (choice.equalsIgnoreCase("N")) {
    System.out.println(optionN);
    } else if (choice.equalsIgnoreCase("R")) {
    System.out.println(optionR);
    } else if (choice.equalsIgnoreCase("X")) {
    System.out.println("Exiting...");
    running = false;
    }
    }
}
}