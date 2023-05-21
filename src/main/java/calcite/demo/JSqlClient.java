package calcite.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.util.JSqlUtils;

public class JSqlClient {
    public static void main(String[] args) {
        JSONObject tabSchema = new JSONObject();
        tabSchema.put("id","integer");
        tabSchema.put("name","varchar");

        JSONArray datasets = JSON.parseArray("[{\"id\":1,\"name\":\"aaa\",\"age\":20},{\"id\":2,\"name\":\"bbb\",\"age\":21},{\"id\":3,\"name\":\"ccc\",\"age\":22}]");

        String tabName = "userinfo";
        String sql = "select count(*) as cnt from \"userinfo\"";
        try{
            String result = JSqlUtils.query(tabSchema,tabName,datasets,sql);
            System.out.println("result: "+result);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}