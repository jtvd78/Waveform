package com.hoosteen.waveform;

import com.hoosteen.Tools;

public class WaveformStart {
	
	public static final boolean running = true;
	public static WaveformWindow mainWindow;
	
	public static void main(String[] args){
		
		//Make it look nice
		Tools.setNativeUI();
		
		mainWindow = new WaveformWindow();
		mainWindow.begin();
		
		
	}

}
