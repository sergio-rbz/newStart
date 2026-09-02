/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Andreas
 */
public class LazyClassAdminDataModel extends LazyDataModel<userData> {
    List<userData> data = new ArrayList<>();
    private List<userData> datasource;

    public LazyClassAdminDataModel(List<userData> datasource) {
        this.datasource = datasource;
    }

    public void setDatasource(List<userData> datasource) {
        this.datasource = datasource;
    }

    @Override
    public userData getRowData(String rowKey) {
        for (userData user : datasource) {
            if (user.getUserUidNumber().equals(rowKey)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(userData user) {
        return user.getUserUidNumber();
    }

    @Override
    public List<userData> load(int first, int pageSize, Map<String, SortMeta> sortMeta, Map<String, FilterMeta> filterMeta) {
        List<userData> data = new ArrayList<>();
        //filter
        for (userData user : datasource) {
            boolean match = true;
            if (filterMeta != null) {
                for (FilterMeta meta : filterMeta.values()) {
                    try {
                        String filterField = meta.getFilterField();
                        Object filterValue = meta.getFilterValue();
                        String fieldValue = String.valueOf(user.getClass().getDeclaredField(filterField).get(user));
                        if (filterField.equals("inetAktiv") || filterField.equals("pwChangeAllowed") || filterField.equals("userProfil")) {
                            if (filterValue == null || fieldValue.startsWith(filterValue.toString())) {
                                match = true;
                            } else {
                                match = false;
                                break;
                            }
                        } else {
                            fieldValue = fieldValue.toLowerCase();
                            if (filterValue == null || fieldValue.startsWith(filterValue.toString().toUpperCase()) || fieldValue.startsWith(filterValue.toString().toLowerCase())) {
                                match = true;
                            } else {
                                match = false;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        match = false;
                    }
                }
            }
            if (match) {
                data.add(user);
            }
        }
        //sort
        if (sortMeta != null && !sortMeta.isEmpty()) {
            sortMeta.values().forEach(meta -> {
                Collections.sort(data, new LazySorter(meta.getSortField(), meta.getSortOrder()));
            });
        }
        //rowCount
        int dataSize = data.size();
        this.setRowCount(dataSize);
        //paginate
        if (dataSize > pageSize) {
            try {
                return data.subList(first, first + pageSize);
            } catch (IndexOutOfBoundsException e) {
                return data.subList(first, first + (dataSize % pageSize));
            }
        } else {
            return data;
        }
    }
}