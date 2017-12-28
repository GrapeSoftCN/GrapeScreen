package interfaceApplication;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import model.Model;
import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.session.session;

public class Screen {
	private GrapeTreeDBModel gDbModel;
	private GrapeDBSpecField gdbField;
	private Model model;
	private session se;
	private JSONObject userInfo = new JSONObject();
	private String userid = "";
	private String sid = null;

	public Screen() {
		gDbModel = new GrapeTreeDBModel();
		gdbField = new GrapeDBSpecField();
        gdbField.importDescription(appsProxy.tableConfig("Screen"));
        gDbModel.descriptionModel(gdbField);
        gDbModel.bindApp();
		
		model = new Model();
		se = new session();
		userInfo = se.getDatas();
		if (userInfo != null && userInfo.size() != 0) {
			userid = userInfo.getMongoID("_id");
		}
		sid = session.getSID();
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
	@SuppressWarnings("unchecked")
	public String AddScreen(String ScreenInfo) {
		Object info = null;
//		JSONObject obj = model.AddMap(getInitData(), ScreenInfo);
		JSONObject obj = JSONObject.toJSON(ScreenInfo);
		obj.put("userid", userid);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		info = gDbModel.data(obj).autoComplete().insertOnce();
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
		if (id == null || id.equals("") || id.equals("null")) {
			return rMsg.netMSG(3, "无效屏幕id");
		}
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}

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
		if (ids == null || ids.equals("") || ids.equals("null")) {
			return rMsg.netMSG(3, "无效屏幕id");
		}
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
		if (idx <= 0 || PageSize <= 0) {
			return rMsg.netMSG(3, "当前页码小于0或者每页最大量小于0");
		}
		if (condString != null && !condString.equals("") && !condString.equals("null")) {
			JSONArray condArray = JSONArray.toJSONArray(condString);
			if (condArray != null && condArray.size() != 0) {
				gDbModel.where(condArray);
			} else {
				return model.pageShow(null, total, totalSize, idx, PageSize);
			}
		}
		if (sid != null && !sid.equals("")) {
//			if (!model.isAdmin()) {
//				gDbModel.eq("userid", userid);
//			}
			gDbModel.eq("userid", userid);
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
	public String ShowFronts(String screenid) {
		Object area = "";
		String themeid = "";
		String modeid = "";
		Theme theme = new Theme();
		JSONObject ScreenInfo = null;
		JSONObject ModeInfo = null;
		JSONObject ThemeInfo = null;
		String[] value = screenid.split("\\*");
		ScreenInfo = Find(value[0]);
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
		String[] value = screenid.split("\\*");
		ScreenInfo = Find(value[0]);
		if (value != null && value.length >= 2) {
			themeid = value[1];
		} else {
			if (ScreenInfo != null && ScreenInfo.size() != 0) {
				themeid = ScreenInfo.getString("currenttid");
			}
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
					areaObj.put("timediff", Integer.parseInt(eleObj.getString("timediff")));
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

	protected JSONObject Find(Object info) {
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
