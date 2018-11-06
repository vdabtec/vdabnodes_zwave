package vdab.extnodes.zwave;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisEvent;
import com.lcrc.af.AnalysisSource;

public class ZWaveSource  extends AnalysisSource {
	

	private String c_ZWavePort;
	private ZWaveManager c_ZWaveManager;
	
	public void set_ZWavePort(String port){
		c_ZWavePort = port;
	}

	public String get_ZWavePort(){
		return  c_ZWavePort;
	}

	public void _start() {
		c_ZWaveManager = ZWaveManager.getZWaveManager(c_ZWavePort);
		c_ZWaveManager.addZWaveSource(this);
		super._start();
	}
	public void _stop() {
		super._stop();
		c_ZWaveManager.removeZWaveSource(this);

	}
	public  synchronized void publishIndividualEvent(AnalysisData ad){
        publish(new AnalysisEvent(this, ad));    
	}
}
