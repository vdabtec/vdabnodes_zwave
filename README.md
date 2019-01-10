# VDAB ZWave Nodes
### Overview
Z-wave is a widely supported SmartHome proocol. A half dozen VDAB ZWave nodes provide complete support for controlling, monitoring and configuring Z-Wave devices.


| | |
|  --- |  :---: |
| Application Page    | [IoT Processing Hub](https://vdabtec.com/vdab/app-guides/zwave) |
| Demo Web Link   | [pi-demo.vdabsoft.com:31156/vdab](http://pi-demo.vdabsoft.com:31158/vdab) |

### Features
<ul>
<li>The <i>ZWaveSwitch</i> and <i>ZWaveDimmer</i> nodes control electrical switches and lighting.
<li>The <i>ZWaveSensor</i> monitors switch state and sensor levels.
<li>The <i>ZWaveNotificationSource</i> monitors all messages received from ZWave devices.
<li>The <i>ZWaveQueryService</i> supports reading any attributes on your ZWave device.
<li>The <i>ZWaveUpdateService</i> supports setting the control attributes of your ZWave device.
</ul>

### Licensing
Use of this software is subject to restrictions of the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

This software makes us of the following components which include their own licensing restrictions:

| | | 
|  --- |  :---: |
| OpenZWave Library - Apache | [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) |
| ZWave4J Library - MIT | [MIT License](https://opensource.org/licenses/MIT) |

### Loading the the Package
The current or standard version can be loaded directly using the VDAB Android Client following the directions
for [Adding Packages](https://vdabtec.com/vdab/docs/VDABGUIDE_AddingPackages.pdf) 
and selecting the <i>MqttNodes</i> package.
 
A custom version can be built using Gradle following the direction below.

* Clone or Download this project from Github.
* Open a command windows from the <i>ZWaveNodes</i> directory.
* Build using Gradle: <pre>      gradle vdabPackage</pre>

This builds a package zip file which contains the components that need to be deployed. These can be deployed by 
manually unzipping these files as detailed in the [Server Updates](https://vdabtec.com/vdab/docs/VDABGUIDE_ServerUpdates.pdf) 
 documentation.

### Known Issues - Updated January 9, 2019
* The node package only loads properly for the Raspberry Pi.
* Devices added to your controller may not appear in the node drop downs until the report their class information. By default some sensors do not report this for hours.




