package com.hoosteen.waveform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

public class Waveform {
	
	float rate;
	boolean finished = false;
	
	Clip clip;
	String songPath;	
	
	ArrayList<Integer> leftFrameList = new ArrayList<Integer>();
	ArrayList<Integer> rightFrameList = new ArrayList<Integer>();
	
	public Waveform(String filepath){
		if(filepath.endsWith(".mp3")){
			setFileAsMP3(filepath);
		}else if(filepath.endsWith(".wav")){
			setFileAsWAV(filepath);
		}
		
		//Generate the waveform in another thread
		new Thread(new Runnable(){

			@Override
			public void run() {
				generateWaveform();
				
				if(finished){
					System.out.println("Sound Successfully Loaded");
				}else{
					System.out.println("Sound Loading Failed");
				}
			}
			
		}).start();
		
	}
	
	public void addFrame(int left, int right){
		leftFrameList.add(left);
		rightFrameList.add(right);
	}
	
	public int getFrameCount(){
		//Same as rightFrameList size
		return leftFrameList.size();
	}
	
	private void setFileAsMP3(String path){
		String newPath = "D:\\justin\\Temp\\WaveformSongCache\\" + path.substring(path.lastIndexOf('\\') + 1, path.lastIndexOf('.'));
		if(!new File(newPath).exists()){
			new File("D:\\justin\\Temp\\WaveformSongCache\\").mkdirs();
			Converter c = new Converter();
			try {
				c.convert(path, newPath  + ".wav");
			} catch (JavaLayerException e) {
				e.printStackTrace();
			}
		}		
		songPath = newPath + ".wav";
	}
	
	private void setFileAsWAV(String path){
		songPath = path;
	}
	
	private void generateWaveform(){

		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(songPath));
			rate = ais.getFormat().getSampleRate();
			int frameSize = ais.getFormat().getFrameSize();
			
			int bytes = -1;
			byte[] frame = new byte[frameSize];			
			
			do{
				bytes = ais.read(frame);		
				
				int left =  ((int)frame[1] << 8) | ((int)frame[0] & 0xFF);
				int right = ((int)frame[3] << 8) | ((int)frame[2] & 0xFF);
				
				addFrame(left,right);
				
			}while(bytes == frameSize);
			
			finished = true;
			ais.close();
			System.out.println("Done");
			
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int[][] getSection(float startPos, float endPos, int width) {
		
		System.out.println("Start: " + startPos + " - End: " + endPos);
		
		
		int[][] output = new int[width][2];
		
		int size = getFrameCount();
		
		int frameStart = (int) (size*startPos);
		int frameEnd = (int) (size*endPos);
		
		//'lil safety thing to avoid index out of bounds
		if(frameEnd >= getFrameCount()){
			frameEnd = getFrameCount()-1;
		}
		
		
		float spacing = ((float)(frameEnd - frameStart))/((float)(width-1));
		
		//Get spaced frames for each pixel
		for(int i = 0; i < width; i++){			
			
			output[i][0] = leftFrameList.get((int) (i*spacing + frameStart));		//left
			output[i][1] = rightFrameList.get((int) (i*spacing + frameStart));      //right
		}
		
		return output;
	}
	
	public float getSamplingRate(){
		return rate;
	}
	
	public int getFrameNumber(float songPos){
		return (int) (getFrameCount()*songPos);
	}
	
	public int average(int[] numbers){
		int out = 0;
		for(int i = 0; i < numbers.length; i ++){
			out += numbers[i];
		}
		
		return out/numbers.length;
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	/**
	 * Does nothing if the track is already playing
	 * 
	 * @param framePos
	 */
	public void play(float framePos){
		if(!finished){
			return;
		}
		
		if(clip == null){
			try {
				clip = AudioSystem.getClip();
				AudioInputStream ais = AudioSystem.getAudioInputStream(new File(songPath));
				clip.open(ais);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		clip.setFramePosition((int) (framePos*getFrameCount()));
		clip.start();
	}
	
	public void pause(){
		if(clip == null){
			return;
		}else{
			clip.stop();
		}
	}
	
	public boolean isPlaying(){
		if(clip == null){
			return false;
		}
		
		return clip.isRunning();
	}
	
	public float getSongPosition(){
		if(clip == null){
			return 0.0f;
		}
		return ((float)clip.getFramePosition())/((float)getFrameCount());
	}
}