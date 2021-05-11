package me.gv7.tools.josearcher.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author apossin
 */
public class NodeO {
    private List<List<String>> chain = new ArrayList<List<String>>();
    private String field_name;
    private Object field_object;
    private int current_depth;

    public NodeO(NodeO.Builder builder){
        this.chain.add(builder.chain);
        this.field_name = builder.field_name;
        this.field_object = builder.field_object;
        this.current_depth = builder.current_depth;
    }

    public List<List<String>> getChain() {
        return chain;
    }

    public String getField_name() {
        return field_name;
    }

    public void setField_name(String field_name) {
        this.field_name = field_name;
    }

    public Object getField_object() {
        return field_object;
    }

    public void setField_object(Object field_object) {
        this.field_object = field_object;
    }

    public int getCurrent_depth() {
        return current_depth;
    }

    public void setCurrent_depth(int current_depth) {
        this.current_depth = current_depth;
    }

    public static class Builder{
        private List<String> chain = new ArrayList<String>();
        private String field_name;
        private Object field_object;
        private int current_depth;

        public NodeO.Builder setChain(String chain) {
            this.chain.add(chain);
            return this;
        }

        public NodeO.Builder setField_name(String field_name) {
            this.field_name = field_name;
            return this;
        }

        public NodeO.Builder setField_object(Object field_object) {
            this.field_object = field_object;
            return this;
        }

        public NodeO.Builder setCurrent_depth(int current_depth) {
            this.current_depth = current_depth;
            return this;
        }

        public NodeO build(){
            return new NodeO(this);
        }
    }
}
