package me.gv7.tools.josearcher.searcher;

import me.gv7.tools.josearcher.entity.NodeT;
import me.gv7.tools.josearcher.utils.LogUtil;

import java.lang.reflect.Field;
import java.util.*;

import static me.gv7.tools.josearcher.utils.CommonUtil.*;
import static me.gv7.tools.josearcher.utils.CheckUtil.*;

/**
 * @author apossin
 */
public class SearchObjectByBFS {
    private String model_name = SearchObjectByBFS.class.getSimpleName();
    private int max_search_depth = 100;  /*递归搜索深度  */
    private int matchHash;
    private Queue<NodeT> queue = new LinkedList<>();
    private String report_save_path = null;
    private String result_file;
    private String err_log_file;
    private Set<Integer> visited = new HashSet<>();

    public SearchObjectByBFS(Object target, Object matchObject) {
        this.matchHash = matchObject.hashCode();
        this.queue.offer(new NodeT.Builder().setChain("").setField_name("TargetObject").setCurrent_depth(0).setField_object(target).build());
    }

    public SearchObjectByBFS(Object target, int hashcode) {
        this.matchHash = hashcode;
        this.queue.offer(new NodeT.Builder().setChain("").setCurrent_depth(0).setField_object(target).build());
    }

    public void initSavePath() {
        if (report_save_path == null) {
            this.result_file = String.format("%s_result_%s.txt", this.model_name, getCurrentDate());
            this.err_log_file = String.format("%s_error_%s.txt", this.model_name, getCurrentDate());
        } else {
            this.result_file = String.format("%s/%s_result_%s.txt", this.report_save_path, this.model_name, getCurrentDate());
            this.err_log_file = String.format("%s_error_%s.txt", this.report_save_path, this.model_name, getCurrentDate());
        }
    }

    public void setMax_search_depth(int max_search_depth) {
        this.max_search_depth = max_search_depth;
    }

    public void setReport_save_path(String report_save_path) {
        this.report_save_path = report_save_path;
    }

    public void searchObject() {
        initSavePath();
        while (!queue.isEmpty()) {
            NodeT node = queue.poll();
            String filed_name = node.getField_name();
            Object filed_object = node.getField_object();
            String log_chain = node.getChain();
            int current_depth = node.getCurrent_depth();
            String new_log_chain = null;

            if (filed_object == null){
                continue;
            }

            int hash_code = filed_object.hashCode();

            if (current_depth > this.max_search_depth) {
                continue;
            }

            try {
                if (!visited.contains(hash_code)) {
                    if (log_chain != null && log_chain != "") {
                        current_depth++;
                        new_log_chain = String.format("%s \n%s ---> %s = {%s}", log_chain, getBlank(current_depth), filed_name, filed_object.getClass().getName());
                    } else {
                        new_log_chain = String.format("%s = {%s}", "TargetObject", filed_object.getClass().getName());
                    }

                    if (hash_code == this.matchHash) {
                        write2log(result_file, new_log_chain + "\n\n\n");
                    } else {
                        visited.add(hash_code);
                    }

                    Class clazz = filed_object.getClass();
                    if (filed_object instanceof List) {
                    }

                    if (filed_object instanceof Map) {
                        current_depth++;
                        Map map = (Map) filed_object;
                        if (map != null && map.size() > 0) {
                            Iterator iterator = map.values().iterator();
                            while (iterator.hasNext()) {
                                NodeT n = new NodeT.Builder().setField_name(filed_name).setField_object(iterator.next()).setChain(new_log_chain).setCurrent_depth(current_depth).build();
                                queue.offer(n);
                            }
                        }
                    }

                    if (clazz.isArray()) {
                        current_depth++;
                        try {
                            Object[] obj_arr = (Object[]) filed_object;
                            if (obj_arr != null && obj_arr.length > 0) {
                                for (int i = 0; i < obj_arr.length; i++) {
                                    if (obj_arr[i] == null) {
                                        continue;
                                    }
                                    String arr_type = obj_arr[i].getClass().getName();
                                    String arr_name = String.format("[%d] = {%s}", i, arr_type);
                                    NodeT n = new NodeT.Builder().setField_object(obj_arr[i]).setField_name(arr_name).setChain(new_log_chain).setCurrent_depth(current_depth).build();
                                    queue.offer(n);
                                }
                            }
                        } catch (Throwable e) {
                            LogUtil.saveThrowableInfo(e, this.err_log_file);
                        }
                    }

                    for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                        Field[] fields = clazz.getDeclaredFields();

                        for (Field field : fields) {
                            field.setAccessible(true);
                            String proType = field.getGenericType().toString();
                            String proName = field.getName();
                            Object subObj = null;
                            try {
                                subObj = field.get(filed_object);
                            } catch (Throwable e) {
                                LogUtil.saveThrowableInfo(e, this.err_log_file);
                            }

                            if (subObj == null) {
                                continue;
                            } else if (isList(field)) {
                                try {
                                    List list = (List) field.get(filed_object);
                                    if (list != null && list.size() > 0) {
                                        current_depth++;
                                        String tmp_log_chain = String.format("%s \n%s ---> %s = {%s}", new_log_chain, getBlank(current_depth), proName, proType);
                                        for (int i = 0; i < list.size(); i++) {
                                            if (list.get(i) == null) {
                                                continue;
                                            }
                                            String list_name = String.format("[%d]", i);
                                            NodeT n = new NodeT.Builder().setField_name(list_name).setField_object(list.get(i)).setChain(tmp_log_chain).setCurrent_depth(current_depth).build();
                                            queue.offer(n);
                                        }
                                    }
                                } catch (Throwable e) {
                                    LogUtil.saveThrowableInfo(e, this.err_log_file);
                                }
                            } else if (isMap(field)) {
                                try {
                                    Map map = (Map) field.get(filed_object);
                                    if (map != null && map.size() > 0) {
                                        current_depth++;
                                        String tmp_log_chain = String.format("%s \n%s ---> %s = {%s}", new_log_chain, getBlank(current_depth), proName, proType);

                                        Iterator<String> iter = map.keySet().iterator();
                                        while (iter.hasNext()) {
                                            Object key = iter.next();
                                            Object value = map.get(key);
                                            String map_name = String.format("[%s]", key.toString());
                                            NodeT n = new NodeT.Builder().setField_name(map_name).setField_object(value).setChain(tmp_log_chain).setCurrent_depth(current_depth).build();
                                            queue.offer(n);
                                        }
                                    }
                                } catch (Throwable e) {
                                    LogUtil.saveThrowableInfo(e, this.err_log_file);
                                }
                            } else if (field.getType().isArray()) {
                                try {
                                    Object obj = field.get(filed_object);
                                    if (obj == null) {
                                        continue;
                                    }
                                    current_depth++;
                                    Object[] objArr = (Object[]) obj;
                                    if (objArr != null && objArr.length > 0) {

                                        for (int i = 0; i < objArr.length; i++) {
                                            if (objArr[i] == null) {
                                                continue;
                                            }

                                            String tmp_log_chain = String.format("%s \n%s ---> %s = {%s}", new_log_chain, getBlank(current_depth), proName, proType);
                                            String arr_name = String.format("[%d]", i);
                                            NodeT n = new NodeT.Builder().setField_name(arr_name).setField_object(objArr[i]).setChain(tmp_log_chain).setCurrent_depth(current_depth).build();
                                            queue.offer(n);
                                        }
                                    }
                                } catch (Throwable e) {
                                    LogUtil.saveThrowableInfo(e, this.err_log_file);
                                }
                            } else {
                                try {
                                    NodeT n = new NodeT.Builder().setField_name(proName).setField_object(subObj).setChain(new_log_chain).setCurrent_depth(current_depth).build();
                                    queue.offer(n);
                                } catch (Throwable e) {
                                    LogUtil.saveThrowableInfo(e, this.err_log_file);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                LogUtil.saveThrowableInfo(e, this.err_log_file);
            }

        }
    }

}