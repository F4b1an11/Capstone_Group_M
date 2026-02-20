package com.notam.controller;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    //do not share CLIENT_SECRET in plain text
    private static final String CLIENT_ID = "5982191bfef7458aa9cb8e8c9674b645";
    //this is a secret value, FAA_CLIENT_ID is put in terminal manually with ~export FAA_CLIENT_ID=abc123
    private static final String CLIENT_SECRET = System.getenv("FAA_CLIENT_ID");
 
    public static void main(String[] args) throws Exception {
        System.out.println("----- Search Notams by ICAO Location-----\n");

        //get user location input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 'EXIT' to quit program");
        System.out.println("Enter ICAO Location (example: KOKC): ");
        String input = scanner.nextLine().trim().toUpperCase();

        //get NOTAMs for different locations until "EXIT"
        while (!input.equals("EXIT")) {

            //domain to FAA API server
            String domain = "external-api.faa.gov";

            //full url to send request. does not have to be icaoLocation=.
            //it can be other parameters such as notamType,classification,notamNumber, etc... 
            String url = "https://" + domain + "/notamapi/v1/notams?icaoLocation=" + input;
            
            //HTTP Client used to connect to server, send request, and recieve responses
            HttpClient client = HttpClient.newHttpClient();

            //create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    //send request to this url
                    .uri(URI.create(url))
                    //checks credentials
                    .header("client_id", CLIENT_ID)
                    .header("client_secret", CLIENT_SECRET)
                    //tells server "I want to retrieve data"
                    .GET()
                    //finalize request
                    .build();

            //send the request and store the response        
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            //from Jackson Library used to parse JSON object        
            ObjectMapper mapper = new ObjectMapper();

            //takes repsonse and converts it to a navigable JSON tree object
            JsonNode root = mapper.readTree(response.body());

            //retrieves the "items" key from JSON object
            JsonNode items = root.get("items");

            //check if Notam was recieved
            if (items.isEmpty()) {
                System.out.println("No NOTAMs found.");
            } else {
                //iterate through all notam items and print
                for (JsonNode notam : items) {
                    System.out.println("----- NOTAM -----");

                    JsonNode textNode = notam.get("notamText");

                    if (textNode != null && !textNode.isNull()) {
                        System.out.println(textNode.asText());
                    } else {
                        System.out.println("No text field found. Full object:");
                        System.out.println(notam.toPrettyString());
                    }
                }
            }

            //get next input
            System.out.println("Enter 'EXIT' to quit program");
            System.out.println("Enter ICAO Location (example: KOKC): ");
            input = scanner.nextLine().trim().toUpperCase();
        }
        scanner.close();
    }
}
