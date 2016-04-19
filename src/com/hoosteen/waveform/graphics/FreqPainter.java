package com.hoosteen.waveform.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.hoosteen.fft.Complex;
import com.hoosteen.fft.FFT;
import com.hoosteen.waveform.Sound;
import com.hoosteen.waveform.Start;

public class FreqPainter {
	
	int width;
	int height;
	
	Sound sound;
	float samplingRate;
	
	double[] left;
	double[] right;
	
	public FreqPainter(Sound sound){
		this.sound = sound;
		samplingRate = sound.getSamplingRate();
	}
	
	public void update(int width, int height){
		this.width = width;
		this.height = height;	
	}
	
	public void loop(final ImageHandler h){
		
		//Begin generating images in a new thread
		new Thread(new Runnable(){
			public void run(){
				while(Start.running){
					BufferedImage newImage = paintFrequencies();
					h.handleImage(newImage);
				}
			}
		}).start();
	}	
	
	private BufferedImage paintFrequencies(){
		
		if(width == 0 || height == 0){
			return null;
		}
		
		update();
		
		if(left == null || right == null){
			return null;
		}
		
		//Create image and draw to it
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0,0,width, height);
		
		//Frequency Numbers
		float step = 1f/6f;
		float maxFreq = samplingRate / 2;	
		
		float beginning = 4.0f;
		double end = Math.log(maxFreq)/Math.log(2.0);
		
		for(float i = beginning; i < end; i+=step){
			int location = (int)(Math.pow(2, i)*left.length/samplingRate);
			
			if(location == 0){
				continue;
			}				
			
			int halfForward = (int)(Math.pow(2, i + step/2.0)*left.length/samplingRate);
			int halfBackward = (int)(Math.pow(2, i - step/2.0)*left.length/samplingRate);				
			
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
			int x1 = (int)((i-beginning)*width/(end-beginning));
			int x2 = (int)((i+step-beginning)*width/(end-beginning));
			
			//Draw bars
			g.setColor(Color.red);
			g.fillRect(x1, (int)(height/2 - leftTotal),  x2- x1, (int)leftTotal);
			g.setColor(Color.blue);
			g.fillRect(x1, (int)(height - rightTotal),  x2- x1, (int)rightTotal);
			
			//Draw outline
			g.setColor(Color.black);
			g.drawRect(x1, (int)(height/2 - leftTotal),  x2- x1, (int)leftTotal);
			g.drawRect(x1, (int)(height - rightTotal),  x2- x1, (int)rightTotal);
			
			g.setColor(Color.green);
			g.drawString(location + "", x1, height/4);
		}
		
		//Draw Frequencies
		
		for(float i = beginning; i < end ; i+=0.5f){
			int location = (int)(Math.pow(2, i)*left.length/samplingRate);
			float freq = location * samplingRate / left.length;
			int x1 = (int)((i-beginning)*width/(end-beginning));
			g.setColor(Color.white);
			g.drawString(String.format("%1$,.0f", freq), x1, height/2);
		}
		
		return bi;
	}
	
	float window = 0.0005f;
	int fftLengthMultiplier = 14;
	
	public void update(){
		
		float soundPosition = sound.getSoundPosition();		
		int length = (int)Math.pow(2,fftLengthMultiplier);
		
		if(soundPosition >= window){
			int[][] fft = sound.getSection(soundPosition - window, soundPosition + window, length);
			
			Complex[] leftComplex = new Complex[length];
			Complex[] rightComplex = new Complex[length];
			
		    // original data
		    for (int i = 0; i < length; i++) {
		    	leftComplex[i] = new Complex(fft[i][0], 0);
		        rightComplex[i] = new Complex(fft[i][1], 0);
		    }

		    // FFT of original data
		    Complex[] leftFFT = FFT.fft(leftComplex);
		    Complex[] rightFFT = FFT.fft(rightComplex);			   
		    
		    double[] leftFFTdouble = new double[leftFFT.length];
		    double[] rightFFTdouble = new double[rightFFT.length];
		    
		    for(int i = 0; i < leftFFTdouble.length; i++){
		    	leftFFTdouble[i] = leftFFT[i].abs();
		    	rightFFTdouble[i] = rightFFT[i].abs();
		    }		    
		    
		    left = leftFFTdouble;
		    right = rightFFTdouble;
		    
		}
	}
}