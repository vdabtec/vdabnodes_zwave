package vdab.extnodes.zwave;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationType;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.Options;
import org.zwave4j.ValueId;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisObject;

public class ZWaveManager  extends AnalysisObject implements NotificationWatcher{
	
	

	private static HashMap<String, ZWaveManager> s_PortZWaveManager_map = new HashMap<String, ZWaveManager>();

	private static AnalysisDataDef[] s_ZWaveManger_ddefs = new AnalysisDataDef[]{
		AnalysisObject.getClassAttributeDataDef(ZWaveManager.class,"ZWaveConfigDirectory")
				.setRequired().setEditOrder(12)};
		
	public static ZWaveManager getZWaveManager(String port){
		if (port == null)
			return null;
		
		ZWaveManager manager = s_PortZWaveManager_map.get(port);
		if (manager == null){
			manager = new ZWaveManager(port);
			s_PortZWaveManager_map.put(port, manager);
		}
		return manager;
	}
	private ArrayList<ZWaveSource_A> c_ZWaveSourceList = new ArrayList<ZWaveSource_A>();
	private ArrayList<ZWaveTarget_A> c_ZWaveTargetList = new ArrayList<ZWaveTarget_A>();
	private ArrayList<ZWaveNotificationSource> c_ZWaveNotificationSourceList = new ArrayList<ZWaveNotificationSource>();
	private long c_HomeId;
	private Manager c_Manager;
	private boolean c_IsReady = false;
	private String c_ZWaveConfigDirectory = "/home/pi/open-zwave/config" ;
	
	private String c_ZWavePort;
	public ZWaveManager(String port){
		c_ZWavePort = port;
	}
	
	public String get_ZWaveConfigDirectory(){
		return c_ZWaveConfigDirectory;
		
	}
	public void set_ZWaveConfigDirectory(String dir){
		c_ZWaveConfigDirectory = dir;
		
	}
	public void init(){

		if (c_Manager != null) // Only init if needed.
			return;
	
		if (c_ZWaveConfigDirectory == null || !(new File(c_ZWaveConfigDirectory).exists())){
			setError("ZWave Config Directory can not be found, please this attribute");
			c_Manager = null;
			return;
		}

		try {
			final Options options = Options.create(c_ZWaveConfigDirectory, "", "");
			options.addOptionBool("ConsoleOutput", false);
			options.lock();
			c_Manager = Manager.create();
			c_Manager.addWatcher(this, null);
			c_Manager.addDriver(c_ZWavePort);

		}
		catch (Exception e){
			setError("Uable to initialize ZWave connection e>"+e);
			c_Manager = null;
		}

	}
	public boolean isInitialized(){
		return c_Manager != null;
	}
	public void addZWaveSource(ZWaveSource_A as){
		
		try {
			as.addDelegatedAttribute("ZWaveConfigDirectory", this);
			if (!c_ZWaveSourceList.contains(as))
				c_ZWaveSourceList.add(as);	
		}
		catch (Exception e){
			as.setError("Unable to add this ZWaveSource to the manager e>"+e);
		}

	}
	public void removeZWaveSource(ZWaveSource_A as){
		c_ZWaveSourceList.remove(as);	
	}
	public void addZWaveTarget(ZWaveTarget_A at){
		try {
			at.addDelegatedAttribute("ZWaveConfigDirectory", this);
			if (!c_ZWaveTargetList.contains(at))
				c_ZWaveTargetList.add(at);	
		}
		catch (Exception e){
			at.setError("Unable to add this ZWaveTarget to the manager e>"+e);
		}
	
	}
	public void removeZWaveTarget(ZWaveTarget_A at){
		c_ZWaveTargetList.remove(at);	
	}
	public void addZWaveNotificationSource(ZWaveNotificationSource as){	
		try {
			as.addDelegatedAttribute("ZWaveConfigDirectory", this);
			if (!c_ZWaveNotificationSourceList.contains(as))
				c_ZWaveNotificationSourceList.add(as);	
		}
		catch (Exception e){
			as.setError("Unable to add this ZWaveControlService to the manager e>"+e);
		}

	}
	public void removeZWaveNotificationSource(ZWaveNotificationSource as){
		c_ZWaveNotificationSourceList.remove(as);	
	}
	private void handleNotification(short nodeId, String info){
		if (c_ZWaveNotificationSourceList.size() > 0){
			AnalysisData ad = new AnalysisData("Notification",info);
			for (ZWaveNotificationSource as: c_ZWaveNotificationSourceList){
				if (as.isRunning() && as.shouldReport((int) nodeId))
					as.publishNotificationEvent(ad);
			}
		}
	}

	public Manager getManager(){
		return c_Manager;
	}
	@Override
	public void onNotification(Notification notification, Object objArg) {
		short nodeId = notification.getNodeId() ;
		short classId = notification.getValueId().getCommandClassId();
		
		NotificationType  type = notification.getType();
		if (nodeId > 1 && nodeId < 255){
			// Handle keeping the Node Data.
			ZWaveNodeInfo.updateFromNotification(c_Manager, nodeId, notification, objArg);

			// Handle events that should be published.
			switch (type){
			
			case VALUE_ADDED:
			case VALUE_CHANGED:
				if (classId == 48 || classId == 49)
					handleValueChange(nodeId, notification, objArg);
				break;

			}
		}

		AnalysisObject.logInfo("onNotification",">>>> NODEID="+notification.getNodeId()+" TYPE="+type.name());
		try {
			switch (type) {

			// OVERALL ---------------------------------------------------
			case DRIVER_READY:
				handleNotification(nodeId, String.format("Driver ready\n" +
						"\thome id: %d",
						notification.getHomeId()
						));
				c_HomeId = notification.getHomeId();
				break;
			case DRIVER_FAILED:
				handleNotification(nodeId, "Driver failed");
				break;
			case DRIVER_RESET:
				handleNotification(nodeId, "Driver reset");
				break;
			case AWAKE_NODES_QUERIED:
				handleNotification(nodeId, "Awake nodes queried");
				break;
			case ALL_NODES_QUERIED:
				handleNotification(nodeId, "All nodes queried");
				c_Manager.writeConfig(c_HomeId);
				c_IsReady = true;
				break;

			case ALL_NODES_QUERIED_SOME_DEAD:
				handleNotification(nodeId, "All nodes queried some dead");
				c_Manager.writeConfig(c_HomeId);
				c_IsReady = true;
				break;

			case POLLING_ENABLED:
				handleNotification(nodeId, "Polling enabled");
				break;

			case POLLING_DISABLED:
				handleNotification(nodeId, "Polling disabled");
				break;

			// NODE ---------------------------------------------------
			case NODE_NEW:
				handleNotification(nodeId, String.format("Node new\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;

			case NODE_ADDED:
				handleNotification(nodeId, String.format("Node added\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;
			case NODE_REMOVED:
				handleNotification(nodeId, String.format("Node removed\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;
			case ESSENTIAL_NODE_QUERIES_COMPLETE:
				handleNotification(nodeId, String.format("Node essential queries complete\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;
			case NODE_QUERIES_COMPLETE:
				handleNotification(nodeId, String.format("Node queries complete\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;

			case NODE_EVENT:
				handleNotification(nodeId, String.format("Node event\n" +
						"\tnode id: %d\n" +
						"\tevent id: %d",
						notification.getNodeId(),
						notification.getEvent()
						));
				break;

			case NODE_NAMING:
				handleNotification(nodeId, String.format("Node naming\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;

			case NODE_PROTOCOL_INFO:
				handleNotification(nodeId, String.format("Node protocol info\n" +
						"\tnode id: %d\n" +
						"\ttype: %s",
						notification.getNodeId(),
						c_Manager.getNodeType(notification.getHomeId(), notification.getNodeId())
						));
				break;

			case VALUE_ADDED:
				handleNotification(nodeId, String.format("Value added\n" +
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
				handleNotification(nodeId, String.format("Value removed\n" +
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
				handleNotification(nodeId, String.format("Value changed\n" +
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
				handleNotification(nodeId, String.format("Value refreshed\n" +
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
				handleNotification(nodeId, String.format("Group\n" +
						"\tnode id: %d\n" +
						"\tgroup id: %d",
						notification.getNodeId(),
						notification.getGroupIdx()
						));
				break;

			case SCENE_EVENT:
				handleNotification(nodeId, String.format("Scene event\n" +
						"\tscene id: %d",
						notification.getSceneId()
						));
				break;

			case CREATE_BUTTON:
				handleNotification(nodeId, String.format("Button create\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case DELETE_BUTTON:
				handleNotification(nodeId, String.format("Button delete\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case BUTTON_ON:
				handleNotification(nodeId, String.format("Button on\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case BUTTON_OFF:
				handleNotification(nodeId, String.format("Button off\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case NOTIFICATION:
				handleNotification(nodeId, "Notification");
				break;

			default:
				handleNotification(nodeId, notification.getType().name());
				break;
			}
		}
		catch(Exception e){
			AnalysisObject.logError("ZWaveManager.onNotifcation()", "Exception processing notification e>"+e);
		}


	}

	public void handleValueChange(short id, Notification notification, Object objRef){
		if (c_ZWaveSourceList.size() > 0){
			AnalysisData ad = new AnalysisData("Data",ValueConverter.getValue(notification.getValueId()));		
			for (ZWaveSource_A as: c_ZWaveSourceList){
				if (as.get_Node().byteValue() == id && as.isRunning())
					as.publishIndividualEvent(ad);
			}
		}
		
	}
	public void setSwitchLevel(int nodeId, int level){
		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) ZWaveDeviceType.SWITCH_MULTILEVEL);
		if (valId == null){
			AnalysisObject.logError("ZWaveManager.setSwitchLevel()", "Failed retrieving value id for NODE="+nodeId);
			return;
		}
		c_Manager.setValueAsByte(valId, (short) level);
	}
	
	public void switchOneOn(int nodeId){

		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) ZWaveDeviceType.SWITCH_BINARY);
		if (valId == null){
			AnalysisObject.logError("ZWaveManager.switchOneOn()", "Failed retrieving value id for NODE="+nodeId);
			return;
		}
		c_Manager.setValueAsBool(valId, true);
	}

	public void switchOneOff(int nodeId){

		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) ZWaveDeviceType.SWITCH_BINARY);
		if (valId == null){
			AnalysisObject.logError("ZWaveManager.switchOneOff()", "Failed retrieving value id for NODE="+nodeId);
			return;
		}
		c_Manager.setValueAsBool(valId, false);
	}
	public void switchAllOn(){
		c_Manager.switchAllOn(c_HomeId);

	}
	public void switchAllOff(){
		c_Manager.switchAllOff(c_HomeId);

	}


}