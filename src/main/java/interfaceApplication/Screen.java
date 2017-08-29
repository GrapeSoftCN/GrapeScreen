package interfaceApplication;

import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import broadCast.broadCastGroup;
import check.formHelper;
import check.formHelper.formdef;
import check.tableField;
import httpServer.grapeHttpUnit;
import interfaceModel.GrapeTreeDBModel;
import io.netty.channel.Channel;
import model.Model;
import nlogger.nlogger;
import rpc.execRequest;
import session.session;

public class Screen {
	private GrapeTreeDBModel gDbModel;
	private Model model;
	private session se;
	private JSONObject userInfo = new JSONObject();
	private String userid = "";
	private String sid = null;

	public Screen() {
		model = new Model();
		gDbModel = new GrapeTreeDBModel();
		gDbModel.form("screen").bindApp();
		se = new session();
		userInfo = se.getDatas();
		if (userInfo != null && userInfo.size() != 0) {
			userid = userInfo.getMongoID("_id");
		}
		sid = session.getSID();
	}

	/**
	 * 添加默认值
	 * 
	 * @return
	 */
	private HashMap<String, Object> getInitData() {
		HashMap<String, Object> defmap = model.AddFixField();
		// 设置screen表独有的字段
		defmap.put("userid", userid); // 所属用户id
		defmap.put("width", 100); // 大屏宽度
		defmap.put("height", 100); // 大屏长度
		defmap.put("col", 1); // 横向屏幕网格数
		defmap.put("row", 1); // 纵向屏幕网格数
		defmap.put("name", ""); // 大屏名称
		defmap.put("currenttid", ""); // 当前主题id
		return defmap;
	}

	public String SetTheme(String screenid, String themeId) {
		Theme theme = new Theme();
		int code = 99;
		String themes = "{\"currenttid\":\"" + themeId + "\"}";
		code = gDbModel.eq("_id", screenid).data(themes).update() != null ? 0 : 99;
		if (code == 0) {
			String content = theme.getThemeById(themeId);
			broadManage.broadEvent(themeId, 0, content);
		}
		return model.resultMessage(0, "设置当前主题成功");
	}

	/**
	 * 新增大屏信息
	 * 
	 * @param ScreenInfo
	 * @return
	 */
	public String AddScreen(String ScreenInfo) {
		Object info = null;
		JSONObject obj = model.AddMap(getInitData(), ScreenInfo);
		info = gDbModel.data(obj).insertEx();
		if (info == null) {
			return model.resultmsg(1);
		}
		obj = gDbModel.eq("_id", info.toString()).find();
		return model.resultJSONInfo(obj);
	}

	/**
	 * 修改大屏信息
	 * 
	 * @param id
	 * @param ScreenInfo
	 * @return
	 */
	public String UpdateScreen(String id, String ScreenInfo) {
		int code = 99;
		JSONObject obj = JSONObject.toJSON(ScreenInfo);
		if (obj != null && obj.size() != 0) {
			gDbModel.eq("_id", id);
			code = (gDbModel.dataEx(obj).updateEx() ? 0 : 99);
		}
		return model.resultMessage(code, "修改大屏信息成功");
	}

	/**
	 * 删除大屏信息，支持批量删除 使用批量删除功能，则id之间使用","隔开
	 * 
	 * 删除大屏，同时删除该大屏所包含的模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteScreen(String ids) {
		long tipcode = 99;
		int l = 0;
		if (ids != null && !ids.equals("")) {
			String[] value = ids.split(",");
			l = value.length;
			gDbModel.or();
			for (String id : value) {
				gDbModel.eq("_id", id);
			}
			tipcode = gDbModel.deleteAll();
		}
		return model.resultMessage(tipcode == l ? 0 : 99, "删除成功");
	}

	/**
	 * 分页显示大屏信息,当查询条件为null时，默认分页查询
	 * 
	 * 系统管理员用户，可以查询所有大屏数据
	 * 
	 * 非系统管理员用户，只能查询与自己相关的大屏信息
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
	public String PageScreen(int idx, int PageSize, String condString) {
		JSONArray array = null;
		long total = 0, totalSize = 0;
		if (condString != null && !condString.equals("") && !condString.equals("null")) {
			JSONArray condArray = JSONArray.toJSONArray(condString);
			if (condArray != null && condArray.size() != 0) {
				gDbModel.where(condArray);
			} else {
				return model.pageShow(null, total, totalSize, idx, PageSize);
			}
		}
		if (sid != null && !sid.equals("")) {
			if (!model.isAdmin()) {
				gDbModel.eq("userid", userid);
			}
			nlogger.logout(gDbModel.condString());
			array = gDbModel.dirty().mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,userid")
					.page(idx, PageSize);
			total = gDbModel.dirty().count();
			totalSize = gDbModel.pageMax(PageSize);
		}
		return model.pageShow(array, total, totalSize, idx, PageSize);
	}

	/**
	 * 显示所有的大屏信息，非管理员用户只能查看与自己相关的大屏信息
	 * 
	 * @return {"message":{"records":[{}]},"errorcode":0}
	 */
	public String ShowScreen() {
		JSONArray array = null;
		if (sid != null && !sid.equals("")) {
			if (model.isAdmins(userInfo) == false) {
				gDbModel.eq("userid", userid);
			}
			array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,userid")
					.select();
		}
		return model.resultArray(array);
	}

	/**
	 * 前台大屏显示
	 * 
	 * @param screenid
	 * @param mid
	 * @param tid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ShowScreenFront(String screenid, String mid, String tid) {
		Object area = "";
		JSONObject ScreenInfo = null;
		ScreenInfo = Find(screenid);
		JSONObject ModeInfo = Ele2Mode(mid, tid);
		area = (ModeInfo != null) && (ModeInfo.size() != 0) ? ModeInfo.get("area") : "";
		if ((ScreenInfo != null) && (ScreenInfo.size() != 0)) {
			ScreenInfo.put("area", area);
		}
		return this.model.resultJSONInfo(ScreenInfo);
	}

	/**
	 * 
	 * 
	 * @param val
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ShowFront(String screenid) {
		Object area = "";
		String themeid = "";
		String modeid = "";
		Theme theme = new Theme();
		JSONObject ScreenInfo = null;
		JSONObject ModeInfo = null;
		JSONObject ThemeInfo = null;
		ScreenInfo = Find(screenid);
		if (ScreenInfo != null && ScreenInfo.size() != 0) {
			themeid = ScreenInfo.getString("currenttid");
		}
		if (!themeid.equals("")) {
			ThemeInfo = theme.Find(themeid);
			if (ThemeInfo != null && ThemeInfo.size() != 0) {
				modeid = ThemeInfo.getString("mid");
			}
		}
		ModeInfo = Ele2Mode(modeid, themeid);
		area = (ModeInfo != null) && (ModeInfo.size() != 0) ? ModeInfo.get("area") : "";
		if ((ScreenInfo != null) && (ScreenInfo.size() != 0)) {
			ScreenInfo.put("area", area);
		}
		return model.resultJSONInfo(ScreenInfo);
	}

	@SuppressWarnings("unchecked")
	private JSONObject Ele2Mode(String mid, String tid) {
		JSONObject modeInfo = getMode(mid);
		JSONObject ThemeInfo = getTheme(tid);
		JSONArray ele = new JSONArray();
		JSONArray area = new JSONArray();

		if ((modeInfo != null) && (modeInfo.size() != 0)) {
			area = JSONArray.toJSONArray(modeInfo.getString("area"));
		}
		if ((ThemeInfo != null) && (ThemeInfo.size() != 0)) {
			ele = JSONArray.toJSONArray(ThemeInfo.getString("element"));
		}
		if ((ele != null) && (ele.size() != 0) && (area != null) && (area.size() != 0)) {
			int l = area.size();
			for (int i = 0; i < l; i++) {
				JSONObject areaObj = (JSONObject) area.get(i);
				String areaid = areaObj.getString("areaid");
				JSONArray contentArray = new JSONArray();
				for (Object object : ele) {
					JSONObject eleObj = (JSONObject) object;
					String bid = eleObj.getString("bid");
					if (areaid.equals(bid)) {
						contentArray = JSONArray.toJSONArray(eleObj.getString("content"));
					}
					areaObj.put("element", contentArray);
				}

				area.set(i, areaObj);
				modeInfo.put("area", area);
			}
		}
		return modeInfo;
	}

	private JSONObject getMode(String mid) {
		Mode mode = new Mode();
		JSONObject ModeInfo = new JSONObject();
		if (mid != null && !mid.equals("")) {
			ModeInfo = mode.Find(mid);
		}
		return ModeInfo;
	}

	private JSONObject getTheme(String tid) {
		Theme theme = new Theme();
		JSONObject ThemeInfo = new JSONObject();
		if (tid != null && !tid.equals("")) {
			ThemeInfo = theme.Find(tid);
		}
		return ThemeInfo;
	}

	/**
	 * 显示大屏详细信息
	 * 
	 * @param info
	 * @return
	 */
	public String FindScreen(String info) {
		return model.resultJSONInfo(Find(info));
	}

	private JSONObject Find(Object info) {
		gDbModel.eq("_id", info);
		JSONObject object = gDbModel.limit(1).find();
		return object;
	}

	@SuppressWarnings("unchecked")
	protected JSONObject getScreenInfo(String sids) {
		String sid, sname;
		JSONObject object, obj = new JSONObject();
		JSONArray array = null;
		if (sids != null && !sids.equals("")) {
			String[] value = sids.split(",");
			for (String id : value) {
				gDbModel.eq("_id", id);
			}
			array = gDbModel.field("_id,name").select();
		}
		if (array != null && array.size() != 0) {
			for (Object object2 : array) {
				object = (JSONObject) object2;
				sid = object.getMongoID("_id");
				sname = object.getString("name");
				obj.put(sid, sname);
			}
		}
		return obj;
	}
}
