package com.hoosteen.waveform.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jtransforms.fft.DoubleFFT_1D;

import com.hoosteen.fft.Complex;
import com.hoosteen.fft.FFT;
import com.hoosteen.graphics.GraphicsWrapper;
import com.hoosteen.waveform.Sound;
import com.hoosteen.waveform.MainWindow;

public class FreqPainter {
	
	int width;
	int height;
	
	Sound sound;
	
	double[] left;
	double[] right;
	
	public FreqPainter(Sound sound){
		this.sound = sound;
	}
	
	public void update(int width, int height){
		this.width = width;
		this.height = height;	
	}
	
	public void loop(final ImageHandler h){
		
		final FrameCounter fc = new FrameCounter("Freq");
		
		//Begin generating images in a new thread
		Runnable loop = new Runnable(){
			public void run(){
				BufferedImage newImage = paintFrequencies();
				h.handleImage(newImage);
		//		fc.update();
					
			}
		};

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
		executor.scheduleAtFixedRate(loop, 0, 33, TimeUnit.MILLISECONDS);
	}	
	
	public void update1(){
		
		int fftLengthMultiplier = 13;
		
		float soundPosition = sound.getSoundPosition();		
		int length = (int)Math.pow(2,fftLengthMultiplier);
		
		float posAmt = ((float)length)/sound.getFrameCount()/2;		
		
		if(soundPosition >= posAmt){
			int[][] fft = sound.getSection(soundPosition - posAmt, soundPosition +  posAmt, length);
			
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
	
	public void update2(){
		
		int fftLengthMultiplier = 14;
		
		float soundPosition = sound.getSoundPosition();		
		int length = (int)Math.pow(2,fftLengthMultiplier);
		
		float posAmt = ((float)length)/sound.getFrameCount()/2;		
		
		if(soundPosition >= posAmt){
			
		    double [][] in = sound.getFFTData(soundPosition, length);		    
		    
		    in =  sound.getSectionDouble(soundPosition - posAmt, soundPosition +  posAmt, length);
		    
		    double [] leftIn = in[0];
		    double [] rightIn = in[1];
		    
		    DoubleFFT_1D fftDo = new DoubleFFT_1D(leftIn.length);

		    //Output data
	        double[] fftLeft = new double[leftIn.length * 2];
	        double[] fftRight = new double[rightIn.length * 2];	        
	        
	        System.arraycopy(leftIn, 0, fftLeft, 0, leftIn.length);
	        System.arraycopy(rightIn, 0, fftRight, 0, rightIn.length);
	        
	        fftDo.realForwardFull(fftLeft);
	        fftDo.realForwardFull(fftRight);
	        
	        for(int i = 0; i < leftIn.length *2; i++){
	        	fftLeft[i] = Math.abs(fftLeft[i]);
	        	fftRight[i] = Math.abs(fftRight[i]);
	        }
	        
	        left = fftLeft;
	        right = fftRight;	
		}
	}
	
	private BufferedImage paintFrequencies(){
		
		//SETTINGS
		double step = 1f/Math.pow(2, 3);
		//double step = 1f/((double)width/40.0);
		double beginning = 4.0f;	
		
		double samplingRate =  sound.getSamplingRate();
		
		if(width == 0 || height == 0){
			return null;
		}
		
		update2();
		
		if(left == null || right == null){
			return null;
		}
		
		//Create image and draw to it
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		GraphicsWrapper g2 = new GraphicsWrapper(g);
		
		//Background
		g.setColor(Color.black);
		g.fillRect(0,0,width, height);	
		
		// samplingRate/2 is the halfway point in the fft,
		// so there's no reason to use the second half
		double end = Math.log(samplingRate/2)/Math.log(2.0);
		
		for(double i = beginning; i < end; i+=step){
			
			int location = (int)(Math.pow(2, i)*left.length/samplingRate);
			
			if(location == 0){
				continue;
			}				
			
			double halfForward = Math.pow(2, i + step/2.0)*left.length/samplingRate;
			double halfBackward = Math.pow(2, i - step/2.0)*left.length/samplingRate;	
			
			double leftTotal = 0;
			double rightTotal = 0;
			
			//Before
			leftTotal += left[(int)Math.floor(halfBackward)]*(Math.ceil(halfBackward)-halfBackward);
			rightTotal += right[(int)Math.floor(halfBackward)]*(Math.ceil(halfBackward)-halfBackward);
			
			//Middle
			for(int s = (int) Math.ceil(halfBackward); s <= Math.floor(halfForward); s++){
				leftTotal += left[s];
				rightTotal += right[s];		
			}
			
			//After
			leftTotal += left[(int)Math.ceil(halfForward)]*(halfForward-Math.floor(halfForward));
			rightTotal += right[(int)Math.ceil(halfForward)]*(halfForward-Math.floor(halfForward));
			
			//Average out the totals
			leftTotal/=(halfForward-halfBackward);
			rightTotal/=(halfForward-halfBackward);			
			//Trying something
			
			double leftMax = 0;
			double rightMax = 0;
			
			for(int s = (int) Math.round(halfBackward); s <= Math.round(halfForward); s++){
				if(left[s] > leftMax){
					leftMax = left[s];
				}
				
				if(right[s] > rightMax){
					rightMax = right[s];
				}
			}
			
			//Perform log on totals
			leftTotal = 32 * Math.log(leftTotal)/Math.log(10);
			rightTotal = 32 * Math.log(rightTotal)/Math.log(10);
			leftMax = 32 * Math.log(leftMax)/Math.log(10);
			rightMax = 32 * Math.log(rightMax)/Math.log(10);
			
			//Scale the left side
			leftTotal -= 110;
			leftTotal  *= 2.5f;
			leftMax -= 110;
			leftMax *= 2.5f;
			
			//Left and right sides of a box
			int x1 = (int)((i     -beginning)*width/(end-beginning));
			int x2 = (int)((i+step-beginning)*width/(end-beginning));
			int width = x2 - x1;
			
			//draw max bars
			g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.6f));
			g.fillRect(x1, (int)(height/2 - leftMax),  width, (int)leftMax);
			g.setColor(new Color(0.0f, 0.0f, 1.0f, 0.6f));
			g.fillRect(x1, (int)(height - rightMax), width, (int)rightMax);
			
			
			//Draw bars
			g.setColor(Color.red);
			g.fillRect(x1, (int)(height/2 - leftTotal),  width, (int)leftTotal);
			g.setColor(Color.blue);
			g.fillRect(x1, (int)(height - rightTotal), width, (int)rightTotal);
			
			
			//Draw outline
			g.setColor(Color.black);
			g.drawRect(x1, (int)(height/2 - leftTotal),  width, (int)leftTotal);
			g.drawRect(x1, (int)(height - rightTotal),  width, (int)rightTotal);
		}
		
		//Draw Frequencies and Array Indexes
		for(double i = beginning; i < end ; i+=step*2){
			
			//Where to draw them (Centered on the corresponding bar)
			int x =(int)((i+step/2.0-beginning)*width/(end-beginning));
			
			//Draw Array Indexes
			g.setColor(Color.green);
			double location = (Math.pow(2, i)*left.length/samplingRate);
			g2.drawCenteredString(formatNumber(location) + "", x, height/2 - 30);
			g2.drawCenteredString(formatNumber(location), x, height - 30);
			
			//Draw Frequencies
			g.setColor(Color.white);
			double freq = location * samplingRate / left.length;
			g2.drawCenteredString(formatNumber(freq), x, height/2 - 15);
			g2.drawCenteredString(formatNumber(freq), x, height - 15);
		}
		
		return bi;
	}
	
	
	/**
	 * Formats a float to a string, nice and good. 
	 * @param freq The float to convert to a string
	 * @return a String representation of the float (with k if its over 1000)
	 */
	public String formatNumber(double freq){
		
		if(freq / 1000.0 > 1.0){
			return String.format("%1$,.0f", freq).split(",")[0] + "k";
		}
		
		return String.format("%1$,.0f", freq);
	}
}