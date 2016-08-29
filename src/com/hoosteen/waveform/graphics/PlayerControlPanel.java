package com.hoosteen.waveform.graphics;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.hoosteen.graphics.Circle;
import com.hoosteen.graphics.GraphicsWrapper;

public class PlayerControlPanel extends JPanel{
	
	private static int padding = 2;
	private static int weight = 3;
	private static int margin = 0;
	
	int buttonSize = 35;
	
	int sliderRadius = 7;
	int barHeight = 5;

	private static Color fgColor = Color.BLACK;
	private static Color bgColor = Color.WHITE;
	
	
	JComponent leftSkip = new LeftSkipButton();
	JComponent playButton = new PlayButton();
	JComponent rightSkip = new RightSkipButton();
	
	SoundPlayer player;
	
	public PlayerControlPanel(SoundPlayer player){
		
		this.player = player;
		
		setLayout(new BorderLayout());
		setBackground(bgColor);
		
		JPanel buttons = new JPanel();
		buttons.setBackground(bgColor);
		buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		add(buttons, BorderLayout.WEST);
		
		buttons.add(leftSkip);
		buttons.add(playButton);
		buttons.add(rightSkip);	
		
		add(new SongSlider(), BorderLayout.CENTER);
	}
	
	class SongSlider extends JComponent{
		
		int sideMargin = 20;
		
		
		
		
		double draggingPosition;
		boolean dragging = false;
		
		public SongSlider(){
			SliderListener listener = new SliderListener();
			addMouseListener(listener);
			addMouseMotionListener(listener);
		}
		
		
		public void paintComponent(Graphics g){
			
			RenderingHints rh =  new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D)g).addRenderingHints(rh);
			
			g.setColor(Color.BLACK);
			g.fillRect(sideMargin + barHeight/2, getHeight()/2 - barHeight/2, getWidth() - sideMargin*2 - barHeight/2, barHeight);			
			g.fillOval(sideMargin , getHeight()/2 - barHeight/2, barHeight, barHeight);
			g.fillOval(sideMargin + barHeight/2 + getWidth() - sideMargin*2 - barHeight , getHeight()/2 - barHeight/2, barHeight, barHeight);
			
			
			GraphicsWrapper gw = new GraphicsWrapper(g);
			
			Circle draggingCircle;
			if(dragging){
				draggingCircle = getDraggingCircle(draggingPosition);
			}else{				
				draggingCircle = getDraggingCircle(player.getSoundPosition());
			}
			
			g.setColor(Color.red);
			gw.fillCircle(draggingCircle);			
			
			g.setColor(Color.BLACK);
			gw.setLineWeight(2);			
			gw.drawCircle(draggingCircle);
			gw.setLineWeight(1);		
		}	
		
		private double pxToPosition(int x){
			
			double left = sideMargin;
			double right =  getWidth() - sideMargin;
			
			
			double out = (x-left)/(right-left);
			
			if(out > 1){
				return 1;
			}else if(out < 0){
				return 0;
			}
			
			return out;
			
		}
		
		private int positionToPx(double position){
			int left = sideMargin;
			int right = getWidth() - sideMargin;
			
			return (int) ((right-left)*position + left);
		}
		
		private Circle getDraggingCircle(double position){
			return new Circle(positionToPx(position),getHeight()/2, sliderRadius);
		}
		
		class SliderListener implements MouseMotionListener, MouseListener{
			
			int px;
			
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				if(x > sideMargin && x < getWidth() - sideMargin && Math.abs(getHeight()/2 - e.getY()) < sliderRadius){
					player.setSoundPosition(pxToPosition(e.getX()));
				}
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				
				Circle drag = getDraggingCircle(player.getSoundPosition());
				if(drag.contains(e.getX(), e.getY())){
					dragging = true;
					mouseMoved(e);
				}
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(dragging){
					player.setSoundPosition(draggingPosition);
				}
				
				dragging = false;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if(dragging){
					draggingPosition = pxToPosition(e.getX());
				}
			}			
		}
	}
	
	class HoverListener implements MouseMotionListener, MouseListener{
		
		boolean hover = false;
		ButtonComponent comp;
		
		public HoverListener(ButtonComponent comp){
			this.comp = comp;
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {}

		@Override
		public void mouseMoved(MouseEvent e) {
			if(comp.containsPoint(e.getX(), e.getY())){
				hover = true;
			}else{
				hover = false;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e) && hover){
				comp.buttonPressed();
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {}
		
	}
	
	private abstract class ButtonComponent extends JComponent{
		
		public ButtonComponent(){
			
			setPreferredSize(new Dimension(buttonSize, buttonSize));
			
			HoverListener listener = new HoverListener(this);
			
			addMouseMotionListener(listener);
			addMouseListener(listener);
		}
		
		boolean containsPoint(int x, int y){
			int minDim = (getWidth() < getHeight()) ? getWidth() : getHeight();			
			return new Circle(minDim/2, minDim/2, minDim/2 - margin).contains(x, y);			
		}		
		
		public abstract void buttonPressed();
	}
	
	private void drawCircle(Graphics g, int weight, int margin, int diameter){
		
		g.setColor(fgColor);
		g.fillOval(margin, margin, diameter, diameter);
		
		g.setColor(bgColor);
		g.fillOval(margin + weight, margin + weight, diameter - weight * 2, diameter - weight *2 );
		
	}
	
	private class PlayButton extends ButtonComponent{
		
		public void paintComponent(Graphics g){
			
			RenderingHints rh =  new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D)g).addRenderingHints(rh);
			
			int diameter = (getWidth() < getHeight()) ? getWidth() : getHeight()  - margin * 2;
			
			drawCircle(g, weight, margin, diameter);
			
			int center = diameter/2 + margin;
			
			
			
			
			if(player.isPlaying()){
				
				int innerRadius = diameter/2 - (padding + weight);			
				int innerWidth = (int) (2.0 * innerRadius / Math.sqrt(2));
				
				int left = margin + diameter/2 -innerWidth/2;
				int y1 = (int) (diameter/2 - innerWidth / 2 + margin);
				
				g.setColor(Color.black);
				g.fillRect(left + 1*innerWidth/5, y1, innerWidth/5, innerWidth);
				g.fillRect(left + 3*innerWidth/5, y1, innerWidth/5, innerWidth);
				
				g.setColor(Color.RED);
			//	g.fillRect(left + 4*innerWidth/5, y1, innerWidth/5, innerWidth);
				
				
			}else{
				int triangleWidth = 3*diameter/4 - padding * 2 - weight * 2;
				
				int[] xPoints = new int[3];
				int[] yPoints = new int[3];
				
				xPoints[0] = center - triangleWidth/3;
				xPoints[1] = center - triangleWidth/3;
				xPoints[2] = center + 2*triangleWidth/3;
				
				yPoints[0] = (int) (diameter/2 - triangleWidth / Math.sqrt(3));
				yPoints[1] = (int) (diameter/2 + triangleWidth / Math.sqrt(3));;
				yPoints[2] = diameter/2;   
				
				g.setColor(fgColor);
				g.fillPolygon(xPoints, yPoints, 3);
			}
			}
			
			

		@Override
		public void buttonPressed() {
			player.playPause();
		}
	}
	
	private class RightSkipButton extends ButtonComponent{
		
		public void paintComponent(Graphics g){
			
			RenderingHints rh =  new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D)g).addRenderingHints(rh);
			
			int diameter = (getWidth() < getHeight()) ? getWidth() : getHeight() - margin * 2;
			
			drawCircle(g, weight, margin, diameter);
			
			int innerRadius = diameter/2 - (padding + weight);			
			int innerWidth = (int) (2.0 * innerRadius / Math.sqrt(2));
			
			int triangleHeight = innerWidth / 2;			
			int triangleWidth = (int) (innerWidth / 2.0 * Math.sqrt(3));
			
			int barWidth = innerWidth - triangleWidth;			
			
			int[] xPoints = new int[3];
			int[] yPoints = new int[3];
			
			int left = margin + diameter/2 -innerWidth/2;
			
			xPoints[0] = left;
			xPoints[1] = left;
			xPoints[2] = left + triangleWidth;
			
			yPoints[0] = (int) (diameter/2 - triangleHeight + margin);
			yPoints[1] = (int) (diameter/2 + triangleHeight + margin);
			yPoints[2] = diameter/2 + margin;			
			
			g.setColor(fgColor);
			g.fillPolygon(xPoints, yPoints, 3);			
			
			
			g.fillRect(left + triangleWidth, yPoints[0], barWidth, innerWidth);		
		
		}

		@Override
		public void buttonPressed() {
			player.skipForward();
		}		
	}
	
	private class LeftSkipButton extends ButtonComponent{
		
		public void paintComponent(Graphics g){
			
			RenderingHints rh =  new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D)g).addRenderingHints(rh);
			
			int diameter = (getWidth() < getHeight()) ? getWidth() : getHeight() - margin * 2;
			
			drawCircle(g, weight, margin, diameter);
			
			int innerRadius = diameter/2 - (padding + weight);			
			int innerWidth = (int) (2.0 * innerRadius / Math.sqrt(2));
			
			int triangleHeight = innerWidth / 2;			
			int triangleWidth = (int) (innerWidth / 2.0 * Math.sqrt(3));
			
			int barWidth = innerWidth - triangleWidth;			
			
			int[] xPoints = new int[3];
			int[] yPoints = new int[3];
			
			int left = margin + diameter/2 -innerWidth/2 + barWidth;
			
			xPoints[0] = left + triangleWidth;
			xPoints[1] = left + triangleWidth;
			xPoints[2] = left;
			
			yPoints[0] = (int) (diameter/2 - triangleHeight + margin);
			yPoints[1] = (int) (diameter/2 + triangleHeight + margin);
			yPoints[2] = diameter/2 + margin;			
			
			g.setColor(fgColor);
			g.fillPolygon(xPoints, yPoints, 3);			
			
			
			g.fillRect(left - barWidth, yPoints[0], barWidth, innerWidth);		
		
		}

		@Override
		public void buttonPressed() {
			player.skipBack();
		}		
	}
}