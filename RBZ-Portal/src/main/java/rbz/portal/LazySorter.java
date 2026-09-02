/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.primefaces.model.SortOrder;

import java.util.Comparator;

/**
 * @author EDV-PC-Andreas
 */
public class LazySorter implements Comparator<userData> {

    private String sortField;

    private SortOrder sortOrder;

    public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(userData user1, userData user2) {
        try {
            Object value1 = userData.class.getField(this.sortField).get(user1);
            Object value2 = userData.class.getField(this.sortField).get(user2);

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
