/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disproveviakeyandjoanagui;

import java.util.ArrayList;

/**
 *
 * @author holger
 */
public class ErrorLogger {

    public enum ErrorTypes {
        ERROR_USER_CHOOSING_FILE,
        UNKNOWN_FILE_EXTENSION,
        ERROR_PARSING_JOAK
    }

    private static ArrayList<String> errorMsgs = new ArrayList<>();
    private static ArrayList<ErrorTypes> errorTypes = new ArrayList<>();

    public static void logError(String msg, ErrorTypes errorType) {
        errorMsgs.add(msg);
        errorTypes.add(errorType);
        System.out.println("Error: " + msg);
    }
}