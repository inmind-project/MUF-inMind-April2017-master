package edu.cmu.inmind.multiuser.common.model;

import java.util.Arrays;

/**
 * Created by fpecune on 9/7/2017.
 */
public class Utils {

    public static void checkContents(String variable, String... values) throws IllegalArgumentException {
        if (!Arrays.asList(values).contains(variable))
            throw new IllegalArgumentException(variable);
    }


}