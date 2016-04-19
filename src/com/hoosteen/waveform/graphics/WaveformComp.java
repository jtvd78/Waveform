package com.hoosteen.waveform.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
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
import com.hoosteen.waveform.Sound;


public class WaveformComp extends JComponent implements ImageHandler {
	
	//The sound being played
	Sound sound;
	
	//The painter which creates the waveform image
	WaveformPainter wp;
	
	//The next generated image to be displayed
	Image nextImage;
	
	//0.0 is beginning, 1.0 is end;
	float startPos = 0.0f;
	float endPos = 1.0f;
	
	//The current position in the song (0.0 - 1.0)
	public float soundPosition;
	
	public WaveformComp(Sound sound){
		
		//Save the sound
		this.sound = sound;
		
		//Init the WaveformPainter
		wp = new WaveformPainter(sound);
		
		//Add necessary input listeners
		Listener l = new Listener();
		addMouseListener(l);		
		addMouseWheelListener(l);
		addMouseMotionListener(l);
		addKeyListener(l);
		
		//Enable focusable for keyboard input
		setFocusable(true);
		
		//Begin looping the painter
		//Pass this as a handler
		wp.loop(this);
	}
	
	@Override
	public void handleImage(Image i) {
		
		//Saves the new image and repaints the screen
		nextImage = i;
		repaint();
	}
	
	public void paintComponent(Graphics g){
		
		//width and height
		int width = getWidth();
		int height = getHeight();
		
		if(nextImage == null){
			
			//Display loading sound if the sound has not loaded 
			g.setColor(Color.red);
			g.drawString("Loading Sound", width/2, height/2);
		}else{
			
			//Draw generated waveform
			g.drawImage(nextImage, 0, 0, null);
			
			//Draw song marker
			int x =(int) ((float)width * (soundPosition - startPos) / (endPos - startPos));
			g.setColor(Color.white);
			g.drawLine(x, 0, x, height);
		}
	}
	
	public void update(){
		
		//Update sound position
		soundPosition = sound.getSoundPosition();
		
		//Get position on screen of soundPosition
		int x = (int) ((float)getWidth() * (soundPosition - startPos) / (endPos -  startPos));
		
		//If playing and songPosition is offscreen, move the screen to the soundPosition
		if(sound.isPlaying() && x > getWidth()){
			float diff = endPos-startPos;
			startPos+= diff;
			endPos += diff;
		}
		
		//Update painter
		wp.update(getWidth(), getHeight(), startPos, endPos);
	}
	
	private class Listener implements MouseListener, MouseWheelListener, MouseMotionListener, KeyListener{

		//The last known X position of the mouse. 
		int lastX = 0;

		@Override
		public void mouseDragged(MouseEvent e) {
			
			//If you're pressing the left mouse button
			if(e.getButton() == 0){
				
				//Some helper variables
				float diff = endPos - startPos;
				float xMovement = e.getX() - lastX;
				
				//Amount (0.0 - 1.0) to move the starting and ending positions
				float amtToMove = xMovement*diff/((float)getWidth());
				
				//Adjust the start and end
				startPos -= amtToMove;
				endPos   -= amtToMove;
				
				
				//Don't adjust outside of the sound
				if(startPos < 0){
					startPos = 0;
					endPos = diff;
				}
				
				if(endPos > 1){
					startPos = 1.0f - diff;
					endPos = 1;
				}
			}
			
			//Update mouse X position to help with dragging the window
			lastX = e.getX();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			//Update mouse X position to help with dragging the window
			lastX = e.getX();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
			//Allows pause and play with the spacebar button
			if(e.getKeyChar() == ' '){				
				if(!sound.isPlaying()){
					sound.play(sound.getSoundPosition());
				}else{
					sound.pause();
				}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		
			//Begins playing the song where the middle mouse button is pressed
			if(e.getButton() == 2){
				soundPosition = startPos + (endPos-startPos)*((float)e.getX())/((float)getWidth());
				sound.play(soundPosition);
			}
			
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			
			//Get magnitude of wheel rotation
			int clicks = e.getWheelRotation();
			
			//If positive, zoom out. If negative, zoom in. 
			if(clicks < 0){
				zoomIn(e.getX());
			}else if(clicks > 0){
				zoomOut(e.getX());
			}
		}

		//Unused Inherited Input Methods
		
		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void keyTyped(KeyEvent e) {}
		
		@Override
		public void mouseReleased(MouseEvent e) {}
		
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
	}
	
	/**
	 * Zooms in towards mouse
	 * @param mouseX location of mouse
	 */
	public void zoomIn(int mouseX){
		zoom(mouseX, 0.8f);;
		
	}
	
	/**
	 * Zooms out away from mouse
	 * @param mouseX location of mouse
	 */
	public void zoomOut(int mouseX){		
		zoom(mouseX, 1.2f);
	}
	
	/**
	 * Zooms an amount, around the mouse, with a given ratio
	 * @param mouseX Location of the mouse
	 * @param ratio Ratio to zoom
	 */
	public void zoom(int mouseX, float ratio){
		
		//Calculate the current spacing between screen edges
		//Then, calculate the new spacing
		//Then, calculate the spacing between the new and old spacing
		float oldSpacing = endPos - startPos;
		float newSpacing = oldSpacing*ratio;
		float spacingDifference = newSpacing - oldSpacing;
		
		//Position of mouse on screen (0.0 to 1.0)
		float mousePos = ((float)mouseX)/((float)getWidth());		
		
		//Adjust the start and end position by the ratio
		//of where the mouse is on the screen and how much
		//spacing should be changed in total. 
		startPos -= spacingDifference*mousePos;
		endPos   -= spacingDifference*(mousePos-1.0f);
		
		//Don't zoom off screen
		if(startPos < 0.0){
			startPos = 0;
		}
		
		if(endPos > 1.0){
			endPos = 1.0f;
		}
	}
}