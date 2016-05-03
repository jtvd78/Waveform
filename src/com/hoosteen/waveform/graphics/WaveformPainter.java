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
	
	private BufferedImage paintWaveform(){
		
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
		
		//Init point arrays, add first point at x=-1
		ArrayList<Integer> yPointsLeftTop = new ArrayList<Integer>();
		yPointsLeftTop.add(height/4);
		ArrayList<Integer> yPointsLeftBottom = new ArrayList<Integer>();
		yPointsLeftBottom.add(height/4);
		
		ArrayList<Integer> yPointsRightTop = new ArrayList<Integer>();
		yPointsRightTop.add(3*height/4);
		ArrayList<Integer> yPointsRightBottom = new ArrayList<Integer>();		
		yPointsRightBottom.add(3*height/4);
		
		ArrayList<Integer> xPoints = new ArrayList<Integer>();		
		xPoints.add(-1);
		
		int[][] section = sound.getSection(startPos, endPos, width);
		 
		for(int i = 0; i < section.length; i++){
			
			int[] frame = section[i];
			xPoints.add(i);
			
			//Left
			if(frame[0] > 0){
				yPointsLeftTop.add(frame[0]*height/(2*65535) + height/4);
				yPointsLeftBottom.add(height/4);
			}else{
				yPointsLeftBottom.add(frame[0]*height/(2*65535) + height/4);
				yPointsLeftTop.add(height/4);
			}
			
			//Right
			if(frame[1] > 0){
				yPointsRightTop.add(frame[1]*height/(2*65535) + 3*height/4);
				yPointsRightBottom.add(3*height/4);
				
			}else{
				yPointsRightBottom.add(frame[1]*height/(2*65535) + 3*height/4);
				yPointsRightTop.add(3*height/4);
			}
		}	
		
		yPointsLeftTop.add(height/4);
		yPointsLeftBottom.add(height/4);
		yPointsRightTop.add(3*height/4);	
		yPointsRightBottom.add(3*height/4);
		
		xPoints.add(width+1);	
		
		int pointCount = xPoints.size();
		int[] xPointsList = toIntArray(xPoints);
		//left
		g.setColor(Color.red);
		
		g.fillPolygon(xPointsList, toIntArray(yPointsLeftTop), pointCount); //top
		g.fillPolygon(xPointsList, toIntArray(yPointsLeftBottom), pointCount); //bottom
		
		//right
		g.setColor(Color.blue);
		g.fillPolygon(xPointsList, toIntArray(yPointsRightTop),pointCount); //top
		g.fillPolygon(xPointsList, toIntArray(yPointsRightBottom), pointCount); //bottom				
		
		return bi;
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
		
		ArrayList<Integer> yPointsLeft = new ArrayList<Integer>();
		yPointsLeft.add(height/4);
		
		ArrayList<Integer> yPointsRight = new ArrayList<Integer>();
		yPointsRight.add(3*height/4);
		
		ArrayList<Integer> xPoints = new ArrayList<Integer>();		
		xPoints.add(-1);
		
		int[][] section = sound.getSectionMinMax(startPos, endPos, width);
		
		//Top
		for(int i = 0; i < section.length; i++){
			
			int[] frame = section[i];
			xPoints.add(i);
			
			yPointsLeft.add(frame[1]*height/(2*65535) + height/4);			
			yPointsRight.add(frame[3]*height/(2*65535) + 3*height/4);				
			
		}
		
		//Bottom
		for(int i = section.length - 1; i >= 0; i--){
			
			int[] frame = section[i];
			xPoints.add(i);
			
			yPointsLeft.add(frame[0]*height/(2*65535) + height/4);
			yPointsRight.add(frame[2]*height/(2*65535) + 3*height/4);
		}	
		
		int pointCount = xPoints.size();
		int[] xPointsList = toIntArray(xPoints);
		//left
		g.setColor(Color.red);
		
		g.fillPolygon(xPointsList, toIntArray(yPointsLeft), pointCount); //top
		
		//right
		g.setColor(Color.blue);
		g.fillPolygon(xPointsList, toIntArray(yPointsRight), pointCount); //top			
		
		return bi;
	}
	
	
	private int[] toIntArray(ArrayList<Integer> arrList){
		 
		int[] out = new int[arrList.size()];
		for(int i = 0; i < arrList.size(); i++){
			out[i] = arrList.get(i).intValue();
		}
		return out;
	}

	public void setSound(Sound s) {
		this.sound = s;
	}
}