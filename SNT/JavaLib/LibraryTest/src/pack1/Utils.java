/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pack1;

import java.util.stream.IntStream;

/**
 *
 * @author User
 */
public class Utils {

    public static int findMaxInArray(int[] Array) {
        int max = Array[0];
        
        for (int i = 0; i < Array.length; i++) {
            if (max < Array[i]) {
                max = Array[i];
            }

        }

        return max;
    }

    public static int findMinInArray(int[] Array) {
        int min = Array[0];

        for (int i = 0; i < Array.length; i++) {
            if (min > Array[i]) {
                min = Array[i];
            }

        }

        return min;
    }

    public static int findCountInArray(int[] array, int number) {
        int count = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == number) {
                count++;
            }
        }
        return count;
    }

    public static int findCommonInArray(int[] array1, int[] array2) {
        int count = 0;
        array1 = IntStream.of(array1).distinct().toArray();
        for (int i = 0; i < array1.length; i++) {
            for (int j = 0; j < array2.length; j++) {
                if (array1[i] == array2[j]) {
                    count++;
                    break;
                }
            }

        }
        return count;
    }

}
