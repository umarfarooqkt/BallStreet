package cs2212.team5

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

import java.sql.Connection
import java.sql.Statement
import java.sql.ResultSet
import java.sql.DriverManager
import cs2212.team5.TransactionService

class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
        while (true) {
            sleep(60000);
            def currentTime = new Date();
            def hours = currentTime.getHours()
            def minutes = currentTime.getMinutes()
            System.out.println("The current time  is " + hours + ":" + minutes)
            if (minutes%2==0) { //flush transaction queue every two minutes
                System.out.println("Flushing Queue...")
                String url2 = "http://localhost:8080/update/updateTransactions";

                URL obj = new URL(url2);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("POST");

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url2);
                System.out.println("Response Code : " + responseCode);
            } //end if
        } //end infinite loop
    }
}
