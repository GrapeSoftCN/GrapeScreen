package interfaceApplication;

import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import check.formHelper;
import check.tableField;
import check.formHelper.formdef;
import interfaceModel.GrapeTreeDBModel;
import model.Model;
import session.session;
import string.StringHelper;

public class Mode {
	private GrapeTreeDBModel gDbModel;
	private Model model;
	private session se;
	private JSONObject userInfo = new JSONObject();
	private String userid = "";
	private String sid = null;
	private Block block = new Block();

	public Mode() {
		model = new Model();
		gDbModel = new GrapeTreeDBModel();
		gDbModel.form("mode").bindApp();
		se = new session();
		userInfo = se.getDatas();
		if (userInfo != null && userInfo.size() != 0) {
			userid = userInfo.getMongoID("_id");
		}
		sid = session.getSID();
	}

	/**
	 * 设置验证内容
	 * 
	 * @return
	 */
	/*private GrapeTreeDBModel setCheckData() {
		formHelper form = new formHelper();
		tableField field = new tableField("sid", "0");
		field.putRule(formdef.notNull, "0");
		form.addField(field);
		gDbModel.setCheckModel(form);
		return gDbModel;
	}*/

	/**
	 * 添加默认值
	 * 
	 * @return
	 */
	private HashMap<String, Object> getInitData() {
		HashMap<String, Object> defmap = model.AddFixField();
		// 设置mode表独有的字段
		defmap.put("userid", userid); // 所属用户id
		defmap.put("name", ""); // 模式名称
		defmap.put("bid", "0"); // 模式名称
		return defmap;
	}

	/**
	 * 获取大屏id
	 * @param mid
	 * @return
	 */
	public String getSid(String mid) {
		JSONObject object = Find(mid);
		String sid = "";
		if (object != null && object.size() != 0) {
			sid = object.getString("sid");
		}
		return sid;
	}

	/**
	 * 新增模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ScreenInfo
	 * @return
	 *
	 */
	public String AddMode(String ModeInfo) {
		Object info;
		JSONObject obj = model.AddMap(getInitData(), ModeInfo);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		gDbModel.checkMode();
		info = gDbModel.data(obj).insertEx();
		obj = gDbModel.eq("_id", (info.toString())).limit(1).find();
		return model.resultJSONInfo(obj);
	}

	/**
	 * 修改模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param id
	 * @param ScreenInfo
	 * @return
	 *
	 */
	public String UpdateMode(String id, String ScreenInfo) {
		int code = 99;
		JSONObject obj = JSONObject.toJSON(ScreenInfo);
		if (id == null || id.equals("") || id.equals("null")) {
			return rMsg.netMSG(3, "无效模式id");
		}
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (obj != null && obj.size() != 0) {
			gDbModel.eq("_id", id);
			code = (gDbModel.dataEx(obj).updateEx()) ? 0 : 99;
		}
		return model.resultMessage(code, "修改成功");
	}

	/**
	 * 删除模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteMode(String ids) {
		String bid = "";
		JSONArray BlockInfo = new JSONArray();
		long tipcode = 99;
		int l = 0;
		if (ids == null || ids.equals("") || ids.equals("null")) {
			return rMsg.netMSG(3, "无效模式id");
		}
		if (ids != null && !ids.equals("")) {
			String[] value = ids.split(",");
			l = value.length;
			gDbModel.or();
			for (String id : value) {
				gDbModel.eq("_id", id);
			}
			BlockInfo = gDbModel.dirty().select();
			bid = getBid(BlockInfo);
			if (bid != null && !bid.equals("")) {
				block.DeleteBlock(bid);
			}
			tipcode = gDbModel.deleteAll();
		}
		return model.resultMessage(tipcode == l ? 0 : 99, "删除成功");
	}

	/**
	 * 分页显示模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param idx
	 * @param PageSize
	 * @param condString
	 * @return
	 *
	 */
	public String PageMode(int idx, int PageSize, String condString) {
		JSONArray array = null;
		JSONArray condArray;
		long total = 0, totalSize = 0;
		if (idx <= 0 || PageSize <= 0) {
			return rMsg.netMSG(3, "当前页码小于0或者每页最大量小于0");
		}
		if (condString != null && !condString.equals("") && !condString.equals("null")) {
			condArray = JSONArray.toJSONArray(condString);
			if (condArray != null && condArray.size() != 0) {
				gDbModel.where(condArray);
			} else {
				return model.pageShow(null, total, totalSize, idx, PageSize);
			}
		}
		if (sid != null && !sid.equals("")) {
			array = gDbModel.dirty().mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,userid")
					.page(idx, PageSize);
			total = gDbModel.dirty().count();
			totalSize = gDbModel.pageMax(PageSize);
		}
		return model.pageShow(getBlock(array), total, totalSize, idx, PageSize);
	}

	/**
	 * 显示所有的模式信息，非管理员用户只能查看与自己相关的模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @return
	 *
	 */
	public String ShowMode() {
		JSONArray array = null;
		if (sid != null && !sid.equals("")) {
			if (model.isAdmin() == false) {
				gDbModel.eq("userid", userid);
			}
			array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,userid")
					.select();
		}
		return model.resultArray(getBlock(array));
	}

	/**
	 * 显示详细信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param info
	 * @return
	 *
	 */
	public JSONObject Find(String info) {
		Block block = new Block();
		String bid;
		JSONObject blockInfo;
		JSONObject obj = gDbModel.eq("_id", info)
				.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").limit(1).find();
		if (obj != null && obj.size() != 0) {
			bid = obj.getString("bid");
			if (bid != null && !bid.equals("") && bid.length() > 0) {
				blockInfo = block.GetBlockInfo(bid);
				obj = block.FillBlock(obj, blockInfo);
			}
		}
		return obj;
	}

	public String FindMode(String info) {
		return model.resultJSONInfo(Find(info));
	}
	// public String FindMode(String info) {
	// return model.resultJSONInfo(Find(info));
	// }

	/**
	 * 获取模式 - 区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Mode.java
	 * 
	 * @param array
	 * @return
	 *
	 */
	public JSONArray getBlock(JSONArray array) {
		String bid = "";
		bid = getBid(array);
		if (bid.length() > 0) {
			array = block.Block2Mode(bid, array);
		}
		return array;
	}

	/**
	 * 获取模式所包含的区域id
	 * 
	 * @param array
	 * @return
	 */
	private String getBid(JSONArray array) {
		JSONObject object;
		String tempId, bid = "";
		if (array != null && array.size() != 0) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				object = (JSONObject) array.get(i);
				tempId = object.getString("bid");
				bid += tempId + ",";
			}
			if (bid.length() > 0) {
				bid = StringHelper.fixString(bid, ',');
			}
		}
		return bid;
	}

	/**
	 * 根据大屏id查询所有的模式id
	 * 
	 * @param sid
	 * @return
	 */
	public String getMidBySid(String sid) {
		String mid = "";
		JSONObject object;
		String tmpsid;
		JSONArray array;
		if (sid != null && !sid.equals("")) {
			String[] value = sid.split(",");
			gDbModel.or();
			for (String id : value) {
				gDbModel.eq("_id", id);
			}
			array = gDbModel.eq("sid", sid).select();
			for (Object obj : array) {
				object = (JSONObject) obj;
				tmpsid = object.getMongoID("_id");
				mid += tmpsid + ",";
			}
			mid = StringHelper.fixString(mid, ',');
		}
		return mid;
	}

	protected JSONObject getModeInfo(String mids) {
		JSONObject object, obj = new JSONObject();
		gDbModel.or();
		JSONArray array = null;
		if (mids != null && !mids.equals("")) {
			String[] value = mids.split(",");
			for (String id : value) {
				gDbModel.eq("_id", id);
			}
			array = gDbModel.select();
		}
		if (array != null && array.size() != 0) {
			for (Object object2 : array) {
				object = (JSONObject) object2;
				obj = getInfo(obj, object);
			}
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getInfo(JSONObject obj, JSONObject object) {
		Screen screen = new Screen();
		String sid, mid, ScreenName = "", modeName = "";
		JSONObject tempObj = new JSONObject(), ScreenObj;
		if (object != null && object.size() != 0) {
			sid = object.getString("sid");
			mid = ((JSONObject) object.get("_id")).getString("$oid");
			// 获取大屏信息
			ScreenObj = screen.getScreenInfo(sid);
			if (ScreenObj != null && ScreenObj.size() != 0) {
				ScreenName = ScreenObj.getString(sid);
			}
			tempObj.put("modeName", modeName);
			tempObj.put("ScreenName", ScreenName);
			tempObj.put("sid", sid);
			obj.put(mid, tempObj);
		}
		return obj;
	}
}
