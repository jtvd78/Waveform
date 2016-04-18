package com.hoosteen.window;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.hoosteen.fft.Complex;
import com.hoosteen.fft.FFT;
import com.hoosteen.waveform.Waveform;


public class WaveformComp extends JComponent {
	
	Waveform wf;
	
	//0.0 is beginning, 1.0 is end;
	float startPos = 0.01f;
	float endPos = .99f;
	
	float songPosition;
	
	boolean running = true;
	
	
	int frameAdjConstant = 0;
	
	ArrayList<Integer> markerList = new ArrayList<Integer>();
	
	
	FreqWindow fw = new FreqWindow();
	
	
	public WaveformComp(String filepath){
		
		System.out.println("Waveform Comp");
		
		wf = new Waveform(filepath);
		
		Listener l = new Listener();
		addMouseListener(l);		
		addMouseWheelListener(l);
		addMouseMotionListener(l);
		addKeyListener(l);
		setFocusable(true);
	}
	
	public void start(){		
		FrameCounter fc = new FrameCounter();
		while(running){
			
			songPosition = wf.getSongPosition();
			update();
			repaint();
			
			fc.update();
		}
	}
	
	class FrameCounter{
		
		long lastTime;
		ArrayList<Long> frameList = new ArrayList<Long>();
		
		public FrameCounter(){
			lastTime = System.nanoTime();
		}
		
		public void update(){
			long timePassed = System.nanoTime() - lastTime;
			lastTime += timePassed;		
			
			frameList.add(timePassed);
			
			if(sum(frameList) > 1000000000){
				frameList.remove(0);
			}			
			
			try {
				Thread.sleep(8);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		//	System.out.println(frameList.size() + " FPS");
		}
		
		public long sum(ArrayList<Long> longList){
			
			long out = 0;
			for(Long l : longList){
				out += l;
			}
			
			return out;
		}		
	}
	
	public void update(){
		int x = (int) ((float)getWidth() * (songPosition - startPos) / (endPos -  startPos));
		if(wf.isPlaying() && x > getWidth()){
			float diff = endPos-startPos;
			startPos+= diff;
			endPos += diff;
		}
	}
	
	public void paintComponent(Graphics g){
		
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0,0,getWidth(), getHeight());		
		
		//hm
		if(wf.isFinished()){
			System.out.println("Finished!");
			
			int w = getWidth();	
			
			//Init point arrays, add first point at x=-1
			ArrayList<Integer> yPointsLeftTop = new ArrayList<Integer>();
			yPointsLeftTop.add(getHeight()/4);
			ArrayList<Integer> yPointsLeftBottom = new ArrayList<Integer>();
			yPointsLeftBottom.add(getHeight()/4);
			
			ArrayList<Integer> yPointsRightTop = new ArrayList<Integer>();
			yPointsRightTop.add(3*getHeight()/4);
			ArrayList<Integer> yPointsRightBottom = new ArrayList<Integer>();		
			yPointsRightBottom.add(3*getHeight()/4);
			
			ArrayList<Integer> xPoints = new ArrayList<Integer>();		
			xPoints.add(-1);
			
			int[][] section = wf.getSection(startPos, endPos, w);
			 
			for(int i = 0; i < w; i++){
				
				int[] frame = section[i];
				xPoints.add(i);
				
				//Left
				if(frame[0] > 0){
					yPointsLeftTop.add(frame[0]*getHeight()/(2*65535) + getHeight()/4);
					yPointsLeftBottom.add(getHeight()/4);
				}else{
					yPointsLeftBottom.add(frame[0]*getHeight()/(2*65535) + getHeight()/4);
					yPointsLeftTop.add(getHeight()/4);
				}
				
				//Right
				if(frame[1] > 0){
					yPointsRightTop.add(frame[1]*getHeight()/(2*65535) + 3*getHeight()/4);
					yPointsRightBottom.add(3*getHeight()/4);
					
				}else{
					yPointsRightBottom.add(frame[1]*getHeight()/(2*65535) + 3*getHeight()/4);
					yPointsRightTop.add(3*getHeight()/4);
				}
			}	
			
			yPointsLeftTop.add(getHeight()/4);
			yPointsLeftBottom.add(getHeight()/4);
			yPointsRightTop.add(3*getHeight()/4);	
			yPointsRightBottom.add(3*getHeight()/4);
			
			xPoints.add(w+1);	
			
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
			
			//song marker
			int x =(int) ((float)w * (songPosition - startPos) / (endPos - startPos));
			g.setColor(Color.white);
			g.drawLine(x, 0, x, getHeight());		
			
			int frameStart = wf.getFrameNumber(startPos);
			int frameEnd = wf.getFrameNumber(endPos);
			int frameCount =  frameEnd - frameStart;
			
			//beat markers
			
			//	int startFrame = 0;
			int beatWidth = averageDiff(markerList);	
			
			if(beatWidth != -1){
				
				float framesPerPixel = (float) frameCount / (float)w;
				
				int averagePos = averagePos(markerList);				
				
				int frameOffset = beatWidth - ((frameStart - averagePos - frameAdjConstant + beatWidth) % beatWidth);			
				
				int startX = (int) ((beatWidth - frameStart % beatWidth) / framesPerPixel);
				startX = (int) (frameOffset/framesPerPixel);
				
				g.setColor(Color.gray);
				for(int i = 0; i < frameCount / beatWidth + 2; i++){
					g.drawLine((int) (startX + i*beatWidth/framesPerPixel), 0, (int) (startX + i*beatWidth/framesPerPixel), getHeight());
				}
			}	
			
			//user set markers
			g.setColor(Color.red);
			
			int circleRad = 10;
			
			for(int i : markerList){
				if(i > frameStart && i < frameEnd){
					
					int xx = w * (i - frameStart)/frameCount;
					
					g.drawLine(xx,0,xx,getHeight());
					g.fillOval(xx-circleRad, 0, circleRad*2, circleRad*2);
				}
			}
			
			//fft
			
			System.out.println(songPosition);
			
		//	float window = 0.000625f;
			float window = 0.0005f;
			int length = (int)(window*2*wf.getFrameCount());
			
			//find max power of 2, less than fft.length
			int max = 2;
			while(max*2 < length){
				max*=2;
			}
			
			if(songPosition - window >= 0){
				int[][] fft = wf.getSection(songPosition - window, songPosition + window, max);
				
								
				
				Complex[] left = new Complex[max];
				Complex[] right = new Complex[max];
				
			    // original data
			    for (int i = 0; i < max; i++) {
			    	left[i] = new Complex(fft[i][0], 0);
			        right[i] = new Complex(fft[i][1], 0);
			    }

			    // FFT of original data
			    Complex[] leftFFT = FFT.fft(left);
			    Complex[] rightFFT = FFT.fft(right);			   
			    
			    double[] leftFFTdouble = new double[leftFFT.length];
			    double[] rightFFTdouble = new double[rightFFT.length];
			    
			    for(int i = 0; i < leftFFTdouble.length; i++){
			    	leftFFTdouble[i] = leftFFT[i].abs();
			    	rightFFTdouble[i] = rightFFT[i].abs();
			    }
				
				fw.update(leftFFTdouble,rightFFTdouble, wf.getSamplingRate());
			}		
		}else{
			g.setColor(Color.red);
			g.drawString("Loading Song", getWidth()/2, getHeight()/2);
		}
	}
	
	public double[] intArrToDoubleArr(int[] arr){
		double[] out = new double[arr.length];
		
		for(int i = 0; i < arr.length; i++){
			out[i] = arr[i];
		}
		return out;
	}
	
	public int averageDiff(ArrayList<Integer> list){
		
		if(list.size() < 2){
			return -1;
		}
		
		int out = 0;
		for(int i = 1; i < list.size(); i ++){
			out += list.get(i) - list.get(i-1);
		}
		
		return out/(list.size()-1);
	}
	
	public int averagePos(ArrayList<Integer> list){
		
		if(list.size() < 2){
			return -1;
		}
		
		int out = 0;
		for(int i = 1; i < list.size(); i ++){
			out += list.get(i);
		}
		
		return out/(list.size()-1);
		
	}
	
	private int[] toIntArray(ArrayList<Integer> arrList){
		 
		int[] out = new int[arrList.size()];
		for(int i = 0; i < arrList.size(); i++){
			out[i] = arrList.get(i).intValue();
		}
		return out;
	}	
	
	private class Listener implements MouseListener, MouseWheelListener, MouseMotionListener, KeyListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			
			System.out.println("Click");
			
			int circleRad = 10;
			
			int w = getWidth();
			int frameStart = wf.getFrameNumber(startPos);
			int frameEnd = wf.getFrameNumber(endPos);
			int frameCount =  frameEnd - frameStart;
			
			ArrayList<Integer> remove = new ArrayList<Integer>();
			
			if(e.getButton() == 1){
				for(Integer i : markerList){
					if(i > frameStart && i < frameEnd){
						
						int xx = w * (i - frameStart)/frameCount;						
						
						int centerX = xx; 
						int centerY = circleRad;
						
						if( Math.pow(e.getX() - centerX,2) +  Math.pow(e.getY() - centerY,2) <  Math.pow(circleRad,2)){
							//click inside
							remove.add(i);
						}						
					}
				}
			}			
			
			for(Integer i : remove){
				markerList.remove(i);
			}			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent e) {
		
			if(e.getButton() == 2){
				songPosition = startPos + (endPos-startPos)*((float)e.getX())/((float)getWidth());
				wf.play(songPosition);
				repaint();
			}else if(e.getButton() == 3){
				float clickSongPos = ((float)e.getX()/(float)getWidth())*(endPos - startPos) + startPos;
				int i = wf.getFrameNumber(clickSongPos);
				markerList.add(i);
			}else if(e.getButton() == 4){
				frameAdjConstant -= 200;
			}else if(e.getButton() == 5){
				frameAdjConstant += 200;
			}
			
			System.out.println(e.getButton());
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			
			int clicks = e.getWheelRotation();
			
			if(clicks < 0){
				zoomIn(e.getX());
			}else if(clicks > 0){
				zoomOut(e.getX());
			}
		}
		
		int lastX = 0;
		int lastY = 0;

		@Override
		public void mouseDragged(MouseEvent e) {
			
			if(e.getButton() == 0){
				float diff = endPos - startPos;
				
				float newStartPos = startPos - ((float)(e.getX() - lastX))*diff/((float)getWidth());
				float newEndPos = endPos - ((float)(e.getX() - lastX))*diff/((float)getWidth());
				
				if(! (newStartPos <0.0 || newEndPos > 1.0)){
					startPos = newStartPos;
					endPos = newEndPos;
				}
			}
			
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyChar() == ' '){
				wf.pause();
			}else if(e.getKeyChar() == 'b'){

				int i = wf.getFrameNumber(songPosition);
				markerList.add(i);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void keyTyped(KeyEvent e) {}
	}
	
	public void zoomIn(int mouseX){
		float spacing = endPos - startPos;
		float newSpacing = spacing*0.8f;
		
		float mousePos = ((float)mouseX)/((float)getWidth());
		
		startPos += (spacing-newSpacing)*mousePos;
		endPos -= (spacing-newSpacing)*(1-mousePos);
		
		repaint();
		
	}
	
	public void zoomOut(int mouseX){
		float spacing = endPos - startPos;
		float newSpacing = spacing*1.2f;
		
		float mousePos = ((float)mouseX)/((float)getWidth());
		float newStartPos = startPos + (spacing-newSpacing)*mousePos;
		float newEndPos = endPos - (spacing-newSpacing)*(1-mousePos);
		
		if(!(newStartPos < 0.0 || newEndPos > 1.0)){
			startPos = newStartPos;
			endPos = newEndPos;
		}
		
		repaint();
		
	}
}