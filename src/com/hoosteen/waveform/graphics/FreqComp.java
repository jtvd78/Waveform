package com.hoosteen.waveform.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.hoosteen.fft.Complex;
import com.hoosteen.fft.FFT;
import com.hoosteen.waveform.Sound;

public class FreqComp extends JComponent implements ImageHandler{
	
	FreqPainter fp;
	
	float samplingRate;
	
	Sound sound;
	
	public FreqComp(Sound sound){	
		
		this.sound = sound;
		samplingRate = sound.getSamplingRate();
		
		fp = new FreqPainter(sound);
		fp.loop(this);
	}	

	public void paintComponent(Graphics g){
		
		int width = getWidth();
		int height = getHeight();
		
		fp.update(width,height);
		
		if(nextImage == null){			
			g.setColor(Color.red);
			g.drawString("Loading Song", width/2, height/2);
		}else{
			g.drawImage(nextImage, 0, 0, null);

		}
	}

	Image nextImage;
	
	@Override
	public void handleImage(Image i) {
		nextImage = i;
		repaint();
	}	
}