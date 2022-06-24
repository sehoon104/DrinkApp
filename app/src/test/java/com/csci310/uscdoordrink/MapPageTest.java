package com.csci310.uscdoordrink;

import static org.junit.Assert.assertEquals;

import junit.framework.TestCase;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class MapPageTest{

    @Test
    public void testParseDuration() {
        String res;
        String expected;
        int durationInSeconds;
        String transportMethod;
        DateFormat formatter = new SimpleDateFormat("hh:mm aa");

        //Test walking
        durationInSeconds = 600; //ten minutes
        transportMethod = "Walking";
        res = MapPage.parseDuration(durationInSeconds, transportMethod);
        expected = "ETA: " + formatter.format(new Date(System.currentTimeMillis() + durationInSeconds*1000))+ "\n" +
                "By Walking";
        assertEquals(expected, res);

        durationInSeconds = 6600; //110 minutes
        transportMethod = "Walking";
        res = MapPage.parseDuration(durationInSeconds, transportMethod);
        expected = "ETA: " + formatter.format(new Date(System.currentTimeMillis() + durationInSeconds*1000))+ "\n" +
                "By Walking";
        assertEquals(expected, res);


        //Test Car
        durationInSeconds = 600; //ten minutes
        transportMethod = "Driving";
        res = MapPage.parseDuration(durationInSeconds, transportMethod);
        expected = "ETA: " + formatter.format(new Date(System.currentTimeMillis() + durationInSeconds*1000))+ "\n" +
                "By Car";
        assertEquals(expected, res);

        durationInSeconds = 6600; //110 minutes
        transportMethod = "Driving";
        res = MapPage.parseDuration(durationInSeconds, transportMethod);
        expected = "ETA: " + formatter.format(new Date(System.currentTimeMillis() + durationInSeconds*1000))+ "\n" +
                "By Car";
        assertEquals(expected, res);


        //Test Cycling
        durationInSeconds = 600; //ten minutes
        transportMethod = "Cycling";
        res = MapPage.parseDuration(durationInSeconds, transportMethod);
        expected = "ETA: " + formatter.format(new Date(System.currentTimeMillis() + durationInSeconds*1000))+ "\n" +
                "By Bike";
        assertEquals(expected, res);

        durationInSeconds = 6600; //110 minutes
        transportMethod = "Cycling";
        res = MapPage.parseDuration(durationInSeconds, transportMethod);
        expected = "ETA: " + formatter.format(new Date(System.currentTimeMillis() + durationInSeconds*1000))+ "\n" +
                "By Bike";
        assertEquals(expected, res);
    }
}