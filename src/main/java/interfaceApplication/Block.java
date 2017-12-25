package interfaceApplication;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import apps.appsProxy;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import model.Model;
import string.StringHelper;

public class Block {
	private Model model;
	private GrapeDBSpecField gdbField;
	private GrapeTreeDBModel gDbModel;

	public Block() {
		model = new Model();
		
		gDbModel = new GrapeTreeDBModel();
		gdbField = new GrapeDBSpecField();
        gdbField.importDescription(appsProxy.tableConfig("Block"));
        gDbModel.descriptionModel(gdbField);
        gDbModel.bindApp();
	}

	/**
	 * 新增区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ScreenInfo
	 * @return
	 *
	 */
	public String AddBlock(String BlockInfo) {
		Object info = "";
		JSONObject object = JSONObject.toJSON(BlockInfo);
//		JSONObject object = model.AddMap(getInitData(), BlockInfo);
		if (object == null || object.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		info = gDbModel.dataEx(object).autoComplete().insertOnce();
		object = gDbModel.eq("_id", info).find();
		return model.resultJSONInfo(object);
	}

	/**
	 * 批量新增区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param BlockInfo
	 * @return
	 *
	 */
	public String AddAllBlock(String BlockInfo) {
		String info = "";
		List<Object> list = new ArrayList<Object>();
		JSONArray Condarray = JSONArray.toJSONArray(BlockInfo);
		JSONObject obj;
		if (Condarray == null || Condarray.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (Condarray != null && Condarray.size() > 0) {
			for (Object object : Condarray) {
				obj = (JSONObject) object;
				info = gDbModel.data(obj).insertOnce().toString();
				list.add(info);
			}
		}
		info = StringHelper.join(list);
		JSONArray array = FindBlock(info);
		return model.resultArray(array);
	}

	/**
	 * 修改区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param id
	 * @param BlockInfo
	 * @return
	 *
	 */
	public String UpdateBlock(String id, String BlockInfo) {
		int code = 99;
		JSONObject obj = JSONObject.toJSON(BlockInfo);
		if (id == null || id.equals("") || id.equals("null")) {
			return rMsg.netMSG(3, "无效区域id");
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

	public String UpdateAllBlock(JSONArray blockArray) {
		JSONObject obj;
		String id;
		if (blockArray != null && blockArray.size() != 0) {
			for (Object object : blockArray) {
				obj = (JSONObject) object;
				id = obj.getString("_id");
				obj.remove("_id");
				gDbModel.eq("_id", id).data(obj).update();
			}
		}
		return this.model.resultMessage(0, "修改成功");
	}

	/**
	 * 删除区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteBlock(String ids) {
		long code = 0;
		if (ids == null || ids.equals("") || ids.equals("null")) {
			return rMsg.netMSG(3, "无效区域id");
		}
		String[] value = ids.split(",");
		int l = value.length;
		gDbModel.or();
		for (String id : value) {
			if (!id.equals("0")) {
				gDbModel.eq("_id", id);
			}
		}
		code = gDbModel.deleteAll();
		return model.resultMessage(code == l ? 0 : 99, "删除成功");
	}

	/**
	 * 查询区域详细信息,包含批量查询
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param bid
	 * @return
	 *
	 */
	private JSONArray FindBlock(String bids) {
		JSONArray array = null;
		gDbModel.or();
		if (bids != null && !bids.equals("")) {
			String[] value = bids.split(",");
			for (String bid : value) {
				if (!bid.equals("") && !bid.equals("0")) {
					gDbModel.eq("_id", bid);
				}
			}
			array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").select();
		}
		return array;
	}

	/**
	 * 批量查询区域信息，封装成固定格式输出，{"message":{"records":[]},"errorcode":0}
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String ShowBlock(String ids) {
		return model.resultArray(FindBlock(ids));
	}

	/**
	 * 获取区域信息，封装成{bid:area,bid:area}
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public JSONObject GetBlockInfo(String ids) {
		JSONObject tempObj, obj = new JSONObject();
		String id;
		int l = 0;
		JSONArray array = FindBlock(ids);
		if (array != null && array.size() != 0) {
			l = array.size();
			for (int i = 0; i < l; i++) {
				tempObj = (JSONObject) array.get(i);
				if (tempObj != null && tempObj.size() != 0) {
					id = tempObj.getMongoID("_id");
					tempObj.remove("mid");
					tempObj.put("areaid", id);
					obj.put(id, tempObj);
				}
			}
		}
		return obj;
	}

	/**
	 * 添加区域信息到模式信息数据中
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Mode.java
	 * 
	 * @param array
	 * @param ElementInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public JSONArray Block2Mode(String bid, JSONArray array) {
		JSONObject BlockInfo = GetBlockInfo(bid);
		JSONObject object;
		if (BlockInfo != null && BlockInfo.size() != 0) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				object = (JSONObject) array.get(i);
				array.set(i, FillBlock(object, BlockInfo));
			}
		}
		return array;
	}

	/**
	 * 填充模式数据中的区域信息
	 * 
	 * @param ModeInfo
	 * @param BlockInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject FillBlock(JSONObject ModeInfo, JSONObject BlockInfo) {
		JSONObject tempObj = new JSONObject();
		JSONArray tempArray = new JSONArray();
		String bid;
		String[] value;
		if (ModeInfo != null && ModeInfo.size() != 0 && BlockInfo != null && BlockInfo.size() != 0) {
			bid = ModeInfo.getString("bid");
			value = bid.split(",");
			for (String str : value) {
				if (str != null && !str.equals("")) {
					tempObj = (JSONObject) BlockInfo.get(str);
					if ((tempObj != null) && (tempObj.size() != 0)) {
						tempArray.add(tempObj);
					}
				}
			}
			ModeInfo.remove("bid");
			ModeInfo.put("area", (tempArray != null) && (tempArray.size() != 0) ? tempArray : new JSONArray());
		}
		return ModeInfo;
	}

}
