import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.deepz.fileparse.JsonParser;
import com.deepz.fileparse.PptParser;
import com.deepz.fileparse.vo.StructableFileVO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 张定平
 * @date 2019/7/22 17:18
 * @description
 */
public class FileTest {

    PptParser parser = new PptParser();

    JsonParser jsonParser = new JsonParser();
    String path = "C:\\Users\\zhangdingping\\Desktop\\tika\\array.json";

    @Test
    public void process() {
        String text = jsonParser.getText(path);
        StructableFileVO fileVO = new StructableFileVO();
//        fileVO.setHeaders(getHeaders(text));
        List<List<Object>> data = getData(text);
        System.out.println(JSON.parse(data.toString()));
    }

    /**
     * @author zhangdingping
     * @description 解析json字符串中的value值
     * @date 2019/7/23 14:14
     */
    private List<List<Object>> getData(String jsonStr) {


        List<List<Object>> results = new ArrayList<>();

        int checkJson1 = checkJson(jsonStr);
        if (checkJson1 == 1) {
            JSONArray jsonArray = JSON.parseArray(jsonStr);
            if (jsonArray.get(0) == null && jsonArray.size() == 1) {
                List<Object> list = new ArrayList<>();
                list.add(null);
                results.add(list);
                return results;
            }
            if (jsonArray.size() == 1 && checkJson(jsonArray.get(0).toString()) == -1) {
                List<Object> list = new ArrayList<>();
                list.add(jsonArray.get(0).toString());
                results.add(list);
                return results;
            }

            for (int i = 0; i < jsonArray.size(); i++) {
                List<Object> result = new ArrayList<>();

                JSONObject jsonObject = JSON.parseObject(jsonArray.get(i).toString());
                if (jsonObject.size() == 0) {
                    List<Object> list = new ArrayList<>();
                    list.add(null);
                    results.add(list);
                    return results;
                }
                Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    String value = entry.getValue().toString();
                    int checkJson = checkJson(value);
                    if (checkJson == 1) {
                        result.add(getObjectFromArray(value));
                    } else if (checkJson == 0) {
                        result.add(getObjectFromObj(value));
                    } else {
                        result.add(value);
                    }
                }
                results.add(result);
            }
            return results;
        } else {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            List<Object> result = new ArrayList<>();
            Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                String value = entry.getValue().toString();
                int checkJson = checkJson(value);
                if (checkJson == 1) {
                    result.add(getObjectFromArray(value));
                } else if (checkJson == 0) {
                    result.add(getObjectFromObj(value));
                } else {
                    result.add(value);
                }
            }
            results.add(result);
        }
        return results;
    }


    private Object getObjectFromArray(String jsonStr) {

        List<Object> results = new ArrayList<>();

        /*results.add(extractContents(jsonStr));*/

        JSONArray jsonArray = JSON.parseArray(jsonStr);
        if (jsonArray.size() == 1 && checkJson(jsonArray.get(0).toString()) == -1) {
            results.add(jsonArray.get(0).toString());
            return results;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            List<Object> result = new ArrayList<>();
            System.out.println(jsonArray.get(i).toString());
            JSONObject jsonObject = JSON.parseObject(jsonArray.get(i).toString());
            Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                String value = entry.getValue().toString();
                int checkJson = checkJson(value);
                if (checkJson == 1) {
                    result.add(getObjectFromArray(value));
                } else if (checkJson == 0) {
                    result.add(getObjectFromObj(value));

                } else {
                    result.add(value);
                }
            }
            results.add(result);
        }
        return results;
    }

    private Object getObjectFromObj(String jsonStr) {

        /*results.add(extractContents(jsonStr));*/

        JSONObject jsonObject = JSON.parseObject(jsonStr);

        List<Object> result = new ArrayList<>();

        Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String value = entry.getValue().toString();
            int checkJson = checkJson(value);
            if (checkJson == 1) {
                result.add(getObjectFromArray(value));
            } else if (checkJson == 0) {
                result.add(getObjectFromObj(value));
            } else {
                result.add(value);
            }

        }

        return result;
    }


    /**
     * @author zhangdingping
     * @description 获取json对象的所有key值(去重)
     * @date 2019/7/23 14:08
     */
    private List<String> getHeaders(String jsonStr) {
        int checkJson = checkJson(jsonStr);
        if (checkJson == 1) {
            JSONArray parse = (JSONArray) JSONArray.parse(jsonStr);
            List<String> jsonObjs = new ArrayList<>();
            for (Object o : parse) {
                jsonObjs.add(o.toString());
            }
            return getDistinctKeys(jsonObjs);
        } else if (checkJson == 0) {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            return getKeys(jsonObject.toString());
        }
        return null;
    }


    /**
     * @param jsonStr json对象字符串
     * @author zhangdingping
     * @description 获取json对象字符串集合，拿到所有key值
     * @date 2019/7/23 13:58
     */
    public List<String> getKeys(String jsonStr) {
        List<String> headers = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        Set<String> strings = jsonObject.keySet();
        headers.addAll(strings);

        return headers;
    }


    /**
     * @param jsonStr json对象字符串
     * @author 张定平
     * @description 获取json对象字符串集合，拿到所有key值(去重)
     * @date 2019/7/23 13:28
     */
    public List<String> getDistinctKeys(List<String> jsonStr) {
        List<String> headers = new ArrayList<>();
        for (String s : jsonStr) {
            JSONObject jsonObject = JSON.parseObject(s);
            Set<String> strings = jsonObject.keySet();
            headers.addAll(strings);
        }
        //将key值去重
        headers = headers.stream().distinct().collect(Collectors.toList());
        return headers;

    }

    /**
     * @author zhangdingping
     * @description 判断字符串是Json数组还是Json对象
     * @date 2019/7/23 13:53
     */
    private int checkJson(String jsonStr) {
        if (jsonStr.startsWith("[")) {
            //如果是JSONArray
            return 1;
        } else if (jsonStr.startsWith("{")) {
            //如果是JSONObject
            return 0;
        } else {
            return -1;
        }
    }
}
