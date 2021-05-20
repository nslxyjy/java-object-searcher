package me.gv7.tools.josearcher.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author apossin
 */
public class NodeO {
    private List<Object> filed_obj = new ArrayList<>();
    private List<String> filed_name = new ArrayList<>();

    public NodeO(Object obj,String name){
        filed_obj.add(obj);
        filed_name.add(name);
    }

    public void setFiled_name(String filed_name) {
        this.filed_name.add(filed_name);
    }

    public void setFiled_obj(Object filed_obj) {
        this.filed_obj.add(filed_obj);
    }

    public List<Object> getFiled_obj() {
        return filed_obj;
    }

    public List<String> getFiled_name() {
        return filed_name;
    }
}
