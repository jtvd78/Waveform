package com.hoosteen.window;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class FreqWindow extends JFrame{
	
	FreqComp fc;
	
	double[] left;
	double[] right;
	
	float rate;
	
	public FreqWindow(){		
		fc = new FreqComp();
		add(fc);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800,600);
		setVisible(true);
		
		
	}
	
	public void update(double[] left, double[] right, float rate){
		this.left = left;
		this.right = right;
		this.rate = rate;
		
		
		System.out.println(left.length + " : " + right.length);
		
		repaint(); 
	}
	
	class FreqComp extends JComponent{
		
		public void paintComponent(Graphics g){
			//Background
			g.setColor(Color.BLACK);
			g.fillRect(0,0,getWidth(), getHeight());	
			
			if(left == null || right == null){
				return;
			}		
				
			int w = getWidth();
			int h = getHeight();
			
			 /*
			//bars
			for(int i = 0; i < left.length/2; i++){
				
				float pos = ((float)i)/left.length;
				float nextPos = ((float)(i + 1))/left.length;
				
				int x1 = (int) (w*pos)*2;
				int x2 = (int) (w*nextPos)*2;			
				
				int leftBase = h/2;
				int rightBase = h;
				
				int leftHeight = (int)(left[i]);
				int rightHeight = (int)(right[i]/65535 *  h/2)/32;
				
				leftHeight = (int) (50.0 * (Math.log(leftHeight)/Math.log(5.0)-5));
				
				g.setColor(Color.blue);
				g.fillRect(x1, rightBase - rightHeight, x2 - x1, rightHeight);
				
				g.setColor(Color.red);
				g.fillRect(x1, leftBase - leftHeight , x2 - x1, leftHeight);
			}	
			
			 */
			
			//Frequency Numbers
			float step = 1f/9f;
			float maxFreq = rate / 2;	
			
			float beginning = 5.0f;
			double end = Math.log(maxFreq)/Math.log(2.0);
			
			for(float i = beginning; i < end; i+=step){
				int location = (int)(Math.pow(2, i)*left.length/rate);
				
				if(location == 0){
					continue;
				}				
				
				int halfForward = (int)(Math.pow(2, i + step/2.0)*left.length/rate);
				int halfBackward = (int)(Math.pow(2, i - step/2.0)*left.length/rate);				
				
				double leftTotal = 0;
				double rightTotal = 0;
				for(int s = halfBackward; s < halfForward; s++){
					leftTotal += left[s];
					rightTotal += right[s];		
				}
				
				//Average out the totals
				leftTotal/=((float)(halfForward-halfBackward));
				rightTotal/=((float)(halfForward-halfBackward));
				
				//Perform log on totals
				leftTotal = 32 * Math.log(leftTotal)/Math.log(10);
				rightTotal = 32 * Math.log(rightTotal)/Math.log(10);
				
				//Left and right sides of a box
				int x1 = (int)((i-beginning)*w/(end-beginning));
				int x2 = (int)((i+step-beginning)*w/(end-beginning));
				
				//Draw bars
				g.setColor(Color.red);
				g.fillRect(x1, (int)(h/2 - leftTotal),  x2- x1, (int)leftTotal);
				g.setColor(Color.blue);
				g.fillRect(x1, (int)(h - rightTotal),  x2- x1, (int)rightTotal);
				
				//Draw outline
				g.setColor(Color.black);
				g.drawRect(x1, (int)(h/2 - leftTotal),  x2- x1, (int)leftTotal);
				g.drawRect(x1, (int)(h - rightTotal),  x2- x1, (int)rightTotal);
				
				g.setColor(Color.green);
				g.drawString(location + "", x1, h/4);
			}
			
			//Draw Frequencies
			
			for(float i = beginning; i < end ; i+=0.5f){
				int location = (int)(Math.pow(2, i)*left.length/rate);
				float freq = location * rate / left.length;
				int x1 = (int)((i-beginning)*w/(end-beginning));
				g.setColor(Color.white);
				g.drawString(String.format("%1$,.0f", freq), x1, h/2);
			}
		}		
	}	
}