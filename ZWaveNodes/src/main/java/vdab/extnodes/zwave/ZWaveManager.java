package vdab.extnodes.zwave;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationType;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.Options;
import org.zwave4j.ValueId;
import org.zwave4j.ZWave4j;

import com.lcrc.af.AnalysisCompoundData;
import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisNode;
import com.lcrc.af.AnalysisObject;
import com.lcrc.af.constants.LogLevel;

public class ZWaveManager  extends AnalysisObject implements NotificationWatcher{
	
		private static HashMap<String, ZWaveManager> s_PortZWaveManager_map = new HashMap<String, ZWaveManager>();

	public static ZWaveManager initManager(AnalysisNode an){
		ZWaveManager manager = ZWaveManager.getZWaveManager(ZWaveCommonAttributes.getInstance());
		if (manager == null)
			return null;

		manager.addZWaveNode(an);
		manager.init();		
		return manager;


	}
	public static String[] getZWavePorts(){
		Set<String> keys = s_PortZWaveManager_map.keySet();
		return keys.toArray(new String[keys.size()]);	
	}
	public static ZWaveManager getZWaveManager(ZWaveCommonAttributes attrs){
		if (attrs == null)
			return null;
		String port = attrs.get_ZWavePort();
		ZWaveManager manager = s_PortZWaveManager_map.get(port);
		if (manager == null){
			manager = new ZWaveManager(port);
			String dir = attrs.get_ZWaveConfigDirectory();
		
			if (dir != null)
				manager.setConfigDirectory(dir);
			s_PortZWaveManager_map.put(port, manager);
		}
		return manager;
	}

	private ArrayList<AnalysisNode> c_ZWaveNodeList = new ArrayList<AnalysisNode>();
	private ArrayList<ZWaveSource_A> c_ZWaveSourceList = new ArrayList<ZWaveSource_A>();
	private ArrayList<ZWaveNotificationSource> c_ZWaveNotificationSourceList = new ArrayList<ZWaveNotificationSource>();
	private long c_HomeId;
	private Manager c_Manager;
	private boolean c_IsReady = false;
	private String c_ZWaveConfigDirectory ;
	
	private String c_ZWavePort;

	public ZWaveManager(String port){
		c_ZWavePort = port;
	}
	
	public void setConfigDirectory(String dir){
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
			NativeLibraryLoader.loadLibrary(this, ZWave4j.LIBRARY_NAME, ZWave4j.class);
			final Options options = Options.create(c_ZWaveConfigDirectory, "", "");
			options.addOptionBool("ConsoleOutput", false);
			options.lock();
			c_Manager = Manager.create();
			c_Manager.addWatcher(this, null);
			c_Manager.addDriver(c_ZWavePort);

		}
		catch (Throwable e){
			setError("Uable to initialize ZWave connection, ZWave nodes will not work. e>"+e);
			c_Manager = null;
		}

	}
	public boolean isInitialized(){
		return c_Manager != null;
	}
	public void addZWaveNode(AnalysisNode an){
		
		try {
			if (!c_ZWaveNodeList.contains(an))
				c_ZWaveNodeList.add(an);	
			if (an.isTraceLogging())
				set_LogLevel(LogLevel.TRACE);
			if (an instanceof ZWaveSource_A){
				ZWaveSource_A as = (ZWaveSource_A) an;
				if (!c_ZWaveSourceList.contains(as))
					c_ZWaveSourceList.add(as);	
			}
			else if (an instanceof ZWaveNotificationSource ){
				ZWaveNotificationSource  as = (ZWaveNotificationSource ) an;
				if (!c_ZWaveNotificationSourceList.contains(as))
					c_ZWaveNotificationSourceList.add(as);	
			}
		}
		catch (Exception e){
			an.setError("Unable to add this ZWaveNode to the manager NODE="+an.get_Title()+" e>"+e);
		}

	}
	public void removeZWaveNode(AnalysisNode an){
		c_ZWaveNodeList.remove(an);	
		if (an.isTraceLogging()) // Check to see if it should still nneds to be trace logging
			updateLogLevel(); 
		if (an instanceof ZWaveSource_A){
			c_ZWaveSourceList.remove((ZWaveSource_A) an);	
		}
		else if (an instanceof ZWaveNotificationSource ){
			c_ZWaveNotificationSourceList.remove((ZWaveNotificationSource ) an);	
		}
	}

	public boolean isAnyNodeRunning(){
		
		for (AnalysisNode an :c_ZWaveNodeList){
			if (an.isRunning())
				return true;
		}
		return false;
	}
	// If any of the related nodes are trace logging, log trace info.
	public void updateLogLevel(){	
		for (AnalysisNode an :c_ZWaveNodeList){
			if (an.isTraceLogging()){
				set_LogLevel(LogLevel.TRACE);
				return;
			}
		}
		set_LogLevel(LogLevel.INFO);	
	}
	private AnalysisData getVDABData(Notification notification, String info){
		
		AnalysisCompoundData acd = new AnalysisCompoundData("Notification");
		acd.addAnalysisData(new AnalysisData("Node", Integer.valueOf(notification.getNodeId())));
		acd.addAnalysisData(new AnalysisData("Type", notification.getType().name()));
		acd.addAnalysisData(new AnalysisData("Details", info));
		ValueId valId = notification.getValueId();
		if (valId != null){
			acd.addAnalysisData("ClassCode", Integer.valueOf(valId.getCommandClassId()));
			acd.addAnalysisData("Index", Integer.valueOf(valId.getIndex()));
			String label = c_Manager.getValueLabel(valId);
			acd.addAnalysisData(label,ValueConverter.getValue(notification.getValueId()));
		}	
		return acd;
	}

			
	private void handleNotification(short nodeId, Notification notification, String info){

		if (isTraceLogging())
			logTrace(info);
		
		if (c_ZWaveNotificationSourceList.size() > 0){
			AnalysisData ad = getVDABData(notification,info);
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
				handleNotification(nodeId, notification, String.format("Driver ready\n" +
						"\thome id: %d",
						notification.getHomeId()
						));
				c_HomeId = notification.getHomeId();
				break;
			case DRIVER_FAILED:
				handleNotification(nodeId, notification, "Driver failed");
				break;
			case DRIVER_RESET:
				handleNotification(nodeId, notification, "Driver reset");
				break;
			case AWAKE_NODES_QUERIED:
				handleNotification(nodeId, notification, "Awake nodes queried");
				break;
			case ALL_NODES_QUERIED:
				handleNotification(nodeId, notification, "All nodes queried");
				c_Manager.writeConfig(c_HomeId);
				c_IsReady = true;
				break;

			case ALL_NODES_QUERIED_SOME_DEAD:
				handleNotification(nodeId, notification, "All nodes queried some dead");
				c_Manager.writeConfig(c_HomeId);
				c_IsReady = true;
				break;

			case POLLING_ENABLED:
				handleNotification(nodeId, notification, "Polling enabled");
				break;

			case POLLING_DISABLED:
				handleNotification(nodeId, notification, "Polling disabled");
				break;

			// NODE ---------------------------------------------------
			case NODE_NEW:
				handleNotification(nodeId, notification, String.format("Node new\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;

			case NODE_ADDED:
				handleNotification(nodeId, notification, String.format("Node added\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;
			case NODE_REMOVED:
				handleNotification(nodeId, notification, String.format("Node removed\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;
			case ESSENTIAL_NODE_QUERIES_COMPLETE:
				handleNotification(nodeId, notification, String.format("Node essential queries complete\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;
			case NODE_QUERIES_COMPLETE:
				handleNotification(nodeId, notification, String.format("Node queries complete\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;

			case NODE_EVENT:
				handleNotification(nodeId, notification, String.format("Node event\n" +
						"\tnode id: %d\n" +
						"\tevent id: %d",
						notification.getNodeId(),
						notification.getEvent()
						));
				break;

			case NODE_NAMING:
				handleNotification(nodeId, notification, String.format("Node naming\n" +
						"\tnode id: %d",
						notification.getNodeId()
						));
				break;

			case NODE_PROTOCOL_INFO:
				handleNotification(nodeId, notification, String.format("Node protocol info\n" +
						"\tnode id: %d\n" +
						"\ttype: %s",
						notification.getNodeId(),
						c_Manager.getNodeType(notification.getHomeId(), notification.getNodeId())
						));
				break;

			case VALUE_ADDED:
				handleNotification(nodeId, notification, String.format("Value added\n" +
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
				handleNotification(nodeId, notification, String.format("Value removed\n" +
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
				handleNotification(nodeId, notification, String.format("Value changed\n" +
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
				handleNotification(nodeId, notification, String.format("Value refreshed\n" +
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
				handleNotification(nodeId, notification, String.format("Group\n" +
						"\tnode id: %d\n" +
						"\tgroup id: %d",
						notification.getNodeId(),
						notification.getGroupIdx()
						));
				break;

			case SCENE_EVENT:
				handleNotification(nodeId, notification, String.format("Scene event\n" +
						"\tscene id: %d",
						notification.getSceneId()
						));
				break;

			case CREATE_BUTTON:
				handleNotification(nodeId, notification, String.format("Button create\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case DELETE_BUTTON:
				handleNotification(nodeId, notification, String.format("Button delete\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case BUTTON_ON:
				handleNotification(nodeId, notification, String.format("Button on\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case BUTTON_OFF:
				handleNotification(nodeId, notification, String.format("Button off\n" +
						"\tbutton id: %d",
						notification.getButtonId()
						));
				break;

			case NOTIFICATION:
				handleNotification(nodeId, notification, "Notification");
				break;

			default:
				handleNotification(nodeId, notification, notification.getType().name());
				break;
			}
		}
		catch(Exception e){
			AnalysisObject.logError("ZWaveManager.onNotifcation()", "Exception processing notification e>"+e);
		}


	}

	public void handleValueChange(short id, Notification notification, Object objRef){
		if (c_ZWaveSourceList.size() > 0){
			String label = 	c_Manager.getValueLabel(notification.getValueId());
			AnalysisData ad = new AnalysisData(label,ValueConverter.getValue(notification.getValueId()));		
			for (ZWaveSource_A as: c_ZWaveSourceList){
				if (as.get_Node().byteValue() == id && as.isRunning())
					as.publishIndividualEvent(ad);
			}
		}
		
	}
	public void setSwitchLevel(int nodeId, int level){
		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) ZWaveClassCode.SWITCH_MULTILEVEL);
		if (valId == null){
			AnalysisObject.logError("ZWaveManager.setSwitchLevel()", "Failed retrieving value id for NODE="+nodeId);
			return;
		}
		c_Manager.setValueAsByte(valId, (short) level);
	}
	
	public void switchOneOn(int nodeId){

		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) ZWaveClassCode.SWITCH_BINARY);
		if (valId == null){
			AnalysisObject.logError("ZWaveManager.switchOneOn()", "Failed retrieving value id for NODE="+nodeId);
			return;
		}
		c_Manager.setValueAsBool(valId, true);
	}

	public void switchOneOff(int nodeId){

		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) ZWaveClassCode.SWITCH_BINARY);
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
	public boolean  setZWaveValue(AnalysisData ad, int nodeId, int classId, int index){
		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) classId, (short) index);	
		if (valId == null)
			return false;
		String label = c_Manager.getValueLabel(valId);
		if (!ad.isSimple()){
			return false;
		}
		boolean status = false;
		if (ad.isBoolean()){
			status = c_Manager.setValueAsBool(valId, ad.getDataAsBoolean().booleanValue());
		}
		else if (ad.isDouble()){
			status = c_Manager.setValueAsFloat(valId, ad.getDataAsDouble().floatValue());
		}
		else if (ad.isNumeric()){
			status = c_Manager.setValueAsInt(valId, ad.getDataAsInteger().intValue());
		}
		else if (ad.isString()){
			status = c_Manager.setValueAsString(valId, ad.getDataAsString());
		}
		return status;
	}
	public AnalysisData getZWaveValue(int nodeId, int classId, int index){
		ValueId valId = ZWaveNodeInfo.getValueIdForClass((short) nodeId, (short) classId, (short) index);	
		if (valId == null)
			return null;
		String label = c_Manager.getValueLabel(valId);
		return new AnalysisData(label, ValueConverter.getValue(valId));
	}
	public AnalysisData getZWaveValuesForClass(int nodeId, int classId){
		ValueId [] valIds = ZWaveNodeInfo.getValueIdsForClass((short) nodeId, (short) classId);	
		if (valIds.length <= 0)
			return null;
		AnalysisCompoundData acd = new AnalysisCompoundData("ClassData");
		for (ValueId valId: valIds){
			String label = c_Manager.getValueLabel(valId);
			acd.addAnalysisData(new AnalysisData(label, ValueConverter.getValue(valId)));
		}
		return acd;
	}


}