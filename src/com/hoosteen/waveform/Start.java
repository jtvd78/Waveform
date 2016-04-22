package com.hoosteen.waveform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
		sound = new Sound("D:\\Justin\\Music\\Trap Songs\\Buku___Fullagold____Out_Now_on_Never_Say_Die_Records___.mp3");
		//sound = new Sound("D:\\justin\\Temp\\WaveformSongCache\\Buku - All Deez (Jauz Hoestep Bootleg).wav");
		//sound = new Sound("D:\\Justin\\Music\\Without Me.wav");
		
		//Load the sound
		wc = new WaveformComp(sound);
		makeWindow(wc);		
		
		fc = new FreqComp(sound);
		makeWindow(fc);
				
		//Begin the main loop
		mainLoop();
	}
	
	public void mainLoop(){
		
		Runnable loop = new Runnable(){
			public void run(){
				if(!running){
					System.exit(-1);
				}
				wc.update();			
				wc.repaint();
			}
		};

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(loop, 0, 17, TimeUnit.MILLISECONDS);
		
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