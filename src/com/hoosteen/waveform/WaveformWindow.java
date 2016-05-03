package com.hoosteen.waveform;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import com.hoosteen.file.FileComp;
import com.hoosteen.file.FileNode;
import com.hoosteen.waveform.graphics.FreqComp;
import com.hoosteen.waveform.graphics.WaveformComp;

public class WaveformWindow extends JFrame{
	
	Sound sound;
	FreqComp fc;
	WaveformComp wc;	
	
	FileComp fileComp;

	public static final boolean running = true;
	public static WaveformWindow mainWindow;
	
	public static void main(String[] args){
		mainWindow = new WaveformWindow();
		mainWindow.run();
	}
	
	public WaveformWindow(){		
		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		//sound = new Sound("D:\\Justin\\Music\\Trap Songs\\Buku___Fullagold____Out_Now_on_Never_Say_Die_Records___.mp3");
		//sound = new Sound("D:\\justin\\Temp\\WaveformSongCache\\Buku - All Deez (Jauz Hoestep Bootleg).wav");
		//sound = new Sound("D:\\Justin\\Music\\Without Me.wav");
		
		//Load the sound
		wc = new WaveformComp(sound);	
		fc = new FreqComp();
		
		fileComp = new FileComp(this, new FileNode(new File("C:\\Users\\justi\\Music\\")));
		
		
		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, wc, fc);
		JSplitPane topBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, leftRight,fileComp);
		
		
		leftRight.setDividerLocation(400);
		topBottom.setDividerLocation(200);
		add(topBottom);
		
		setVisible(true);
	}
	
	public void setSound(Sound s){
		if(sound != null){
			sound.stop();
		}
		
		sound = s;
		wc.setSound(s);
		fc.setSound(s);
	}
	
	public void run(){			
		//Begin the main loop
		mainLoop();
	}
	
	public void mainLoop(){
		
		Runnable loop = new Runnable(){
			public void run(){
			//	if(!running){
			//		System.exit(-1);
			//	}
				
				
				wc.update();
				wc.repaint();
			}
		};

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(loop, 0, 17, TimeUnit.MILLISECONDS);
		
	}
}