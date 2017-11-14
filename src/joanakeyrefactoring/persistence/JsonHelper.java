/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joanakeyrefactoring.persistence;

/**
 *
 * @author holger
 */
public class JsonHelper {

    public static void addJsonStringToStringBuilder(StringBuilder stringBuilder,
                                                    String key, String value) {
        stringBuilder.append("\"" + key + "\" : " + "\"" + value + "\"");
    }

    public static void addKeyValueToJsonStringbuilder(StringBuilder stringBuilder,
                                                      String key, String value) {
        stringBuilder.append("\"" + key + "\" : " + value);
    }

    public static void addJsonObjValueToStringBuiler(StringBuilder stringBuilder,
                                                     String key, String value) {
        stringBuilder.append("\"" + key + "\" : {\n");
        stringBuilder.append(value);
        stringBuilder.append("}\n");
    }

}
