package vdab.extnodes.zwave;

import java.util.ArrayList;
import java.util.HashMap;

import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationWatcher;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisObject;

public class ZWaveManager  extends AnalysisObject implements NotificationWatcher{
	
	
	private static long c_HomeId;
	private static Manager c_Manager;
	private static boolean c_IsReady = false;
	private static HashMap<String, ZWaveManager> s_PortZWaveManager_map = new HashMap<String, ZWaveManager>();

	
	public static ZWaveManager getZWaveManager(String port){
		ZWaveManager manager = s_PortZWaveManager_map.get(port);
		if (manager == null){
			manager = new ZWaveManager(port);
			s_PortZWaveManager_map.put(port, manager);
		}
		return manager;
	}
	private ArrayList<ZWaveSource> c_ZWaveSourceList = new ArrayList<ZWaveSource>();
	private ArrayList<ZWaveTarget> c_ZWaveTargetList = new ArrayList<ZWaveTarget>();

	
	private String c_ZWavePort;
	public ZWaveManager(String port){
		Manager c_Manager = Manager.create();
		c_ZWavePort = port;
		c_Manager.addWatcher(this, null);
		c_Manager.addDriver(port);
	}
	public void addZWaveSource(ZWaveSource as){
		c_ZWaveSourceList.add(as);	
	}
	public void removeZWaveSource(ZWaveSource as){
		c_ZWaveSourceList.remove(as);	
	}
	public void addZWaveTarget(ZWaveTarget at){
		c_ZWaveTargetList.add(at);	
	}
	public void removeZWaveTarget(ZWaveTarget at){
		c_ZWaveTargetList.remove(at);	
	}
	private void pushNotification(String info){
		pushNotification(new AnalysisData("Notification",info));
	}
	private void pushNotification(AnalysisData ad){	
		for (ZWaveSource as: c_ZWaveSourceList)
			as.publishIndividualEvent(ad);
	}
	public Manager getManager(){
		return c_Manager;
	}
	public void onNotification(Notification notification, Object context) {
		switch (notification.getType()) {
		case DRIVER_READY:
			pushNotification(String.format("Driver ready\n" +
					"\thome id: %d",
					notification.getHomeId()
					));
			c_HomeId = notification.getHomeId();
			break;
		case DRIVER_FAILED:
			pushNotification("Driver failed");
			break;
		case DRIVER_RESET:
			pushNotification("Driver reset");
			break;
		case AWAKE_NODES_QUERIED:
			pushNotification("Awake nodes queried");
			break;
			
		case ALL_NODES_QUERIED:
			pushNotification("All nodes queried");
			c_Manager.writeConfig(c_HomeId);
			c_IsReady = true;
			break;
			
		case ALL_NODES_QUERIED_SOME_DEAD:
			pushNotification("All nodes queried some dead");
			c_Manager.writeConfig(c_HomeId);
			c_IsReady = true;
			break;
			
		case POLLING_ENABLED:
			pushNotification("Polling enabled");
			break;
			
		case POLLING_DISABLED:
			pushNotification("Polling disabled");
			break;
			
		case NODE_NEW:
			pushNotification(String.format("Node new\n" +
					"\tnode id: %d",
					notification.getNodeId()
					));
			break;
			
		case NODE_ADDED:
			pushNotification(String.format("Node added\n" +
					"\tnode id: %d",
					notification.getNodeId()
					));
			break;
		case NODE_REMOVED:
			pushNotification(String.format("Node removed\n" +
					"\tnode id: %d",
					notification.getNodeId()
					));
			break;
		case ESSENTIAL_NODE_QUERIES_COMPLETE:
			pushNotification(String.format("Node essential queries complete\n" +
					"\tnode id: %d",
					notification.getNodeId()
					));
			break;
		case NODE_QUERIES_COMPLETE:
			pushNotification(String.format("Node queries complete\n" +
					"\tnode id: %d",
					notification.getNodeId()
					));
			break;
			
		case NODE_EVENT:
			pushNotification(String.format("Node event\n" +
					"\tnode id: %d\n" +
					"\tevent id: %d",
					notification.getNodeId(),
					notification.getEvent()
					));
			break;
			
		case NODE_NAMING:
			pushNotification(String.format("Node naming\n" +
					"\tnode id: %d",
					notification.getNodeId()
					));
			break;
			
		case NODE_PROTOCOL_INFO:
			pushNotification(String.format("Node protocol info\n" +
					"\tnode id: %d\n" +
					"\ttype: %s",
					notification.getNodeId(),
					c_Manager.getNodeType(notification.getHomeId(), notification.getNodeId())
					));
			break;
			
		case VALUE_ADDED:
			pushNotification(String.format("Value added\n" +
					"\tnode id: %d\n" +
					"\tcommand class: %d\n" +
					"\tinstance: %d\n" +
					"\tindex: %d\n" +
					"\tgenre: %s\n" +
					"\ttype: %s\n" +
					"\tlabel: %s\n" +
					"\tvalue: %s",
					notification.getNodeId(),
					notification.getValueId().getCommandClassId(),
					notification.getValueId().getInstance(),
					notification.getValueId().getIndex(),
					notification.getValueId().getGenre().name(),
					notification.getValueId().getType().name(),
					c_Manager.getValueLabel(notification.getValueId()),
					ValueConverter.getValue(notification.getValueId())
					));
			break;
			
		case VALUE_REMOVED:
			pushNotification(String.format("Value removed\n" +
					"\tnode id: %d\n" +
					"\tcommand class: %d\n" +
					"\tinstance: %d\n" +
					"\tindex: %d",
					notification.getNodeId(),
					notification.getValueId().getCommandClassId(),
					notification.getValueId().getInstance(),
					notification.getValueId().getIndex()
					));
			break;
			
		case VALUE_CHANGED:
			pushNotification(String.format("Value changed\n" +
					"\tnode id: %d\n" +
					"\tcommand class: %d\n" +
					"\tinstance: %d\n" +
					"\tindex: %d\n" +
					"\tvalue: %s",
					notification.getNodeId(),
					notification.getValueId().getCommandClassId(),
					notification.getValueId().getInstance(),
					notification.getValueId().getIndex(),
					ValueConverter.getValue(notification.getValueId())
					));
			break;
			
		case VALUE_REFRESHED:
			pushNotification(String.format("Value refreshed\n" +
					"\tnode id: %d\n" +
					"\tcommand class: %d\n" +
					"\tinstance: %d\n" +
					"\tindex: %d" +
					"\tvalue: %s",
					notification.getNodeId(),
					notification.getValueId().getCommandClassId(),
					notification.getValueId().getInstance(),
					notification.getValueId().getIndex(),
					ValueConverter.getValue(notification.getValueId())
					));
			break;
			
		case GROUP:
			pushNotification(String.format("Group\n" +
					"\tnode id: %d\n" +
					"\tgroup id: %d",
					notification.getNodeId(),
					notification.getGroupIdx()
					));
			break;

		case SCENE_EVENT:
			pushNotification(String.format("Scene event\n" +
					"\tscene id: %d",
					notification.getSceneId()
					));
			break;
			
		case CREATE_BUTTON:
			pushNotification(String.format("Button create\n" +
					"\tbutton id: %d",
					notification.getButtonId()
					));
			break;
			
		case DELETE_BUTTON:
			pushNotification(String.format("Button delete\n" +
					"\tbutton id: %d",
					notification.getButtonId()
					));
			break;
			
		case BUTTON_ON:
			pushNotification(String.format("Button on\n" +
					"\tbutton id: %d",
					notification.getButtonId()
					));
			break;
			
		case BUTTON_OFF:
			pushNotification(String.format("Button off\n" +
					"\tbutton id: %d",
					notification.getButtonId()
					));
			break;
			
		case NOTIFICATION:
			pushNotification("Notification");
			break;
			
		default:
			pushNotification(notification.getType().name());
			break;
		}
	}
	public void switchAllOn(){
		c_Manager.switchAllOn(c_HomeId);
		
	}
	public void switchAllOff(){
		c_Manager.switchAllOff(c_HomeId);
		
	}

}