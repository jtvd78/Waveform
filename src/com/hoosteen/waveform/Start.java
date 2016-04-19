package com.hoosteen.waveform;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.hoosteen.waveform.graphics.FreqComp;
import com.hoosteen.waveform.graphics.WaveformComp;

public class Start {
	
	Sound sound;
	FreqComp fc;
	WaveformComp wc;
	

	public static final boolean running = true;
	
	public static void main(String[] args){
		new Start().run();
	}
	
	public void run(){
		//sound = new Sound("D:\\Justin\\Temp\\WaveformSongCache\\Flux Pavilion - Bass Cannon (Zomboy Remix) _HD_.wav");
		sound = new Sound("D:\\Justin\\Music\\Trap Songs\\Buku___Tip_Toe____Out_Now_on_Pilot_Records___.mp3");
		
		//Load the sound
		wc = new WaveformComp(sound);
		makeWindow(wc);		
		
		fc = new FreqComp(sound);
		makeWindow(fc);
				
		//Begin the main loop
		mainLoop();
	}
	
	public void mainLoop(){
		
		while(Start.running){
			wc.update();
			fc.update();
			
			wc.repaint();
			fc.repaint();
		}
	}
	
	public void makeWindow(JComponent comp){
		//Init a basic frame
		JFrame f = new JFrame();
		f.setSize(800,600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(comp);
		f.setVisible(true);	
	}
}