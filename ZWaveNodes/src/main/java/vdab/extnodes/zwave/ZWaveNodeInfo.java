package vdab.extnodes.zwave;

import java.util.ArrayList;

import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationType;

import com.lcrc.af.AnalysisObject;
import com.lcrc.af.util.ControlDataBuffer;

public class ZWaveNodeInfo {
	private static ZWaveNodeInfo[] s_Nodes = new ZWaveNodeInfo[255];
	private static int s_MaxNodeId = 1;
	public static void updateFromNotification(Manager manager, short id, Notification notification, Object objRef){
		if (id > 254){
			AnalysisObject.logError("ZWadeNodeInfo.updateFromNotification()","Node ID must be < 255 ID="+id);
			return;
		}
		if (s_Nodes[id] == null){
			s_Nodes[id] = new ZWaveNodeInfo(id);
			if (id > s_MaxNodeId )
				s_MaxNodeId = id;		
		}
		NotificationType  type = notification.getType();
		switch (type) {
		case NODE_NEW:
			AnalysisObject.logError("ZWadeNodeInfo.updateFromNotification()","Unexpected notification type TYPE="+type);
			break;

		case NODE_ADDED:
			s_Nodes[id].setStatus(1);
			break;

		case NODE_REMOVED:
			s_Nodes[id].setStatus(0);
			break;

		case NODE_PROTOCOL_INFO:
			s_Nodes[id].setNodeType(manager.getNodeType(notification.getHomeId(), id));
			break;

		case VALUE_ADDED:	
			short classId = notification.getValueId().getCommandClassId();
			s_Nodes[id].recordClassCode(classId);
			break;
		}

	}
	public static String[] getNodesSupportingClass(short classId){
		String type = ZWaveDeviceType.getEnum().getLabel(classId);
		ArrayList<String> l = new ArrayList<String>();
		for (int n = 2 ; n <= s_MaxNodeId; n++){			
			if (s_Nodes[n] != null && s_Nodes[n].supportsClass(classId)){
				StringBuilder sb = new StringBuilder(type);
				sb.append("_");
				sb.append(n);
				l.add(sb.toString());
			}
		}	
		return l.toArray(new String[l.size()]);
	}
	private int c_NodeId ;
	private int c_Status ;    // Make AFEnum?
	private String c_NodeType;
	private ControlDataBuffer c_SupportedClasses_cdb = new ControlDataBuffer("ZWaveNodeSupportedClasses");
	
	public ZWaveNodeInfo(int id){
		c_NodeId = id;
	}
	public void setStatus(int status){
		c_Status = status;
	}
	public void setNodeType(String type){
		c_NodeType = type;
	}
	public void recordClassCode(short code){
		c_SupportedClasses_cdb.set(Short.toString(code));
	}
	public boolean supportsClass(short code){
		return c_SupportedClasses_cdb.isSet(Short.toString(code));
	}
}
