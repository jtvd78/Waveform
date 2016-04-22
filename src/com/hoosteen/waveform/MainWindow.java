package com.hoosteen.waveform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

import com.hoosteen.waveform.graphics.FreqComp;
import com.hoosteen.waveform.graphics.WaveformComp;

public class MainWindow extends JFrame{
	
	Sound sound;
	FreqComp fc;
	WaveformComp wc;
	

	public static final boolean running = true;
	
	public static void main(String[] args){
		new MainWindow().run();
	}
	
	public MainWindow(){
		
		
		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		sound = new Sound("D:\\Justin\\Music\\Trap Songs\\Buku___Fullagold____Out_Now_on_Never_Say_Die_Records___.mp3");
		//sound = new Sound("D:\\justin\\Temp\\WaveformSongCache\\Buku - All Deez (Jauz Hoestep Bootleg).wav");
		//sound = new Sound("D:\\Justin\\Music\\Without Me.wav");
		
		//Load the sound
		wc = new WaveformComp(sound);	
		fc = new FreqComp(sound);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, wc, fc);
		jsp.setDividerLocation(400);
		add(jsp);
		
		setVisible(true);
	}
	
	public void run(){			
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
}