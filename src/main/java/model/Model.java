package model;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.jGrapeFW_Message;
import authority.plvDef.UserMode;
import authority.plvDef.plvType;
import nlogger.nlogger;
import session.session;

public class Model {
	private JSONObject _obj = new JSONObject();
	private HashMap<String, Object> map = new HashMap<String, Object>();

	/**
	 * 设置基础字段，所有表都包含的字段
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> AddFixField() {
		map.put("itemfatherID", "0");
		map.put("itemSort", 0);
		map.put("deleteable", 0);
		map.put("visable", 0);
		map.put("itemLevel", 0);
		JSONObject rMode = new JSONObject("chkType", plvType.powerVal);
		rMode.put("chkCond", 100);
		String mode = rMode.toJSONString();
		map.put("rMode", mode);
		map.put("uMode", mode);
		map.put("dMode", mode);
		return map;
	}

	/**
	 * 判断当前用户是否为管理员
	 * 
	 * @return
	 */
	public boolean isAdmin() {
		String temp = "";
		int userType = 0;
		session se = new session();
		JSONObject userInfo = se.getDatas();
		if (userInfo != null && userInfo.size() != 0) {
			temp = userInfo.getString("userType");
		}
		userType = !temp.equals("") ? Integer.parseInt(temp) : 0;
		return UserMode.root == userType;
	}

	/**
	 * 判断当前用户是否为管理员
	 * 
	 * @return
	 */
	public boolean isAdmins(JSONObject userInfo) {
		String temp = "";
		int userType = 0;
		if (userInfo != null && userInfo.size() != 0) {
			temp = userInfo.getString("userType");
		}
		userType = !temp.equals("") ? Integer.parseInt(temp) : 0;
		return UserMode.root == userType;
	}

	@SuppressWarnings("unchecked")
	public JSONObject AddMap(HashMap<String, Object> map, String info) {
		JSONObject object = JSONObject.toJSON(info);
		if (object != null && object.size() != 0) {
			if (map.entrySet() != null) {
				Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
					if (!object.containsKey(entry.getKey())) {
						object.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return object;
	}

	public String getFileUrl() {
		String url = getConfig("file").split("/")[0];
		return "http://" + url;
	}

	public String getFilepath() {
		return getConfig("imgurl");
	}

	/**
	 * 读取配置文件
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @param key
	 *            配置文件中 key值
	 * @return
	 *
	 */
	private String getConfig(String key) {
		String value = "";
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream("URLConfig.properties"));
			value = pro.getProperty(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}

	/**
	 * 分页数据输出
	 * 
	 * @param array
	 *            当前页数据
	 * @param total
	 *            总数据量
	 * @param totalSize
	 *            总页数
	 * @param idx
	 *            当前页
	 * @param pageSize
	 *            每页数据量
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String pageShow(JSONArray array, long total, long totalSize, int idx, int pageSize) {
		array = (array != null && array.size() != 0) ? array : new JSONArray();
		JSONObject object = new JSONObject();
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("total", total);
		object.put("totalSize", totalSize);
		object.put("data", array);
		return resultJSONInfo(object);
	}

	@SuppressWarnings("unchecked")
	public String resultArray(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
		_obj.put("records", array);
		return resultMessage(0, _obj.toString());
	}

	@SuppressWarnings("unchecked")
	public String resultJSONInfo(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

	public String resultmsg(int num) {
		return resultMessage(num, "");
	}

	public String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填字段为空";
			break;
		default:
			msg = "其他操作异常";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}

	/**
	 * 配置文件中缩略图配置
	 * 
	 * @param key
	 * @return
	 */
	private String getConfigs(String key) {
		String value = "";
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream("OfficeUrl.properties"));
			value = pro.getProperty(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}
	public String getThumbnailConfig(String key) {
		String value = "";
		value = getConfig(key);
		return value;
	}
}
