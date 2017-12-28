package interfaceApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

import common.java.broadCast.broadCastGroup;
import common.java.httpServer.grapeHttpUnit;
import common.java.rpc.execRequest;
import io.netty.channel.Channel;
import model.Model;

/**
 * @author win7
 *
 */
public class broadManage {
	private Model model = new Model();
	private static HashMap<String, broadCastGroup> rl = null;
	static{
		if( rl == null ){
			rl = new HashMap<>();
		}
	}
	// 大屏客户端WS连接成功后第一时间使用本接口注册
	/**
	 * @param themeid 主题ID
	 * @return
	 */
	public String registerBorad(String screenid){
		String rs = null;
		broadCastGroup br = null;
		int code = 0;
		br = (broadCastGroup)rl.get(screenid);
		Channel ch = (Channel) execRequest.getChannelValue(grapeHttpUnit.ws.wsid);
		if (ch != null) {
			if( br == null){//当前大屏不在线
				rl.put( screenid , (new broadCastGroup()).add(ch));
				rs = model.resultMessage(code, "注册成功");
			}
			else{
				br = (broadCastGroup)rl.get(screenid);
				br.add(ch);
				rs = model.resultMessage(code, "添加成功");
			}
		}
		else{
			rs = model.resultMessage(code, "不支持ws以外协议");
		}
		return rs;
	}
	/**主题数据发生改变后，通过该接口把改变后的数据推送到指定的大屏客户端
	 * @param screenid	要更换主题的屏幕id
	 * @param themeid	屏幕的新主题的ID
	 * @param outData	新主题对应的数据 ->一个包含了事件ID和事件内容的数据
	 * 事件ID：
	 * {
	 * "event":0
	 * "data":0:"asdasdasd",1:{"target":name,"content":"asdasdasds"}
	 * }
	 * 0:主题全部重新渲染
	 * 1:重新渲染对应区域内容
	 * @return
	 */
	public static boolean pushData(String screenid, String outData) {
		boolean rb = false;
		broadCastGroup bCastGroup = rl.get(screenid);
		if (bCastGroup != null) {
			bCastGroup.broadCast(outData);
			rb = true;
		}
//		else{ throw new RuntimeException("广播对象不存在"); }
		return rb;
	}
	
	/**通过主题ID获得使用该主题的全部屏幕ID
	 * @param themeid
	 * @return
	 */
	public static List<String> findScreenIDsByThemeID(String themeid){
		Theme theme = new Theme();
		List<String> rList = new ArrayList<>();
		rList = theme.getScreenid(themeid);
		return rList;
	}
	
	@SuppressWarnings("unchecked")
	public static void broadEvent(String themeid, int eventID,String content){
		JSONObject in  = JSONObject.toJSON(content);
		JSONObject out = new JSONObject();
		List<String> screenids = findScreenIDsByThemeID(themeid);
		if( screenids.size() > 0 ){
			out.put("event", eventID);
			out.put("data", in);
			int i,l = screenids.size();
			for(i =0; i<l;i++){
				pushData( screenids.get(i) ,out.toJSONString());
			}
		}
	}
	
	/*
	 * 修改主题->检测当前使用该主题的屏幕ID->查找各个ID对应的广播组->广播新主题内容
	 * */

	//获取主题信息
	private String getThemeData(String themeid) {
		Theme theme = new Theme();
		String Data = theme.getTheme(themeid);
		return Data;
	}
}
