package com.myles.demo;

import com.google.gson.Gson;

public class App {
    public static void main( String[] args ){
        // Serialization
        Gson gson = new Gson();
        System.out.println(gson.toJson(1));            // ==> 1
        System.out.println(gson.toJson("abcd"));       // ==> "abcd"
        System.out.println(gson.toJson(new Long(10))); // ==> 10
        int[] values = { 1 };
        System.out.println(gson.toJson(values));       // ==> [1]
    }
}
