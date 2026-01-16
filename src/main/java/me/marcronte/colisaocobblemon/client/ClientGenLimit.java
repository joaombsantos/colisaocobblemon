package me.marcronte.colisaocobblemon.client;

public class ClientGenLimit {
    private static int maxGeneration = 9;

    public static void setMaxGeneration(int gen) {
        maxGeneration = gen;
    }

    public static int getMaxGeneration() {
        return maxGeneration;
    }
}