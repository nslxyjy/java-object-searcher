package me.gv7.tools.josearcher.searcher;

import me.gv7.tools.josearcher.entity.Blacklist;
import me.gv7.tools.josearcher.entity.NodeO;
import me.gv7.tools.josearcher.entity.NodeT;
import me.gv7.tools.josearcher.utils.CheckUtil;
import me.gv7.tools.josearcher.utils.LogUtil;
import me.gv7.tools.josearcher.utils.MatchUtil;

import java.lang.reflect.Field;
import java.util.*;

import static me.gv7.tools.josearcher.utils.CommonUtil.*;
import static me.gv7.tools.josearcher.utils.CheckUtil.*;

/**
 * @author apossin
 */
public class SearchObjectByBFS {
    private String model_name = SearchObjectByBFS.class.getSimpleName();
    private int max_search_depth = 40;  /*递归搜索深度  */
    private int matchHash;
    private List<Blacklist> blacklists = new ArrayList<>();
    private Queue<NodeT> queue = new LinkedList<>();
    private String report_save_path = null;
    private String result_file;
    private String err_log_file;
    private Object startObj;
    private Object targetObj;
    private Map<Object, NodeO> visited = new HashMap<>();

    public SearchObjectByBFS(Object target, Object matchObject) {
        this.matchHash = matchObject.hashCode();
        this.startObj = target;
        this.queue.offer(new NodeT.Builder().setField_name("TargetObject").setCurrent_depth(0).setField_object(target).build());
    }

    public SearchObjectByBFS(Object target, int hashcode) {
        this.matchHash = hashcode;
        this.startObj = target;
        this.queue.offer(new NodeT.Builder().setCurrent_depth(0).setField_object(target).build());
    }

    public void setBlacklists(List<Blacklist> blacklists) {
        this.blacklists = blacklists;
    }

    public void initSavePath() {
        if (report_save_path == null) {
            this.result_file = String.format("%s_result_%s.txt", this.model_name, getCurrentDate());
            this.err_log_file = String.format("%s_error_%s.txt", this.model_name, getCurrentDate());
        } else {
            this.result_file = String.format("%s/%s_result_%s.txt", this.report_save_path, this.model_name, getCurrentDate());
            this.err_log_file = String.format("%s/%s_error_%s.txt", this.report_save_path, this.model_name, getCurrentDate());
        }
    }

    public void setMax_search_depth(int max_search_depth) {
        this.max_search_depth = max_search_depth;
    }

    public void setReport_save_path(String report_save_path) {
        this.report_save_path = report_save_path;
    }

    public void getResult() {
        getResultDFS(targetObj, "", 0);
    }

    public void getResultDFS(Object filed_object, String log_chain, Integer currentDepth) {
        if (currentDepth > this.max_search_depth) {
            return;
        }

        if (filed_object == this.startObj) {
            String new_log_chain = String.format("%s---> %s = {%s}", log_chain, "TargetObject", filed_object.getClass().getName());
            writeFile(new_log_chain);
            return;
        }
        try {
            List<Object> pre_filed_obj = visited.get(filed_object).getFiled_obj();
            List<String> pre_filed_name = visited.get(filed_object).getFiled_name();

            for (int i = 0; i < pre_filed_name.size(); i++) {
                if(log_chain.contains(filed_object.getClass().getName().toString())){
                    continue;
                }
                String new_log_chain = String.format("%s---> %s = {%s}\n", log_chain, pre_filed_name.get(i), filed_object.getClass().getName());
                getResultDFS(pre_filed_obj.get(i), new_log_chain, currentDepth + 1);
            }
        }catch (Exception e){
            return;
        }
    }

    public void writeFile(String log_chain) {
        //write2log(result_file, new_log_chain + "\n\n\n");
        String[] log_chains = log_chain.split("\n");
        StringBuilder chain = new StringBuilder();
        int len = log_chains.length;
        for (int i = 1; i <= len; i++) {
            String new_log_chain = String.format("%s%d%s\n", getBlank(i-1), i, log_chains[len - i]);
            chain.append(new_log_chain);
        }
        write2log(result_file, chain + "\n\n\n");
    }

    public void searchObject() {
        initSavePath();
        while (!queue.isEmpty()) {
            NodeT node = queue.poll();
            String filed_name = node.getField_name();
            Object filed_object = node.getField_object();
            Object pre_filed_object = node.getPre_filed_object();

            if(filed_object == pre_filed_object){
                continue;
            }

            int current_depth = node.getCurrent_depth();

            if (filed_object == null || CheckUtil.isSysType(filed_object) || MatchUtil.isInBlacklist(filed_name, filed_object, this.blacklists)) {
                continue;
            }

            if (current_depth > this.max_search_depth) {
                continue;
            }

            try {
                int hash_code = filed_object.hashCode();

                if (!visited.containsKey(filed_object)) {
                    current_depth++;

                    NodeO nodeo = new NodeO(pre_filed_object, filed_name);

                    visited.put(filed_object, nodeo);

                    if (hash_code == this.matchHash) {
                        if (this.targetObj == null) {
                            this.targetObj = filed_object;
                        }
                        continue;
                    }

                    Class clazz = filed_object.getClass();
                    if (filed_object instanceof List) {
                    }

                    if (filed_object instanceof Map) {
                        Map map = (Map) filed_object;
                        if (map != null && map.size() > 0) {
                            Iterator iterator = map.values().iterator();
                            while (iterator.hasNext()) {
                                NodeT n = new NodeT.Builder().setField_name(filed_name).setField_object(iterator.next()).setPre_filed_object(filed_object).setCurrent_depth(current_depth).build();
                                queue.offer(n);
                            }
                        }
                    }

                    if (clazz.isArray()) {
                        try {
                            Object[] obj_arr = (Object[]) filed_object;
                            if (obj_arr != null && obj_arr.length > 0) {
                                for (int i = 0; i < obj_arr.length; i++) {
                                    if (obj_arr[i] == null) {
                                        continue;
                                    }
                                    String arr_type = obj_arr[i].getClass().getName();
                                    String arr_name = String.format("[%d] = {%s}", i, arr_type);
                                    NodeT n = new NodeT.Builder().setField_object(obj_arr[i]).setField_name(arr_name).setPre_filed_object(filed_object).setCurrent_depth(current_depth).build();
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
                            String proName = field.getName();
                            Object subObj = null;
                            try {
                                subObj = field.get(filed_object);
                            } catch (Throwable e) {
                                LogUtil.saveThrowableInfo(e, this.err_log_file);
                            }

                            if (subObj == null) {
                                continue;
                            } else if (CheckUtil.isSysType(field)) {
                                //属性是系统类型跳过
                                continue;
                            } else if (MatchUtil.isInBlacklist(proName, subObj, this.blacklists)) {
                                continue;
                            } else if (isList(field)) {
                                try {
                                    List list = (List) field.get(filed_object);
                                    if (list != null && list.size() > 0) {
                                        for (int i = 0; i < list.size(); i++) {
                                            if (list.get(i) == null) {
                                                continue;
                                            }
                                            String list_name = String.format("[%d]", i);
                                            NodeT n = new NodeT.Builder().setField_name(list_name).setField_object(list.get(i)).setPre_filed_object(filed_object).setCurrent_depth(current_depth + 1).build();
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

                                        Iterator<String> iter = map.keySet().iterator();
                                        while (iter.hasNext()) {
                                            Object key = iter.next();
                                            Object value = map.get(key);
                                            String map_name = String.format("[%s]", key.toString());
                                            NodeT n = new NodeT.Builder().setField_name(map_name).setField_object(value).setPre_filed_object(filed_object).setCurrent_depth(current_depth + 1).build();
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
                                    Object[] objArr = (Object[]) obj;
                                    if (objArr != null && objArr.length > 0) {

                                        for (int i = 0; i < objArr.length; i++) {
                                            if (objArr[i] == null) {
                                                continue;
                                            }

                                            String arr_name = String.format("[%d]", i);
                                            NodeT n = new NodeT.Builder().setField_name(arr_name).setField_object(objArr[i]).setPre_filed_object(filed_object).setCurrent_depth(current_depth + 1).build();
                                            queue.offer(n);
                                        }
                                    }
                                } catch (Throwable e) {
                                    LogUtil.saveThrowableInfo(e, this.err_log_file);
                                }
                            } else {
                                try {
                                    NodeT n = new NodeT.Builder().setField_name(proName).setField_object(subObj).setPre_filed_object(filed_object).setCurrent_depth(current_depth).build();
                                    queue.offer(n);
                                } catch (Throwable e) {
                                    LogUtil.saveThrowableInfo(e, this.err_log_file);
                                }
                            }
                        }
                    }
                } else {
                    NodeO nodeo = visited.get(filed_object);
                    if(nodeo.getFiled_obj().contains(pre_filed_object)){
                        continue;
                    }
                    nodeo.setFiled_name(filed_name);
                    nodeo.setFiled_obj(pre_filed_object);
                    visited.put(filed_object, nodeo);
                }
            } catch (Throwable e) {
                LogUtil.saveThrowableInfo(e, this.err_log_file);
            }
        }
    }

//    public static void main(String[] args) {
//        System.out.println("ssss");
//        me.gv7.tools.josearcher.searcher.SearchObjectByBFS s = new me.gv7.tools.josearcher.searcher.SearchObjectByBFS(Thread.currentThread(),Thread.currentThread().getContextClassLoader());
//        s.setReport_save_path("D:\\logs");
//        s.setMax_search_depth(20);
//        s.searchObject();
//        s.getResult();
//    }
}
