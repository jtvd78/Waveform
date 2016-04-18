package com.hoosteen.window;

import javax.swing.JFrame;

import com.hoosteen.waveform.Waveform;

public class Start {
	
	public static void main(String[] args){
		 
		JFrame f = new JFrame();
		f.setSize(800,600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		WaveformComp wc = new WaveformComp("D:\\Justin\\Music\\Trap Songs\\Tomsize____Oh_My_Gosh.mp3");
		//WaveformComp wc = new WaveformComp("D:\\Justin\\Downloads\\audiocheck.net_sweep_5Hz_20000Hz_-3dBFS_10s.wav");
		
		
		f.add(wc);
		f.setVisible(true);	
		
		wc.start();
	}
}