/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;

import java.util.UUID;

/**
 *
 * @author nidhinair
 */
public class UniqueIdGenerator {
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
