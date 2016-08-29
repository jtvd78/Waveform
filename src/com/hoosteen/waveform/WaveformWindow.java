package com.hoosteen.waveform;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.hoosteen.file.FileComp;
import com.hoosteen.file.FileNode;
import com.hoosteen.tree.NodeEvent;
import com.hoosteen.tree.NodeEventListener;
import com.hoosteen.waveform.graphics.FreqComp;
import com.hoosteen.waveform.graphics.PlayerControlPanel;
import com.hoosteen.waveform.graphics.SoundPlayer;
import com.hoosteen.waveform.graphics.WaveformComp;

public class WaveformWindow extends JFrame{
	
	FreqComp fc;
	WaveformComp wc;	
	
	FileComp fileComp;
	
	SoundPlayer player = new SoundPlayer();
	PlayerControlPanel playerControls = new PlayerControlPanel(player);
	
	public static boolean running = true;


	public WaveformWindow(){		
		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		wc = new WaveformComp(player);	
		fc = new FreqComp(player);		
		
		player.addPlayerListener(wc);
		player.addPlayerListener(fc);
		
		fileComp = new FileComp(this, new FileNode(new File("D:\\"))); 
		fileComp.allowNodeRemoval(false);
		
		fileComp.addNodeEventListner(new NodeListener());
		
		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, wc, fc);
		JSplitPane waveformFreqSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, fileComp, leftRight);
		
		JPanel topBottom = new JPanel();
		topBottom.setLayout(new BorderLayout());
		
	//	JSplitPane topBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, leftRight,new PlayerControlPanel(player));
		
		leftRight.setDividerLocation(400);
		waveformFreqSplit.setDividerLocation(200);
		topBottom.add(waveformFreqSplit, BorderLayout.CENTER);
		topBottom.add(playerControls, BorderLayout.SOUTH);
		
		
		add(topBottom);
		
		
		setVisible(true); 
	}
	
	class NodeListener implements NodeEventListener{

		@Override
		public void nodeRightClicked(String text, NodeEvent event) {
			if(text.equals("Load Sound")){				
				Sound newSound  = new Sound(((FileNode) event.getNode()).getFile().getPath());
				player.play(newSound);
			}
		}

		@Override
		public void nodeLeftClicked(NodeEvent nodeEvent) {
			//Do nothing
		}

		@Override
		public void nodeDoubleClicked(NodeEvent nodeEvent) {
			nodeEvent.getNode().toggleExpanded();
		}		
	}
	
	public void begin(){	
		
		//Begin the main loop		
		wc.begin();
		
		while(true){
			playerControls.repaint();
			
			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}