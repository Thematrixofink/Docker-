package com.ink.inkojsandbox;

public class Solution {
    public static void main(String[] args) {
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        try {
            Thread.sleep(1000000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("exec");
        System.out.println(a+b);
    }
}
