/**
 *  Original Copyright 2016 AdamV - modified by Guy Mayhew (2020) for Hubitat Elevation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * V0.9.3 20/09/2016
 *
 * Note: this is tested and working with the HeatIt Thermostat v1 running Firmware 1.8
 *
 * Changelog:
 *
 * 0.9.3 - Corrected the energySaveHeat Command so that you can specifically activate this from CoRE rules
 * 0.9.2 - Fixed an issue preventing some commands being fired when they are not triggered from the DTH UI
 */
 
 preferences {
            
            def myOptions = ["F - Floor temperature mode", "A - Room temperature mode", "AF - Room mode w/floor limitations", "A2 - Room temperature mode (external)", "P - Power regulator mode", "FP - Floor mode with minimum power limitation"]
			input "tempSen", 
            "enum", 
            title: "Select Temperature Sensor Mode",
           // description: "F - Floor mode: Regulation is based on the floor temperature sensor reading \nA - Room temperature mode: Regulation is based on the measured room temperature using the internal sensor (Default) \nAF - Room mode w/floor limitations: Regulation is based on internal room sensor but limited by the floor temperature sensor (included) ensuring that the floor temperature stays within the given limits (FLo/FHi) \nA2 - Room temperature mode: Regulation is based on the measured room temperature using the external sensor \nP (Power regulator): Constant heating power is supplied to the floor. Power rating is selectable in 10% increments ( 0% - 100%) \nFP - Floor mode with minimum power limitation: Regulation is based on the floor temperature sensor reading, but will always heat with a minimum power setting (PLo)",
           	defaultValue: "A - Room temperature mode",
            required: true, 
            options: myOptions, 
            displayDuringSetup: false
            
            input title: "Explanation:", 
            description: "F - Floor mode: Regulation is based on the floor temperature sensor reading \nA - Room temperature mode: Regulation is based on the measured room temperature using the internal sensor (Default) \nAF - Room mode w/floor limitations: Regulation is based on internal room sensor but limited by the floor temperature sensor (included) ensuring that the floor temperature stays within the given limits (FLo/FHi) \nA2 - Room temperature mode: Regulation is based on the measured room temperature using the external sensor \nP (Power regulator): Constant heating power is supplied to the floor. Power rating is selectable in 10% increments ( 0% - 100%) \nFP - Floor mode with minimum power limitation: Regulation is based on the floor temperature sensor reading, but will always heat with a minimum power setting (PLo)", 
            displayDuringSetup: false, 
            type: "paragraph", 
            element: "paragraph"
            
            input "FLo",
        	"number",
            range: "5..40",
            title: "FLo: Floor min limit",
            description: "Minimum Limit for floor sensor (5°-40°)",
			defaultValue: 5,
			required: false,
            displayDuringSetup: false
            
            input "FHi",
        	"number",
            range: "5..40",
            title: "FHi: Floor max limit",
            description: "Maximum Limit for floor sensor  (5°-40°)",
			defaultValue: 40,
			required: false,
            displayDuringSetup: false
            
            def sensOptions = ["10k ntc (Default)", "12k ntc", "15k ntc", "22k ntc", "33k ntc", "47k ntc"]
			input "sensorType", 
            "enum", 
            title: "Select Floor Sensor Type",
            //description: "",
            defaultValue: "10k ntc (Default)",
            required: false, 
            options: sensOptions, 
            displayDuringSetup: false
            
            input "ALo",
        	"number",
            range: "5..40",
            title: "ALo: Air min limit",
            description: "Minimum Limit for Air sensor (5°-40°)",
			defaultValue: 5,
			required: false,
            displayDuringSetup: false
            
            input "AHi",
        	"number",
            range: "5..40",
            title: "AHi: Air max limit",
            description: "Maximum Limit for Air sensor  (5°-40°)",
			defaultValue: 40,
			required: false,
            displayDuringSetup: false
            
            input "PLo",
        	"number",
            range: "0..9",
            title: "PLo: FP-mode P setting",
            description: "FP-mode P setting (0 - 9)",
			defaultValue: 0,
			required: false,
            displayDuringSetup: false
            
            def pOptions = ["0%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%"]
            input "PSet",
        	"enum",
            //range: "0..100",
            title: "PSetting",
            description: "Power Regulator setting (0 - 100%)",
			defaultValue: "20%",
			required: false,
            options: pOptions,
            displayDuringSetup: false
           
}
/*    
def updated() {
	if (settings.tempSen == null) settings.tempSen = "A - Room temperature mode"
    configure()
}
*/ 
metadata {
	definition (name: "heatit Thermostat", namespace: "heatit", author: "AdamV") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
        capability "Thermostat Mode"
        capability "Thermostat Heating Setpoint"
        capability "Thermostat Setpoint"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
        capability "Refresh"
		
		//attribute "thermostatFanState", "string"

		command "switchMode"
        command "energySaveHeat"
        command "quickSetHeat"
        command "quickSetecoHeat"
        command "pressUp"
        command "pressDown"

		fingerprint deviceId: "0x0806"
		fingerprint inClusters: "0x5E, 0x43, 0x31, 0x86, 0x40, 0x59, 0x85, 0x73, 0x72, 0x5A, 0x70"
	}

	tiles (scale: 2){
		
        	multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) {
  			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
    		attributeState("default", label:'${currentValue}°', unit:"C", action:"switchMode", icon:"st.Home.home1")
  		}
  			     
		/*	tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
			attributeState "heat", action:"quickSetHeat"
        }
        */    
            tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
    		attributeState("VALUE_UP", action: "pressUp")
    		attributeState("VALUE_DOWN", action: "pressDown")
  		}
  			//tileAttribute("device.heatingSetpoint", key: "SECONDARY_CONTROL") {
            tileAttribute("device.tempSenseMode", key: "SECONDARY_CONTROL") {
    		attributeState("default", label:'${currentValue}', unit:"", icon:" ")
  			//attributeState("default", label:'${currentValue}°', unit:"°", icon:"st.Weather.weather2")
        
        }
  			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    		attributeState("idle", backgroundColor:"#44b621")
  			attributeState("heating", backgroundColor:"#bc2323")
            attributeState("energySaveHeat", backgroundColor:"#ffa81e")
  		}
  			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    		attributeState("off", label:'${name}', action:"switchMode", nextState:"heat")
    		attributeState("heat", label:'${name}', action:"switchMode", nextState:"energy")
            attributeState("energySaveHeat", label:'${name}', action:"switchMode", nextState:"off")

  		}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
    		attributeState("default", label:'${currentValue}')
  		}

        

}
        /*
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
		
        */
        
        valueTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "off", label:'${name}', action:"switchMode", nextState:"to_heat"
			state "heat", label:'${name}', action:"switchMode", nextState:"energySaveHeat"
			state "energySaveHeat", label: "eco heat", action:"switchMode", nextState:"off"
		}
        valueTile("heatLabel", "device.thermostatMode", inactiveLabel: false, decoration: "flat", height: 1, width: 4) {
			state "default", label:"Heat Set Point:" 
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range: "(5..40)") {
			state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
		}
        valueTile("ecoLabel", "device.thermostatMode", inactiveLabel: false, decoration: "flat", height: 1, width: 4) {
			state "default", label:"Eco Mode Set Point:" 
		}
		controlTile("ecoheatSliderControl", "device.ecoheatingSetpoint", "slider", height: 1, width: 4, inactiveLabel: false, range: "(5..40)") {
			state "setecoHeatingSetpoint", action:"quickSetecoHeat", backgroundColor:"#d04e00"
		}
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", height: 2, width: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        
		main "thermostatMulti"
		details(["thermostatMulti", "mode", "heatLabel", "heatSliderControl", "refresh", "ecoLabel", "ecoheatSliderControl", "configure"])
	}
}
def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
		log.debug("An error has occurred")
		} 
    else {
       
       	def cmd = zwave.parse(description.replace("98C1", "9881"), [0x98: 1, 0x20: 1, 0x84: 1, 0x80: 1, 0x60: 3, 0x2B: 1, 0x26: 1])
        if (cmd) {
       	results = zwaveEvent(cmd)
		}
    }
}
// Event Generation
def zwaveEvent(hubitat.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
    
   	if (cmd.setpointType == 1){
        def heating = cmd.scaledValue
        sendEvent(name: "heatingSetpoint", value: heating)
    }
    if (cmd.setpointType == 2){
    	def energyHeating = cmd.scaledValue
        sendEvent(name: "ecoHeatingSetpoint", value: energyHeating)
        state.ecoheatingSetpoint = energyHeating
    }
   	
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	//map
}

def zwaveEvent(hubitat.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
	log.debug("Sensor report: $cmd")
    
    //def precision = cmd.precision / 10
    log.debug("Precision: $precision °C")
    if (cmd.scale == 0){
    log.debug("Scale is Celcius")
    }
    log.debug("Air Temperature is: $cmd.scaledSensorValue °C")
    sendEvent(name: "temperature", value: cmd.scaledSensorValue) 

   
}

def zwaveEvent(hubitat.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd)
{
	log.debug("operating rep: $cmd")
	def map = [:]
	switch (cmd.operatingState) {
		case hubitat.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
			break
		case hubitat.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
			break
	}
	map.name = "thermostatOperatingState"
	map
}

def zwaveEvent(hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [:]
	switch (cmd.mode) {
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
            sendEvent(name: "thermostatOperatingState", value: "idle")
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
            sendEvent(name: "thermostatOperatingState", value: "heating")
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergency heat"
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			break
		case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
			break
        case hubitat.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_ENERGY_SAVE_HEAT:
			map.value = "energySaveHeat"
            sendEvent(name: "thermostatOperatingState", value: "energySaveHeat")
			break
	}
	map.name = "thermostatMode"
	map
}

def zwaveEvent(hubitat.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	log.debug("support reprt: $cmd")
    def supportedModes = ""
	if(cmd.off) { supportedModes += "off " }
	if(cmd.heat) { supportedModes += "heat " }
	if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergency heat " }
	if(cmd.cool) { supportedModes += "cool " }
	if(cmd.auto) { supportedModes += "auto " }
    if(cmd.energySaveHeat) { supportedModes += "energySaveHeat " }

	state.supportedModes = supportedModes
}

def zwaveEvent(hubitat.zwave.commands.configurationv2.ConfigurationReport cmd) {
    
    if (cmd.parameterNumber == 1){
    	if (cmd.configurationValue == [0, 0]){
            log.debug("Current Mode is Off")
            sendEvent(name: "thermostatMode", value: "off")
            sendEvent(name: "thermostatOperatingState", value: "idle")
        }
        else if (cmd.configurationValue == [0, 1]){
            log.debug("Current Mode is Heat")
            sendEvent(name: "thermostatMode", value: "heat")
            sendEvent(name: "thermostatOperatingState", value: "heating")
        }
        else if (cmd.configurationValue == [0, 2]){
            log.debug("Current Mode is Cool")
        }
        else if (cmd.configurationValue == [0, 11]){
            log.debug("Current Mode is Energy Save Heat")
            sendEvent(name: "thermostatMode", value: "energySaveHeat")
            sendEvent(name: "thermostatOperatingState", value: "energySaveHeat")
        }
	}
    if (cmd.parameterNumber == 2){
    	if (cmd.configurationValue == [0, 0]){
            log.debug("Temperature Sensor F - Floor mode: Regulation is based on the floor temperature sensor reading")
        	sendEvent(name: "tempSenseMode", value: "F")
        }
        else if (cmd.configurationValue == [0, 1]){
            log.debug("Temperature Sensor A - Room temperature mode: Regulation is based on the measured room temperature using the internal sensor (Default)")
        	sendEvent(name: "tempSenseMode", value: "A")
        }
        else if (cmd.configurationValue == [0, 2]){
            log.debug("Temperature Sensor AF - Room mode w/floor limitations: Regulation is based on internal room sensor but limited by the floor temperature sensor (included) ensuring that the floor temperature stays within the given limits (FLo/FHi")
        	sendEvent(name: "tempSenseMode", value: "AF")
        }
        else if (cmd.configurationValue == [0, 3]){
            log.debug("Temperature Sensor 2 - Room temperature mode: Regulation is based on the measured room temperature using the external sensor")
        	sendEvent(name: "tempSenseMode", value: "A2")
        }
        else if (cmd.configurationValue == [0, 4]){
            log.debug("Temperature Sensor P (Power regulator): Constant heating power is supplied to the floor. Power rating is selectable in 10% increments ( 0% - 100%)")
        	sendEvent(name: "tempSenseMode", value: "P")
        }
         else if (cmd.configurationValue == [0, 5]){
            log.debug("Temperature Sensor FP - Floor mode with minimum power limitation: Regulation is based on the floor temperature sensor reading, but will always heat with a minimum power setting (PLo)")
        	sendEvent(name: "tempSenseMode", value: "FP")
        }
	}
    if (cmd.parameterNumber == 3){
    	if (cmd.configurationValue == [0, 0]){
            log.debug("Floor sensor type 10k ntc (Default)")
        }
        else if (cmd.configurationValue == [0, 1]){
            log.debug("Floor sensor type 12k ntc")
        }
        else if (cmd.configurationValue == [0, 2]){
            log.debug("Floor sensor type 15k ntc")
        }
        else if (cmd.configurationValue == [0, 3]){
            log.debug("Floor sensor type 22k ntc")
        }
        else if (cmd.configurationValue == [0, 4]){
            log.debug("Floor sensor type 33k ntc")
        }
         else if (cmd.configurationValue == [0, 5]){
            log.debug("Floor sensor type 47k ntc")
        }
	}
    if (cmd.parameterNumber == 4){
    	
       def val = cmd.configurationValue[1]
       def diff = val / 10
       def newHys = 0.2 + diff
       log.debug("DIFF l. Temperature control Hysteresis is $newHys °C")
	}
    if (cmd.parameterNumber == 5){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
       
       log.debug("FLo: Floor min limit is $diff °C")
	}
    if (cmd.parameterNumber == 6){
    	
     	def valX1 = cmd.configurationValue[0]
       	def valY1 = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX1, valY1)
       log.debug("FHi: Floor max limit is $diff °C")
	}
    if (cmd.parameterNumber == 7){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
       log.debug("ALo: Air min limit is $diff °C")
	}
    if (cmd.parameterNumber == 8){
    	
        def valX1 = cmd.configurationValue[0]
       	def valY1 = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX1, valY1)
       log.debug("AHi: Air max limit is $diff °C")
	}
    if (cmd.parameterNumber == 9){
    	
       def val = cmd.configurationValue[1]
       log.debug("PLo: Min temperature in Power Reg Mode is $val °C")
	}
    if (cmd.parameterNumber == 10){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
       log.debug("CO mode setpoint is $diff °C")
	}
    if (cmd.parameterNumber == 11){
    	
        def valX = cmd.configurationValue[0]
       	def valY = cmd.configurationValue[1]
       	def diff = binaryToDegrees(valX, valY)
       log.debug("ECO mode setpoint is $diff °C")
       sendEvent(name: "ecoheatingSetpoint", value: diff)
	}
    if (cmd.parameterNumber == 12){
    	
       def val = cmd.configurationValue[0]
       def diff = val * 10
       log.debug("P (Power regulator) is $diff %")
	}
}
    
def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Basic Zwave event received: $cmd.payload"
}

def pressUp(){
	log.debug("pressed Up")
	def currTemp = device.latestValue("heatingSetpoint")
    log.debug(" pressed up currently $currTemp")
    def newTemp = currTemp + 0.5
    log.debug(" pressed up new temp is $newTemp")
	quickSetHeat(newTemp)
}

def pressDown(){
	log.debug("pressed Down")
	def currTemp = device.latestValue("heatingSetpoint")
    def newTemp = currTemp - 0.5
	quickSetHeat(newTemp)
}

def poll() {
	delayBetween([
		zwave.sensorMultilevelV5.sensorMultilevelGet().format(), // current temperature
		zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format(),
		zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 2).format(),
        zwave.thermostatModeV2.thermostatModeGet().format(),
        zwave.configurationV2.configurationGet(parameterNumber: 1).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 2).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 3).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 4).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 5).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 6).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 7).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 8).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 9).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 10).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 11).format(),
        zwave.configurationV2.configurationGet(parameterNumber: 12).format()
	], 650)
}

def configure() {
    
    def floorMinY = 0
    def floorMinX = 0
    def floorMin = 0
    if (FLo){
    	floorMin = FLo
        if (floorMin <= 25.5){
        	floorMinX = 0
        	floorMinY = floorMin*10
        }
        else if (floorMin > 25.5){
            floorMinX = 1
        	floorMinY = floorMin*10 - 256
        }
    }
    def floorMax = 0
    def floorMaxY = 0
    def floorMaxX = 0
    if (FHi){
    		floorMax = FHi
    	    if (floorMax <= 25.5){
        	floorMaxX = 0
        	floorMaxY = floorMax*10
        }
        else if (floorMax > 25.5){
            floorMaxX = 1
        	floorMaxY = floorMax*10 - 256
        }
    }
    def AirMin = 0
    def AirMinY = 0
    def AirMinX = 0
    if (ALo){
    		AirMin = ALo
    	    if (AirMin <= 25.5){
        	AirMinX = 0
        	AirMinY = AirMin*10
        }
        else if (AirMin > 25.5){
            AirMinX = 1
        	AirMinY = AirMin*10 - 256
        }
    }
    def AirMax = 0
    def AirMaxY = 0
    def AirMaxX = 0
    if (AHi){
    		AirMax = AHi
    	    if (AirMax <= 25.5){
        	AirMaxX = 0
        	AirMaxY = AirMax*10
        }
        else if (AirMax > 25.5){
            AirMaxX = 1
        	AirMaxY = AirMax*10 - 256
        }
    }
    def tempSensorMode = ""
    def tempModeParam = 1
    if (tempSen){
    tempSensorMode = tempSen
        if (tempSensorMode == "F - Floor temperature mode"){
        tempModeParam = 0
        }
        if (tempSensorMode == "A - Room temperature mode"){
        tempModeParam = 1
        }
        if (tempSensorMode == "AF - Room mode w/floor limitations"){
        tempModeParam = 2
        }
        if (tempSensorMode == "A2 - Room temperature mode (external)"){
        tempModeParam = 3
        }
       	if (tempSensorMode == "P - Power regulator mode"){
        tempModeParam = 4
        }
        if (tempSensorMode == "FP - Floor mode with minimum power limitation"){
        tempModeParam = 5
        }
    }
    def floorSensor = ""
    def floorSensParam = 0
    	if (sensorType){
        floorSensor = sensorType
            if (floorSensor == "10k ntc (Default)"){
            floorSensParam = 0
            }
            if (floorSensor == "12k ntc"){
            floorSensParam = 1
            }
            if (floorSensor == "15k ntc"){
            floorSensParam = 2
            }
            if (floorSensor == "22k ntc"){
            floorSensParam = 3
            }
            if (floorSensor == "33k ntc"){
            floorSensParam = 4
            }
            if (floorSensor == "47k ntc"){
            floorSensParam = 5
            }
        }
   	def powerLo = 0
    	if (PLo){
        	powerLo = PLo  
        }
    def powerSet = 0
    def powerSetPer = ""
    	if (PSet){
        powerSetPer = PSet
            if (powerSetPer == "0%"){
        	powerSet = 0
       		}
            if (powerSetPer == "10%"){
        	powerSet = 1
       		}
            if (powerSetPer == "20%"){
        	powerSet = 2
       		}
            if (powerSetPer == "30%"){
        	powerSet = 3
            log.debug("powerset 3")
       		}
            if (powerSetPer == "40%"){
        	powerSet = 4
       		}
            if (powerSetPer == "50%"){
        	powerSet = 5
       		}
            if (powerSetPer == "60%"){
        	powerSet = 6
       		}
            if (powerSetPer == "70%"){
        	powerSet = 7
       		}
            if (powerSetPer == "80%"){
        	powerSet = 8
       		}
            if (powerSetPer == "90%"){
        	powerSet = 9
       		}
            if (powerSetPer == "100%"){
        	powerSet = 10
       		}
        }

    
	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(),
        zwave.configurationV2.configurationSet(configurationValue: [0, tempModeParam], parameterNumber: 2, size: 2).format(),
      	zwave.configurationV2.configurationSet(configurationValue: [0, floorSensParam], parameterNumber: 3, size: 2).format(),
       // zwave.configurationV2.configurationSet(configurationValue: [0, 0], parameterNumber: 3, size: 2).format(),
       	zwave.configurationV2.configurationSet(configurationValue: [floorMinX, floorMinY], parameterNumber: 5, size: 2).format(),
        //zwave.configurationV2.configurationSet(configurationValue: [0, 50], parameterNumber: 5, size: 2).format(),
       	zwave.configurationV2.configurationSet(configurationValue: [floorMaxX, floorMaxY], parameterNumber: 6, size: 2).format(),
        zwave.configurationV2.configurationSet(configurationValue: [AirMinX, AirMinY], parameterNumber: 7, size: 2).format(),
        zwave.configurationV2.configurationSet(configurationValue: [AirMaxX, AirMaxY], parameterNumber: 8, size: 2).format(),
        zwave.configurationV2.configurationSet(configurationValue: [powerLo], parameterNumber: 9, size: 1).format(),
        zwave.configurationV2.configurationSet(configurationValue: [powerSet], parameterNumber: 12, size: 1).format(),//zwave.configurationV2.configurationSet(configurationValue: [1, 144], parameterNumber: 6, size: 2).format(),
        //zwave.configurationV2.configurationSet(configurationValue: [1, 144], parameterNumber: 6, size: 2).format(),
        zwave.thermostatModeV2.thermostatModeSupportedGet().format(),
        poll()
	], 650)
    
}

def modes() {
	["off", "heat", "energySaveHeat"]
}

def binaryToDegrees(x, y) {
	def degrees = 0
    def preDegrees = 0
    if (x == 0){
    	degrees = y / 10
    }
    else if (x == 1){
    	preDegrees = y + 256
        degrees = preDegrees / 10
    }
    
    return degrees
}

def switchMode() {
	
    def currentMode = device.currentState("thermostatMode")?.value
   // log.debug("currentMode is $currentMode")
    if (currentMode == "off"){
    	def nextMode = "heat"
        sendEvent(name: "thermostatMode", value: "heat")
        sendEvent(name: "thermostatOperatingState", value: "heating")
        delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        poll()
	], 650)
    }
    else if (currentMode == "heat"){
    	def nextMode = "energySaveHeat"
        sendEvent(name: "thermostatMode", value: "energySaveHeat")
        sendEvent(name: "thermostatOperatingState", value: "energySaveHeat")
        delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 11).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        poll()
	], 650)
    }
    else if (currentMode == "energySaveHeat"){
    	def nextMode = "off"
        sendEvent(name: "thermostatMode", value: "off")
        sendEvent(name: "thermostatOperatingState", value: "idle")
        delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 0).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        poll()
	], 650)
    }
    



}

def switchToMode(nextMode) {
	def supportedModes = getDataByName("supportedModes")
	if(supportedModes && !supportedModes.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"
	if (nextMode in modes()) {
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def getModeMap() { [
	"off": 0,
	"heat": 1,
	"energySaveHeat": 11
]}

def setThermostatMode(String value) {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

def off() {
        delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 0).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        sendEvent(name: "thermostatMode", value: "off"),
        sendEvent(name: "thermostatOperatingState", value: "idle"),
        poll()
	], 650)
    	
}

def heat() {
        delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        sendEvent(name: "thermostatMode", value: "heat"),
        sendEvent(name: "thermostatOperatingState", value: "heating"),
        poll()
	], 650)
}

def energySaveHeat() {
        delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 11).format(),
		zwave.thermostatModeV2.thermostatModeGet().format(),
        sendEvent(name: "thermostatMode", value: "energySaveHeat"),
        sendEvent(name: "thermostatOperatingState", value: "energySaveHeat"),
        poll()
	], 650)
}

def auto() {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 3).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], standardDelay)
}

private getStandardDelay() {
	1000
}
