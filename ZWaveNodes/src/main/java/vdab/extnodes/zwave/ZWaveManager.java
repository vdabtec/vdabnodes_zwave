package vdab.extnodes.zwave;

import java.util.ArrayList;
import java.util.HashMap;

import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.Options;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisObject;

public class ZWaveManager  extends AnalysisObject implements NotificationWatcher{
	
	

	private static HashMap<String, ZWaveManager> s_PortZWaveManager_map = new HashMap<String, ZWaveManager>();

	private static AnalysisDataDef[] s_ZWaveManger_ddefs = new AnalysisDataDef[]{
		AnalysisObject.getClassAttributeDataDef(ZWaveManager.class,"ZWaveConfigDirectory")
				.setRequired().setEditOrder(12)};
		
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
	private long c_HomeId;
	private final Manager c_Manager;
	private boolean c_IsReady = false;
	private String c_ZWaveConfigDirectory = "/home/pi/open-zwave/config";
	
	private String c_ZWavePort;
	public ZWaveManager(String port){
	
		c_ZWavePort = port;
		final Options options = Options.create(c_ZWaveConfigDirectory, "", "");
	    options.addOptionBool("ConsoleOutput", false);
	    options.lock();
	    c_Manager = Manager.create();
		
	
		c_Manager.addWatcher(this, null);
		c_Manager.addDriver(port);
	}
	public String get_ZWaveConfigDirectory(){
		return c_ZWaveConfigDirectory;
		
	}
	public void set_ZWaveConfigDirectory(String dir){
		c_ZWaveConfigDirectory = dir;
		
	}
	public void addZWaveSource(ZWaveSource as){
		
		try {
			as.addDelegatedAttribute("ZWaveConfigDirectory", this);
			c_ZWaveSourceList.add(as);	
		}
		catch (Exception e){
			as.setError("Unable to add this ZWaveSource to the manager e>"+e);
		}

	}
	public void removeZWaveSource(ZWaveSource as){
		c_ZWaveSourceList.remove(as);	
	}
	public void addZWaveTarget(ZWaveTarget at){
		try {
			at.addDelegatedAttribute("ZWaveConfigDirectory", this);
			c_ZWaveTargetList.add(at);	
		}
		catch (Exception e){
			at.setError("Unable to add this ZWaveTarget to the manager e>"+e);
		}
	
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
	
	public void switchAllOn(){
		c_Manager.switchAllOn(c_HomeId);
		
	}
	public void switchAllOff(){
		c_Manager.switchAllOff(c_HomeId);
		
	}
	@Override
	public void onNotification(Notification notification, Object objArg) {
		short nodeId = notification.getNodeId() ;
		if (nodeId > 1 && nodeId < 255)
			ZWaveNodeInfo.updateFromNotification(c_Manager, nodeId, notification, objArg);

		AnalysisObject.logInfo("onNotification",">>>> NODEID="+notification.getNodeId()+" TYPE="+notification.getType().name());
		try {
			switch (notification.getType()) {

			// OVERALL ---------------------------------------------------
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

			// NODE ---------------------------------------------------
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
			//  MISC ------------------------------------------------------
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
		catch(Exception e){
			AnalysisObject.logError("ZWaveManager.onNotifcation()", "Exception processing notification e>"+e);
		}


	}

}