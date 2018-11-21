package vdab.extnodes.zwave;

import java.util.ArrayList;

import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationType;
import org.zwave4j.ValueGenre;
import org.zwave4j.ValueId;

import com.lcrc.af.AnalysisObject;
import com.lcrc.af.datatypes.AFEnum;
import com.lcrc.af.util.ControlDataBuffer;
import com.lcrc.af.util.StringUtility;

public class ZWaveNodeInfo {
	// Inner class to hold details of ValueIds for each class.
	private class ClassInfo {
		private final static int MAX_INDEX = 5;
		private ValueId[] c_ValueIds = new ValueId[MAX_INDEX];
		public void updateValueId(ValueId valId){
			int i= valId.getIndex();
			if (i>= 0 && i < MAX_INDEX)
				c_ValueIds[i] = valId;
		}
		public ValueId getValueId(int i){
			if (i>= 0 && i < MAX_INDEX)
				return c_ValueIds[i];
			else
				return null;
		}
		
	}
	private static ZWaveNodeInfo[] s_Nodes = new ZWaveNodeInfo[255];
	private static int s_MaxNodeId = 1;
	public static void updateFromNotification(Manager manager, short id, Notification notification, Object objRef){
		if (id > 254){
			AnalysisObject.logError("ZWadeNodeInfo.updateFromNotification()","Node ID must be < 255 ID="+id);
			return;
		}
		// If needed create the object for this Node.
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
			s_Nodes[id].updateClassCodeInfo(notification.getValueId().getCommandClassId(), notification);
			break;

		case VALUE_CHANGED:	
			s_Nodes[id].updateClassCodeInfo(notification.getValueId().getCommandClassId(), notification);
			break;
		}

	}
	public static int getIdFromName(String name){
		String digits = StringUtility.digitsOnly(name);
		return Integer.parseInt(digits);		
	}
	public static ValueId getValueIdForClass(short nodeId, short classId){
		if (s_Nodes[nodeId] == null)
			return null;
		
		return s_Nodes[nodeId].getValueIdForClass(classId,  0);
		
	}
	public static AFEnum getNodeEnum(){
		AFEnum theEnum = new AFEnum("ZWaveNodeInfo_AllNodes");
		theEnum.addEntry(0, "ALL");
		theEnum.addEntry(1, "Controller");
		for (int n = 2 ; n <= s_MaxNodeId; n++){			
			if (s_Nodes[n] != null){
				StringBuilder sb = new StringBuilder("Node");
				sb.append("_");
				sb.append(n);
				theEnum.addEntry(n, sb.toString());
			}
		}	
		return theEnum;
	}
	public static AFEnum getNodeEnumForClasses(String label, short[] classIds){
		
		AFEnum theEnum = new AFEnum("ZWaveNodeInfo_"+label);
		
		for (int n = 2 ; n <= s_MaxNodeId; n++){			
			if (s_Nodes[n] != null && s_Nodes[n].supportsClass(classIds)){
				StringBuilder sb = new StringBuilder(label);
				sb.append("_");
				sb.append(n);
				theEnum.addEntry(n, sb.toString());
			}
		}	
		return theEnum;
	}
	private int c_NodeId ;
	private int c_Status ;    // Make AFEnum?
	private String c_NodeType;	
	private ClassInfo[] c_ClassInfoArray = new ClassInfo[255] ;
		
	public ZWaveNodeInfo(int id){
		c_NodeId = id;
	}
	public void setStatus(int status){
		c_Status = status;
	}
	public void setNodeType(String type){
		c_NodeType = type;
	}
	public void updateClassCodeInfo(short code, Notification notification){
		if (c_ClassInfoArray[code] == null)
			c_ClassInfoArray[code] = new ClassInfo();
		c_ClassInfoArray[code].updateValueId(notification.getValueId());
	}
	public boolean supportsClass(short[] codes){
		for (short code : codes){
			if (c_ClassInfoArray[code] != null)
				return true;
		}
		return false;
	}
	public ValueId getValueIdForClass(short code, int index){
		if (c_ClassInfoArray[code] == null)
			return null;
		
		return c_ClassInfoArray[code].getValueId(index);
	}
}
