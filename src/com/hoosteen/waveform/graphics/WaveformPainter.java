package com.hoosteen.waveform.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.hoosteen.waveform.Sound;
import com.hoosteen.waveform.WaveformWindow;

public class WaveformPainter {
	
	//Width and height of waveform
	int width;
	int height;
	
	//The sound being played
	Sound sound;
	
	//0.0 is beginning, 1.0 is end
	float startPos;
	float endPos;
	
	//True when a new waveform needs to be generated
	boolean changed = false;
	
	public void loop(final ImageHandler h){
		
		//Begin generating image in a new thread
		new Thread(new Runnable(){
			public void run(){
				while(WaveformWindow.running){
					
					//The image will only update if the input information has changed
					if(changed){
						
						//Generate the image and send it to the component
						BufferedImage newImage = paintWaveformMinMax();
						h.handleImage(newImage);
						
						//Set changed to false since the image was generated
						changed = false;
					}					
					
					//Sleep for a while because we won't need this to be that fast (max 60fps)
					try {
						Thread.sleep(17);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		
	}
	
	/**
	 * Updates the painter with important information
	 * @param width Width of Waveform
	 * @param height Height of Waveform
	 * @param startPos Starting position to be drawn (0.0 - 1.0)
	 * @param endPos Ending position to be drawn (0.0 - 1.0)
	 */
	public void update(int width, int height, float startPos, float endPos){
		
		//If anything is changed, set the boolean so the graphics can be redrawn
		if(width != this.width || height != this.height || startPos != this.startPos || endPos != this.endPos){
			changed = true;
		}
		
		//Save the data
		this.width = width;
		this.height = height;
		
		this.startPos = startPos;
		this.endPos = endPos;
	}
	
	private BufferedImage paintWaveformMinMax(){

		if(sound == null){
			return null;
		}		
		
		//If the sound is not finished loading, don't start
		if(!sound.isFinished()){			
			return null;
		}
		
		//Don't start if we don't have a width and height
		if(width == 0 || height == 0){
			return null;
		}
		
		//Create image and draw to it
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();

		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0,0,width, height);
		
		int lastLeftBottom = 0;
		int lastLeftTop = 0;
		int lastRightBottom = 0;
		int lastRightTop = 0;
		
		boolean first = true;
		
		int[][] section = sound.getSectionMinMax(startPos, endPos, width);
		
		int[] frame;
		for(int i = 0; i < section.length; i++){
			
			frame = section[i];
			
			int leftBottom = frame[0];
			int leftTop = frame[1];
			int rightBottom = frame[2];
			int rightTop = frame[3];
			
			g.setColor(Color.RED);
			g.drawLine(i, leftTop*height/(2*65535) + height/4, i, leftBottom*height/(2*65535) + height/4);
			
			g.setColor(Color.BLUE);
			g.drawLine(i, rightTop*height/(2*65535) + 3*height/4, i, rightBottom*height/(2*65535) + 3*height/4);	
			
			
			if(!first){
				g.setColor(Color.RED);
				g.drawLine(i-1, lastLeftTop*height/(2*65535) + height/4, i, leftTop*height/(2*65535) + height/4);
				g.drawLine(i-1, lastLeftBottom*height/(2*65535) + height/4, i, lastLeftBottom*height/(2*65535) + height/4);
				
				g.setColor(Color.BLUE);
				g.drawLine(i-1, lastRightTop*height/(2*65535) + 3*height/4, i, rightTop*height/(2*65535) + 3*height/4);	
				g.drawLine(i-1, lastRightBottom*height/(2*65535) + 3*height/4, i, rightBottom*height/(2*65535) + 3*height/4);	
			}else{
				first = false;
			}
			
			lastRightTop = rightTop;
			lastRightBottom = rightBottom;
			lastLeftTop = leftTop;
			lastLeftBottom = leftBottom;
			
		}
		
		return bi;
	}

	public void setSound(Sound s) {
		this.sound = s;
	}

	public void soundLoaded() {
		changed = true;
	}
}